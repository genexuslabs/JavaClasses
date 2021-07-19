package com.genexus.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Vector;

import com.genexus.IHttpContext;
import com.genexus.ModelContext;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.db.driver.ResourceAccessControlList;
import com.genexus.db.driver.ExternalProvider;
import com.genexus.webpanels.HttpContextWeb;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.output.FileWriterWithEncoding;

import com.genexus.Application;
import com.genexus.CommonUtil;
import com.genexus.common.classes.AbstractGXFile;
import org.apache.logging.log4j.Logger;

public class GXFile extends AbstractGXFile {

	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(HttpContextWeb.class);

    private IGXFileInfo FileSource;
    private int ErrCode;
    private String ErrDescription;
    private boolean ret;
    private boolean isExternal = false;
    private String uploadFileId;
    
    public static ICleanupFile CleanUp;
    
    public GXFile() {
    }

    public GXFile(String fileName) {
        this(fileName, ResourceAccessControlList.Default);
    }

    //For compatibility reasons
	public GXFile(String fileName, boolean isPrivate) {
		this(fileName, isPrivate ? ResourceAccessControlList.Private: ResourceAccessControlList.Default, false);
	}

    public GXFile(String fileName, ResourceAccessControlList fileAcl) {
    		this(fileName, fileAcl, false);
    }
    
    public GXFile(String fileName,  ResourceAccessControlList fileAcl, boolean isLocal) {
		if (com.genexus.CommonUtil.isUploadPrefix(fileName)) {
			uploadFileId = fileName;
			fileName = SpecificImplementation.GXutil.getUploadValue(fileName);
		}

    	ExternalProvider storageProvider = Application.getExternalProvider();
        if (storageProvider != null && !isLocal) {
            FileSource = new GXExternalFileInfo(fileName, storageProvider, true, fileAcl);
        } else {
            FileSource = new GXFileInfo(new File(fileName));
        }
    }

    public GXFile(IGXFileInfo fileInfo) {
        FileSource = fileInfo;
    }

    public static String getgxFilename(String fileName) {
        return new GXFile(fileName, ResourceAccessControlList.Default, true).getNameNoExt();
    }

    public static String getgxFileext(String fileName) {
        return new GXFile(fileName).getExt();
    }

    public static String getCompleteFileName(String name, String type) {
        if (name.length() == 0) {
            return "";
        }
        if (type.length() == 0) {
            return name;
        }

        return name + "." + type;
    }

    public void setFileInfo(IGXFileInfo fileInfo) {
        FileSource = fileInfo;
    }

    public void setExternal(boolean isExternal) {
    		this.isExternal = isExternal;
    }	

    public void setSource(String FileName) {
		boolean isUpload = com.genexus.CommonUtil.isUploadPrefix(FileName);
		if (isUpload) {
			uploadFileId = FileName;
			FileName = SpecificImplementation.GXutil.getUploadValue(FileName);
		}

        if (Application.getGXServices().get(GXServices.STORAGE_SERVICE) != null && (isUpload || isExternal)) {
        		FileSource = new GXExternalFileInfo(FileName, Application.getExternalProvider());
        } else {
                String absoluteFileName = FileName;
        		try {
        		    if (ModelContext.getModelContext() != null && ! new File(absoluteFileName).isAbsolute())
                    {
                        IHttpContext webContext = ModelContext.getModelContext().getHttpContext();
                        if((webContext != null) && (webContext instanceof HttpContextWeb) && !FileName.isEmpty()) {
                            absoluteFileName = ModelContext.getModelContext().getHttpContext().getDefaultPath() + File.separator + FileName;
                        }
                    }
        			URI uriFile = URI.create(absoluteFileName);
        			FileSource = new GXFileInfo(new File(uriFile));
        		} catch(Exception e) {
        				FileSource = new GXFileInfo(new File(absoluteFileName));
        		}
        }
    }

    public String getSource() {
        if (FileSource == null) {
            return "";
        }
        return FileSource.getName();
    }

