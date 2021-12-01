package com.genexus.cryptography.signing.xml;


import java.io.ByteArrayOutputStream;


import org.apache.xml.security.Init;

public class Canonicalizer {

	static {
		// XML canonicalizers must be added to hash array before we call
		// getInstance
		// Probably there is another way. I cannot found it though
		(new Init()).init();

	}

	public static String canonize(String input) throws Exception {

		byte inputBytes[] = input.getBytes();
		org.apache.xml.security.c14n.Canonicalizer cononicalizer = org.apache.xml.security.c14n.Canonicalizer
				.getInstance(org.apache.xml.security.c14n.Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);

		byte result[];
		try(ByteArrayOutputStream newStream = new ByteArrayOutputStream())
		{
			cononicalizer.canonicalize(inputBytes, newStream, false);
			result = newStream.toByteArray();
		}
		
		
		return new String(result);
	}

}
