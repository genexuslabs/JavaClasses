package com.genexus;
import java.util.Date;

import java.io.*;
import java.util.zip.*;

import com.genexus.common.interfaces.SpecificImplementation;

import java.util.*;

public final class GXParameterUnpacker
{
	private int count = 0;
	private byte[] buf;
	private int originalSize;

	public GXParameterUnpacker(byte[] buf)
	{
		originalSize = buf.length;
		reset(buf);
	}

	public int getOriginalSize()
	{
		return originalSize;
	}
	public GXParameterUnpacker()
	{
		// Attenti, si se usa este metodo, hay que hacer un
		// reset antes de poder leer nada
		this.buf = null;
	}

	public byte[] getUnreadBuff()
	{
		byte[] res = new byte[buf.length-count];
		System.arraycopy(buf, count, res, 0, res.length);
		return res;
	}

	public boolean eof()
	{
		return buf.length == count;
	}

	public int getCount()
	{
		return count;
	}
	public int getLength()
	{
		return buf.length;
	}

	public void resetFromString(String str)
	{
		try
		{
			reset(com.genexus.util.Codecs.base64Decode(str.getBytes()));
		}catch(Exception e)
		{
			System.err.println(e.toString());
		}
	}
	
	public void reset(byte[] buf)
	{
		count = 0;

		if	(GXParameterPacker.COMPRESS)
		{
			if	(buf.length == 0)
			{
				this.buf = new byte[0];
			}
			else if	(buf[0] == 0)
			{
				// Viene descomprimido
				this.buf = new byte[buf.length - 1];
				System.arraycopy(buf, 1, this.buf, 0, buf.length - 1);
			}			
			else
			{
				try
				{
					this.buf = CommonUtil.readToByteArray(new GZIPInputStream(new ByteArrayInputStream(buf, 1, buf.length - 1)));
				}
				catch (IOException e)
				{
					System.err.println("Error decompressing parameters");
					throw new InternalError("Error decompressing parameters");
				}
			}
		}
		else
		{
			this.buf = buf;
		}
	}

    /**
     * Writes an 8 bit byte.
     * @param data the byte value to be written
     * @since     JDK1.1
     */
    public final boolean readBoolean()
    {
		return ((byte) buf[count++]) == 1;
    }

    public final byte readByte() 
    {
		return (byte) buf[count++];
    }

    public final byte[] readLongByteArray() 
    {
		return readByteArray();
    }

