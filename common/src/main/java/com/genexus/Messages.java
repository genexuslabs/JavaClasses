// $Log: Messages.java,v $
// Revision 1.15  2006/03/22 20:49:54  gusbro
// - Arreglo para que los mensajes queden bien al ejecutar desde el commandline el wizard
//
// Revision 1.14  2006/03/20 18:22:14  iroqueta
// Los archivos de traduccion ya no se generan con espacios entre el igual la clave y el valor.
// Cambio la lectura de los mismos para que tenga en cuenta esto.
//
// Revision 1.13  2006/03/20 17:25:12  iroqueta
// Los archivos de traduccion se vuleven a generar siempre en UTF-8 con lo cual Yo no tengo que hacer mas la diferencia para los idiomas chino y japones.
//
// Revision 1.12  2005/11/23 20:41:17  iroqueta
// Hago que los mensajes en japones y en Chino no se lean con UTF8 sino con su codepage correspondiente porque asi se estan generando ya que si se generan como UTF8 no sabemos porque no esta funcionando.
//
// Revision 1.11  2005/10/18 13:03:38  iroqueta
// Leo el archivo de mensajes con UTF-8 ya que se paso a generar de esa forma
//
// Revision 1.10  2005/03/09 15:57:50  iroqueta
// Arreglo para que encuentre los archivos de resources si se corre desde el DeveloperMenu en win
//
// Revision 1.9  2005/03/07 18:52:55  iroqueta
// Al crear el ModelContext seteo en la Application la className para luego poder leerla en la clase Message para saber de donde leer el recurso del lenguaje
//
// Revision 1.8  2005/03/07 16:43:38  iroqueta
// Se pasa a leer el archivo de recursos con el codePage default de la maquina.
//
// Revision 1.7  2005/03/07 14:28:50  iroqueta
// Cambio para que se pueda leer el txt de los mensajes como recurso (ESTO NO ESTABA FUNCIONANDO EN 3 CAPAS, ahora si deberia)
//
// Revision 1.6  2005/03/03 22:23:47  iroqueta
// Cambio para que se pueda leer el txt de los mensajes como recurso (ESTO NO ESTABA FUNCIONANDO EN WEB, ahora si deberia)
//
// Revision 1.5  2005/03/03 18:52:03  iroqueta
// Cambio para que se pueda leer el txt de los mensajes como recurso
//
// Revision 1.4  2005/03/02 14:55:17  iroqueta
// Cambio en la implementacion para que se puedan ver bien los caracteres con tildes.
//
// Revision 1.3  2005/02/26 17:54:16  iroqueta
// Implementacion de traduccion en runtime
//
// Revision 1.2  2004/04/20 19:24:21  gusbro
// - Arreglo mensajes de reorg para que aparezcan indentados
//
// Revision 1.1.1.1  2000/12/04 19:25:36  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2000/12/04 19:25:36  gusbro
// GeneXus Java Olimar
//
//
//    Rev 1.2   23 Sep 1998 19:48:04   AAGUIAR
//
//    Rev 1.1   10 Aug 1998 16:38:54   AAGUIAR
// 	-	Se despliega can't find key si no encuentra la clave.
//
//    Rev 1.0   23 Jul 1998 19:06:28   AAGUIAR
// Initial revision.

package com.genexus;

import java.text.*;
import java.util.*;

import com.genexus.common.interfaces.SpecificImplementation;

import java.io.*;


public class Messages
{
	Properties messages;
	MessageFormat  formatter;
	static Hashtable messageList = new Hashtable();

	private Messages(String resourceName, Locale formatterLocale)
	{
		messages = new Properties();
		load(resourceName);

		formatter = new MessageFormat("");
		formatter.setLocale(formatterLocale);
	}

	public static Messages getMessages(String resourceName, Locale formatterLocale)
	{
		Messages msg = (Messages) (messageList.get(resourceName));
		if	(msg == null)
		{
			msg = new Messages(resourceName, formatterLocale);
			messageList.put(resourceName, msg);
		}

		return msg;
	}

	public static void endMessages()
	{
		messageList = null;
	}

	public String getMessage(String key1)
	{
		boolean trimSpaces = key1 != null && key1.length() > 0 && (key1.charAt(0) == ' ' || key1.charAt(key1.length() - 1) == ' ');
		String key = trimSpaces ? key1.trim() : key1;
		String value = messages.getProperty(key);
		return (value != null)? (trimSpaces ? copySpaces(key1, value) : value) :key1;
	}

	public String getMessage(String key1, Object[] arguments)
	{
		boolean trimSpaces = key1 != null && key1.length() > 0 && (key1.charAt(0) == ' ' || key1.charAt(key1.length() - 1) == ' ');
		String key = trimSpaces ? key1.trim() : key1;
		String strFormat = messages.getProperty(key);
        if (strFormat == null)
        {
            strFormat = key1;
        }
		else
		{
			synchronized(this)
			{
				formatter.applyPattern(strFormat);
				strFormat = formatter.format(arguments);
			}
            if (trimSpaces)
                strFormat = copySpaces(key1, strFormat);
		}

		return strFormat;
	}

		private String copySpaces(String strId1, String strFormat)
		{
			StringBuffer str = new StringBuffer();
			for (int i = 0; i < strId1.length(); i++)
			{
				if (strId1.charAt(i) == ' ')
				{
					str.append(' ');
				}
				else
				{
					str.append(strFormat);
					break;
				}
			}
			for (int i = strId1.length()-1; i >=0; i--)
			{
				if (strId1.charAt(i) == ' ')
				{
					str.append(' ');
				}
				else
				{
					break;
				}
			}
			return str.toString();
		}

	protected static final String getTab()
	{
		return "                ";
	}

	private void load(String resourceName)
	{
		String line;
		InputStream is = null;
		try
		{
			is = SpecificImplementation.Messages.getInputStream(resourceName);

            if (is != null)
            {
              BufferedReader bufread = new BufferedReader(new InputStreamReader(is, "UTF8"));
              line = bufread.readLine();
              while (line != null)
              {
                parseLine(line);
                line = bufread.readLine();
              }
              bufread.close();
            }
          }
          catch(UnsupportedEncodingException e)
          {
            System.err.println(e.toString());
          }
          catch(FileNotFoundException e)
          {
            System.err.println(e.toString());
          }
          catch (IOException e)
          {
            System.err.println(e.toString());
          }
        }

        private void parseLine(String line)
        {
    		int len = line.length();
    		boolean escaped = false;
    		StringBuffer builder = new StringBuffer();
    		String code = null;
    		boolean hasEqual = false;

    		for( int i = 0; i < len; i++)
    		{
      			char ch = line.charAt(i);

				if (ch != 61 && ch!= 92) //caracter que no es signo de igual ni barra
      				builder.append(ch);
      			else if (ch==61 && escaped) //signo de igual escapeado
      				builder.append(ch);
      			else if (ch==92 && escaped) //barra escapeada
      				builder.append(ch);

      			if (ch == 61 && !escaped) //signo de igual no escapeado: separador de codigo=descripcion
      			{
      				code = builder.toString(); //guarda codigo y resetea builder
      				hasEqual = true;
      				builder = new StringBuffer();
      			}

      			if (escaped)
      			{
      				escaped=false;
      			}
      			else if (ch == 92)
      			{
      				escaped = true;
      			}
    		}
    		if (len>0 && code!=null && hasEqual && builder.length()>0)
    		{
    			messages.put(code, builder.toString());
    		}
        }
}
