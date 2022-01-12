package com.genexus;

import java.io.*;
import java.util.zip.*;
import java.util.*;

public final class GXParameterPacker
{

	private static final int expandSize = 512;
	private int count;
	private byte[] buf;

	static boolean COMPRESS = true;

	public GXParameterPacker()
	{
		this(expandSize);
	}

	public GXParameterPacker(int size)
	{
		reset(size);
	}

	public int getLength()
	{
		return count;
	}
	public final void reset(int size)
	{
		buf = new byte[size];
		count = 0;
	}

	public final void reset()
	{
		buf = new byte[expandSize];
		count = 0;
	}

	public final int size()
	{
		return count;
	}

	private final void expand()
	{
		expand (expandSize);
	}

	private final void expand(int step)
	{
		int  exp_val = step>expandSize?step:expandSize;

	    byte newbuf[] = new byte[buf.length + exp_val];
	    System.arraycopy(buf, 0, newbuf, 0, buf.length);
	    buf = newbuf;
	}


    /**
     * Writes an 8 bit byte.
     * @param data the byte value to be written
     * @since     JDK1.1
     */
    public final void writeBoolean(boolean data)
    {
		if (count >= buf.length)
	    	expand();

		buf[count++] = (byte) (data?1:0);
    }

    /**
     * Writes an 8 bit byte.
     * @param data the byte value to be written
     * @since     JDK1.1
     */
    public final void writeByte(int data)
    {
		if (count >= buf.length)
	    	expand();

		buf[count++] = (byte) data;
    }

    public final void writeLongByte(byte[] data)
	{
		writeByte(data);
	}

    public final void writeByte(byte[] data)
    {
		writeInt(data.length);
		for (int i = data.length - 1; i >= 0; i --)
			writeByte(data[i]);
    }

	public final void writeByteArray(byte [] data)
	{
		writeByte(data);
	}

    public final void writeByte(byte[][] data)
    {
		writeInt(data.length);
		writeInt(data[0].length);

		for (int i = data.length - 1; i >= 0; i --)
			for (int j = data[0].length - 1; j >= 0; j --)
				writeByte(data[i][j]);
    }

	public final void writeByteArray2(byte [][] data)
	{
		writeByte(data);
	}
    /**
     * Writes a 16 bit short.
     * @param data the short value to be written
     * @since     JDK1.1
     */
    public final void writeShort (int data)
    {
		if (count + 2 > buf.length)
	    	expand();

		buf[count++] = (byte)((data >>>  8) & 0xFF);
		buf[count++] = (byte)((data >>>  0) & 0xFF);
    }

    public final void writeShort(short[] data)
    {
		writeInt(data.length);
		for (int i = data.length - 1; i >= 0; i --)
			writeShort(data[i]);
    }

	public final void writeShortArray(short [] data)
	{
		writeShort(data);
	}

    public final void writeShort(short[][] data)
    {
		writeInt(data.length);
		writeInt(data[0].length);

		for (int i = data.length - 1; i >= 0; i --)
			for (int j = data[0].length - 1; j >= 0; j --)
				writeShort(data[i][j]);
    }

	public final void writeShortArray2(short [][] data)
	{
		writeShort(data);
	}

    /**
     * Writes a 16 bit char.
     * @param data the char value to be written
     * @since     JDK1.1
     */
    public final void writeChar(int data)
    {
		if (count + 2 > buf.length)
	    	expand();
		buf[count++] = (byte)((data >>>  8) & 0xFF);
		buf[count++] = (byte)((data >>>  0) & 0xFF);
    }

    public final void writeChar(int[] data)
    {
		writeInt(data.length);
		for (int i = data.length - 1; i >= 0; i --)
			writeChar(data[i]);
    }

	public final void writeCharArray(char[] data)
	{
		writeInt(data.length);
		for (int i = data.length - 1; i >= 0; i --)
			writeChar(data[i]);
	}

    public final void writeChar(int[][] data)
    {
		writeInt(data.length);
		writeInt(data[0].length);

		for (int i = data.length - 1; i >= 0; i --)
			for (int j = data[0].length - 1; j >= 0; j --)
				writeChar(data[i][j]);
    }

	public final void writeCharArray2(char [][] data)
	{
		writeInt(data.length);
		writeInt(data[0].length);

		for (int i = data.length - 1; i >= 0; i --)
			for (int j = data[0].length - 1; j >= 0; j --)
				writeChar(data[i][j]);
	}