    public void create() {
        resetErrors();
        try {
            if (FileSource.exists() && FileSource.isFile()) {
                ErrCode = 1;
                ErrDescription = "File already exists";
            } else {
                try {
                    ret = FileSource.createNewFile();
                } catch (IOException e) {
                    ErrCode = 100;
                    ErrDescription = e.getMessage();
                } catch (SecurityException e) {
                    ErrCode = 100;
                    ErrDescription = e.getMessage();
                }
            }
        } catch (Exception e) {
            setUnknownError(e);
        }
    }

    public boolean create(InputStream input) {
    	return create(input, false);
    }
    
    public boolean create(InputStream input, boolean overwrite) {
        resetErrors();
        try {
            if (!overwrite && FileSource.exists() && FileSource.isFile()) {
                ErrCode = 1;
                ErrDescription = "File already exists";
            } else {
                try {
                    ret = FileSource.createNewFile(input);
                } catch (IOException e) {
                    ErrCode = 100;
                    ErrDescription = e.getMessage();
                } catch (SecurityException e) {
                    ErrCode = 100;
                    ErrDescription = e.getMessage();
                }
            }
        } catch (Exception e) {
            setUnknownError(e);
        }
        return ErrCode == 0;
    }

    public void delete() {
        if (sourceSeted()) {
            resetErrors();
            try {
                if (!(FileSource.isFile() && FileSource.exists())) {
                    ErrCode = 2;
                    ErrDescription = "The file couldn't be deleted; file does not exist";
                } else {
                    try {
                        ret = FileSource.delete();
                        if (!ret) {
                            setUnknownError();
                        }
                    } catch (SecurityException e) {
                        ErrCode = 100;
                        ErrDescription = e.getMessage();
                    }
                }
            } catch (Exception e) {
                setUnknownError(e);
            }
        }
    }

    public boolean exists() {
        if (sourceSeted()) {
            try {
                resetErrors();
                return FileSource.exists();
            } catch (SecurityException e) {
                ErrCode = 100;
                ErrDescription = e.getMessage();
                return false;
            } catch (Exception e) {
                setUnknownError(e);
                return false;
            }
        } else {
            return false;
        }
    }

    public void setExt(String FileExt) {
        String FileName = getAbsoluteName();
        if (FileExt.trim().length() == 0 || com.genexus.CommonUtil.getFileType(FileName).compareToIgnoreCase(FileExt) == 0) {
            ErrCode = 0;
        } else {
            String sFilePath = (FileSource == null) ? "" : FileSource.getParent();
            rename(sFilePath + File.separator + com.genexus.CommonUtil.getFileName(FileName) + "." + FileExt);
        }
    }

    public void setBlobToDelete() {
        com.genexus.webpanels.BlobsCleaner.getInstance().addBlobFile(getAbsoluteName());
    }

    public void rename(String FileName) {
        if (sourceSeted()) {
            resetErrors();
            try {
                if ((FileSource == null) || !(FileSource.isFile() && FileSource.exists())) {
                    ErrCode = 2;
                    ErrDescription = "The file couldn't be renamed; file does not exist";
                } else if (new GXFile(FileName).exists()) {
                    ErrCode = 3;
                    ErrDescription = "File already exists";
                } else {
                    try {
                        ret = FileSource.renameTo(FileName);
                    } catch (SecurityException e) {
                        ErrCode = 100;
                        ErrDescription = e.getMessage();
                    }
                }
            } catch (Exception e) {
                setUnknownError(e);
            }
        }
    }

    public void copy(String FileName) {
        if (sourceSeted()) {
            resetErrors();
            try {
                if ((FileSource == null) || !(FileSource.isFile() && FileSource.exists())) {
                    ErrCode = 2;
                    ErrDescription = "The file couldn't be copied; file does not exist";
                } else {
                    try {
                        FileSource.copy(FileSource.getFilePath(), FileName);
                    } catch (java.io.IOException e) {
                        setUnknownError(e);
                    }
                }
            } catch (Exception e) {
                resetErrors();
                try {
                    FileSource.copy(FileSource.getName(), FileName);
                } catch (Exception ex) {
                    setUnknownError(e);
                }
            }
        }
    }

