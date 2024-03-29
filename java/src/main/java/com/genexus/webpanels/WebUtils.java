package com.genexus.webpanels;

import java.io.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import com.genexus.*;
import com.genexus.internet.HttpContext;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.xml.XMLReader;

import static com.genexus.util.Encryption.decrypt64;


public class WebUtils
{	
	public static final ILogger logger = LogManager.getLogger(WebUtils.class);
	
	public static final String STATIC_DYNURL  = "genexus.staticweb.dynurl";

	public static String encodeCookie(String parm)
	{
		return PrivateUtilities.URLEncode(parm, "UTF8");
	}

	public static String decodeCookie(String parm)
	{
		return com.genexus.util.Encoder.decodeURL(parm);
	}
	/** 
	* @deprecated use localutil.parseDateParm(String valueString);
	* */
	public static Date parseDateParm(String valueString)
	{
		if	(valueString.trim().length() == 0 || !Character.isDigit(valueString.trim().charAt(0)))
			return CommonUtil.nullDate();
		return CommonUtil.ymdhmsToT_noYL((int) CommonUtil.val(valueString.substring(0, 4)),
									 (int) CommonUtil.val(valueString.substring(4, 6)),
									 (int) CommonUtil.val(valueString.substring(6, 8)),
									 0,
									 0,
									 0);
	}
	/** 
	* @deprecated use localutil.parseDTimeParm(String valueString);
	* */
	public static Date parseDTimeParm(String valueString)
	{
		if	(valueString.trim().length() == 0)
			return CommonUtil.nullDate();

		return CommonUtil.ymdhmsToT_noYL((int) CommonUtil.val(valueString.substring(0, 4)),
									 (int) CommonUtil.val(valueString.substring(4, 6)),
									 (int) CommonUtil.val(valueString.substring(6, 8)),
									 (int) CommonUtil.val(valueString.substring(8, 10)),
									 (int) CommonUtil.val(valueString.substring(10, 12)),
									 (int) CommonUtil.val(valueString.substring(12, 14)));
	}

	public static String getDynURL()
	{
		return PrivateUtilities.addLastChar(getSystemProperty(STATIC_DYNURL), "/");
	}

	public static String getSystemProperty(String property)
	{
		return PrivateUtilities.getSystemProperty(property, "");
	}

	public static String getHTMLColor(long parm)
	{
		return "#" + CommonUtil.padl(Long.toHexString(parm), 6, "0");
	}
	
	public static String htmlDecode(String str)
	{
		if (str == null)
			return "";
		if (str.indexOf("&") < 0)
			return str;
		StringBuffer buffer = new StringBuffer();
		htmlDecode(str, buffer);
		return buffer.toString();
	}
	
	public static String htmlEncode(String str)
	{
		return htmlEncode(str, false);
	}

