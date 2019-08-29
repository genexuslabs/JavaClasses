package com.genexus;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Iterator;
import java.io.InputStream;
import java.io.StringWriter;

import com.genexus.common.classes.AbstractGXFile;
import com.genexus.ModelContext;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.db.DynamicExecute;
import com.genexus.internet.IGxJSONAble;
import com.genexus.internet.IGxJSONSerializable;
import com.genexus.internet.StringCollection;
import com.genexus.util.GXMap;
import com.genexus.util.Quicksort;
import com.genexus.xml.GXXMLSerializer;
import com.genexus.xml.XMLReader;
import com.genexus.xml.GXXMLSerializable;

import com.genexus.xml.XMLWriter;

import json.org.json.IJsonFormattable;
import json.org.json.JSONArray;
import json.org.json.JSONException;

import org.simpleframework.xml.*;

@Root(name="Collection")
public class GXSimpleCollection<T> extends Vector<T> implements Serializable, IGxJSONAble, IGxJSONSerializable {

	@ElementList(entry="item",inline=true)
    GXSimpleCollection<T> list;
	protected Class<T> elementsType;
	protected String elementsName;
	protected String xmlElementsName;
	protected String containedXmlNamespace = "";
	protected int remoteHandle = -1;
	protected JSONArray jsonArr = new JSONArray();

	public GXSimpleCollection()
	{
	}

