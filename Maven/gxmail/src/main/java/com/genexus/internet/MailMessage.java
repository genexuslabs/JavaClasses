// $Log: MailMessage.java,v $
// Revision 1.5  2006/11/30 15:14:31  alevin
// - Cambios para desencodear bien los mensajes japoneses de acuerdo al RFC1468.
//   Se agrega la clase JapaneseMimeDecoder.
//
// Revision 1.4  2006/07/07 20:27:30  alevin
// - Agrego getReceivedDate.
//
// Revision 1.3  2006/07/05 13:48:47  alevin
// - Arreglo en el readBody, el texto html se estaba leyendo como adjunto del mensaje.
//   Ahora no se lee como adjunto y se guarda en un string buffer. Agrego el getHtmlText.
//
// Revision 1.2  2006/07/04 22:07:25  alevin
// - En el readeHeader() salgo del loop cuando temp es null.
//   En el constructor llamo al readUnreadedBytes luego del readMessage(), en algunos casos
//   hay mails que tienen footers luego del ultimo terminator.
//
// Revision 1.1  2001/12/14 17:49:58  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/12/14 17:49:58  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.genexus.CommonUtil;
import com.genexus.common.interfaces.SpecificImplementation;

class MailMessage
{
	private MailProperties keys = new MailProperties ();
	private StringBuffer message = new StringBuffer();
        private StringBuffer messageHtml = new StringBuffer();

	private String text;
	private String attachmentsPath;
	private String attachments = "";
	private boolean downloadAttachments;
	
	MailMessage(MailReader reader, String attachmentsPath, boolean downloadAttachs)
	{
		this.downloadAttachments = downloadAttachs;
		this.attachmentsPath = attachmentsPath;
		try
		{
			keys = readHeader(reader);
			readMessage(reader, keys, null);
                        if(reader instanceof RFC822Reader)
                        {
                          ((RFC822Reader)reader).readUnreadedBytes();
                        }
		}
		catch (IOException e)
		{
			System.err.println("reading msg " + e);
			e.printStackTrace();
		}
	}



	
	private void readMessage(MailReader reader, MailProperties partProps, String separator) throws IOException
	{
		if	(partProps.getMimeMediaType().equals(GXInternetConstants.TYPE_MULTIPART)) 
		{
			readMultipartBody(reader, partProps);
		}
		else
		{
			readBody(reader, partProps, separator);
		}
	}

	private MailProperties readHeader(MailReader reader) throws IOException
	{
		String temp;
		String reply = reader.readLine();
		MailProperties properties = null;

		while (reply != null && !reply.equals(""))
		{
			if	(properties == null)
				 properties = new MailProperties();

			temp = reader.readLine();

                        if(temp == null)
                        {
                          break;
                        }

			// MultiLine headers have a space\tab at begining of each extra line
			while ((temp.startsWith(" ")) || (temp.startsWith("\t"))) 
			{
				reply = reply + GXInternetConstants.CRLFString + temp;
				temp  = reader.readLine();	
			} 

			properties.putKey(reply);
			reply = temp;
		}

		return properties;
	}

	private static String getSeparator(MailProperties properties)
 	{
		String separator = properties.getKeyProperty(GXInternetConstants.CONTENT_TYPE, GXInternetConstants.BOUNDARY);

		if	(separator.length() == 0)
			return null;

		if	(separator.charAt(0) == '"')
			separator = separator.substring(1, separator.length() - 1);

		return "--" + separator;
	}

	private void readMultipartBody(MailReader reader, MailProperties properties) throws IOException
	{
		String separator = getSeparator(properties);

		reader.setSeparator(null);

		// Busco el startup
		String reply = reader.readLine();

		if	(reply != null)
		{
			while (reply != null && !reply.startsWith(separator)) 
			{
				reply = reader.readLine();
			}
		}

		reader.setSeparator(separator);

		while (true)
		{
			MailProperties props = readHeader(reader);

			if (props == null)
				break;

			// Leo el cabezal de la parte MIME, armando propiedades nuevas.
			readMessage(reader, props, separator);
		}
	}