	public static String htmlEncode(String str, boolean inputValue)
	{
            final StringBuilder buffer = new StringBuilder();
            final StringCharacterIterator iterator = new StringCharacterIterator(str);
            char character =  iterator.current();
            while (character != CharacterIterator.DONE )
            {
                if (character == '<')
                {
                    buffer.append("&lt;");
                }
                else if (character == '>')
                {
                    buffer.append("&gt;");
                }
                else if (character == '\'')
                {
                    htmlEncode(39, buffer);
                }
	            else if (character == '&')
	            {
					buffer.append("&amp;");
	            }
                else if (!inputValue)
                {
	                if (character == '\"')
	                {
	                    buffer.append("&quot;");
	                }
	                else if (character == '\t')
	                {
	                    htmlEncode(9, buffer);
	                }
	                else if (character == '!')
	                {
	                    htmlEncode(33, buffer);
	                }
	                else if (character == '#')
	                {
	                    htmlEncode(35, buffer);
	                }
	                else if (character == '$')
	                {
	                    htmlEncode(36, buffer);
	                }
	                else if (character == '%')
	                {
	                    htmlEncode(37, buffer);
	                }
	                else if (character == '(')
	                {
	                    htmlEncode(40, buffer);
	                }
	                else if (character == ')')
	                {
	                    htmlEncode(41, buffer);
	                }
	                else if (character == '*')
	                {
	                    htmlEncode(42, buffer);
	                }
	                else if (character == '+')
	                {
	                    htmlEncode(43, buffer);
	                }
	                else if (character == ',')
	                {
	                    htmlEncode(44, buffer);
	                }
	                else if (character == '-')
	                {
	                    htmlEncode(45, buffer);
	                }
	                else if (character == '.')
	                {
	                    htmlEncode(46, buffer);
	                }
	                else if (character == '/')
	                {
	                    htmlEncode(47, buffer);
	                }
	                else if (character == ':')
	                {
	                    htmlEncode(58, buffer);
	                }
	                else if (character == ';')
	                {
	                    htmlEncode(59, buffer);
	                }
	                else if (character == '=')
	                {
	                    htmlEncode(61, buffer);
	                }
	                else if (character == '?')
	                {
	                    htmlEncode(63, buffer);
	                }
	                else if (character == '@')
	                {
	                    htmlEncode(64, buffer);
	                }
	                else if (character == '[')
	                {
	                    htmlEncode(91, buffer);
	                }
	                else if (character == '\\')
	                {
	                    htmlEncode(92, buffer);
	                }
	                else if (character == ']')
	                {
	                    htmlEncode(93, buffer);
	                }
	                else if (character == '^')
	                {
	                    htmlEncode(94, buffer);
	                }
	                else if (character == '_')
	                {
	                    htmlEncode(95, buffer);
	                }
	                else if (character == '`')
	                {
	                    htmlEncode(96, buffer);
	                }
	                else if (character == '{')
	                {
	                    htmlEncode(123, buffer);
	                }
	                else if (character == '|')
	                {
	                    htmlEncode(124, buffer);
	                }
	                else if (character == '}')
	                {
	                    htmlEncode(125, buffer);
	                }
	                else if (character == '~')
	                {
	                    htmlEncode(126, buffer);
	                }
	                else
	                {
	                    buffer.append(character);
	                }
	           }
	           else
                {
                    buffer.append(character);
                }
                character = iterator.next();
            }
            return buffer.toString();
        }
        
        private static void htmlEncode(Integer i, StringBuilder buffer)
        {
            String padding = "";
            if( i <= 9 )
            {
                padding = "00";
            }
            else if( i <= 99 )
            {
                padding = "0";
            }
            String number = padding + i.toString();
            buffer.append("&#" + number + ";");
	}

	private static void htmlDecode(String str, StringBuffer buffer)
	{
		if (str != null)
		{
			if (str.indexOf("&") < 0)
				buffer.append(str);
			else
			{
				int length = str.length();
				for (int i = 0; i < length; i++)
				{
					char ch = str.charAt(i);
					if (ch == '&')
					{
						int index = str.indexOf(";", i);
						if (index > 0)
						{
							String entity = str.substring(i+1, index);
							if ((entity.charAt(0) == '#') && (entity.length() > 1))
							{
								try
								{
									if ((entity.charAt(1) == 'x') || (entity.charAt(1) == 'X'))
										ch = (char)Integer.parseInt(entity.substring(2), 16);
									else
										ch = (char)Integer.parseInt(entity.substring(1));
									i = index;
								}
								catch (Throwable e) { i++; }
							}
							else
							{
								i = index;
								char ch2 = '\0';
								if (entity.equals("quot")) ch2 = '\"';
								else if (entity.equals("amp")) ch2 = '&';
								else if (entity.equals("lt")) ch2 = '<';
								else if (entity.equals("gt")) ch2 = '>';
								else if (entity.equals("nbsp")) ch2 = ' ';
								if (ch2 != '\0')
									ch = ch2;
								else
								{
									buffer.append('&');
									buffer.append(entity);
									buffer.append(';');
								}
							}
						}
					}
					buffer.append(ch);
				}
			}
		}
	}

