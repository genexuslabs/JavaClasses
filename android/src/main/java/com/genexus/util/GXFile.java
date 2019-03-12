package com.genexus.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.output.FileWriterWithEncoding;

import com.artech.base.services.AndroidContext;
import com.genexus.CommonUtil;
import com.genexus.common.classes.AbstractGXFile;

public class GXFile extends AbstractGXFile {
	private File FileSource;
	private int ErrCode;
	private String ErrDescription;
	private boolean ret;

	public GXFile() {
	}

	public static String getgxFilename(String fileName) {
		return new GXFile(fileName).getNameNoExt();
	}

	public static String getgxFileext(String fileName) {
		return new GXFile(fileName).getExt();
	}

	public static String getCompleteFileName(String name, String type) {
		if (name.length() == 0)
			return "";
		if (type.length() == 0)
			return name;

		return name + "." + type;
	}

	public GXFile(String FileName) {
		this(FileName, false, false);
	}

	public GXFile(String FileName, boolean isPrivate, boolean isLocal) {
		FileName = convertToLocalFullPath(FileName);
		FileSource = new File(FileName);
	}

	public void setSource(String FileName) {
		FileName = convertToLocalFullPath(FileName);
		FileSource = new File(FileName);
	}

	public static String convertToLocalFullPath(String FileName) {
		if (FileName != null && FileName.length() > 0) {
			final String FILE_SCHEME = "file://";
			final String blobDBFilePrefix = "gxblobdata://";
			if (FileName.startsWith(FILE_SCHEME)) {
				FileName = FileName.substring(FILE_SCHEME.length());
			} else if (FileName.startsWith(blobDBFilePrefix)) {
				// relative path in android file system.
				String blobBasePath = com.genexus.Preferences.getDefaultPreferences().getBLOB_PATH();

				// get File Name and add full Path.
				String gxdbFileUriFileName = FileName.substring(blobDBFilePrefix.length());

				// Local path in sdcard.
				FileName = blobBasePath + File.separator + gxdbFileUriFileName;
			} else if (!FileName.contains(File.separator)) // check if its a relative path.
			{
				// is a relative path, add app root
				FileName = AndroidContext.ApplicationContext.getApplicationDataPath() + File.separator + FileName;
			}
		}
		return FileName;
	}

	public String getSource() {
		return FileSource.getPath();
	}

	public void create() {
		resetErrors();
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
	}

	public void delete() {
		if (sourceSeted()) {
			resetErrors();
			if (!(FileSource.isFile() && FileSource.exists())) {
				ErrCode = 2;
				ErrDescription = "The file couldn't be deleted; file does not exist";
			} else {
				try {
					ret = FileSource.delete();
					if (!ret)
						setUnknownError();
				} catch (SecurityException e) {
					ErrCode = 100;
					ErrDescription = e.getMessage();
				}
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
			}
		} else {
			return false;
		}
	}

	public void setExt(String FileExt) {
		String FileName = getAbsoluteName();
		if (FileExt.trim().length() == 0
				|| com.genexus.CommonUtil.getFileType(FileName).compareToIgnoreCase(FileExt) == 0)
			ErrCode = 0;
		else {
			String sFilePath = (FileSource == null) ? "" : FileSource.getParent();
			rename(sFilePath + FileSource.separator + com.genexus.CommonUtil.getFileName(FileName) + "." + FileExt);
		}
	}

	public void setBlobToDelete() {
	}

	public void rename(String FileName) {
		if (sourceSeted()) {
			resetErrors();
			if ((FileSource == null) || !(FileSource.isFile() && FileSource.exists())) {
				ErrCode = 2;
				ErrDescription = "The file couldn't be renamed; file does not exist";
			} else if (new File(FileName).exists()) {
				ErrCode = 3;
				ErrDescription = "File already exists";
			} else {
				try {
					ret = FileSource.renameTo(new File(FileName));
					if (ret)
						FileSource = new File(FileName);
					else
						setUnknownError();
				} catch (SecurityException e) {
					ErrCode = 100;
					ErrDescription = e.getMessage();
				}
			}
		}
	}

	public void copy(String FileName) {
		if (sourceSeted()) {
			resetErrors();
			if ((FileSource == null) || !(FileSource.isFile() && FileSource.exists())) {
				ErrCode = 2;
				ErrDescription = "The file couldn't be copied; file does not exist";
			} else {
				try {
					com.genexus.PrivateUtilities.copyFile(new java.io.File(FileSource.getPath()),
							new java.io.File(FileName));
				} catch (java.io.IOException e) {
					setUnknownError();
					// ErrCode = -1;
					// ErrDescription = "Error copying file " + e.getMessage();
				}
			}
		}
	}

	public String getName() {
		if (sourceSeted()) {
			resetErrors();
			if ((FileSource == null) || !(FileSource.isFile() && FileSource.exists())) {
				ErrCode = 2;
				ErrDescription = "File does not exist";
				return "";
			} else
				return FileSource.getName();
		} else
			return "";
	}

	public boolean hasExtension() {
		return !getExt().equals("");
	}

	public String getExt() {
		String sExtension = FileSource.getName();
		int pos = sExtension.lastIndexOf(".");
		if ((pos == -1) || (pos == sExtension.length()))
			return "";
		return sExtension.substring(pos + 1, sExtension.length());
	}

	public String getNameNoExt() {
		String FName = FileSource.getName();
		int pos = FName.lastIndexOf(".");
		if (pos < 1)
			return FName;
		return FName.substring(0, pos);
	}