	public GXSimpleCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace)
	{
		this(elementsType, elementsName, containedXmlNamespace, (int)-1);
	}

	public GXSimpleCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace, int remoteHandle)
	{
		this(elementsType, elementsName, containedXmlNamespace, null, remoteHandle);
	}

	public GXSimpleCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace, Vector<T> data)
	{
		this(elementsType, elementsName, containedXmlNamespace, data, (int)-1);
	}

	public GXSimpleCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace, Vector data, int remoteHandle)
	{
		this.elementsType = elementsType;
		this.elementsName = elementsName;
		xmlElementsName = elementsName;
		this.containedXmlNamespace = containedXmlNamespace;
		this.remoteHandle = remoteHandle;
		if (data != null){
			Iterator it = data.iterator();
			while(it.hasNext())
				addObject(it.next());
		}
		list=this;
	}	

	public Class getElementsType()
	{
		return elementsType;
	}

	public boolean IsSimpleCollection()
	{
		return true;
	}

	public short readxml(XMLReader reader)
	{
		return readxml(reader, "");
	}

	public short readxml( XMLReader oReader ,
			String sName )
	{
		short currError ;
		String arrayType ;
		short gxi ;
		currError = (short)(1) ;
		arrayType = "" ;
		gxi = (short)(0) ;
		while ( gxi <= oReader.getAttributeCount() )
		{
			if ( CommonUtil.strcmp(oReader.getAttributeLocalName(gxi), "arrayType") == 0 )
			{
				arrayType = oReader.getAttributeByIndex(gxi) ;
			}
			gxi = (short)(gxi+1) ;
		}
		if ( CommonUtil.strcmp(arrayType, "") != 0 )
		{
			currError = readEncodedArray(arrayType,oReader) ;
		}
		else
		{
			currError = readxmlcollection(oReader,sName,"") ;
		}
		return currError ;
	}
	public short readEncodedArray( String arrayType ,
			XMLReader oReader )
	{
		short currError ;
		int arrayLength ;
		int arraySizeStartPos ;
		int arraySizeLength ;
		short gxi ;
		arraySizeStartPos = (int)(CommonUtil.strSearch( arrayType, "[", 1)+1) ;
		arraySizeLength = (int)(CommonUtil.len( arrayType)-arraySizeStartPos) ;
		if ( ( arraySizeStartPos == 1 ) || ( arraySizeLength == 0 ) )
		{
			throw new RuntimeException("GxSimpleCollection error: Could not read encoded array size" + "(" + 0+ ")");
		}
		arrayLength = (int)(CommonUtil.lval( CommonUtil.substring( arrayType, arraySizeStartPos, arraySizeLength))) ;
		currError = oReader.read() ;
		gxi = (short)(0) ;
		while ( ( gxi < arrayLength ) && ( currError > 0 ) )
		{
			currError = AddObjectInstance(oReader) ;
			oReader.read();
			gxi = (short)(gxi+1) ;
		}
		return currError ;
	}
	private short readIntegralCollectionFromXML(XMLReader reader, String itemName)
	{
		reader.read();
		short currError = reader.read();
		Class [] constructor = new Class[] { String.class };
		removeAllItems();
		try
		{
			while( reader.getLocalName().equalsIgnoreCase(itemName) && currError > 0)
			{
				add(elementsType.getConstructor(constructor).newInstance(new Object[]{reader.getValue()}));
				reader.read();
			}
			return (byte)currError;
		}catch(Exception e)
		{
			System.err.println("GXSimpleCollection<" + elementsType.getName() + "> (readxml): " + e.toString());
			if(e instanceof InvocationTargetException)
			{
				System.err.println(((InvocationTargetException)e).getTargetException().toString());
			}
			return -1;
		}
	}

	public void writexml(XMLWriter writer, String name, String namespace)
	{
		writexmlcollection(writer, name, namespace, xmlElementsName, containedXmlNamespace, true);
	}

	public void writexml(XMLWriter writer, String name, String namespace, boolean includeState)
	{
		writexmlcollection(writer, name, namespace, xmlElementsName, containedXmlNamespace, includeState);
	}
	public void writexmlcollection(XMLWriter writer, String name, String namespace, String itemName, String itemNamespace)
	{
		writexmlcollection(writer, name, namespace, itemName, itemNamespace, true);
	}
	public void writexmlcollection(XMLWriter writer, String name, String namespace, String itemName, String itemNamespace, boolean includeState)
	{
		try
		{
			if (!name.trim().equals(""))
			{
				writer.writeStartElement(name);
				if (!namespace.startsWith("[*:nosend]"))
					writer.writeAttribute("xmlns", namespace);
				else
					namespace = namespace.substring(10);
			}
			// Si se trata de una collection de tipos basicos
			String itemName1 = "item";
			if (!itemName.trim().equals("") && !itemName.trim().equals("internal"))
			{
				itemName1 = itemName;
			}

			if (itemNamespace.startsWith("[*:nosend]"))
				itemNamespace = itemNamespace.substring(10);

			boolean sendItemNamespace = false;
			if (name.trim().equals(""))
			{
				sendItemNamespace = true;
			}
			else if (!namespace.equals(itemNamespace))
			{
				sendItemNamespace = true;
			}

			for(T item : this)
			{
				writer.writeElement(itemName1, item.toString());
				if (sendItemNamespace)
				{
					writer.writeAttribute("xmlns", itemNamespace);
				}
			}
			if (!name.trim().equals(""))
			{
				writer.writeEndElement();
			}
		}catch(Exception e)
		{
			System.err.println("GXSimpleCollection<" + elementsType.getName() + "> (writexml): " + e.toString());
		}
	}

	public short AddObjectInstance(XMLReader reader)
	{
		try
		{
			add(elementsType.getConstructor(new Class[] { String.class }).newInstance(new Object[]{reader.getValue()}));
			return 1;
		}catch(Exception e)
		{
			System.err.println("GXSimpleCollection<" + elementsType.getName() + "> (AddObjectInstance): " + e.toString());
			if(e instanceof InvocationTargetException)
			{
				System.err.println("Contained exception: " + ((InvocationTargetException)e).toString());
			}
			return -1;
		}
	}

	public String toxml(String header, String namespace)
	{
		return toxml(false, header, namespace);
	}
	public String toxml(boolean includeHeader, String header, String namespace)
	{
		return toxml(includeHeader, true, header, namespace);
	}

    public String toxml(boolean includeHeader, boolean includeState, String header, String namespace) {
		
		if(SpecificImplementation.Application.getProperty("SIMPLE_XML_SUPPORT", "0").equals("1"))
		{
			try {
				return GXXMLSerializer.serializeSimpleXml(includeHeader, SpecificImplementation.Application.createCollectionWrapper(this), header);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}
		else{
			XMLWriter writer = new XMLWriter();
			writer.setEncoding("UTF8");
			writer.openToString();
			if (includeHeader)
				writer.writeStartDocument("UTF-8");
			if (namespace == "")
				namespace = "[*:nosend]";
			writexml(writer, header, namespace, includeState);
			writer.close();
			return writer.getResultingString();
		}
    }

	public short readCollection( XMLReader oReader )
	{
		return readxmlcollection(oReader, "", "");
	}

	public short readxmlcollection( XMLReader oReader ,
			String sName ,
			String itemName )
	{
		short currError ;
		String sTagName ;
		String itemName1 ;
		currError = (short)(1) ;
		itemName1 = GetContainedName() ;
		if ( ! (CommonUtil.strcmp("", itemName)==0) )
		{
			itemName1 = itemName ;
		}
		if ( ( CommonUtil.strcmp(oReader.getLocalName(), itemName1) != 0 ) || ( CommonUtil.strcmp(sName, itemName1) == 0 ) )
		{
			currError = oReader.read() ;
		}
		sTagName = oReader.getName() ;
		if ( ! (CommonUtil.strcmp("", sName)==0) )
		{
			this.clearCollection();
		}
		while ( ( CommonUtil.strcmp(oReader.getName(), sTagName) == 0 ) && ( oReader.getNodeType() == 1 ) && ( currError > 0 ) )
		{
			if ( IsSimpleCollection() || ( oReader.getIsSimple() == 0 ) || ( oReader.getAttributeCount() > 0 ) || com.genexus.xml.GXXMLSerializable.class.isAssignableFrom(elementsType))
			{
				currError = AddObjectInstance(oReader) ;
			}
			oReader.read();
		}
		return currError ;
	}

	public boolean fromxml(String xml)
	{
		return fromxml(xml, "");
	}

	public boolean fromxmlfile(AbstractGXFile xml)
	{
		return fromxml(xml.readAllText(""));
	}

	public boolean fromxmlfile(AbstractGXFile xml, String collName)
	{
		return fromxml(xml.readAllText(""));
	}

	public boolean fromxmlfile(AbstractGXFile xml, GXBaseCollection<SdtMessages_Message> messages, String collName)
	{
		return fromxml(xml.readAllText(""), messages, "");
	}

	public boolean fromxml(String xml, String collName)
	{
		return fromxml(xml, null, collName);
	}
	public boolean fromxml(String xml, GXBaseCollection<SdtMessages_Message> messages, String collName)
	{
		try{
			if(SpecificImplementation.Application.getProperty("SIMPLE_XML_SUPPORT", "0").equals("1"))
            {
                xml=xml.replaceAll("<item>", "<item class=\""+elementsType.getName()+"\">");
                GXXMLSerializer.deserializeSimpleXml(this, xml);
                return true;
            } else {
				XMLReader reader = new XMLReader();
				reader.openFromString(xml);
				short result;
				result = readIntegralCollectionFromXML(reader, "item");

				reader.close();
				if (result <= 0) { 
					CommonUtil.ErrorToMessages(String.valueOf(reader.getErrCode()), reader.getErrDescription(), messages);
					return false;
				}
				else
					return true;
			}
		}
		catch(Exception ex)
		{
			CommonUtil.ErrorToMessages("fromxml error", ex.getMessage(), messages);
			return false;
		}
	}
	public String GetContainedName()
	{
		return elementsName;
	}

	public int getItemCount()
	{
		return size();
	}

	public T item(int index)
	{
		return super.get(index - 1);
	}

	public String stringElementAt(int index)
	{
		return super.get(index).toString();
	}


	Object currentItem;
	public byte currentItem(Object item)
	{
		if(super.contains(item))
		{
			currentItem = item;
		}
		else
		{
			currentItem = null;
		}
		return (byte)((currentItem != null) ? 1 : 0);
	}

	public Object currentItem()
	{
		if (currentItem == null)
		{
			try
			{
				currentItem = elementsType.newInstance();
			}
			catch (Exception e)
			{
				System.err.println("[currentItem]:" + e.toString());
			}
		}
		return currentItem;
	}

	//-- Este add se usa cuando se quiere agregar a las lineas de un BC sin usar la logica de manteniomiento del estado
	// de la linea
	public void addBase( Object item)
	{
		super.add((T)item);
	}
	protected String getMethodName(boolean isGet, String method)
	{
		String getName = elementsType.getName();
		int packageIndex = getName.lastIndexOf('.');
		if(packageIndex != -1)
		{
			getName = getName.substring(packageIndex+1);
		}
		return (isGet ? GET_METHOD_NAME : SET_METHOD_NAME) + getName + "_" + method;
	}

	//-- Add

	public void addInternal(Object item)
	{
		super.add((T)item);
	}

	public void add(Object item, int index)
	{
		if(index < 1 || index > size())
		{
			add((T)item); //this.add, GXBCLevelCollection.add for example
		}
		else
		{
			super.add(index - 1, (T)item); //Vector insert element
		}
	}

	public void add(byte item)
	{
		addInternal(new Byte(item));
	}

	public void add(char item)
	{
		addInternal(new Character(item));
	}

	public void add(short item)
	{
		addInternal(new Short(item));
	}

	public void add(int item)
	{
		if (elementsType != null)
			addIntegralConstant(item);
		else
			addInternal(new Integer(item));
	}

	public void addIntegralConstant(double item)
	{
		// Este caso es especial, pues las constantes enteras son de tipo int
		if(elementsType == Long.class)
		{
			add((long)item);
		}else if(elementsType == Short.class)
		{
			add((short)item);
		}else if(elementsType == Byte.class)
		{
			add((byte)item);
		}else if(elementsType == Float.class)
		{
			add((float)item);
		}else if(elementsType == Integer.class)
		{
			add(new Integer((int)item));
		}else if(elementsType == java.math.BigDecimal.class)
		{
			addObject(new java.math.BigDecimal(item));
		}else
		{
			add(new Double(item));
		}
	}

	public void addObject(Object obj){
		super.add((T)obj);
	}

	public void add(long item)
	{
		addInternal(new Long(item));
	}

	public void add(float item)
	{
		addInternal(new Float(item));
	}

	public void add(double item)
	{
		addIntegralConstant(item);
	}

	public void add(byte item, int index)
	{
		add(new Byte(item), index);
	}

	public void add(char item, int index)
	{
		add(new Character(item), index);
	}

	public void add(short item, int index)
	{
		add(new Short(item), index);
	}

	public void add(int item, int index)
	{
		addIntegralConstant(item, index);
	}

	public void addIntegralConstant(double item, int index)
	{
		// Este caso es especial, pues las constantes enteras son de tipo int
		if(elementsType == Long.class)
		{
			add((long)item, index);
		}else if(elementsType == Short.class)
		{
			add((short)item, index);
		}else if(elementsType == Byte.class)
		{
			add((byte)item, index);
		}else if(elementsType == Float.class)
		{
			add((float)item, index);
		}else if(elementsType == Integer.class)
		{
			add(new Integer((int)item), index);
		}else if(elementsType == java.math.BigDecimal.class)
		{
			add(new java.math.BigDecimal(item), index);
		}else
		{
			add(new Double(item), index);
		}
	}

	public void add(long item, int index)
	{
		add(new Long(item), index);
	}

	public void add(float item, int index)
	{
		add(new Float(item), index);
	}

	public void add(double item, int index)
	{
		addIntegralConstant(item, index);
	}

	public void removeAllItems()
	{
		super.clear();
	}

	public void clearCollection()
	{
		removeAllItems();
	}


	//-- indexof

	public int indexof(Object item)
	{
		return super.indexOf(item) + 1;
	}

	public int indexof(byte item)
	{
		return indexof(new Byte(item));
	}

	public int indexof(char item)
	{
		return indexof(new Character(item));
	}

	public int indexof(short item)
	{
		return indexof(new Short(item));
	}

	public int indexof(int item)
	{
		// Este caso es especial, pues las constantes enteras son de tipo int
		if(elementsType == Long.class)
		{
			return indexof((long)item);
		}else if(elementsType == Short.class)
		{
			return indexof((short)item);
		}else if(elementsType == Byte.class)
		{
			return indexof((byte)item);
		}else if(elementsType == Float.class)
		{
			return indexof((float)item);
		}else if(elementsType == Integer.class)
		{
			return indexof(new Integer(item));
		}else if(elementsType == java.math.BigDecimal.class)
		{
			return indexof(new java.math.BigDecimal(item));
		}else
		{
			return indexof(new Double(item));
		}
	}

	public int indexof(long item)
	{
		return indexof(new Long(item));
	}

	public int indexof(float item)
	{
		return indexof(new Float(item));
	}

	public int indexof(double item)
	{
		return indexof(new Double(item));
	}

	//-- remove
	// El problema con el remove es que hay 2 versiones.
	// remove(index) y remove(object) y cuando el objeto es un tipo b�sico(num�rico)
	// entonces no tenemos manera de diferenciar un caso del otro.
	// Lo que hacemos por ahora es asumir que si viene un remove con un par�metro numerico,
	// siempre se intente eliminar por index.

	public byte remove(double index)
	{
		return removeElement(index);
	}

	public boolean remove(char item)
	{
		return remove(new Character(item));
	}

	public byte removeItem(int index)
	{
		T item = null;
		if(index > 0 && index <= size())
		{
			item = super.remove((int)index - 1);//Vector.remove(int)
			return (byte)1;
		}
		return (byte)0;
	}

	public byte removeElement(double index)
	{
		if(index > 0 && index <= size())
		{
			super.remove((int)index - 1);//Vector.remove(int)
			return (byte)1;
		}
		else
		{
			return (byte)0;
		}
	}

	/** Ordena la Collection de acuerdo a un proc pasado por parametro
	 * El proc tiene que recibir como parms
	 *	parm(IN: &SDT1, IN: &SDT2, OUT: INT)
	 * O sea, los 2 primero parametros son los SDTs a comparar y el tercero es el resultado
	 */
	public void sortproc(String procName, ModelContext modelContext, int handle)
	{
		//Quicksort.sort(vector, new ProcComparer(procName, modelContext, handle));
	}

	// LS 12/07/09
	// agregado para tener visibilidad del constructor usado por el generador

	public String getelementsName( )
	{
		return elementsName ;
	}

	public String getcontainedXmlNamespace( )
	{
		return containedXmlNamespace ;
	}

	class ProcComparer implements com.genexus.util.Comparer
	{
		private String procName;
		private ModelContext modelContext;
		private int handle;
		public ProcComparer(String procName, ModelContext modelContext, int handle)
		{
			this.procName = procName.trim().toLowerCase();
			this.modelContext = modelContext;
			this.handle = handle;
		}

		public int compare(Object a, Object b)
		{
			Object [] params = new Object[3];
			params[0] = a;
			params[1] = b;
			params[2] = new Integer(0);
			DynamicExecute.dynamicExecute(modelContext, handle, GXSimpleCollection.class, procName, params);
			return ((Number)params[2]).intValue();
		}
	}

	protected static final String GET_METHOD_NAME = "getgxTv_";
	protected static final String SET_METHOD_NAME = "setgxTv_";
	protected static final Object[] NULL_OBJECT_ARRAY = new Object[]{};
	protected static final Class [] NULL_CLASS_ARRAY = new Class[]{};

	/** Ordena la Collection de acuerdo a algun miembro de sus items
	 * Por ejemplo si los SDTs tienen estructura itemA.itemB.codigo
	 * puedo poner &colSDT.sort("itemA.itemB.codigo");
	 * Si quiero que el orden sea descendente puedo poner
	 * &colSDT.sort("(itemA.itemB.codigo)");
	 *
	 * Tambien puedo poner sort("a.b, (c.d), e")
	 * Esto quiere decir ordenar primero segun a.b, en caso de tener
	 * items iguales ordenar esos items segun c.d descendente, etc
	 * Tambien se aceptan [] como delimitadores para indicar orden descendente
	 */
	public void sort(String membersList)
	{
		// Obtengo el primer elemento de esta collection
		// Si no hay ninguno no debo hacer nada
		if(size() == 0)
		{
			return;
		}

		StringTokenizer tokenizer = new StringTokenizer(membersList, ",", false);
		List<MemberComparerItem> memberComparers = new ArrayList<MemberComparerItem>();
		//MemberComparerItem [] memberComparers = new MemberComparerItem[tokenizer.countTokens()];
		int size = tokenizer.countTokens();
		for(int i = 0; i < size; i++)
		{
			String member = (String)tokenizer.nextElement();
			try
			{
				memberComparers.add(getMethodList(member));
			}catch(Exception e)
			{
				System.err.println("Collection sort failed: " + e.toString());
				return;
			}
		}
		// Ok, ahora si puedo ordenar la collection
		Quicksort.sort(this, new CollectionMemberComparer(memberComparers));
	}


	private MemberComparerItem getMethodList(String memberList)throws Exception
	{
		memberList = memberList.trim();
		boolean isAscending = true;
		if((memberList.startsWith("[") && memberList.endsWith("]")) ||
				(memberList.startsWith("(") && memberList.endsWith(")")))
		{
			isAscending = false;
			memberList = memberList.substring(1, memberList.length() - 1).trim();
		}

		// Primero obtengo la lista de miembros
		int cantMembers = 0;
		int index = 0;
		memberList += ".";
		while((index = memberList.indexOf('.', index+1)) != -1)
		{
			cantMembers++;
		}
		Method [] members = new Method[cantMembers];
		index = 0;
		String managerName = elementsType.getName();
		Class curElementType = Class.forName(managerName);
		//Class curElementType = elementsType;
		for (int i = 0; i < cantMembers; i++) {
			int index2 = memberList.indexOf('.', index);
			String member = memberList.substring(index, index2);
			index = index2 + 1;
			try {
				String getName = curElementType.getName();
				int packageIndex = getName.lastIndexOf('.');
				if(packageIndex != -1)
				{
					getName = getName.substring(packageIndex+1);
				}
				String javaMember = GET_METHOD_NAME + getName + "_" +
						Character.toUpperCase(member.charAt(0)) +
						member.toLowerCase().substring(1);
				members[i] = curElementType.getMethod(javaMember, NULL_CLASS_ARRAY);
			}
			catch (NoSuchMethodException e) {
				// Si no existe dicho este miembro
				System.err.println("Member '" + member +
						"' does not exist in member list '" +
						memberList.substring(0, memberList.length() - 1) +
						"'");
				throw e;
			}
			curElementType = members[i].getReturnType();
		}

		return new MemberComparerItem(members, isAscending);
	}

	class MemberComparerItem
	{
		public Method []methodList;
		public boolean isAscending;
		public boolean isIntegral;

		public MemberComparerItem(Method [] methodList, boolean isAscending)
		{
			this.methodList = methodList;
			this.isAscending = isAscending;
			this.isIntegral = methodList[methodList.length - 1].getReturnType().isPrimitive();
		}
	}

	class CollectionMemberComparer implements com.genexus.util.Comparer
	{
		private List<MemberComparerItem> members;
		public CollectionMemberComparer(List<MemberComparerItem> members)
		{
			this.members = members;
		}

		public int compare(Object a, Object b)
		{
			//Este es el caso donde se hace un sort de una collecion de tipos de datos basicos (NO SDT)
			if (members.size() == 0)
			{
				int result = compareMember(null, a, b);
				if(result != 0)
				{
					return result;
				}
				return 0;
			}

			for(int i = 0; i < members.size(); i++)
			{
				int result = compareMember(members.get(i), a, b);
				if(result != 0)
				{
					return result;
				}
			}
			return 0;
		}

		private int compareMember(MemberComparerItem comparer, Object a, Object b)
		{
			try
			{
				if(!a.getClass().equals(elementsType) || !b.getClass().equals(elementsType))
				{
					return 1;
				}

				boolean isIntegral;
				boolean isAscending;
				if (comparer != null)
				{
					Method [] members = comparer.methodList;
					isIntegral = comparer.isIntegral;
					isAscending = comparer.isAscending;

					// Primero obtengo el objeto asociado al miembro por el cual quiero comparar
					int i;
					for(i = 0; i < members.length; i++)
					{
						a = members[i].invoke(a, NULL_OBJECT_ARRAY);
						b = members[i].invoke(b, NULL_OBJECT_ARRAY);
					}
					i--;
				}
				else
				{
					isIntegral = isIntegral = a.getClass().isPrimitive() || (a instanceof Number && !(a instanceof java.math.BigDecimal));
					isAscending = true;
				}

				// Ahora hago la comparaci�n
				// Si el miembro es un integral, puedo hacer la comparaci�n aritm�tica
				if(isIntegral)
				{
					// 					return ((Comparable)a).compareTo(b); // En JDK1.2+ podr�a hacer esto!
					if (a instanceof Boolean)
					{
						return ((Comparable)a).compareTo(b);
					}
						
					if(isAscending)
					{
						return (int)(((Number)a).doubleValue() - ((Number)b).doubleValue());
					}
					else
					{
						return -((int)(((Number)a).doubleValue() - ((Number)b).doubleValue()));
					}
				}
				else
				{
					if(isAscending)
					{
						if (a instanceof java.util.Date)
						{
							if (((java.util.Date)a).equals(b))
								return 0;
							else
								if (((java.util.Date)a).after((java.util.Date)b))
									return 1;
								else
									return -1;
						}
						else
							if (a instanceof java.math.BigDecimal)
							{
								return ((java.math.BigDecimal)a).compareTo((java.math.BigDecimal)b);
							}
							else
								return a.toString().compareTo(b.toString());
					}
					else
					{
						if (a instanceof java.util.Date)
						{
							if (((java.util.Date)a).equals(b))
								return 0;
							else
								if (((java.util.Date)a).after((java.util.Date)b))
									return -1;
								else
									return 1;
						}
						else
							if (a instanceof java.math.BigDecimal)
							{
								return -((java.math.BigDecimal)a).compareTo((java.math.BigDecimal)b);
							}
							else
								return -(a.toString().compareTo(b.toString()));
					}
				}
			}catch(Throwable compareException)
			{
				System.err.println("GXObjectColection.sort: " + compareException.toString());
				return 0;
			}
		}
	}

	public Object clone()
	{
		return super.clone();
	}

	public GXSimpleCollection<T> Clone()
	{
		return (GXSimpleCollection<T>)clone();
	}

	public Vector<T> getStruct()
	{
		return (Vector<T>)super.clone();
	}

	public void setStruct(Vector<T> data)
	{
		super.addAll(data);
	}

	public String toJSonString()
	{
		return toJSonString(true);
	}

	public String toJSonString(boolean includeState)
	{
		return ToJavascriptSource(includeState);
	}

	public String ToJavascriptSource()
	{
		return ToJavascriptSource(true);
	}

	public String ToJavascriptSource(boolean includeState)
	{
		return GetJSONObject(includeState).toString();
	}

	public void tojson()
	{
		tojson(true);
	}
	public void tojson(boolean includeState)
	{
		jsonArr = new JSONArray();
		int size = size();
		for (int i = 0; i < size; i++)
		{
			AddObjectProperty(elementAt(i), includeState);
		}
	}
	public void AddObjectProperty(String name, Object prop)
	{
		//throw new Exception("Method Not implemented");
	}

	public void AddObjectProperty(int prop)
	{
		AddObjectProperty(new Integer(prop));
	}
	public void AddObjectProperty(Object prop)
	{
		AddObjectProperty(prop, true);
	}
	public void AddObjectProperty(Object prop, boolean includeState)
	{
		if (prop instanceof IGxJSONAble)
		{
			if (prop instanceof GxSilentTrnSdt)
				jsonArr.put(((GxSilentTrnSdt)prop).GetJSONObject(includeState));
			else
				jsonArr.put(((IGxJSONAble)prop).GetJSONObject());
		}
		else if (prop instanceof IGxJSONSerializable)
		{
			jsonArr.put(((IGxJSONSerializable)prop).GetJSONObject());
		}		
		else if (prop instanceof Date)
		{
			jsonArr.put(SpecificImplementation.GXutil.timeToCharREST((Date)prop));
		}
		else
		{
			jsonArr.put(prop);
		}
	}
	public Object GetJSONObject()
	{
		return GetJSONObject(true);
	}
	public Object GetJSONObject(boolean includeState)
	{
		tojson(includeState);
		return jsonArr;
	}
	public void FromJSONObject(IJsonFormattable obj)
	{
		
		this.clear();
		JSONArray jsonArr = (JSONArray)obj;
		for (int i = 0; i < jsonArr.length(); i++)
		{
			try
			{
				Object jsonObj = jsonArr.get(i);
				Object currObj = jsonObj;
				Class[] parTypes = new Class[] {};
				Object[] arglist = new Object[] {};
				if (elementsType == null)
				{
					addObject(currObj);
				}
				else
				{
					if (elementsType.getSuperclass().getName().equals("com.genexus.GxSilentTrnSdt"))
					{
						parTypes = new Class[] {int.class};
						arglist = new Object[] {new Integer(remoteHandle)};
					}
					if (IGxJSONAble.class.isAssignableFrom(elementsType))
					{
						Constructor constructor = elementsType.getConstructor(parTypes);
						currObj = constructor.newInstance(arglist);
						((IGxJSONAble)currObj).FromJSONObject((IJsonFormattable)jsonObj);
					}
					if (IGxJSONSerializable.class.isAssignableFrom(elementsType))
					{
						Constructor constructor = elementsType.getConstructor(parTypes);
						currObj = constructor.newInstance(arglist);
						((IGxJSONSerializable)currObj).fromJSonString(jsonObj.toString());
					}
					if (elementsType == Integer.class)
					{
						currObj = new Integer(jsonArr.getInt(i));
					}
					else if (elementsType == Short.class)
					{
						currObj = new Short((short)jsonArr.getInt(i));
					}
					else if (elementsType == Boolean.class)
					{
						currObj = new Boolean(jsonArr.getBoolean(i));
					}
					else if (elementsType == Double.class)
					{
						currObj = new Double(jsonArr.getDouble(i));
					}
					else if (elementsType == java.math.BigDecimal.class)
					{
						currObj = java.math.BigDecimal.valueOf(jsonArr.getDouble(i));
					}
					else if (elementsType == Long.class)
					{
						currObj = new Long(jsonArr.getLong(i));
					}
					else if (elementsType == Date.class)
					{
						currObj = SpecificImplementation.GXutil.charToTimeREST(jsonArr.getString(i));
					}
					else if (elementsType == java.util.UUID.class)
					{
						currObj = CommonUtil.strToGuid(jsonArr.getString(i));
					}
					else if (elementsType == String.class && currObj instanceof Number)
					{
						currObj = new String().valueOf(currObj);
					}
					addObject(currObj);
				}
			}
			catch (Exception exc) {
				exc.printStackTrace(); 
			}
		}
	}
	public boolean fromJSonString(String s)
	{
		return fromJSonString(s, null);
	}
	public boolean fromJSonString(String s, GXBaseCollection<SdtMessages_Message> messages)
	{
		try
		{
			jsonArr = new JSONArray(s);
			FromJSONObject(jsonArr);
			return true;
		}
		catch (JSONException ex)
		{
			CommonUtil.ErrorToMessages("fromjson error", ex.getMessage(), messages);
			return false;
		}
	}

	public boolean fromJSonFile(AbstractGXFile s)
	{
		return fromJSonString(s.readAllText(""));
	}

	public boolean fromJSonFile(AbstractGXFile s, GXBaseCollection<SdtMessages_Message> messages)
	{
		return fromJSonString(s.readAllText(""), messages);
	}

	public void fromStringCollectionJsonString(String s)
	{
		this.clear();
		try
		{
			jsonArr = new JSONArray(s);
			for (int i = 0; i < jsonArr.length(); i++)
			{
				JSONArray jsonObj = jsonArr.getJSONArray(i);
				StringCollection gxsyncline = new StringCollection();
				for (int j = 0; j < jsonObj.length(); j++)
				{
					gxsyncline.add(jsonObj.getString(j));
				}
				addObject(gxsyncline);
			}
		}
		catch (JSONException e)
		{
		}
	}

	public String toStringCollectionJsonString()
	{
		JSONArray jsonObj = new JSONArray();
		String result = "[";
		StringBuilder sb = new StringBuilder(result);
		int size = size();
		if (size > 0)
		{
			StringCollection metaData = (StringCollection)super.get(0);
			for (int l = 1; l <= metaData.getCount(); l++)
			{
				jsonObj.put((String)metaData.item(l));
			}
			sb.append(jsonObj.toString());
		}
		for (int i = 1; i < size; i++)
		{
			sb.append(",");
			Object item = super.get(i);
			String json;
			if (item instanceof StringCollection)
			{
				json = ((StringCollection)item).ToJavascriptSource();
				sb.append(json);
			}
			else //item instanceof GxUnknownObjectCollection
			{
				GxUnknownObjectCollection tableCollection = (GxUnknownObjectCollection)super.get(i);
				int tableCollectionSize = tableCollection.size();
				sb.append("[");
				for (int j = 1; j <= tableCollectionSize; j++)
				{

					Object subItem = tableCollection.item(j);
					if (subItem instanceof StringCollection)
					{
						StringCollection gxsyncline = (StringCollection)subItem;
						json = gxsyncline.ToJavascriptSource() ;
					}
					else //item instanceof GxUnknownObjectCollection
					{
						GxUnknownObjectCollection metadata = (GxUnknownObjectCollection)subItem;
						json = metadata.toStringCollectionJsonString() ;
					}
					if (j==1)
						sb.append(json);
					else
						sb.append(",").append(json);
				}
				sb.append("]");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	// load GxUnknownObjectCollection as client proc expect.
	// Could be GxUnknownObjectCollection / GxUnknownObjectCollection / StringCollection
	// Or GxUnknownObjectCollection / 3 GxUnknownObjectCollection / StringCollection
	public void fromStringCollectionClientJsonString(String s)
	{
		this.clear();
		try {
			jsonArr = new JSONArray(s);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int size = jsonArr.length();

		//processArray((GxUnknownObjectCollection)this, (GxUnknownObjectCollection)this, jsonArr, 1, size);

	}

	private boolean processArray(GxUnknownObjectCollection oldParent, GxUnknownObjectCollection parent, JSONArray localArray, int startIndex, int size )
	{
		GxUnknownObjectCollection tableCollection = new GxUnknownObjectCollection();
		StringCollection gxsyncline = new StringCollection();
		boolean stringColl = false;
		boolean isLeaf = false;
		for (int i = startIndex; i < size; i++)
		{
			isLeaf = false;
			//Send one GxUnknownObjectCollection per table with StringCollection for each line
			tableCollection = new GxUnknownObjectCollection();
			JSONArray jsonObj = null;
			try
			{
				if (stringColl)
				{
					gxsyncline.add((String)localArray.getString(i));
				}
				else
				{
					jsonObj = localArray.getJSONArray(i);
					if (jsonObj.length()>0)
					{
						isLeaf = processArray(parent, tableCollection, jsonObj, 0, jsonObj.length());
					}
				}
			}
			catch (JSONException e)
			{
				stringColl = true;
				//if not a jsonArray , try reading as string.
				//jsonObjData.
				try {
					gxsyncline.add((String)localArray.getString(i));
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//tableCollection.add(gxsyncline);
			}
			if (!stringColl && !isLeaf)
			{
				// if not empty root collection
				if (!(startIndex==1 && tableCollection.size()==0))
					parent.add(tableCollection);
			}
		}
		if (stringColl)
			oldParent.add(gxsyncline);
		// is Leaf if is a string collection
		return stringColl;
	}

	public JSONArray getRawJSONArray()
	{
		return jsonArr;
	}

	// toString of GxUnknownObjectCollection as client proc expect, table info + rows.
	public String toStringCollectionClientJsonString()
	{
		JSONArray jsonObj = new JSONArray();
		String result = "[";
		int size = size();
		if (size > 0)
		{
			GxUnknownObjectCollection tableCollection = (GxUnknownObjectCollection)super.get(0);
			int tableCollectionSize = tableCollection.size();
			jsonObj = new JSONArray();
			for (int j = 1; j <= tableCollectionSize; j++)
			{
				jsonObj.put((StringCollection)tableCollection.item(j));
			}
			result = result + jsonObj.toString() + "]";
		}
		for (int i = 1; i < size; i++)
		{
			result = result + ",[";
			GxUnknownObjectCollection tableCollection = (GxUnknownObjectCollection)super.get(i);
			int tableCollectionSize = tableCollection.size();
			jsonObj = new json.org.json.JSONArray();
			for (int j = 1; j <= tableCollectionSize; j++)
			{
				jsonObj.put((StringCollection)tableCollection.item(j));
			}
			result = result + jsonObj.toString() + "]";
		}
		result = result + "]";
		return result;
	}

	public GXMap toDictionary()
	{
		GXMap dic = new GXMap();
		int size = size();
		for (int i = 0; i < size; i++)
		{
			StringCollection strColl = (StringCollection)elementAt(i);
			dic.put(strColl.item(1).trim(), strColl.item(2));
		}
		return dic;
	}
	public boolean insertOrUpdate(){
		return false;
	}
	public boolean delete(){
		return false;
	}
	public boolean insert(){
		return false;
	}
	public boolean update(){
		return false;
	}


}
