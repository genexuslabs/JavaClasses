package com.genexus.compression;

import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import java.util.Vector;

public interface IGXCompressor {
	static Boolean compressFiles(Vector<String> files, String path, String format, GXBaseCollection<SdtMessages_Message>[] messages) {return null;}
	static Boolean compressFolder(String folder, String path, String format, GXBaseCollection<SdtMessages_Message>[] messages) {return null;}
	static Compression newCompression(String path, String format, int dictionarySize, GXBaseCollection<SdtMessages_Message>[] messages) { return new Compression();}
	static Boolean decompress(String file, String path, GXBaseCollection<SdtMessages_Message>[] messages) {return null;}
}