    public final byte[] readByteArray() 
    {
		byte[] data = new byte[readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			data[i] = readByte();

		return data;
    }

    public final void readByteArray(byte[] data) 
    {
		int length = readInt();

		for (int i = length - 1; i >=0 ; i--)
			data[i] = readByte();
    }

    public final byte[][] readByteArray2() 
    {
		byte[][] data = new byte[readInt()][readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			for (int j = data[0].length - 1; j >=0 ; j--)
				data[i][j] = readByte();

		return data;
    }

    public final void readByteArray2(byte[][] data) 
	{
		readByteArray(data);
	}

    public final void readByteArray(byte[][] data) 
    {
		int length = readInt();
		int length2 = readInt();

		for (int i = length - 1; i >=0 ; i--)
			for (int j = length2 - 1; j >=0 ; j--)
				data[i][j] = readByte();
    }

    public final short readShort() 
	{
		return (short) (
						(((int) buf[count++] & 0xFF) << 8) +
						((int) buf[count++] & 0xFF)
					   );
    }

    public final short[] readShortArray() 
    {
		short[] data = new short[readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			data[i] = readShort();

		return data;
    }

    public final void readShortArray(short[] data) 
    {
		int length = readInt();

		for (int i = length - 1; i >=0 ; i--)
			data[i] = readShort();
    }

    public final short[][] readShortArray2() 
    {
		short[][] data = new short[readInt()][readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			for (int j = data[0].length - 1; j >=0 ; j--)
				data[i][j] = readShort();

		return data;
    }

    public final void readShortArray2(short[][] data) 
	{
		readShortArray(data);
	}

    public final void readShortArray(short[][] data) 
    {
		int length = readInt();
		int length2 = readInt();

		for (int i = length - 1; i >=0 ; i--)
			for (int j = length2 - 1; j >=0 ; j--)
				data[i][j] = readShort();
    }

    public final char readChar() 
    {
		return (char) ((buf[count++] << 8) + (int) (buf[count++] & 0xFF)) ;
		//(buf[count++] << 0));
    }

    public final char[] readCharArray() 
    {
		char[] data = new char[readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			data[i] = readChar();

		return data;
    }

    public final void readCharArray(char[] data) 
    {
		int length = readInt();

		for (int i = length - 1; i >=0 ; i--)
			data[i] = readChar();
    }

    public final char[][] readCharArray2() 
    {
		char[][] data = new char[readInt()][readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			for (int j = data[0].length - 1; j >=0 ; j--)
				data[i][j] = readChar();

		return data;
    }

    public final void readCharArray2(char[][] data) 
	{
		readCharArray(data);
	}

    public final void readCharArray(char[][] data) 
    {
		int length = readInt();
		int length2 = readInt();

		for (int i = length - 1; i >=0 ; i--)
			for (int j = length2 - 1; j >=0 ; j--)
				data[i][j] = readChar();
    }

    public final int readInt() 
    {
		return  
			(((int) buf[count++] & 0xFF) << 24) +
				(((int) buf[count++] & 0xFF) << 16) +
				(((int) buf[count++] & 0xFF) <<  8) +
				((int) buf[count++] & 0xFF) ;
    }

    public final int[] readIntArray() 
    {
		int[] data = new int[readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			data[i] = readInt();

		return data;
    }

    public final void readIntArray(int[] data) 
    {
		int length = readInt();

		for (int i = length - 1; i >=0 ; i--)
			data[i] = readInt();
    }

    public final int[][] readIntArray2() 
    {
		int[][] data = new int[readInt()][readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			for (int j = data[0].length - 1; j >=0 ; j--)
				data[i][j] = readInt();

		return data;
    }

    public final void readIntArray2(int[][]  data) 
	{
    	readIntArray(data);
	}

    public final void readIntArray(int[][]  data) 
    {
		int length = readInt();
		int length2 = readInt();

		for (int i = length - 1; i >=0 ; i--)
			for (int j = length2 - 1; j >=0 ; j--)
				data[i][j] = readInt();
    }

    public final long readLong() 
    {
		return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
    }

    public final long[] readLongArray() 
    {
		long[] data = new long[readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			data[i] = readLong();

		return data;
    }

    public final void readLongArray(long[] data) 
    {
		int length = readInt();

		for (int i = length - 1; i >=0 ; i--)
			data[i] = readLong();
    }

    public final long[][] readLongArray2() 
    {
		long[][] data = new long[readInt()][readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			for (int j = data[0].length - 1; j >=0 ; j--)
				data[i][j] = readLong();

		return data;
    }

    public final void readLongArray2(long[][] data) 
	{
    	readLongArray(data);

	}

    public final void readLongArray(long[][] data) 
    {
		int length = readInt();
		int length2 = readInt();

		for (int i = length - 1; i >=0 ; i--)
			for (int j = length2 - 1; j >=0 ; j--)
				data[i][j] = readLong();

    }


    public final float readFloat() 
    {
		return Float.intBitsToFloat(readInt());
    }

    public final float[] readFloatArray() 
    {
		float[] data = new float[readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			data[i] = readFloat();

		return data;
    }

    public final void readFloatArray(float[]  data) 
    {
		int length = readInt();

		for (int i = length - 1; i >=0 ; i--)
			data[i] = readFloat();

    }

    public final float[][] readFloatArray2() 
    {
		float[][] data = new float[readInt()][readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			for (int j = data[0].length - 1; j >=0 ; j--)
				data[i][j] = readFloat();

		return data;
    }

    public final void readFloatArray2(float[][] data ) 
	{
    	readFloatArray(data ) ;

	}
    public final void readFloatArray(float[][] data ) 
    {
		int length = readInt();
		int length2 = readInt();

		for (int i = length - 1; i >=0 ; i--)
			for (int j = length2 - 1; j >=0 ; j--)
				data[i][j] = readFloat();
    }

    public final double readDouble() 
    {
		return Double.longBitsToDouble(readLong());
    }

    public final double[] readDoubleArray() 
    {
		double[] data = new double[readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			data[i] = readDouble();

		return data;
    }

    public final void readDoubleArray(double[] data) 
    {
		int length = readInt();

		for (int i = length - 1; i >=0 ; i--)
			data[i] = readDouble();
    }

    public final double[][] readDoubleArray2() 
    {
		double[][] data = new double[readInt()][readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			for (int j = data[0].length - 1; j >=0 ; j--)
				data[i][j] = readDouble();

		return data;
    }

    public final void readDoubleArray2(double[][] data) 
	{
		readDoubleArray(data);
	}

    public final void readDoubleArray(double[][] data) 
    {
		int length = readInt();
		int length2 = readInt();

		for (int i = length - 1; i >=0 ; i--)
			for (int j = length2 - 1; j >=0 ; j--)
				data[i][j] = readDouble();
    }


    public final String readBlobfile() 
    {
		String fileName = SpecificImplementation.Application.getDefaultPreferences().getBLOB_PATH() + SpecificImplementation.FileUtils.getTempFileName("tmp");
		byte[] data = new byte[readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			data[i] = readByte();
	
		try (FileOutputStream fos = new FileOutputStream(new File(fileName))){
			OutputStream destination = new BufferedOutputStream(fos);
			destination.write(data, 0, data.length);
			destination.close();
		}
		catch (IOException e)
		{
			System.err.println("Error reading " + fileName + " e " + e.getMessage());
		}

		return fileName;
    }
    public final String readString()
    {
		int length = readInt();

		StringBuffer sb = new StringBuffer(length);
		for (int i = length - 1; i >= 0; i--)
			sb.append(readChar());
		
		return sb.toString();
    }
/*
    public final String readString()
    {
		int length = readInt();
		
		String aux = new String(buf, count, length);
		count += length;

		return aux;
    }
*/

    public final String[] readStringArray() 
    {
		String[] data = new String[readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			data[i] = readString();

		return data;
    }

    public final void readStringArray(String[] data) 
    {
		int length = readInt();

		for (int i = length - 1; i >=0 ; i--)
			data[i] = readString();

    }

    public final String[][] readStringArray2() 
    {
		String[][] data = new String[readInt()][readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			for (int j = data[0].length - 1; j >=0 ; j--)
				data[i][j] = readString();

		return data;
    }


    public final void readStringArray2(String[][] data) 
	{
		readStringArray(data);
	}

    public final void readStringArray(String[][] data) 
    {
		int length = readInt();
		int length2 = readInt();
		
		for (int i = length - 1; i >=0 ; i--)
			for (int j = length2 - 1; j >=0 ; j--)
				data[i][j] = readString();

    }


    public final java.util.Date readDate ()
    {
		return DateUtils.getTimeAsDate(readLong());
    }

    public final java.util.Date[] readDateArray() 
    {
		java.util.Date[] data = new java.util.Date[readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			data[i] = readDate();

		return data;
    }

    public final void readDateArray(java.util.Date[] data) 
    {
		int length = readInt();

		for (int i = length - 1; i >=0 ; i--)
			data[i] = readDate();

    }

    public final Date[][] readDateArray2() 
    {
		Date[][] data = new Date[readInt()][readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			for (int j = data[0].length - 1; j >=0 ; j--)
				data[i][j] = readDate();

		return data;
    }

    public final void readDateArray2(Date[][] data) 
	{
		readDateArray(data);
	}

    public final void readDateArray(Date[][] data) 
    {
		int length = readInt();
		int length2 = readInt();

		for (int i = length - 1; i >=0 ; i--)
			for (int j = length2 - 1; j >=0 ; j--)
				data[i][j] = readDate();
    }

	// -- Decimal
    public final java.math.BigDecimal readDecimal()
    {
		return new java.math.BigDecimal(readString());
    }

    public final java.math.BigDecimal[] readDecimalArray() 
    {
		java.math.BigDecimal[] data = new java.math.BigDecimal[readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			data[i] = readDecimal();

		return data;
    }

    public final void readDecimalArray(java.math.BigDecimal[] data) 
    {
		int length = readInt();

		for (int i = length - 1; i >=0 ; i--)
			data[i] = readDecimal();

    }

    public final java.math.BigDecimal[][] readDecimalArray2() 
    {
		java.math.BigDecimal[][] data = new java.math.BigDecimal[readInt()][readInt()];

		for (int i = data.length - 1; i >=0 ; i--)
			for (int j = data[0].length - 1; j >=0 ; j--)
				data[i][j] = readDecimal();

		return data;
    }

    public final void readDecimalArray2(java.math.BigDecimal[][] data) 
	{
		readDecimalArray(data);
	}

    public final void readDecimalArray(java.math.BigDecimal[][] data) 
    {
		int length = readInt();
		int length2 = readInt();

		for (int i = length - 1; i >=0 ; i--)
			for (int j = length2 - 1; j >=0 ; j--)
				data[i][j] = readDecimal();
    }


	public String toString()
	{
		StringBuffer ret = new StringBuffer();

		for (int i = 0; i < buf.length; i++) 
		{
			ret.append(buf[i]);
			ret.append(',');
		}

		ret.append("count: ");
		ret.append(count);
		ret.append("length: ");
		ret.append(buf.length);
		return ret.toString();
	}

	public void readGeneric(int[] types, Object[] data)
	{
		for (int i = 0; i < data.length; i++)
		{
			switch (types[i])
			{
				case TypeConstants.BYTE:
					((byte[]) data[i])[0] = readByte();
					break;
				case TypeConstants.SHORT:
					((short[]) data[i])[0] = readShort();
					break;
				case TypeConstants.INT:
					((int[]) data[i])[0] = readInt();
					break;
				case TypeConstants.LONG:
					((long[]) data[i])[0] = readLong();
					break;
				case TypeConstants.FLOAT:
					((float[]) data[i])[0] = readFloat();
					break;
				case TypeConstants.DOUBLE:
					((double[]) data[i])[0] = readDouble();
					break;
				case TypeConstants.STRING:
					((String[]) data[i])[0] = readString();
					break;
				case TypeConstants.DATE:
					((java.util.Date[]) data[i])[0] = readDate();
					break;
				case TypeConstants.DECIMAL:
					((java.math.BigDecimal[]) data[i])[0] = readDecimal();
					break;
				case TypeConstants.BOOLEAN:
					((boolean[]) data[i])[0] = readBoolean();
					break;
				case TypeConstants.BLOB:
					((String[]) data[i])[0] = readBlobfile();
					break;

				default:
					System.err.println("Unrecognized parm2 " + types[i]);
			}
		}
	}

	public Object[] readGeneric2()
	{
		short size = readShort();
		Object [] data = new Object[size];
		for (int i = 0; i < data.length; i++)
		{
			byte type = readByte();
			switch (type)
			{
				case TypeConstants.BYTE:
					data[i] = new Byte(readByte());
					break;
				case TypeConstants.SHORT:
					data[i] = new Short(readShort());
					break;
				case TypeConstants.INT:
					data[i] = new Integer(readInt());
					break;
				case TypeConstants.LONG:
					data[i] = new Long(readLong());
					break;
				case TypeConstants.FLOAT:
					data[i] = new Float(readFloat());
					break;
				case TypeConstants.DOUBLE:
					data[i] = new Double(readDouble());
					break;
				case TypeConstants.STRING:
					data[i] = readString();
					break;
				case TypeConstants.DATE:
					data[i] = readDate();
					break;
				case TypeConstants.DECIMAL:
					data[i] = readDecimal();
					break;
				case TypeConstants.BOOLEAN:
					 data[i] = new Boolean(readBoolean());
					break;
				case TypeConstants.BLOB:
					data[i] = readBlobfile();
					break;

				default:
					System.err.println("Unrecognized parm2 " + type);
			}
		}
		return data;
	}

	public final Object readObject()
	{
		try
		{
			ByteArrayInputStream tempStream = new ByteArrayInputStream(readByteArray());
			ObjectInputStream in = new ObjectInputStream(tempStream);
			Object ret = in.readObject();
			in.close();
			tempStream.close();
			return ret;
		}catch(IOException e)
		{
			throw new RuntimeException("Error reading object --> " + e.getMessage());
		}catch(ClassNotFoundException e2)
		{
			throw new RuntimeException("Error reading object --> " + e2.getMessage());
		}
	}
	
	public final GXSimpleCollection readGxObjectCollection()
	{
		return (GXSimpleCollection)readObject();
	}

}