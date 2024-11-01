package com.genexus.compression;

import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;

import java.util.ArrayList;

public interface IGXCompressor {
	static Boolean compress(ArrayList<String> files, String path, GXBaseCollection<SdtMessages_Message>[] messages) {return false;}
	static Compression newCompression(String path, GXBaseCollection<SdtMessages_Message>[] messages) { return new Compression();}
	static Boolean decompress(String file, String path, GXBaseCollection<SdtMessages_Message>[] messages) {return false;}
}
