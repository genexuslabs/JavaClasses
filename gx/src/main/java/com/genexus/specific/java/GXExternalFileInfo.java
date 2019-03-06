package com.genexus.specific.java;

import com.genexus.common.interfaces.IExtensionGXExternalFileInfo;
import com.genexus.db.driver.ExternalProvider;
import com.genexus.util.GXFile;
import com.genexus.util.GXFileCollection;

public class GXExternalFileInfo implements IExtensionGXExternalFileInfo {

	@Override
	public GXFileCollection listFiles(String filter, Object prov, String name) {
	  ExternalProvider provider = (ExternalProvider) prov;
	  GXFileCollection files = new GXFileCollection ();
	  for(String file : (filter != null)? provider.getFiles(name, filter) : provider.getFiles(name)){
        files.add(new GXFile(new com.genexus.util.GXExternalFileInfo(file, provider)));
	  }
	  return files;
	}

}
