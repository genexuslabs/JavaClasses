package com.genexus.diagnostics;

import com.genexus.ModelContext;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.UUID;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GXDebugManager
{
    public static final short GXDEBUG_VERSION = 1;
    private static final GXDebugGenId GENERATOR_ID = GXDebugGenId.JAVA;

    static final int PGM_INFO_NO_PARENT = 0;
    private static final long MICRO_FREQ = 1000;

    private static GXDebugManager instance;
    private static final Object sessionLock = new Object();
    private static boolean initialized = false;
    public static GXDebugManager getInstance()
    {
        if(!initialized)
        {
            synchronized(sessionLock)
            {
                if(!initialized)
                {
                    initialized = true;
                    instance = new GXDebugManager();
                }
            }
        }
        return instance;
    }

    private static int BUFFER_INITIAL_SIZE = 16384;
    private static final long TICKS_NOT_SET = Long.MAX_VALUE;
    private static final long TICKS_NOT_NEEDED = 0;
    private static String fileName = "gxperf.gxd";

    private GXDebugManager()
    {
        fileName = System.getProperty("gxperf", fileName);
        current = new GXDebugItem[BUFFER_INITIAL_SIZE];
        next = new GXDebugItem[BUFFER_INITIAL_SIZE];
        for(int i = 0; i < BUFFER_INITIAL_SIZE; i++)
        {
            current[i] = new GXDebugItem();
            next[i] = new GXDebugItem();
        }
        sessionGuid = UUID.randomUUID();
        lastSId = new AtomicInteger();
        executorService =  new ThreadPoolExecutor(0, 1, 5, TimeUnit.MINUTES, new SynchronousQueue<Runnable>());
        pushSystem(GXDebugMsgCode.INITIALIZE.toByteInt(), new Date());
    }

    public GXDebugInfo getDbgInfo(ModelContext context, int objClass, int objId, int dbgLines, long hash)
    {
        synchronized(sessionLock)
        {
			IntPair objKey = new IntPair(objClass, objId);
            GXDebugInfo dbgInfo = new GXDebugInfo(newSId(), context, objKey);
			if (!pgmInfoTable.contains(objKey))
			{
				PgmInfo pgmInfoObj = new PgmInfo(dbgLines, hash);
				pgmInfoTable.add(objKey);
				pushSystem(GXDebugMsgCode.PGM_INFO.toByteInt(), new Object[]{objKey, pgmInfoObj});
			}
            String clientId = context.getHttpContext().getClientId();
            GXDebugInfo parentDbgInfo = parentTable.get(clientId);
            dbgInfo.parent = parentDbgInfo;
            dbgInfo.registerPgm(parentDbgInfo);
            parentTable.put(clientId, dbgInfo);
            return dbgInfo;
        }
    }

    private final UUID sessionGuid;
    private final AtomicInteger lastSId;
    private int newSId()
    {
        return lastSId.incrementAndGet();
    }

    private GXDebugItem [] current, next, toSave;
    private boolean saving = false;
    private int dbgIndex = 0;
    private final Object saveLock = new Object();
    private final Object mSaveLock = new Object();
    private final ConcurrentHashMap<String, GXDebugInfo> parentTable = new ConcurrentHashMap<String, GXDebugInfo>();
	private final HashSet<IntPair> pgmInfoTable = new HashSet<IntPair>();

    private static ExecutorService executorService;

    protected GXDebugItem pushSystem(int cmdCode){ return pushSystem(cmdCode, null); }
    protected GXDebugItem pushSystem(int cmdCode, Object arg)
    {
        return mPush(null, GXDebugMsgType.SYSTEM, cmdCode, 0, arg);
    }

    protected GXDebugItem push(GXDebugInfo dbgInfo, int lineNro, int colNro)
    {
        return mPush(dbgInfo, GXDebugMsgType.PGM_TRACE, lineNro, colNro, null);
    }

    protected GXDebugItem pushPgm(GXDebugInfo dbgInfo, int parentSId, IntPair pgmKey)
    {
        return mPush(dbgInfo, GXDebugMsgType.REGISTER_PGM, parentSId, 0, pgmKey);
    }

	protected GXDebugItem pushRange(GXDebugInfo dbgInfo, int lineNro, int colNro, int lineNro2, int colNro2)
	{
		if((colNro != 0 || colNro2 != 0))
			return mPush(dbgInfo, GXDebugMsgType.PGM_TRACE_RANGE_WITH_COLS, lineNro, lineNro2, new IntPair(colNro, colNro2));
		else return mPush(dbgInfo, GXDebugMsgType.PGM_TRACE_RANGE, lineNro, lineNro2, null);
	}

    private GXDebugItem mPush(GXDebugInfo dbgInfo, GXDebugMsgType msgType, int arg1, int arg2, Object argObj)
    {
        synchronized (saveLock)
        {
            if (toSave != null)
            {
                save(toSave);
                toSave = null;
            }
            GXDebugItem currentItem = current[dbgIndex];
            currentItem.dbgInfo = dbgInfo;
            currentItem.msgType = msgType;
            currentItem.arg1 = arg1;
            currentItem.arg2 = arg2;
            currentItem.argObj = argObj;
            switch(msgType)
            {
                case SYSTEM:
                {
                    switch (GXDebugMsgCode.valueOf(arg1))
                    {
                        case INITIALIZE:
                        case EXIT:
                        case OBJ_CLEANUP:
                            currentItem.ticks = TICKS_NOT_NEEDED;
                            break;
                        default: currentItem.ticks = TICKS_NOT_SET; break;
                    }
                }
                break;
                case REGISTER_PGM:
                {
                    currentItem.ticks = TICKS_NOT_NEEDED;
                }
                break;
                default:
                    currentItem.ticks = TICKS_NOT_SET;
                    break;
            }
            dbgIndex++;
            if (dbgIndex == current.length)
            {
                synchronized(mSaveLock)
                {
                    while(saving)
                    {
                        try
                        {
                            mSaveLock.wait();
                        }catch(InterruptedException ignore){}
                    }
                }
                toSave = current;
                GXDebugItem[] swap = current;
                current = next;
                next = swap;
                pgmInfoTable.clear();
                dbgIndex = 0;
            }
            return currentItem;
        }

    }

    private void save(GXDebugItem[] toSave)
    {
        save(toSave, -1, true);
    }

    private void save(GXDebugItem[] toSave, int saveTop, boolean saveInThread)
    {
        int saveCount = 0;
        if (saveTop == -1)
        {
            toSave = next;
            saveTop = toSave.length;
            for (int idx = 0; idx < saveTop; idx++)
            {
                if (toSave[idx].ticks == TICKS_NOT_SET)
                { // Items que aun no marcaron sus ticks, quedan para guardar despues
                    GXDebugItem swap = toSave[idx];
                    toSave[idx] = current[dbgIndex];
                    current[dbgIndex] = swap;
                    clearDebugItem(toSave[idx]);
                    toSave[idx].msgType = GXDebugMsgType.SKIP;
                    toSave[idx].argObj = swap;
                    dbgIndex++;
                    if (dbgIndex == current.length)
                    { // Se lleno el current, expando Current y Next
                        int lastTop = current.length;
                        GXDebugItem[] tempL = new GXDebugItem[lastTop + BUFFER_INITIAL_SIZE];
                        System.arraycopy(current, 0, tempL, 0, lastTop);
                        current = tempL;
                        tempL = new GXDebugItem[lastTop + BUFFER_INITIAL_SIZE];
                        System.arraycopy(current, 0, tempL, 0, lastTop);
                        next = tempL;
                        for (int i = lastTop; i < current.length; i++)
                        {
                            current[i] = new GXDebugItem();
                            next[i] = new GXDebugItem();
                        }
                    }
                }
                else saveCount++;
            }
        }
        else if(saveTop == 0)
            return;
        else saveCount = saveTop;
        synchronized (mSaveLock)
        {
            saving = true;
        }
        if(saveInThread)
        {
            final GXDebugItem[] mToSave = toSave;
            final int mSaveTop = saveTop;
            final int mSaveCount = saveCount;
            executorService.execute(new Runnable(){public void run(){mSave(mToSave, mSaveTop, mSaveCount);}});
        }
        else mSave(toSave, saveTop, saveCount );
    }

    protected void onExit(GXDebugInfo dbgInfo)
    {
        pushSystem(GXDebugMsgCode.EXIT.toByteInt());
        save();
    }

    protected void onCleanup(GXDebugInfo dbgInfo)
    {
        pushSystem(GXDebugMsgCode.OBJ_CLEANUP.toByteInt(), dbgInfo.sId);
        synchronized (sessionLock)
        {
            if (dbgInfo.parent != null)
                parentTable.put(dbgInfo.context.getHttpContext().getClientId(), dbgInfo.parent);
            else
			{
				parentTable.remove(dbgInfo.context.getHttpContext().getClientId());
				if(!dbgInfo.context.isNullHttpContext())
					save();
			}
        }
    }

    private void save()
	{
		synchronized(saveLock)
		{
			if (toSave != null)
			{
				save(toSave);
				toSave = null;
			}
			save(current, dbgIndex, false);
			dbgIndex = 0;
		}
	}

    private void clearDebugItem(GXDebugItem dbgItem)
    {
        dbgItem.msgType = GXDebugMsgType.INVALID;
        dbgItem.arg1 = 0;
        dbgItem.arg2 = 0;
        dbgItem.argObj = null;
    }

    private void mSave(GXDebugItem [] data, int saveTop, int saveCount)
    {
        synchronized(mSaveLock)
        {
            // Obtengo chunk a grabar
            int idx = 0;
            GXDebugStream stream = null;
            try
            {
                try
                {
                    stream = GXDebugStream.getStream(fileName);
                    stream.writeHeader(sessionGuid, (short)(GXDEBUG_VERSION << 4 | GENERATOR_ID.toByteInt()), saveCount);
                    for (; idx < saveTop; idx++)
                    {
                        GXDebugItem dbgItem = data[idx];
                        switch (dbgItem.msgType)
                        {
                            case SYSTEM:
                            {
                                stream.writeByte((dbgItem.msgType.toByteInt() | (GXDebugMsgCode.valueOf(dbgItem.arg1).toByteInt())));
                                switch (GXDebugMsgCode.valueOf(dbgItem.arg1))
                                {
                                    case INITIALIZE:
                                        stream.writeLong(ToTicks((Date) dbgItem.argObj));
                                        break;
                                    case OBJ_CLEANUP:
                                        stream.writeVLUInt((Integer) dbgItem.argObj);
                                        break;
                                    case EXIT:
									case PGM_INFO:
										Object [] info = (Object[])dbgItem.argObj;
										stream.writeVLUInt(((IntPair)info[0]).left);
										stream.writeVLUInt(((IntPair)info[0]).right);
										stream.writeVLUInt(((PgmInfo)info[1]).dbgLines);
										stream.writeInt(((PgmInfo)info[1]).hash);
                                        break;
                                    default:
                                        throw new IllegalArgumentException(String.format("Invalid DbgItem: %s", dbgItem));
                                }
                            }
                            break;
                            case PGM_TRACE:
                            {
                                stream.writePgmTrace(dbgItem.dbgInfo.sId, dbgItem.arg1, dbgItem.arg2, dbgItem.ticks);
                            }
                            break;
							case PGM_TRACE_RANGE:
							case PGM_TRACE_RANGE_WITH_COLS:
							{
								stream.writeByte(dbgItem.msgType.toByteInt());
								stream.writeVLUInt(dbgItem.dbgInfo.sId);
								stream.writeVLUInt(dbgItem.arg1);
								stream.writeVLUInt(dbgItem.arg2);
								if(dbgItem.msgType == GXDebugMsgType.PGM_TRACE_RANGE_WITH_COLS)
								{
									stream.writeVLUInt(((IntPair)dbgItem.argObj).left);
									stream.writeVLUInt(((IntPair)dbgItem.argObj).right);
								}
							}
							break;
                        	case REGISTER_PGM:
                            {
                                stream.writeByte(dbgItem.msgType.toByteInt());
                                stream.writeVLUInt(dbgItem.dbgInfo.sId);
                                stream.writeVLUInt(dbgItem.arg1);
                                stream.writeVLUInt(((IntPair) dbgItem.argObj).left);
                                stream.writeVLUInt(((IntPair) dbgItem.argObj).right);
                            }
                            break;
                            case SKIP:
                                continue;
                        }
                        clearDebugItem(dbgItem);
                    }
                } finally
                {
                    if (stream != null)
                        stream.close();
                }
            }catch(Exception e)
            {
                Log.warning("Cannot write debug file", "GXDebugManager", e);
            }

            saving = false;
            mSaveLock.notifyAll();
        }
    }

    private static long ToTicks(Date argObj)
    {
        return (argObj.getTime() * 10000) + 621355968000000000L;
    }

    private static final int _SYSTEM = 0x80;
	private static final int _PGM_TRACE_RANGE = _SYSTEM | 0x20;
    enum GXDebugMsgType
    {
        SYSTEM(_SYSTEM),
        PGM_TRACE(0x00),
        REGISTER_PGM(_SYSTEM | 0x40),
		PGM_TRACE_RANGE( _PGM_TRACE_RANGE),
		PGM_TRACE_RANGE_WITH_COLS(_PGM_TRACE_RANGE | 0x01),
        INVALID(0xFE),  // aca esta el limite
        SKIP(0xFF),     // no se graba
        TRACE_HAS_COL(0x40),
        TRACE_HAS_SID(0x30),
        TRACE_HAS_LINE1(0x08);

        private final short value;
        GXDebugMsgType(int value)
        {
            this.value = (short)value;
        }

        private int toByteInt()
        {
            return value;
        }
    }

    enum GXDebugMsgCode
    {
        INITIALIZE(0),
        OBJ_CLEANUP(1),
        EXIT(2),
		PGM_INFO(3),
        MASK_BITS(0x3);

        private final short value;
        GXDebugMsgCode(int value)
        {
            this.value = (short)value;
        }

        static GXDebugMsgCode valueOf(int value)
        {
            switch(value)
            {
                case 0: return INITIALIZE;
                case 1: return OBJ_CLEANUP;
                case 2: return EXIT;
				case 3: return PGM_INFO;
                default: throw new IllegalArgumentException(String.format("GXDebugMsgCode(%d)", value));
            }
        }

        int toByteInt()
        {
            return value;
        }
    }

    enum GXDebugGenId
    {
        CSHARP(1),
        JAVA(2),

        INVALID(0xF);

        private final short value;
        GXDebugGenId(int value)
        {
            this.value = (short)value;
        }

        private int toByteInt()
        {
            return value;
        }
    }


    class GXDebugItem
    {
        GXDebugInfo dbgInfo;
        int arg1;
        int arg2;
        Object argObj;
        public long ticks;

        GXDebugMsgType msgType;

        public String toString()
        {
            return String.format("%s/%d:%s-%d-%s%s", msgType, dbgInfo != null ? dbgInfo.sId : 0, toStringArg1(), arg2, argObj != null ? argObj.toString() : "", toStringTicks());
        }
        private String toStringArg1(){ return (msgType == GXDebugMsgType.SYSTEM ? GXDebugMsgCode.valueOf(arg1).toString() : String.format("%d", arg1)); }
        private String toStringTicks(){ return (msgType == GXDebugMsgType.PGM_TRACE ? String.format(" elapsed:%d", ticks) : ""); }
    }

	class IntPair
	{
		final int left;
		final int right;

		IntPair(int left, int right)
		{
			this.left = left;
			this.right = right;
		}

		@Override
		public int hashCode()
		{
			return left ^ right;
		}

		@Override
		public boolean equals(Object o)
		{
			return (o instanceof IntPair) &&
				((IntPair) o).left == left &&
				((IntPair) o).right == right;
		}

	}

	class PgmInfo
	{
		final int dbgLines;
		final long hash;

		PgmInfo(int dbgLines, long hash)
		{
			this.dbgLines = dbgLines;
			this.hash = hash;
		}

		@Override
		public int hashCode()
		{
			return dbgLines ^ ((int) hash);
		}

		@Override
		public boolean equals(Object o)
		{
			return (o instanceof PgmInfo) &&
				((PgmInfo) o).dbgLines == dbgLines &&
				((PgmInfo) o).hash == hash;
		}
	}

    static class Stopwatch
    {
        private long nanotime;
        public Stopwatch()
        {
            restart();
        }

        public long getElapsedMicroSecs()
        {
            return ((System.nanoTime() - nanotime)/MICRO_FREQ);
        }

        public final void restart()
        {
            nanotime = System.nanoTime();
        }
    }

    static class GXDebugStream extends FilterOutputStream
    {
        class ESCAPE
        {
            static final byte PROLOG = 0;
            static final byte EPILOG = 1;
            static final byte TRIPLE_FF = 3;
            static final byte FF = (byte) 0xFF;
        }

        static final byte[] PROLOG = {ESCAPE.FF, ESCAPE.FF, ESCAPE.FF, ESCAPE.PROLOG};
        static final byte[] EPILOG = {ESCAPE.FF, ESCAPE.FF, ESCAPE.FF, ESCAPE.EPILOG};

        static GXDebugStream getStream(String fileName) throws IOException
        {
            FileOutputStream stream = new FileOutputStream(fileName, true);
            return new GXDebugStream(new BufferedOutputStream(stream), stream.getChannel());
        }

        private GXDebugStream(OutputStream stream, FileChannel channel) throws IOException
        {
            this(stream);
            channel.lock();
        }

        GXDebugStream(OutputStream stream)
        {
            super(stream);
            last = 0;
            lastLast = 0;
            initializeNewBlock();
        }

        @Override
        public void close() throws IOException
        {
            writeEpilog();
            super.close();
        }

        private void writeProlog(short version) throws IOException
        {
            writeRaw(PROLOG, 0, PROLOG.length);
            writeVLUShort(version);
        }

        private void writeEpilog() throws IOException
        {
            writeRaw(EPILOG, 0, EPILOG.length);
        }

        @Override
        public void write(byte[] data) throws IOException
        {
            write(data, 0, data.length);
        }

        void writeRaw(byte[] data, int from, int length) throws IOException
        {
            super.write(data, from, length);
        }

        void writeRaw(byte value) throws IOException
        {
            super.write(value);
        }

        @Override
        public void write(byte[] data, int offset, int count) throws IOException
        {
            while (count-- > 0)
                writeByte(data[offset++]);
        }

        private int last, lastLast;

        @Override
        public void write(int value)throws IOException
        {
            writeByte(value);
        }

        void writeByte(int value) throws IOException
        {
            super.write(value);
            if (value == 0xFF &&
                    value == last &&
                    value == lastLast)
            {
                writeRaw(ESCAPE.TRIPLE_FF);
                last = lastLast = 0;
            } else
            {
                lastLast = last;
                last = value;
            }
        }

        void writeVLUInt(int value) throws IOException
        {
            if (value < 0) throw new IllegalArgumentException("Cannot handle negative values");
            else if (value > 0x3FFFFFFFL)
                throw new IllegalArgumentException("Cannot handle 31bit values");
            if (value < 0x80)
                writeByte((byte) value);
            else if (value < 0x4000)
                writeVLUShort((short) (((value & 0x3F80) << 1) | (value & 0x7F) | 0x80));
            else
            {
                writeVLUShort((short) (((value & 0x3F80) << 1) | (value & 0x7F) | 0x4080));
                writeVLUShort((short) (value >> 14));
            }
        }

        void writeVLUShort(short value) throws IOException
        {
            if (value < 0) throw new IllegalArgumentException("Cannot handle negative values");
            if (value < 0x80)
                writeByte((byte) value);
            else
            {
                writeByte((byte) ((value & 0x7F) | 0x80));
                writeByte((byte) (value >> 7));
            }
        }

        void writeHeader(UUID sessionGuid, short version, int saveCount) throws IOException
        {
            writeProlog(version);
            writeVLUInt(saveCount);
            writeLong(sessionGuid.getLeastSignificantBits());
            writeLong(sessionGuid.getMostSignificantBits());
        }

		void writeLong(long value) throws IOException
		{
			for (int i = 0; i < 8; i++)
			{
				writeByte((byte) (value & 0xFF));
				value >>= 8;
			}
		}

		void writeInt(long value) throws IOException
		{
			for (int i = 0; i < 4; i++)
			{
				writeByte((byte) (value & 0xFF));
				value >>= 8;
			}
		}

		private int LastSId, LastLine1;

        void writePgmTrace(int SId, int line1, int col, long ticks) throws IOException
        {
            int cmd = GXDebugMsgType.PGM_TRACE.toByteInt();
            if (col != 0)
                cmd |= GXDebugMsgType.TRACE_HAS_COL.toByteInt();
            boolean hasSId = false;
            switch (SId - LastSId)
            {
                case 0:
                    break;
                case 1:
                    cmd |= 0x10;
                    break;
                case -1:
                    cmd |= 0x20;
                    break;
                default:
                    cmd |= GXDebugMsgType.TRACE_HAS_SID.toByteInt();
                    hasSId = true;
                    break;
            }
            int difLine1 = line1 - LastLine1;
            boolean hasLine1 = false;
            if (difLine1 < 8 && difLine1 > -8)
                cmd |= (byte) (difLine1 & 0x0F);
            else
            {
                cmd |= GXDebugMsgType.TRACE_HAS_LINE1.toByteInt();
                hasLine1 = true;
            }
            writeByte(cmd);
            writeScaledLong(ticks);
            if (hasSId)
                writeVLUInt(SId);
            if (hasLine1)
                writeVLUInt(line1);
            if (col != 0)
                writeVLUInt(col);

            LastSId = SId;
            LastLine1 = line1;
        }

        void writeScaledLong(long N) throws IOException
        {
            if (N < 0) throw new IllegalArgumentException("Cannot handle negative values");
            int m = 0;
            while (N > 31)
            {
                N -= 32;
                m++;
                if (m == 8)
                {
                    writeByte(0);
                    N++;
                    m = 0;
                }
                N >>= 1;
            }
            if (m == 7 && N == 31)
            {
                writeByte(0);
                writeByte(0xFF);
            } else writeByte((byte) ~((m << 5) | (byte) N));
        }


        void initializeNewBlock()
        {
            LastSId = 0;
            LastLine1 = 0;
        }
    }
}