	public static String getEncodedContentDisposition(String value, int browserType)
	{
		int filenameIdx = value.toLowerCase().indexOf("filename");
		int eqIdx = value.toLowerCase().indexOf("=", filenameIdx);

		if(filenameIdx == -1 || eqIdx == -1 || browserType == HttpContextWeb.BROWSER_SAFARI) { //Safari does not support ContentDisposition Header encoded
			return value;
		}
		
		String filename = value.substring(eqIdx + 1).trim();
		value = value.substring(0, filenameIdx) + String.format("filename*=UTF-8''%1$s; filename=\"%1$s\"", PrivateUtilities.URLEncode(filename, "UTF8").replace('+', ' '));

		return value;
	}

        protected static Object[] parmsToObjectArray(com.genexus.ModelContext context, String parms, String url)
        {
			String encKey = parmsEncryptionKey(context);
			parms = decryptParm(parms, encKey);
			// Remove "salt" part from parameter
			if (encKey != null && encKey.length() != 0)
			{
				int questIdx = url.indexOf("?");
				if (questIdx != -1)
				{
					String name = url.substring(0, questIdx);
					if (parms.startsWith(name))
					{
						parms = parms.substring(name.length());
					}
				}
			}

			boolean endsWithSeparator = false;
			boolean useNamedParameters = ModelContext.getModelContext().getPreferences().getProperty("UseNamedParameters", "1").equals("1") && parms.contains("=");
			if ((parms.endsWith(",") && !useNamedParameters) || (parms.endsWith("=") && useNamedParameters)) //Agrego un caracter al final para que el split funcione bien
			{
				parms = parms + "_";
				endsWithSeparator = true;
			}
			Object[] split = useNamedParameters? parms.split("&") : parms.split(",");
			if (endsWithSeparator)
			{
				split[split.length -1] = useNamedParameters? "_= " : "";
			}
			Object[] parmsArray;
			if (parms.endsWith(","))//Empty parameter at the end
			{
				parmsArray = new Object[split.length + 1];
				parmsArray[parmsArray.length-1] = "";
			}
			else
			{
				parmsArray = new Object[split.length];				
			}

			for (int i = 0; i < split.length; i++)
				parmsArray[i] = useNamedParameters? GXutil.URLDecode(((String)split[i]).split("=")[1]) :GXutil.URLDecode((String)split[i]);

            return parmsArray;
        }
		
        public static String decryptParm(Object parm, String encryptionKey) {
            String value = parm.toString();
            try {
                if (!encryptionKey.isEmpty()) {
                    String strValue = value.toString();
                    strValue = com.genexus.util.Encryption.uridecrypt64(strValue,
                            encryptionKey);
                    if ((CommonUtil.strcmp(CommonUtil.right(strValue, 6),
                                       com.genexus.util.
                                       Encryption.checksum(CommonUtil.
                            left(strValue, CommonUtil.len(strValue) - 6), 6)) == 0)) {
                        value = CommonUtil.left(strValue, CommonUtil.len(strValue) - 6);
                    }
                }
                return value;
            } catch (Exception e) {}
            return value;
        }
		
		public static String parmsEncryptionKey(com.genexus.ModelContext context)
		{
            String keySourceType = com.genexus.Application.getClientPreferences().getUSE_ENCRYPTION();
            if (keySourceType.isEmpty()) {
				return "";
			}
            return getEncryptionKey(context, keySourceType);
		}
		