    public String getName() {
        if (sourceSeted()) {
            resetErrors();
			if (uploadFileId != null) {
				return SpecificImplementation.GXutil.getUploadNameValue(uploadFileId);
			}
            try {
                if ((FileSource == null) || !(FileSource.isFile() && FileSource.exists())) {
                    ErrCode = 2;
                    ErrDescription = "File does not exist";
                    return "";
                } else {
                    return FileSource.getName();
                }
            } catch (Exception e) {
                setUnknownError(e);
            }
        }
        return "";
    }

    public String getExtension() {
        return getExt();
    }

    public boolean hasExtension() {
        return !getExt().equals("");
    }

    public String getExt() {
    	if (uploadFileId != null) {
			return SpecificImplementation.GXutil.getUploadExtensionValue(uploadFileId);
		}
        String sExtension = FileSource.getName();
        int pos = sExtension.lastIndexOf(".");
        if ((pos == -1) || (pos == sExtension.length())) {
            return "";
        }
        return sExtension.substring(pos + 1, sExtension.length());
    }

    public String getNameNoExt() {
		String FName = FileSource.getName();
    	if (uploadFileId != null) {
			FName = SpecificImplementation.GXutil.getUploadNameValue(uploadFileId);
		}
        int pos = FName.lastIndexOf(".");
        if (pos < 1) {
            return FName;
        }
        return FName.substring(0, pos);
    }

    public String getAbsoluteName() {
        if (sourceSeted()) {
            if (FileSource.getName().toLowerCase().startsWith("http")) {
                return FileSource.getName();
            }
            resetErrors();
            try {
                return FileSource.getFilePath();
            } catch (SecurityException e) {
                ErrCode = 100;
                ErrDescription = e.getMessage();
                return "";
            }
        }
        return "";
    }

    public String getURI() {
        try {
            return FileSource.getAbsolutePath();
        } catch (Exception e) {
            setUnknownError(e);
        }
        return "";
    }

    public String getFilePath() {
        try {
            return FileSource.getFilePath();
        } catch (Exception e) {
            setUnknownError(e);
        }
        return "";
    }

    public String getAbsolutePath() {
        try {
            return FileSource.getAbsolutePath();
        } catch (Exception e) {
            setUnknownError(e);
        }
        return "";
    }

    public long getLength() {
        if (sourceSeted()) {
            resetErrors();
            try {
                if ((FileSource == null) || !(FileSource.isFile() && FileSource.exists())) {
                    ErrCode = 2;
                    ErrDescription = "File does not exist";
                    return 0;
                } else {
                    try {
                        return FileSource.length();
                    } catch (SecurityException e) {
                        ErrCode = 100;
                        ErrDescription = e.getMessage();
                        return 0;
                    }
                }
            } catch (Exception e) {
                setUnknownError(e);
            }
        }
        return 0;
    }

    public Date getLastModified() {
        if (sourceSeted()) {
            resetErrors();
            try {
                if ((FileSource == null) || !(FileSource.isFile() && FileSource.exists())) {
                    ErrCode = 2;
                    ErrDescription = "File does not exist";
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.set(0, 0, 0);
					return calendar.getTime();
                } else {
                    try {
                        return FileSource.lastModified();
                    } catch (SecurityException e) {
                        ErrCode = 100;
                        ErrDescription = e.getMessage();
						GregorianCalendar calendar = new GregorianCalendar();
						calendar.set(0, 0, 0);
						return calendar.getTime();
                    }
                }
            } catch (Exception e) {
                setUnknownError(e);
            }
        }
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.set(0, 0, 0);
		return calendar.getTime();
    }
    
    public InputStream getStream() {
        if (sourceSeted()) {
            resetErrors();
            try {
                if ((FileSource == null || !FileSource.exists())) {
                    ErrCode = 2;
                    ErrDescription = "File does not exist";
                    return null;
                } else {
                    return FileSource.getStream();
                }
            } catch (Exception e) {
                setUnknownError(e);
            }
        }
        return null;
    }