    /**
     * Writes a 32 bit int.
     * @param data the integer value to be written
     * @since     JDK1.1
     */
    public final void writeInt(int data)
    {
		if (count + 4 > buf.length)
	    	expand();

		buf[count++] = (byte)((data >> 24) & 0xFF);
		buf[count++] = (byte)((data >> 16) & 0xFF);
		buf[count++] = (byte)((data >>  8) & 0xFF);
		buf[count++] = (byte)((data >>  0));
    }

    public final void writeInt(int[] data)
    {
		writeInt(data.length);
		for (int i = data.length - 1; i >= 0; i --)
			writeInt(data[i]);
    }

	public final void writeIntArray(int [] data)
	{
		writeInt(data);
	}

    public final void writeInt(int[][] data)
    {
		writeInt(data.length);
		writeInt(data[0].length);

		for (int i = data.length - 1; i >= 0; i --)
			for (int j = data[0].length - 1; j >= 0; j --)
				writeInt(data[i][j]);
    }

	public final void writeIntArray2(int [][] data)
	{
		writeInt(data);
	}

    /**
     * Writes a 64 bit long.
     * @param data the long value to be written
     * @since     JDK1.1
     */
    public final void writeLong(long data)
    {
		if (count + 8 > buf.length)
	    	expand();

		buf[count++] = (byte)((int)((data >>> 56)& 0xFF));
		buf[count++] = (byte)((int)((data >>> 48)& 0xFF));
		buf[count++] = (byte)((int)((data >>> 40)& 0xFF));
		buf[count++] = (byte)((int)((data >>> 32)& 0xFF));
		buf[count++] = (byte)((data >>> 24)& 0xFF);
		buf[count++] = (byte)((data >>> 16)& 0xFF);
		buf[count++] = (byte)((data >>>  8)& 0xFF);
		buf[count++] = (byte)((data >>>  0)& 0xFF);
    }

    public final void writeLong(long[] data)
    {
		writeInt(data.length);
		for (int i = data.length - 1; i >= 0; i --)
			writeLong(data[i]);
    }

	public final void writeLongArray(long [] data)
	{
		writeLong(data);
	}

    public final void writeLong(long[][] data)
    {
		writeInt(data.length);
		writeInt(data[0].length);

		for (int i = data.length - 1; i >= 0; i --)
			for (int j = data[0].length - 1; j >= 0; j --)
				writeLong(data[i][j]);
    }

	public final void writeLongArray2(long [][] data)
	{
		writeLong(data);
	}

    /**
     * Writes a 32 bit float.
     * @param data the float value to be written
     * @since     JDK1.1
     */
    public final void writeFloat(float data)
    {
		writeInt(Float.floatToIntBits(data));
    }

    public final void writeFloat(float[] data)
    {
		writeInt(data.length);
		for (int i = data.length - 1; i >= 0; i --)
			writeFloat(data[i]);
    }

	public final void writeFloatArray(float [] data)
	{
		writeFloat(data);
	}

    public final void writeFloat(float[][] data)
    {
		writeInt(data.length);
		writeInt(data[0].length);

		for (int i = data.length - 1; i >= 0; i --)
			for (int j = data[0].length - 1; j >= 0; j --)
				writeFloat(data[i][j]);
    }

	public final void writeFloatArray2(float [][] data)
	{
		writeFloat(data);
	}

    /**
     * Writes a 64 bit double.
     * @param data the double value to be written
     * @since     JDK1.1
     */
    public final void writeDouble(double data)
    {
    	writeLong(Double.doubleToLongBits(data));
    }

    public final void writeDouble(double[] data)
    {
		writeInt(data.length);
		for (int i = data.length - 1; i >= 0; i --)
		{
		 	writeDouble(data[i]);
		}
    }

	public final void writeDoubleArray(double [] data)
	{
		writeDouble(data);
	}

    public final void writeDouble(double[][] data)
	{
		writeInt(data.length);
		writeInt(data[0].length);

		for (int i = data.length - 1; i >= 0; i --)
			for (int j = data[0].length - 1; j >= 0; j --)
				writeDouble(data[i][j]);
	}

	public final void writeDoubleArray2(double [][] data)
	{
		writeDouble(data);
	}