	private String getFileName(String path, String name, String subtype)  throws IOException
	{
		if	(name.length() == 0)
		{
			name = SpecificImplementation.GXutil.getTempFileName(subtype);			
		}
		else
		{
			name = new QuotedPrintableDecoder().decodeHeader(name);
			name = CommonUtil.removeAllQuotes(name);
			
			String extension = "";
			if	(name.lastIndexOf('.') > 0)
			{
				extension = name.substring(name.lastIndexOf('.'), name.length());
				name      = name.substring(0, name.lastIndexOf('.'));
			}

			int idx = 1;
			String nameOri = "" + name;
			while (new File(path + name + extension).exists())
			{
				name = nameOri + " (" + idx + ")";
				idx = idx + 1;
			}

			return name + extension;
		}

		return name;
	}


	private void readBody(MailReader reader, MailProperties partProps, String separator) throws IOException
	{
                boolean isTextPlain = partProps.getKeyPrincipal(GXInternetConstants.CONTENT_TYPE).equalsIgnoreCase("text/plain");
                boolean isTextHtml = partProps.getKeyPrincipal(GXInternetConstants.CONTENT_TYPE).equalsIgnoreCase("text/html");
		boolean isAttachment = (!isTextPlain && !isTextHtml) ||
							   partProps.getKeyPrincipal(GXInternetConstants.CONTENT_DISPOSITION).equalsIgnoreCase("attachment") ;
		
		String oldSeparator = reader.getSeparator();
		reader.setSeparator(separator);
		OutputStream out;

		if	(isAttachment)
		{			
			if (this.downloadAttachments){
				String name     = partProps.getKeyProperty(GXInternetConstants.CONTENT_TYPE, GXInternetConstants.NAME);
				String fileName = partProps.getKeyProperty(GXInternetConstants.CONTENT_DISPOSITION, GXInternetConstants.FILENAME);
				String outname = getFileName(attachmentsPath, fileName.length() == 0?name:fileName, partProps.getMimeMediaSubtype());
	
				attachments += outname + ";";
				out = new FileOutputStream(attachmentsPath + outname);
			}
			else				
			{
				out = new DummyOutputStream();
			}
			
		}
		else
		{
			out = new ByteArrayOutputStream();
		}

		getDecoder(partProps.getField(GXInternetConstants.CONTENT_TRANSFER_ENCODING)).decode(reader, out);

		if	(!isAttachment)
                {
                	String charset = getCharset(partProps.getField(GXInternetConstants.CONTENT_TYPE));
                  if(isTextPlain)
                  	try
                  	{
                    	message.append( ( (ByteArrayOutputStream) out).toString(charset));
                    }
                    catch(UnsupportedEncodingException e)
                    {
                    	message.append( ( (ByteArrayOutputStream) out).toString());
                    }
                  else if(isTextHtml)
                  	try
                  	{
                    	messageHtml.append( ( (ByteArrayOutputStream) out).toString(charset));
                    }
                    catch(UnsupportedEncodingException e)
                    {
                    	messageHtml.append( ( (ByteArrayOutputStream) out).toString());
                    }                 
                }
		out.close();

		reader.setSeparator(oldSeparator);
	}
	
	private String getCharset(String contentType)
	{
		return contentType.substring(contentType.indexOf("charset=")+8);
	}

	private MimeDecoder getDecoder(String encoding)
	{
		if	(encoding.equalsIgnoreCase(GXInternetConstants.BASE64))
		{
			return new Base64Decoder();
		}
		else if	(encoding.equalsIgnoreCase(GXInternetConstants.QUOTED_PRINTABLE))
		{
			return new QuotedPrintableDecoder();
		}

		return new DummyDecoder();
	}

	String getText()
	{
		return JapaneseMimeDecoder.decode(message.toString());
	}

        String getHtmlText()
        {
                return JapaneseMimeDecoder.decode(messageHtml.toString());
        }

	String getAttachments()
	{
		if	(attachments.length() == 0)
			return "";

		return attachments.substring(0, attachments.length() - 1);
	}

        String getReceivedDate()
        {
          String received = getField(GXInternetConstants.RECEIVED);

          int pos = received.lastIndexOf(";");
          if(pos != -1)
          {
            return received.substring(pos+1).trim();
          }

          return "";
        }

	String getField(String field)
	{
		return keys.getField(field);
	}
	
	public MailProperties getKeys() {
		return keys;
	}

}