    public String getSeparator() {
        resetErrors();
        if (FileSource != null)
        {
        	return FileSource.getSeparator();
        }
       	else
       	{
       		return File.separator;
       	}
    }

    public int getErrCode() {
        return ErrCode;
    }

    public String getErrDescription() {
        return ErrDescription;
    }

    public String getPath() {
        return FileSource.getPath();
    }

    public String htmlClean() {
    	if (CleanUp != null)
    		return CleanUp.htmlCleanFile(getAbsoluteName());
    	return "";
    }

    public String XSLTApply(String xslt) {
        resetErrors();
        if (sourceSeted()) {
            if (!FileSource.isFile()) {
                ErrDescription = "Source file does not exist";
                ErrCode = 4;
                return "";
            } else if (!new File(xslt).isFile()) {
                ErrDescription = "Stylesheet file does not exist";
                ErrCode = 5;
                return "";
            }
            try {
                return XSLT.XSLTApplyFromFiles(FileSource.getAbsolutePath(), xslt);
            } catch (java.lang.NoClassDefFoundError cnfe) {
                ErrDescription = "Xalan was not found in classpath";
                System.err.println(cnfe.toString());
                ErrCode = 6;
            } catch (Exception e) {
                ErrDescription = e.toString();
                System.err.println(e.toString());
            }
        }
        return "";
    }

    private void resetErrors() {
        ErrCode = 0;
        ErrDescription = "Ok";
    }

    private boolean sourceSeted() {
        if (FileSource == null) {
            ErrCode = 1;
            ErrDescription = "Invalid File instance";
            return false;
        } else {
            return true;
        }
    }

    private void setUnknownError() {
        ErrCode = -1;
        ErrDescription = "Unknown error";
    }

    private void setUnknownError(Exception e) {
        ErrCode = -1;
        ErrDescription = e.getMessage();
		log.error("Unknown error", e);
    }

    public byte[] toBytes() {
        byte[] data = new byte[0];

        if (FileSource != null && FileSource.isFile()) {
            try {
                data = FileSource.toBytes();
            } catch (IOException e) {
                throw new RuntimeException("Error reading " + FileSource.getName() + " : " + e.getMessage());
            }
        }
        return data;
    }

    public String toBase64() {
        return new String(Codecs.base64Encode(toBytes()));
    }

    public Boolean fromBytes(byte[] data) {
        if (FileSource == null) {
			return false;
		}		
		try {
			FileSource.fromBytes(data);
		} catch (IOException e) {
			throw new RuntimeException("Error reading " + FileSource.getName() + " : " + e.getMessage());
		}
        return true;
    }

    public Boolean fromBase64(String base64String) {
		Boolean ok = true;
		try {
			ok = fromBytes(Codecs.base64Decode(base64String.getBytes()));
		}
		catch(Exception e)
		{
			setUnknownError(e);
			ok = false;
		}
		return ok;
    }

    public String readAllText(String encoding) {
        if (sourceSeted()) {
            resetErrors();
            if ((FileSource == null) || !(FileSource.isFile() && FileSource.exists())) {
                ErrCode = 2;
                ErrDescription = "File does not exist";
                return "";
            } else {
                try {
                    if (encoding.equals("")) {
                        return FileSource.readAllText("UTF8");
                    } else {
                        return FileSource.readAllText(CommonUtil.normalizeEncodingName(encoding));
                    }
                } catch (IOException e) {
                    setUnknownError(e);
                }
            }
        }
        return "";
    }

    public Vector<String> readAllLines() {
        return readAllLines("");
    }