	/**
	* Writes a String
	* @param data the string value to be written
	* @since Beta1
	*/
    public final void writeString(String data)
    {
		int length = data.length();
		writeInt(length);

		if (count + (length * 2) >= buf.length)
	    	expand(length);

		for (int i = 0; i < length ; i++)
			writeChar(data.charAt(i));
    }
/*

    public final void writeString(String data)
    {
		int length = data.length();
		byte[] aux = new byte[length];

		for (int i = length - 1; i >= 0; i--)
			aux[i] = (byte) data.charAt(i);
		writeInt(length);

		if (count + length >= buf.length)
	    	expand(length);

	    System.arraycopy(aux, 0, buf, count, length);
		count += length;
    }
*/

	/**
	* Writes a String
	* @param data the string value to be written
	* @since Beta1
	*/
    public final void writeStringTrim(String data)
    {
		writeString(CommonUtil.rtrim(data));
    }


    public final void writeBlobfile(String fileName)
    {
		byte[] data = new byte[0];

		if (fileName.trim().length() > 0)
		{
			try
			{
				data = CommonUtil.readToByteArray(new BufferedInputStream(new FileInputStream(new File(fileName))));
			}
			catch (IOException e)
			{
				throw new RuntimeException("Error reading " + fileName + " : " + e.getMessage());
			}
		}

		writeInt(data.length);
		for (int i = data.length - 1; i >= 0; i --)
			writeByte(data[i]);
    }

    public final void writeString(String[] data)
    {
		writeInt(data.length);
		for (int i = data.length - 1; i >= 0; i --)
			writeString(data[i]);
    }

	public final void writeStringArray(String [] data)
	{
		writeString(data);
	}

    public final void writeString(String[][] data)
    {
		writeInt(data.length);
		writeInt(data[0].length);

		for (int i = data.length - 1; i >= 0; i --)
			for (int j = data[0].length - 1; j >= 0; j --)
				writeString(data[i][j]);
    }

	public final void writeStringArray2(String [][] data)
	{
		writeString(data);
	}

	/**
	* Writes a Date
	* @param data the date value to be written
	* @since Beta1
	*/
    public final void writeDate(java.util.Date data)
    {
		writeLong(DateUtils.getDateAsTime(data));
    }

    public final void writeDate(java.util.Date[] data)
    {
		writeInt(data.length);
		for (int i = data.length - 1; i >= 0; i --)
			writeDate(data[i]);
    }

	public final void writeDateArray(java.util.Date [] data)
	{
		writeDate(data);
	}

    public final void writeDate(java.util.Date[][] data)
    {
		writeInt(data.length);
		writeInt(data[0].length);

		for (int i = data.length - 1; i >= 0; i --)
			for (int j = data[0].length - 1; j >= 0; j --)
				writeDate(data[i][j]);
    }

	public final void writeDateArray2(java.util.Date [][] data)
	{
		writeDate(data);
	}

	/**
	* Writes a Decimal
	* @param data the Decimal value to be written
	* @since Beta1
	*/

    public final void writeDecimal(java.math.BigDecimal data)
    {
		writeString(data.toString());
    }

    public final void writeDecimal(java.math.BigDecimal[] data)
    {
		writeInt(data.length);
		for (int i = data.length - 1; i >= 0; i --)
			writeDecimal(data[i]);
    }

	public final void writeDecimalArray(java.math.BigDecimal [] data)
	{
		writeDecimal(data);
	}
    public final void writeDecimal(java.math.BigDecimal[][] data)
    {
		writeInt(data.length);
		writeInt(data[0].length);

		for (int i = data.length - 1; i >= 0; i --)
			for (int j = data[0].length - 1; j >= 0; j --)
				writeDecimal(data[i][j]);
    }

	public final void writeDecimalArray2(java.math.BigDecimal [][] data)
	{
		writeDecimal(data);
	}

	public String getAsString()
	{
			return new String(com.genexus.util.Codecs.base64Encode(toByteArray()));
	}

	/**
	* Converts the buffer to a byte array of the size of the buffer
	* @since Beta1
	*/
	public final byte[] toByteArray()
	{
		byte[] returnBuffer;
		if	(COMPRESS)
		{
			if	(needsCompressing(count))
			{
				try
				{
					ByteArrayOutputStream bytes = new ByteArrayOutputStream(count);
					GZIPOutputStream 	  zip   = new GZIPOutputStream(bytes);
					zip.write(buf, 0, count);
					zip.close();

					byte[] zipped = bytes.toByteArray();
					returnBuffer = new byte[zipped.length + 1];
					returnBuffer[0] = 1;
					System.arraycopy(zipped, 0, returnBuffer, 1, zipped.length);
				}
				catch (IOException e)
				{
					System.err.println("Exception compressing output - sending uncompressed");
					returnBuffer = new byte[count + 1];
					System.arraycopy(buf, 0, returnBuffer, 1, count);
				}
			}
			else
			{
				// Si no tiene nada, mando vacio, ya se avivara el unpacker.
				if	(count == 0)
					return new byte[0];

				// Si no hay que comprimir, mando descomprimido.
				returnBuffer = new byte[count + 1];
				System.arraycopy(buf, 0, returnBuffer, 1, count);
			}
		}
		else
		{
			returnBuffer = new byte[count];
			System.arraycopy(buf, 0, returnBuffer, 0, count);
		}

		return returnBuffer;
    }