		public static String getEncryptionKey(com.genexus.ModelContext context, String keySourceType) {
			keySourceType = (keySourceType.isEmpty()) ? Application.getClientPreferences().getUSE_ENCRYPTION(): keySourceType;
			String encryptionKey;

			switch (keySourceType.toUpperCase(Locale.ROOT)) {
				case "SESSION":
					encryptionKey = decrypt64(((HttpContext) context.getHttpContext()).getCookie("GX_SESSION_ID"), context.getServerKey());
					break;
				default:
					encryptionKey = context.getSiteKey();
					break;
			}

			if (encryptionKey.isEmpty()) {
				logger.error(String.format("Encryption Key cannot be empty - Key Source: %s", keySourceType));
			}

            return encryptionKey;
		}
		
	private static final String gxApplicationClassesFileName = "GXApplicationClasses.txt";

	private static InputStream getInputStreamFile(Class<?> gxAppClass, String fileName) throws FileNotFoundException {
		InputStream is = gxAppClass.getResourceAsStream(fileName);
		if (is == null){
			is = new FileInputStream(new File(fileName));
		}
		return is;
	}

	public static void getGXApplicationClasses(Class<?> gxAppClass, Set<Class<?>> rrcs) 
	{
		try (InputStream is = getInputStreamFile(gxAppClass, gxApplicationClassesFileName);)
		{
			BufferedReader input = new BufferedReader(new InputStreamReader(is, "UTF8"));
			String restClass = input.readLine();
			while (restClass != null) 
			{
				try 
				{
					rrcs.add(Class.forName(restClass));
				} 
				catch (ClassNotFoundException e) 
				{
					logger.warn("Could not load Class from GXApplicationClasses file " + e.getMessage(), e);					
				}
				restClass = input.readLine();
			}
			input.close();
			is.close();
			rrcs.add(com.genexus.webpanels.GXMultiCall.class);
		}
		catch (Exception e)
		{
			logger.error("Could not load Class from GXApplicationClasses file.", e);
		}		
	}
	
	public static void AddExternalServices(Class<?> gxAppClass, Set<Class<?>> rrcs) 
	{
		String classFilePath = gxAppClass.getProtectionDomain().getCodeSource().getLocation().getPath();
		String[] files = new File(classFilePath.replaceAll("%20", " ") + gxAppClass.getPackage().getName().replace('.', '/')).list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".services");
			}
		});

		if (files != null) {
			for (String fileName : files) {
				WebUtils.AddExternalServicesFile(gxAppClass, rrcs, fileName);;
			}
		}
	}
	
	public static void AddExternalServicesFile(Class<?> gxAppClass, Set<Class<?>> rrcs, String servicesClassesFileName)
	{
		try 
		{
			InputStream is = getInputStreamFile(gxAppClass, servicesClassesFileName);
			if (is != null)
			{
				WebUtils.AddExternalServicesFile(rrcs, null, is);
				
				is.close();
			}
		}
		catch (FileNotFoundException fnfe) 
		{
		}		
		catch (UnsupportedEncodingException e)
		{
			logger.error("Error loading External Services classes ", e);			
		} 
		catch (IOException ioe) 
		{
			logger.error("Error loading External Services classes ", ioe);
		}
	}

	public static void AddExternalServicesFile(Set<Class<?>> rrcs, ArrayList<String> restImports, InputStream is) {
		String xmlstring = PrivateUtilities.BOMInputStreamToStringUTF8(is);

		XMLReader reader = new XMLReader();
		reader.openFromString(xmlstring);
		if (reader.getErrCode() == 0)
		{
			while (reader.readType(1, "Service") > 0)
			{
				Class serviceClass = processRestService(reader);
				if (serviceClass != null)
					if (rrcs != null)
						rrcs.add(serviceClass);
					else
						restImports.add(serviceClass.getName());
			}
			reader.close();
		}
	}
	
	public static Class processRestService(XMLReader reader) {
		String className = reader.getAttributeByName("fullName");
		try {
			return Class.forName(className + "_services_rest");
		} catch (ClassNotFoundException e) {
			logger.warn("Error loading rest service: '" + className + "_services_rest'", e);			
			return null;
		}
	}


}
