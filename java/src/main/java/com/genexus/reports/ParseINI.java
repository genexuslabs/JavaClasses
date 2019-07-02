package com.genexus.reports;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/** ParseINI, clase para el manejo de INIs
 * @version 1.0.1
 */
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public class ParseINI
{
	private static final int MAX_LINE_LENGTH=255; // Maximo largo de una property
	private static final byte SECTION_SEPARATOR_CHAR=(byte)'&'; // Separador interno de secciones
	private static final String SECTION_SEPARATOR="&";
	private static final String GENERAL="&General&"; // Seccion General

	private String entryName;
	private InputStreamReader inputStream;
	private Hashtable sections=new Hashtable();
	private Hashtable sectionEntries;
	private Hashtable general;
	private Hashtable aliased=new Hashtable(); // Contiene las dulpas (original -> alias)
	private Hashtable alias=new Hashtable();   // Contiene las dulpas (alias -> original)
	private boolean need2Save = false, autoSave = true;
	private String filename = null;

	int []tempBytes=new int[16384];

	public ParseINI()
	{
		general=new Hashtable();
		filename = null;
	}

	public ParseINI(String filename) throws IOException
	{
		this.filename = new File(filename).getAbsolutePath();
		try
		{
			FileInputStream inputStream = new FileInputStream(filename);
			load(inputStream);
			inputStream.close();
		}
		catch(FileNotFoundException fnfe)
		{ // Si debo crear el archivo
			(new FileWriter(filename)).close(); // Creo el archivo
			general=new Hashtable();
		}
	}
	public ParseINI(String filename, String configurationTemplateFile)  throws IOException
	{
		try
		{
			File file = new File(filename);
			String inputFileNameOrTemplate = null;
			if (file.exists()){
				inputFileNameOrTemplate = filename;
			}else{
				(new FileWriter(filename)).close(); // Creo el archivo
				if (new File(configurationTemplateFile).exists()) {
					inputFileNameOrTemplate = configurationTemplateFile;
				}
			}
			if (inputFileNameOrTemplate!=null) {
				FileInputStream inputStream = new FileInputStream(inputFileNameOrTemplate);
				load(inputStream);
				inputStream.close();
			}else{
				general=new Hashtable();
			}
			this.filename = file.getAbsolutePath();
		}
		catch(FileNotFoundException fnfe)
		{ // Si debo crear el archivo
			(new FileWriter(filename)).close(); // Creo el archivo
			general=new Hashtable();
		}
	}

	/**
	* @param inputStream InputStream de donde obtener el INI
	*/
	public ParseINI(InputStream inputStream) throws IOException
	{
		load(inputStream);
	}

    /** Indica si se ha modificado el INI
	 * @return true si se ha modificado el .INI
	 */
	public boolean need2Save()
	{
		return need2Save;
	}

	/** Guardo nuevamente el .INI si este se ha modificado
	 */
	public void finalize()
	{
		if(autoSave && need2Save)
			try
			{
				save();
			}catch(IOException e)
			{
				System.out.println("ParseINI (" + filename + "):" +e);
			}
	}

	/** Devuelve un Enumeration con los Hashtables conteniendo los entries de
	 * cada seccion
	 */
	public Enumeration sectionElements()
	{
		return sections.elements();
	}

	/** Devuelve un Enumeration con los nombres de las secciones
	 */
	public Enumeration sectionNames()
	{
		return sections.keys();
	}

	/** Devuelve una copia del Hashtable conteniendo la seccion especificada
	 * @param section Nombre de la seccion
	 * @return Hashtable con copia de los entries de esa seccion
	 */
	public Hashtable getSection(String section)
	{
		if(sections.containsKey(section))
			return (Hashtable) ((Hashtable)sections.get(section)).clone();
		else return null;

	}

    /** Devuelve el valor booleano de una property
     * @param section Nombre de la seccion
     * @param property Property a obtener su valor booleano
     * @param byDefault Valor por defecto en caso de ser err�neo o no existir
     */
    public boolean getBooleanProperty(String section, String property, boolean byDefault)
    {
        if(byDefault)
            return !getProperty(section, property, "true").equalsIgnoreCase("false");
        else
            return getProperty(section, property, "false").equalsIgnoreCase("true");
    }

    /** Devuelve el valor booleano de una General Property
     * @param property Property a obtener su valor booleano
     * @param byDefault Valor por defecto en caso de ser err�neo o no existir
     */
    public boolean getBooleanGeneralProperty(String property, boolean byDefault)
    {
        if(byDefault)
            return !getGeneralProperty(property, "true").equalsIgnoreCase("false");
        else
            return getGeneralProperty(property, "false").equalsIgnoreCase("true");
    }

    /** Indica si existe una seccion
     *  @param section Nombre de la secci�n
     *  @return boolean True si la secci�n existe
     */
    public boolean sectionExists(String section)
    {
        return sections.containsKey(section);
    }

	/** Elimina una seccion
	 * @param section Nombre de la seccion a eliminar
	 */
	public void removeSection(String section)
	{
		need2Save |= sections.remove(section) != null;
	}

	/** Elimina una property de una seccion
	 * @param section Nombre de la seccion
	 * @param prop Nombre de la property a eliminar
	 */
	public void removeProperty(String section, String prop)
	{
		if(sections.containsKey(section))
			need2Save |= ((Hashtable)sections.get(section)).remove(prop) != null;
	}

	/** Elimina una property General
	 * @param prop Nombre de la property a eliminar
	 */
	public void removeGeneralProperty(String prop)
	{
	
			need2Save |= general.remove(prop) != null;
	}

	/** Devuelve la property asociada
	 * @param prop Nombre de la property a obtener
	 * @return el value de la property o null si no existe es property
	 */
	public String getGeneralProperty(String prop)
	{
		return getProperty(general,prop,null);
	}

	/** Devuelve la property asociada en la seccion General
	 * @param prop Nombre de la property a obtener
	 * @param defecto String a retornar si no se encuentra esa entrada
	 * @return el value de la property o el string por defecto si no existe es property
	 */
	public String getGeneralProperty(String prop, String defecto)
	{
		return getProperty(general,prop,defecto);
	}

	/** Agrega una General property
	 * @param prop Nombre de la property
	 * @param value Valor de la property
	 */
	public void setGeneralProperty(String prop, String value)
	{
		need2Save |= !value.equals(general.put(prop, value));
	}

	/** Agrega una generalProperty solo si no exist�a
	 * @param prop Nombre de la property
	 * @param value Valor de la property
	 * @return true si se agrego la GeneralProperty y false si ya exist�a
	 */
	public boolean setupGeneralProperty(String prop, String value)
	{
		if(general.containsKey(prop))
			return false;
		else setGeneralProperty(prop, value);
		return true;
	}

	/** Agrega una Property solo si no exist�a
	 * @param section Nombre de la Seccion
	 * @param prop Nombre de la property
	 * @param value Valor de la property
	 * @return true si se agrego la GeneralProperty y false si ya exist�a
	 */
	public boolean setupProperty(String section, String prop, String value)
	{
        if(getProperty(section, prop) != null)		
			return false;			
		else 
			setProperty(section, prop, value);
		return true;
	}

	/** Agrega una property a una seccion ( y crea la seccion en caso de que
	 * no exista)
	 * @param section Nombre de la seccion
	 * @param prop Nombre de la property
	 * @param value Valor de la property
	 */
	public void setProperty(String section, String prop, String value)
	{
		if(!sections.containsKey(section))
			sections.put(section, new Hashtable()); //When Value.length == 0, then property will not be included in gxprn.ini save(), so there's not need to save file.
		need2Save |=  !value.equals(((Hashtable)sections.get(section)).put(prop, value)) && value.length() != 0;
	}

	/** Devuelve la property asociada de la secci�n especificada
	 * @param section Nombre de la secci�n
	 * @param prop Nombre de la property a obtener
	 * @return el value de la property o null si no existe es property
	 */
	public String getProperty(String section, String prop)
	{
		return getProperty((Hashtable)sections.get(section), prop, null);
	}

	/** Devuelve la property asociada de la seeci�n especificada
	 * @param section Nombre de la secci�n
	 * @param prop Nombre de la property a obtener
	 * @param defecto String a retornar si no se encuentra esa entrada
	 * @return el value de la property o el string por defecto si no existe es property
	 */
	public String getProperty(String section, String prop, String defecto)
	{
		return getProperty((Hashtable)sections.get(section), prop, defecto);
	}

	private String getProperty(Hashtable section, String prop, String defecto)
	{
		if(section != null && section.containsKey(prop))
			return (String)section.get(prop);
		if(section != null && alias.containsKey(prop)) // veo si es un alias
			return getProperty(section, (String)alias.get(prop), defecto);
		return defecto;
	}

    /** Agrega un item a una property con muli-items (separados por un 'separador')
	 * @param section Nombre de la secci�n
	 * @param prop Nombre de la property a agregar item
     * @param item Item a agregar
     * @param separator separador
     * @return true si el item se agrego y false si ya exist�a ese item en la property
     */
    public boolean addItemToProperty(String section, String prop, String item, String separator)
    {
        Vector tempVector = parseLine(getProperty(section, prop), separator);
        if(tempVector.contains(item))return false;
        tempVector.addElement(item);
        String temp = "";
        for(Enumeration enumera = tempVector.elements(); enumera.hasMoreElements();)
            temp += (String)enumera.nextElement() + separator;
        setProperty(section, prop, temp);
        return true;
    }


    /** Agrega un item a una General property con muli-items (separados por un 'separador')
	 * @param prop Nombre de la property a agregar item
     * @param item Item a agregar
     * @param separator separador
     * @return true si el item se agrego y false si ya exist�a ese item en la property
     */
    public boolean addItemToGeneralProperty(String prop, String item, String separator)
    {
        Vector tempVector = parseLine(getGeneralProperty(prop), separator);
        if(tempVector.contains(item))return false;
        tempVector.addElement(item);
        String temp = "";
        for(Enumeration enumera = tempVector.elements(); enumera.hasMoreElements();)
            temp += (String)enumera.nextElement() + separator;
        setGeneralProperty(prop, temp);
        return true;
    }


    /** Elimina un item a una property con muli-items (separados por un 'separador')
	 * @param section Nombre de la secci�n
	 * @param prop Nombre de la property a eliminar item
     * @param item Item a eliminar
     * @param separator separador
     * @return true si el item se elimino y false si no existi� ese item
     */
    public boolean removeItemFromProperty(String section, String prop, String item, String separator)
    {
        Vector tempVector = parseLine(getProperty(section, prop), separator);
        if(!tempVector.contains(item))return false;
        tempVector.removeElement(item);
        String temp = "";
        for(Enumeration enumera = tempVector.elements(); enumera.hasMoreElements();)
            temp += (String)enumera.nextElement() + separator;
        if(temp.equals(""))removeProperty(section, prop);
        else setProperty(section, prop, temp);
        return true;
    }


    /** Elimina un item a una General Property con muli-items (separados por un 'separador')
	 * @param section Nombre de la secci�n
	 * @param prop Nombre de la property a eliminar item
     * @param item Item a eliminar
     * @param separator separador
     * @return true si el item se elimino y false si no existi� ese item
     */
    public boolean removeItemFromGeneralProperty(String prop, String item, String separator)
    {
        Vector tempVector = parseLine(getGeneralProperty(prop), separator);
        if(!tempVector.contains(item))return false;
        tempVector.removeElement(item);
        String temp = "";
        for(Enumeration enumera = tempVector.elements(); enumera.hasMoreElements();)
            temp += (String)enumera.nextElement() + separator;
        if(temp.equals(""))removeGeneralProperty(prop);
        else setGeneralProperty(prop, temp);
        return true;
    }


	/** Setea un alias a una seccion
	 * @param alias alias de la seccion
	 * @param original nombre original de la seccion
	 * @return true si la operaci�n se realizo con �xito
	 */
	public boolean setAlias(String alias, String original)
	{
		if(!sections.containsKey(original))return false;
		else
			{
				aliased.put(original, alias);  // original -> alias
				this.alias.put(alias, original);    // alias -> original
				return true;
			}
	}

	/** Renombra una seccion
	 * @param nuevo nuevo nombre de la seccion
	 * @param original nombre original de la seccion
	 *
	 * @return true si la operaci�n se realizo con �xito
	 */
	public boolean renameSection(String nuevo, String original)
	{
		if(!sections.containsKey(original) ||  sections.containsKey(nuevo))return false;
		else
			if(!original.equals(nuevo))
			{
				sections.put(nuevo, (Hashtable) sections.get(original));
				sections.remove(original);
				need2Save = true;
			}
		return true;
	}

	/** True si la seccion tiene un alias
	 * @param original Nombre de la seccion
	 * @return True si la seccion tiene un alias
	 */
	public boolean isAliased(String original)
	{
		return aliased.containsKey(original);
	}

	/** Devuelve el alias de una seccion
	 * @param original Nombre de la seccion a obtener su alias
	 * @return Alias de la seccion o nombre original si no existe alias
	 */
	public String getAlias(String original)
	{
		if(isAliased(original)) return (String)aliased.get(original);
		else return original;
	}


	/** Carga de un DataInputStream las properties
	 * Las secci�n se setean con la property 'Name: SectionName' o '[SectionName]'
	 * y termina cuando aparece otro inicio de secci�n
	 * Si no se especifica ninguna seccion se toma una por defecto
	 * @param inputStream el DataInputStream
	 */
	public void load(InputStream iStream) throws IOException
	{
		inputStream = new InputStreamReader(iStream, "UTF8");

		sectionEntries=new Hashtable();
		sections.put(GENERAL, sectionEntries);
		try
		{
			while ((entryName = readEntryName()) != null)
				sectionEntries.put(entryName,readEntry());
			inputStream.close();
		}catch(EOFException e){}
		general = (Hashtable) sections.get(GENERAL);
		sections.remove(GENERAL);
	}

	/** Guarda el INI en el archivo que se abrio este INI
	 */
	public void save() throws IOException
	{
		if(need2Save && filename != null)
			save(new FileOutputStream(filename));
	}

	/** Setea que si se quiere actualizar el archivo del INI automaticamente
	 * @param autoSave True si se quiere actualizar automaticamente el archvo del INI
	 */
	public void setAutoSave(boolean autoSave)
	{
		this.autoSave = autoSave;
	}

	/** Env�a una serializaci�n de una seccion al outputStream pasado como par�metro
	 * @param section Nombre de la secci�n a serializar
	 * @param out OutputStream por donde mandar la seccion
	 * @param sendGeneral True si se desea env�ar las General Properties
	 * @return True si la seccion existia
	*/
	public boolean serializeSection(String section, OutputStream out, boolean sendGeneral)throws IOException
	{

		if(!sections.containsKey(section))return false;
		ObjectOutputStream serialOut = new ObjectOutputStream(out);
		serialOut.writeObject(section);
		out.flush();
		serialOut.writeObject(sections.get(section));
		out.flush();
		serialOut.writeObject(new Boolean(sendGeneral));
		out.flush();
		if(sendGeneral)
			serialOut.writeObject(general);
		out.flush();
		return true;
	}

	/** Recibe una serializaci�n de una seccion y la agraga al INI
	 * @param in InputStream de donde obtener la secci�n
	 * @return section Nombre de la seccion recibida
	 */
	public String unserializeSection(InputStream in) throws IOException, ClassNotFoundException
	{
		ObjectInputStream serialIn = new ObjectInputStream(in);
		String section = (String) serialIn.readObject();
		Hashtable loadedSection = (Hashtable) serialIn.readObject();
		if(!sections.containsKey(section) || !((Hashtable)sections.get(section)).equals(loadedSection))
		{ // Si la seccion no existia o si la seccion le�da es diferente a la que ya existia
			sections.put(section, loadedSection);
			need2Save = true;
		}
		if(((Boolean)serialIn.readObject()).booleanValue())
		{
			general = (Hashtable)serialIn.readObject();
		}

		return section;
	}

	/** Guarda el INI
	 * @param oStrem el Outputstream
	 */
	public void save(OutputStream oStream) throws IOException
	{
		OutputStreamWriter outputStream = new OutputStreamWriter(oStream, "UTF8");
		Enumeration props, secs;
		String prop, value;
		int it;
		if(general!=null)
		{
			props = general.keys();
			while(props.hasMoreElements())
			{
				prop=(String)props.nextElement();
				if((value = (String)getGeneralProperty(prop)).equals(""))continue;
				if((value.length() + prop.length() + 4) > MAX_LINE_LENGTH)it = (MAX_LINE_LENGTH - prop.length() - 4);
				else it = value.length();
				outputStream.write(prop + "= " + value.substring(0, it) + "\r\n");
				for(; (value.length() - it) > ( MAX_LINE_LENGTH - 4) ; it+= (MAX_LINE_LENGTH - 4))
					outputStream.write(" " + value.substring(it, it + (MAX_LINE_LENGTH - 4)) + "\r\n");
				if(it < value.length())outputStream.write(" " + value.substring(it) + "\r\n");
			}
		}
		secs=sectionNames();
		while(secs.hasMoreElements())
		{
			String section=(String)secs.nextElement();
			outputStream.write("\r\n["+section+"]\r\n");
			props = ((Hashtable)sections.get(section)).keys();
			while(props.hasMoreElements())
			{
				prop=(String)props.nextElement();
				if((value = (String)getProperty(section, prop)).equals(""))continue;
				if((value.length() + prop.length() + 4) > MAX_LINE_LENGTH)it = (MAX_LINE_LENGTH - prop.length() - 4);
				else it = value.length();
				outputStream.write(prop + "= " + value.substring(0, it) + "\r\n");
				for(; (value.length() - it) > ( MAX_LINE_LENGTH - 4) ; it+= MAX_LINE_LENGTH - 4)
					outputStream.write(" " + value.substring(it, it + MAX_LINE_LENGTH - 4) + "\r\n");
				if(it < value.length())outputStream.write(" " + value.substring(it) + "\r\n");
			}
		}
		outputStream.close();
		need2Save = false;
	}


	/** Obtiene del InputStream el nombre del entry
	 * @param inputStream el InputStreamReader
	 * @return el nombre del entry
	 */
	private String readEntryName()throws IOException
	{
		int offset=0;
		int car=0;

		while(true && car!=-1)
		{
			switch(car=inputStream.read())
			{
				case '%':  // Comentarios
				case '*':
						 while(true)
						 {
							 car=inputStream.read();
							 if(car=='\n' || car=='\r' || car==-1)break;
						 }
						 break;
				case ' ':  // Continuaci�n del entry anterior -> se concatena con el entry que ya se estaba procesando
						 if(entryName==null)throw new IOException("Invalid entry");
						 sectionEntries.put(entryName,(String) sectionEntries.get(entryName) + readEntry());
						 return readEntryName();
				case '[':  // Comienza nueva seccion
							sectionEntries = new Hashtable();
							String sectionName = readEntry();
							sectionName=sectionName.substring(0,sectionName.length()-1);
							sections.put(sectionName,sectionEntries);
							entryName=null;
							return readEntryName();
				case '\n':
				case -1:
				case '\r': break;
				default:   // Tomo entry
						tempBytes[offset++]=car;
						while(true)
						{
							car=inputStream.read();
							if(/*Character.isWhitespace((char)car) ||*/
							car==':' || car=='=' || car==-1)break;
							else tempBytes[offset++]=car;
						}
						if(offset==4 &&         // Chequeo si es nueva Seccion
						   tempBytes[0]=='N' &&
						   tempBytes[1]=='a' &&
						   tempBytes[2]=='m' &&
						   tempBytes[3]=='e')
						{
							sectionEntries=new Hashtable();
							String section=readEntry();
							if(section.startsWith("./"))section=section.substring(2);
							sections.put(section,sectionEntries);
							entryName=null;
							return readEntryName();
						}
						else
						{
							return new String(tempBytes,0,offset);
						}
			}
		}
		return null;
	}

	/** Obtiene del InputStream el value del entry
	 * @param inputStream el InputStreamReader
	 * @return el value del entry
	 */
	private String readEntry()throws IOException
	{		
		int offset=0;
		int car=inputStream.read();
		if (car==-1)
			return "";
		while(Character.isWhitespace((char)car))
		{
			if (car=='\0' || car=='\n' || car=='\r' ||  car==-1)
            {
				return "";
			}
			car=inputStream.read();
		}
		tempBytes[offset++]=car;
		try
		{
			while((car=inputStream.read())!='\0' && car!='\n' && car!='\r' && car!=-1)
				tempBytes[offset++]=car;
		}catch(EOFException e){}
		return new String(tempBytes,0,offset);
	}

	// M�todos de clase


	/** Remueve del string los espacios al comienzo y deja solo un espacio
	 * cuando hay varios seguidos
	 */
	public static String removeExtraSpaces(String line)
	{

		int index, offset, lineLength;
		if(line==null)return null;
		lineLength=line.length();
		char b[]=new char[lineLength];
		if(lineLength==0)return line;
		for(index=0;index<lineLength;index++)if(line.charAt(index)!=' ')break;
		for(offset=0;index<lineLength-1;index++)
			if(line.charAt(index)!=' ' || line.charAt(index+1)!=' ')b[offset++]=line.charAt(index);
		b[offset++]=line.charAt(index);
		return new String(b,0,offset);
	}

	/** Separa partes de una lines y las retorna en un Vector. Las partes pueden estar encerradas por comillas
	 * en cuyo caso hasta que no se cierren las comillas no se se considera el fin de linea
	 * @param line linea a separar
	 * @param separator String conteniendo el separador de partes
	 * @return Vector de Strings conteniendo las partes separadas
	 */
	public static Vector parseLine(String line, String separator)
	{
        Vector partes = new Vector();
        if(line == null) return partes;
        StringTokenizer tokens = new StringTokenizer(line, separator, false);
        if(!tokens.hasMoreTokens())
               return partes;
        String thisToken;
        String lastToken = tokens.nextToken();
        while(tokens.hasMoreTokens())
        {
            thisToken = tokens.nextToken();
            if(lastToken.startsWith("\"") &&
               (!lastToken.endsWith("\"") || lastToken.length() == 1))
            {
                lastToken = lastToken + separator + thisToken;
            }
            else
            {
                partes.addElement(lastToken);
                lastToken = thisToken;
            }
        }
        if(lastToken.startsWith("\"") && !lastToken.endsWith("\"")) // Chequeo el caso en que comienzo con " y no termino con "
            lastToken = lastToken + "\"";
        partes.addElement(lastToken);
        return partes;
	}
}