	public String toString()
	{
		StringBuffer ret = new StringBuffer();

		for (int i = 0; i < count; i++)
		{
			ret.append(buf[i]);
			ret.append(',');
		}

		return ret.toString();
	}

	private static boolean needsCompressing(int count)
	{
		return count > 512;
	}

	public void writeGeneric(int[] types, Object[] data)
	{
		int len = types.length;

		for (int i = 0 ; i < len; i++)
		{
			switch (types[i])
			{
				case TypeConstants.BYTE:
					writeByte( ((Byte) data[i]).byteValue());
					break;
				case TypeConstants.SHORT:
					writeShort( ((Short) data[i]).shortValue());
					break;
				case TypeConstants.INT:
					writeInt( ((Integer) data[i]).intValue());
					break;
				case TypeConstants.LONG:
					writeLong( ((Long) data[i]).longValue());
					break;
				case TypeConstants.FLOAT:
					writeFloat( ((Float) data[i]).floatValue());
					break;
				case TypeConstants.DOUBLE:
					writeDouble( ((Double) data[i]).doubleValue());
					break;
				case TypeConstants.STRING:
					writeString((String) data[i]);
					break;
				case TypeConstants.DATE:
					writeDate((java.util.Date) data[i]);
					break;
				case TypeConstants.DECIMAL:
					writeDecimal((java.math.BigDecimal) data[i]);
					break;
				case TypeConstants.BOOLEAN:
					writeBoolean(((Boolean)data[i]).booleanValue());
					break;
				case TypeConstants.BLOB:
					writeBlobfile((String) data[i]);
					break;
				default:
					System.err.println("Unrecognized parm2 " + types[i]);
			}
		}
	}

	public void writeGeneric2(int[] types, Object[] data)
	{
		// Es parecido que la WriteGeneric solo que tambi�n manda el tipo de dato
		// De esta manera el Unpacker no necesita tener el array de types
		// (Ademas el reader no devuelve array de objects)
		int len = types.length;
		writeShort(len);

		for (int i = 0 ; i < len; i++)
		{
			writeByte((byte) types[i]);
			switch (types[i])
			{
				case TypeConstants.BYTE:
					writeByte( ((Byte) data[i]).byteValue());
					break;
				case TypeConstants.SHORT:
					writeShort( ((Short) data[i]).shortValue());
					break;
				case TypeConstants.INT:
					writeInt( ((Integer) data[i]).intValue());
					break;
				case TypeConstants.LONG:
					writeLong( ((Long) data[i]).longValue());
					break;
				case TypeConstants.FLOAT:
					writeFloat( ((Float) data[i]).floatValue());
					break;
				case TypeConstants.DOUBLE:
					writeDouble( ((Double) data[i]).doubleValue());
					break;
				case TypeConstants.STRING:
					writeString((String) data[i]);
					break;
				case TypeConstants.DATE:
					writeDate((java.util.Date) data[i]);
					break;
				case TypeConstants.DECIMAL:
					writeDecimal((java.math.BigDecimal) data[i]);
					break;
				case TypeConstants.BOOLEAN:
					writeBoolean(((Boolean)data[i]).booleanValue());
					break;
				case TypeConstants.BLOB:
					writeBlobfile((String) data[i]);
					break;

				default:
					System.err.println("Unrecognized parm2 " + types[i]);
			}
		}
	}

	public void writeObject(Object obj)
	{
		try
		{
			ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(tempStream);
			out.writeObject(obj);
			out.close();
			tempStream.close();
			writeByte(tempStream.toByteArray());
		}catch(IOException e)
		{
			throw new RuntimeException("Error writing object " + obj.toString() + " --> " + e.toString());
		}
	}

	public void writeGxObjectCollection(GXSimpleCollection col)
	{
		writeObject(col);
	}
}