	public String getAbsoluteName() {
		if (sourceSeted()) {
			resetErrors();
			if ((FileSource == null) || !(FileSource.isFile() && FileSource.exists())) {
				ErrCode = 2;
				ErrDescription = "File does not exist";
				return "";
			} else {
				try {
					return FileSource.getAbsolutePath();
				} catch (SecurityException e) {
					ErrCode = 100;
					ErrDescription = e.getMessage();
					return "";
				}
			}
		} else
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
		} else
			return 0;
	}

	public Date getLastModified() {
		if (sourceSeted()) {
			resetErrors();
			if ((FileSource == null) || !(FileSource.isFile() && FileSource.exists())) {
				ErrCode = 2;
				ErrDescription = "File does not exist";
				return new Date(0, 0, 0);
			} else {
				try {
					return new Date(FileSource.lastModified());
				} catch (SecurityException e) {
					ErrCode = 100;
					ErrDescription = e.getMessage();
					return new Date(0, 0, 0);
				}
			}
		} else
			return new Date(0, 0, 0);
	}

	public String getSeparator() {
		resetErrors();
		return File.separator;
	}

	public int getErrCode() {
		return ErrCode;
	}

	public String getErrDescription() {
		return ErrDescription;
	}

	public String getPath() {
		String absoluteName = getAbsoluteName();
		if (!absoluteName.equals(""))
			return new File(absoluteName).getParent();
		else
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
				return XSLT.XSLTApplyFromFiles(FileSource.getPath(), xslt);
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
		ErrDescription = "";
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
	}

	public byte[] toBytes() {
		byte[] data = new byte[0];

		if (FileSource != null && FileSource.isFile()) {
			try {
				data = CommonUtil.readToByteArray(new BufferedInputStream(new FileInputStream(FileSource)));
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
		if (FileSource != null) {
			try {
				OutputStream destination = new BufferedOutputStream(new FileOutputStream(FileSource));
				destination.write(data, 0, data.length);
				destination.close();
				return true;
			} catch (IOException e) {
				throw new RuntimeException("Error reading " + FileSource.getName() + " : " + e.getMessage());
			}
		}
		return false;
	}

	public Boolean fromBase64(String base64String) {
		Boolean ok = false;
		try {
			ok = fromBytes(Codecs.base64Decode(base64String.getBytes()));
		} catch (Exception e) {
			setUnknownError();
			e.printStackTrace();
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
					if (encoding.equals(""))
						return FileUtils.readFileToString(FileSource, "UTF8");
					else
						return FileUtils.readFileToString(FileSource, CommonUtil.normalizeEncodingName(encoding));
				} catch (IOException e) {
					setUnknownError();
					e.printStackTrace();
				}
			}
		}
		return "";
	}

	public Vector readAllLines() {
		return readAllLines("");
	}

	public Vector readAllLines(String encoding) {
		Vector strColl = new Vector();
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
						result = FileUtils.readLines(FileSource, "UTF8");
					} else {
						result = FileUtils.readLines(FileSource, CommonUtil.normalizeEncodingName(encoding));
					}
					for (Iterator j = result.iterator(); j.hasNext();) {
						strColl.add((String) j.next());
					}
				} catch (IOException e) {
					setUnknownError();
					e.printStackTrace();
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
			try {
				if (encoding.equals("")) {
					FileUtils.writeStringToFile(FileSource, value, "UTF8", append);
				} else {
					FileUtils.writeStringToFile(FileSource, value, CommonUtil.normalizeEncodingName(encoding), append);
				}
			} catch (Exception e) {
				setUnknownError();
				e.printStackTrace();
			}
		}
	}

	public void writeAllLines(Vector value, String encoding) {
		writeAllLines(value, encoding, false);
	}

	public void writeAllLines(Vector value, String encoding, boolean append) {
		if (sourceSeted()) {
			try {
				if (encoding.equals("")) {
					FileUtils.writeLines(FileSource, "UTF8", value, append);
				} else {
					FileUtils.writeLines(FileSource, CommonUtil.normalizeEncodingName(encoding), value, append);
				}
			} catch (Exception e) {
				setUnknownError();
				e.printStackTrace();
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
			try {
				if (encoding.equals("")) {
					fileWriter = new FileWriterWithEncoding(FileSource, "UTF8", true);
				} else {
					fileWriter = new FileWriterWithEncoding(FileSource, CommonUtil.normalizeEncodingName(encoding),
							true);
				}
			} catch (Exception e) {
				setUnknownError();
				e.printStackTrace();
			}
		}
	}

	private LineIterator lineIterator;

	public void openRead(String encoding) {
		if (sourceSeted()) {
			try {
				if (encoding.equals("")) {
					lineIterator = FileUtils.lineIterator(FileSource, "UTF8");
				} else {
					lineIterator = FileUtils.lineIterator(FileSource, CommonUtil.normalizeEncodingName(encoding));
				}
			} catch (Exception e) {
				setUnknownError();
				e.printStackTrace();
			}
		}
	}

	public void writeLine(String value) {
		if (fileWriter != null) {
			try {
				fileWriter.append(value + com.genexus.CommonUtil.newLine());
			} catch (Exception e) {
				setUnknownError();
				e.printStackTrace();
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
				setUnknownError();
				e.printStackTrace();
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
				setUnknownError();
				e.printStackTrace();
			}
		}
		if (lineIterator != null) {
			lineIterator.close();
			lineIterator = null;
		}
	}
}
