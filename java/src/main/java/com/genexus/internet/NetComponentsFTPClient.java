package com.genexus.internet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class NetComponentsFTPClient implements IFTPClient
{
	private FTPClient ftp;
	private static final int OK   = 1;
	private static final int FAIL = 0;
	private int lastError;
	private boolean passive = false;

	public NetComponentsFTPClient()
	{
		ftp = new FTPClient();
	}

	private int onFailure()
	{
      	if (ftp.isConnected())
      	{
			disconnect();
		}

		lastError = FAIL;
		return FAIL;
	}

	public int connect(String host, String user, String password)
	{
		if	(ftp.isConnected())
		{
			return FAIL;
		}

		try
		{
	      	ftp.connect(host);
	      	int reply = ftp.getReplyCode();

	      	if(FTPReply.isPositiveCompletion(reply))
	      	{
				if	(user.trim().length() == 0)
				{
					user = "anonymous";
					if	(password.trim().length() == 0)
						password = "anonymous@";
				}

	      		if (ftp.login(user, password))
	      		{
					if (passive)
					{
						ftp.enterLocalPassiveMode();
					}
					lastError = OK;
					return OK;
				}
			}
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
		}

		return onFailure();
	}

	public int command(String cmd)
	{
		try
		{
			ftp.sendSiteCommand(cmd);
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
			return onFailure();
		}

		return OK;
	}

	public int disconnect()
	{
		lastError = FAIL;

		if	(ftp.isConnected())
		{
			try
			{
				ftp.disconnect();
				lastError = FTPReply.isPositiveCompletion(ftp.getReplyCode())?OK:FAIL;
			}
			catch (IOException e)
			{
				System.err.println(e.getMessage());
			}
		}

	    return lastError;
	}

	public String status()
	{
		if	(ftp.isConnected())
		{
	        return ftp.getReplyString();
		}

		return "";
	}

	public void status(String[] status)
	{
		status[0] = status();
	}

	private void setFileType(String mode) throws IOException
	{
		if (mode.charAt(0) == 'a' || mode.charAt(0) == 'A')
			ftp.setFileType(ftp.ASCII_FILE_TYPE);
		else
			ftp.setFileType(ftp.BINARY_FILE_TYPE);
	}

	public int get(String source, String target, String mode)
	{
		lastError = FAIL;

		if	(ftp.isConnected())
		{
			target = normalizeName(source, target, '/', File.separatorChar);
			try (FileOutputStream targetOutputStream = new FileOutputStream(target))
			{
				setFileType(mode);

				OutputStream o = new BufferedOutputStream(targetOutputStream);
				if	(ftp.retrieveFile(source, o))
				{
					o.close();
					lastError = OK;
				}

			}
			catch (IOException e)
			{
				System.err.println(e.getMessage());
			}
		}

	    return lastError;
	}

	public int delete(String source)
	{
		lastError = FAIL;

		if	(ftp.isConnected())
		{
			try
			{
				if	(ftp.deleteFile(source))
				{
					lastError = OK;
				}
			}
			catch (IOException e)
			{
				System.err.println(e.getMessage());
			}
		}

	    return lastError;
	}


	public int put(String source, String target, String mode)
	{
		lastError = FAIL;

		if	(ftp.isConnected())
		{
			target = normalizeName(source, target, File.separatorChar, '/');
			try (FileInputStream fileInputStream = new FileInputStream(source))
			{
				setFileType(mode);
				InputStream file = new BufferedInputStream(fileInputStream);

				if	(ftp.storeFile(target, file))
				{
					lastError = OK;
				}
                                file.close();
			}
			catch (IOException e)
			{
				System.err.println(e.getMessage());
			}
		}

	    return lastError;
	}

	private String getFileName(String path, char sep)
	{
		return path.substring( path.lastIndexOf(sep) + 1);
	}

	private String normalizeName(String oriName, String normName, char oriSep, char destSep)
	{
		// get("/dir/get1.gif", "") 	 		 ' graba en .\get1.gif
		// put("c:\get1.gif", "") 				 ' graba en current/get1.gif
		if	(normName.trim().length() == 0)
		{
			return getFileName(oriName, oriSep);
		}

		// get("/dir/get1.gif", "c:\")   		 ' graba en c:\get1.gif
		// put("c:\get1.gif", "/dir/") 	 		 ' graba en /dir/get1.gif
		if	(getFileName(normName, destSep).trim().length() == 0)
		{
			return normName.trim() + getFileName(oriName, oriSep);
		}

		// get("/dir/get1.gif", "c:\pepe.gif")   ' graba en c:\pepe.gif
		// put("c:\get1.gif", "/dir/dos.gif") 	 ' graba en /dir/dos.gif
		return normName;
	}

	public int mkdir(String path)
	{
		lastError = FAIL;

		if	(ftp.isConnected())
		{
			try
			{
				lastError = ftp.makeDirectory(path)?OK:FAIL;
			}
			catch (IOException e)
			{
				System.err.println(e.getMessage());
			}
		}

		return lastError;
	}

	public void lastError(int[] lastError)
	{
		lastError[0] = this.lastError;
	}
	
	public void setPassive(boolean passive)
	{
		this.passive = passive;
	}	
}