    public Vector<String> readAllLines(String encoding) {
        Vector<String> strColl = new Vector<>();
        if (sourceSeted()) {
            resetErrors();
            if ((FileSource == null) || !(FileSource.isFile() && FileSource.exists())) {
                ErrCode = 2;
                ErrDescription = "File does not exist";
                return strColl;
            } else {
                try {
                    java.util.List<String> result;
                    if (encoding.equals("")) {
                        result = FileSource.readLines("UTF8");
                    } else {
                        result = FileSource.readLines(CommonUtil.normalizeEncodingName(encoding));
                    }
                    if (result != null) {
                        for (Iterator j = result.iterator(); j.hasNext();) {
                            strColl.add((String)j.next());
                        }
                    }
                } catch (IOException e) {
                    setUnknownError(e);
                }
            }
        }
        return strColl;
    }

    public void writeAllText(String value, String encoding) {
        writeAllText(value, encoding, false);
    }

    public void writeAllText(String value, String encoding, boolean append) {
        if (sourceSeted()) {
            resetErrors();
            try {
                if (encoding.equals("")) {
                    FileSource.writeStringToFile(value, "UTF8", append);
                } else {
                    FileSource.writeStringToFile(value, CommonUtil.normalizeEncodingName(encoding), append);
                }
            } catch (Exception e) {
                setUnknownError(e);
            }
        }
    }

    public void writeAllLines(Vector value, String encoding) {
        writeAllLines(value, encoding, false);
    }

    public void writeAllLines(Vector value, String encoding, boolean append) {
        if (sourceSeted()) {
            resetErrors();
            try {
                if (encoding.equals("")) {
                    FileSource.writeLines("UTF8", value, append);
                } else {
                    FileSource.writeLines(CommonUtil.normalizeEncodingName(encoding), value, append);
                }
            } catch (Exception e) {
                setUnknownError(e);
            }
        }
    }

    public void appendAllText(String value, String encoding) {
        writeAllText(value, encoding, true);
    }

    public void appendAllLines(Vector value, String encoding) {
        writeAllLines(value, encoding, true);
    }

    public void open(String encoding) {
        openWrite(encoding);
        openRead(encoding);
    }

    private FileWriterWithEncoding fileWriter;

    public void openWrite(String encoding) {
        if (sourceSeted()) {
            resetErrors();
            try {
                if (encoding.equals("")) {
                    fileWriter = new FileWriterWithEncoding(FileSource.getFileInstance(), "UTF8", true);
                } else {
                    fileWriter = new FileWriterWithEncoding(FileSource.getFileInstance(), CommonUtil.normalizeEncodingName(encoding), true);
                }
            } catch (Exception e) {
                setUnknownError(e);
            }
        }
    }

    private LineIterator lineIterator;

    public void openRead(String encoding) {
        if (sourceSeted()) {
            resetErrors();
            try {
                if (encoding.equals("")) {
                    lineIterator = FileUtils.lineIterator(FileSource.getFileInstance(), "UTF8");
                } else {
                    lineIterator = FileUtils.lineIterator(FileSource.getFileInstance(), CommonUtil.normalizeEncodingName(encoding));
                }
            } catch (Exception e) {
                setUnknownError(e);
            }
        }
    }

    public void writeLine(String value) {
        if (fileWriter != null) {
            try {
                fileWriter.append(value + com.genexus.CommonUtil.newLine());
            } catch (Exception e) {
                setUnknownError(e);
            }
        } else {
            ErrCode = 1;
            ErrDescription = "Invalid File instance";
        }
    }

    public String readLine() {
        if (lineIterator != null) {
            try {
                return lineIterator.nextLine();
            } catch (Exception e) {
                setUnknownError(e);
            }
        } else {
            ErrCode = 1;
            ErrDescription = "Invalid File instance";
        }
        return "";
    }

    public boolean getEof() {
        if (lineIterator != null) {
            return !lineIterator.hasNext();
        }
        return true;
    }

    public void close() {
        if (fileWriter != null) {
            try {
                fileWriter.close();
                fileWriter = null;
            } catch (Exception e) {
                setUnknownError(e);
            }
        }
        if (lineIterator != null) {
            lineIterator.close();
            lineIterator = null;
        }
    }
}
