/*
 * @(#)MD4.java						0.3-1N 20/03/1999
 *
 *  This file is part of the HTTPClient package
 *  Copyright (C) 1998-1999  Ronald Tschal�r
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@innovation.ch
 *
 */

package HTTPClient;

import  java.io.FileInputStream;


/**
 * This class implements the MD4 hash as specified in RFC-1320.
 *
 * The code is based on the MD5 class (included in this package) written
 * by Santeri Paavolainen and the reference implementation in RFC-1320.
 * 
 * @version	0.3-1N  20/03/1999
 * @author	Ronald Tschal�r
 * @since	V0.4
 */

class MD4
{
    /** padding for getHash() */
    private static final byte[] padding = {
	(byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    /** the current state of the hash */
    private MD4State state;

    /** the final hash */
    private byte[] final_hash;


    // Constructors

    /**
     * Start a new MD4 hash.
     */
    public MD4()
    {
	state = new MD4State();
    }


    /**
     * Start a new MD4 hash, initializing it with the given data. This is
     * the same as creating a new MD4 and invoking update().
     *
     * @param data the data to initialize the hash with.
     */
    public MD4(byte[] data)
    {
	this();
	update(data);
    }


    // Methods

    /**
     * Update the hash computation.
     *
     * @param data the array of bytes to add to the hash computation
     * @param off  the offset in <var>data</var> at which to begin
     * @param len  the number of bytes from <var>data</var> to use
     * @exception IllegalStateException if <code>getHash()</code has
     *                                  has already been invoked
     */
    public void update(byte[] data, int off, int len)
    {
	if (final_hash != null)
	    throw new IllegalStateException("Hash already terminated");

	int buf_off = (int) ((state.count >>> 3) & 63);	// offset in state buf
	int part_len = 64 - buf_off;
	state.count += len << 3;

	// transform as much as possible
	if (len >= part_len)
	{
	    System.arraycopy(data, off, state.buffer, buf_off, part_len);
	    transform(state.buffer, 0);

	    int end = off + len;
	    for (off+=part_len; off<(end-63); off+=64)
		transform(data, off);

	    buf_off = 0;
	    len     = end - off;
	}

	// save rest in state buffer
	System.arraycopy(data, off, state.buffer, buf_off, len);
    }


    /**
     * Update the hash computation.
     *
     * @param data the array of bytes to add to the hash computation
     * @exception IllegalStateException if <code>getHash()</code has
     *                                  has already been invoked
     */
    public void update(byte[] data)
    {
	update(data, 0, data.length);
    }


    private static final int rotate_left(int x, int n)
    {
	return (x << n) | (x >>> (32 - n));
    }

    private static final int FF(int a, int b, int c, int d, int x, int s)
    {
	a += ((b & c) | (~b & d)) + x;
	return rotate_left(a, s);
    }

    private static final int GG(int a, int b, int c, int d, int x, int s)
    {
	a += ((b & c) | (b & d) | (c & d)) + x + 0x5a827999;
	return rotate_left(a, s);
    }

    private static final int HH(int a, int b, int c, int d, int x, int s)
    {
	a += (b ^ c ^ d) + x + 0x6ed9eba1;
	return rotate_left(a, s);
    }

    private void transform(byte buffer[], int shift)
    {
	int a = state.state[0],
	    b = state.state[1],
	    c = state.state[2],
	    d = state.state[3];

	int[] x = decode(buffer, shift, 64);

	/* Round 1 */
	a = FF(a, b, c, d, x[ 0],  3); /* 1 */
	d = FF(d, a, b, c, x[ 1],  7); /* 2 */
	c = FF(c, d, a, b, x[ 2], 11); /* 3 */
	b = FF(b, c, d, a, x[ 3], 19); /* 4 */
	a = FF(a, b, c, d, x[ 4],  3); /* 5 */
	d = FF(d, a, b, c, x[ 5],  7); /* 6 */
	c = FF(c, d, a, b, x[ 6], 11); /* 7 */
	b = FF(b, c, d, a, x[ 7], 19); /* 8 */
	a = FF(a, b, c, d, x[ 8],  3); /* 9 */
	d = FF(d, a, b, c, x[ 9],  7); /* 10 */
	c = FF(c, d, a, b, x[10], 11); /* 11 */
	b = FF(b, c, d, a, x[11], 19); /* 12 */
	a = FF(a, b, c, d, x[12],  3); /* 13 */
	d = FF(d, a, b, c, x[13],  7); /* 14 */
	c = FF(c, d, a, b, x[14], 11); /* 15 */
	b = FF(b, c, d, a, x[15], 19); /* 16 */

	/* Round 2 */
	a = GG(a, b, c, d, x[ 0],  3); /* 17 */
	d = GG(d, a, b, c, x[ 4],  5); /* 18 */
	c = GG(c, d, a, b, x[ 8],  9); /* 19 */
	b = GG(b, c, d, a, x[12], 13); /* 20 */
	a = GG(a, b, c, d, x[ 1],  3); /* 21 */
	d = GG(d, a, b, c, x[ 5],  5); /* 22 */
	c = GG(c, d, a, b, x[ 9],  9); /* 23 */
	b = GG(b, c, d, a, x[13], 13); /* 24 */
	a = GG(a, b, c, d, x[ 2],  3); /* 25 */
	d = GG(d, a, b, c, x[ 6],  5); /* 26 */
	c = GG(c, d, a, b, x[10],  9); /* 27 */
	b = GG(b, c, d, a, x[14], 13); /* 28 */
	a = GG(a, b, c, d, x[ 3],  3); /* 29 */
	d = GG(d, a, b, c, x[ 7],  5); /* 30 */
	c = GG(c, d, a, b, x[11],  9); /* 31 */
	b = GG(b, c, d, a, x[15], 13); /* 32 */

	/* Round 3 */
	a = HH(a, b, c, d, x[ 0],  3); /* 33 */
	d = HH(d, a, b, c, x[ 8],  9); /* 34 */
	c = HH(c, d, a, b, x[ 4], 11); /* 35 */
	b = HH(b, c, d, a, x[12], 15); /* 36 */
	a = HH(a, b, c, d, x[ 2],  3); /* 37 */
	d = HH(d, a, b, c, x[10],  9); /* 38 */
	c = HH(c, d, a, b, x[ 6], 11); /* 39 */
	b = HH(b, c, d, a, x[14], 15); /* 40 */
	a = HH(a, b, c, d, x[ 1],  3); /* 41 */
	d = HH(d, a, b, c, x[ 9],  9); /* 42 */
	c = HH(c, d, a, b, x[ 5], 11); /* 43 */
	b = HH(b, c, d, a, x[13], 15); /* 44 */
	a = HH(a, b, c, d, x[ 3],  3); /* 45 */
	d = HH(d, a, b, c, x[11],  9); /* 46 */
	c = HH(c, d, a, b, x[ 7], 11); /* 47 */
	b = HH(b, c, d, a, x[15], 15); /* 48 */

	// update state
	state.state[0] += a;
	state.state[1] += b;
	state.state[2] += c;
	state.state[3] += d;
    }


    /**
     * Ends the hash calculation and returns the hash.
     *
     * @return a byte array containing the hash
     */
    public byte[] getHash()
    {
	if (final_hash != null)  return final_hash;

	int[] orig_count =
		{ (int) (state.count & 0xFFFFFFFF), (int) (state.count >> 32) };

	// pad to 56 (mod 64)
	int buf_off = (int) ((state.count >>> 3) & 63);	// offset in state buf
	int pad_len = (buf_off < 56) ? (56 - buf_off) : (120 - buf_off);
	update(padding, 0, pad_len);

	// append length
	update(encode(orig_count, 0, 2), 0, 8);

	// return state
	final_hash = encode(state.state, 0, 4);
	return final_hash;
    }


    /**
     * Convert an array of byte's into an array of int's (little endian).
     *
     * @param buffer the array of bytes to convert
     * @param off    the offset in <var>buffer</var> to start at
     * @param len    the length of <var>buffer</var>; must be a multiple of 4
     * @return an array of int
     */
    private static final int[] decode(byte buffer[], int off, int len)
    {
	int   ilen = len >>> 2;
	int[] out  = new int[ilen];

	for (int dst=0, src=off; dst<ilen; dst++)
	{
	    out[dst] =  (buffer[src++] & 0xff) |
		       ((buffer[src++] & 0xff) << 8) |
		       ((buffer[src++] & 0xff) << 16) |
		       ((buffer[src++] & 0xff) << 24);
	}

	return out;
    }


    /**
     * Convert an array of int's into an array of byte's (little endian).
     *
     * @param buffer the array of int's to convert
     * @param off    the offset in <var>buffer</var> to start at
     * @param len    the length of <var>buffer</var>
     * @return an array of byte
     */
    private static final byte[] encode(int buffer[], int off, int len)
    {
	int    blen = len << 2;
	byte[] out  = new byte[blen];

	for (int dst=0, src=off; dst<blen; src++)
	{
	    int b = buffer[src];
	    out[dst++] = (byte) (b & 0xff);
	    out[dst++] = (byte) ((b >> 8)  & 0xff);
	    out[dst++] = (byte) ((b >> 16) & 0xff);
	    out[dst++] = (byte) ((b >> 24) & 0xff);
	}

	return out;
    }


    /**
     * Produces a string containing the hash in hex notation. This
     * implicitly ends the hash calculation.
     *
     * @return a string representation of the hash
     */
    public String toString()
    {
	byte[] hash = getHash();
	StringBuffer buf = new StringBuffer(hash.length * 2);

	for (int idx=0; idx<hash.length; idx++)
	{
	    int num = hash[idx] & 0xFF;
	    if (num < 0x10)  buf.append("0");
	    buf.append(Integer.toString(num, 16));
	}

	return buf.toString();
    }


   
}


/**
 * The current state of the hash. Loosely corresponds to MD4_CTX in the
 * reference implementation in RFC-1320.
 */
class MD4State
{
    /** 128 bit internal state */
    int[] state;

    /** number of bits hashed so far */
    long count;

    /** buffer to hold partial 64-byte chunks */
    byte[] buffer;


    /**
     * Create a new MD4State.
     */
    MD4State()
    {
	buffer = new byte[64];

	state    = new int[4];
	state[0] = 0x67452301;
	state[1] = 0xefcdab89;
	state[2] = 0x98badcfe;
	state[3] = 0x10325476;

	count = 0;
    }
}

