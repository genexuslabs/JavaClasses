package com.genexus.xml;
import com.genexus.*;
import com.genexus.common.classes.AbstractGXFile;
import com.genexus.ModelContext;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.json.JSONArrayWrapper;
import com.genexus.json.JSONObjectWrapper;
import com.genexus.util.GXProperties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.internet.IGxJSONAble;
import com.genexus.internet.IGxJSONSerializable;

import static com.genexus.common.interfaces.SpecificImplementation.GXutil;


public abstract class GXXMLSerializable implements Cloneable, Serializable, IGxJSONAble, IGxJSONSerializable
{
	public GXXMLSerializable(ModelContext context, String type)
	{
		this(-1, context, type);
	}

	public GXXMLSerializable(int remoteHandle, ModelContext context, String type)
	{
		this.remoteHandle = remoteHandle;
		this.context = context;
		this.type = type;
		initialize();
	}

	private static final String GET_METHOD_NAME = "getgxTv_";
	private static final String SET_METHOD_NAME = "setgxTv_";
	private JSONObjectWrapper jsonObj = new JSONObjectWrapper();
	private JSONArrayWrapper jsonArr = new JSONArrayWrapper();
	protected boolean isArrayObject = false;
	protected String arrayItemName;
	protected String type;
	public static LocalUtil localUtil = new LocalUtil('.', "MDY", "24", 40, "eng");
	protected transient ModelContext context;
	protected transient int remoteHandle;
	protected String soapHeaderRaw;

	public void writexml(com.genexus.xml.XMLWriter Writer, String sName, String sNameSpace)
	{
		writexml(Writer, sName, sNameSpace, true);
	}
	public void writexml(com.genexus.xml.XMLWriter oWriter, String sName, String sNameSpace, boolean sIncludeState)
	{
		if ( CommonUtil.strcmp(CommonUtil.left( sNameSpace, 10), "[*:nosend]") == 0 )
		{
			sNameSpace = CommonUtil.right( sNameSpace, CommonUtil.len( sNameSpace)-10) ;
		}
		oWriter.writeRawText(toxml(false, sIncludeState, sName, sNameSpace));
	}
	public short readxml( XMLReader oReader, String sName ) {
		try
		{
			String xml = oReader.readRawXML();//UpdateNodeDefaultNamespace(oReader.readRawXML(), oReader.getNamespaceURI(), false);
			fromxml(xml, sName);
			return 1;
		}
		catch (Exception e)
		{
			System.err.println("readxml error: "+ e.toString());
			return -1;
		}
	}

	public abstract String getJsonMap( String value );

	public abstract void initialize();

	public GXProperties getStateAttributes( ){return null;}

	public short readxml(XMLReader reader)
	{
		return readxml(reader, "");
	}

	public byte isNull( )
	{
		return 0 ;
	}

	public Object clone()
	{
		try
		{
			return super.clone();
		}catch(CloneNotSupportedException e)
		{
			return null;
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

	public boolean isVisitorStrategy(boolean includeState){
		return false;
	}

	public String toxml(boolean includeHeader, boolean includeState, String header, String namespace)
	{
		XMLWriter oWriter = new XMLWriter();
		oWriter.openToString();
		if (includeHeader)
			oWriter.writeStartDocument("UTF-8");
		writexml(oWriter, header, namespace, includeState);
		oWriter.close();
		return oWriter.getResultingString() ;
	}
	public boolean fromxmlfile(AbstractGXFile xml, GXBaseCollection<SdtMessages_Message> messages, String collName)
	{
		return fromxml(xml.readAllText(""), messages, collName);
	}

	public boolean fromxmlfile(AbstractGXFile xml)
	{
		return fromxml(xml.readAllText(""));
	}

	public boolean fromxml(String sXML)
	{
		return fromxml(sXML, "");
	}

	public boolean fromxml(String sXML, String sName)
	{
		return fromxml(sXML, null, "");
	}
	public boolean fromxml(String sXML, GXBaseCollection<SdtMessages_Message> messages, String sName)
	{
		try {
			short nResult ;
			XMLReader oReader = new XMLReader();
			oReader.openFromString(sXML);
			oReader.read();
			nResult = readxml(oReader, sName) ;
			oReader.close();
			if (nResult <= 0) {
				CommonUtil.ErrorToMessages(String.valueOf(oReader.getErrCode()), oReader.getErrDescription(), messages);
				return false;
			}
			else
				return true;
		}
		catch (  Exception ex) {
			ex.printStackTrace();
			CommonUtil.ErrorToMessages("fromxml error", ex.getMessage(), messages);
			return false;
		}
	}
	public void tojson()
	{
	}

	public void tojson(boolean includeState)
	{
		tojson(); //compatibilidad para que funcione con fuentes de sdts que no se generen con la nueva version
	}

	public void tojson(boolean includeState, boolean includeNoInitialized)
	{
		tojson(); //compatibilidad para que funcione con fuentes de sdts que no se generen con la nueva version
	}

	public void AddObjectProperty(String name, int prop)
	{
		AddObjectProperty(name, new Integer(prop), true);
	}

	public void AddObjectProperty(String name, int prop, boolean includeState)
	{
		AddObjectProperty(name, new Integer(prop), includeState);
	}

	public void AddObjectProperty(String name, double prop)
	{
		AddObjectProperty(name, new Double(prop), true);
	}

	public void AddObjectProperty(String name, double prop, boolean includeState)
	{
		AddObjectProperty(name, new Double(prop), includeState);
	}

	public void AddObjectProperty(String name, short prop)
	{
		AddObjectProperty(name, new Short(prop), true);
	}

	public void AddObjectProperty(String name, short prop, boolean includeState)
	{
		AddObjectProperty(name, new Short(prop), includeState);
	}

	public void AddObjectProperty(String name, boolean prop)
	{
		AddObjectProperty(name, new Boolean(prop), true);
	}

	public void AddObjectProperty(String name, boolean prop, boolean includeState)
	{
		AddObjectProperty(name, new Boolean(prop), includeState);
	}

	public void AddObjectProperty(String name, Object prop)
	{
		AddObjectProperty(name, prop, true, true);
	}
	public void AddObjectProperty(String name, Object prop, boolean includeState)
	{
		AddObjectProperty(name, prop, includeState, true);
	}
	public void AddObjectProperty(String name, Object prop, boolean includeState, boolean includeNoInitialized)
	{
		try
		{
			if (prop instanceof IGxJSONAble)
			{
				if (prop instanceof GxSilentTrnSdt)
				{
					jsonObj.put(name, ((GxSilentTrnSdt)prop).GetJSONObject(includeState, includeNoInitialized));
				}
				else if (isArrayObject)
				{
					Object obj = ((IGxJSONAble)prop).GetJSONObject(includeState);
					if (obj instanceof JSONArrayWrapper)
						jsonArr = (JSONArrayWrapper)obj;
					else
						jsonArr = new JSONArrayWrapper((JSONArray)obj);
				}
				else
				{
					jsonObj.put(name, ((IGxJSONAble)prop).GetJSONObject(includeState));
				}
			}
			else
			{
				if (this instanceof GxSilentTrnSdt){
					if (includeNoInitialized || (!includeNoInitialized && IsDirty(name)))
					{
						jsonObj.put(name, prop);
					}
				}
				else{
					jsonObj.put(name, prop);
				}
			}
		}
		catch (JSONException e) {
		}
	}

	public boolean IsDirty(String fieldName){
		return false;
	}

	public Object GetJSONObject()
	{
		return GetJSONObject(true);
	}

	public Object GetJSONObject(boolean includeState)
	{
		return GetJSONObject(includeState, true);
	}
	public Object GetJSONObject(boolean includeState, boolean includeNoInitialized)
	{
		jsonObj = new JSONObjectWrapper();
		tojson(includeState, includeNoInitialized);
		if (isArrayObject)
		{
			return jsonArr;
		}
		else
		{
			return jsonObj;
		}
	}

	/*Recorre el iterador y pone los _N del campo al final asi no se pierden los valores null cuando se asigna el valor al campo*/
	private Iterator getFromJSONObjectOrderIterator(Iterator<Map.Entry<String, Object>> it)
	{
		java.util.Vector<String> v = new java.util.Vector<String>();
		java.util.Vector<String> vAtEnd = new java.util.Vector<String>();
		String name;
        while(it.hasNext())
        {
			/* Even though the map entry value is never used and the method could receive just the map's keys list,
				its entries set provides the keys in a specific order required by this method */
			Map.Entry<String, Object> entry = it.next();
        	name = entry.getKey();
	        String map = getJsonMap(name);
	        String className = CommonUtil.classNameNoPackage(this.getClass());
			Method getMethod = getMethod(GET_METHOD_NAME + className + "_" + (map != null? map : name));
        	if (name.endsWith("_N") || getMethod != null && getMethod.getName().startsWith("getgxTv_") && getMethod.isAnnotationPresent(GxUpload.class))
        	{
        		vAtEnd.add(name);
        	}
        	else
        	{
        		v.add(name);//Debe conservar el orden de los atributos que no terminan con _N.
        	}
        }
        if (vAtEnd.size()>0)
			v.addAll(vAtEnd);

        return v.iterator();
	}

	public void FromJSONObject(Object obj)
	{
		String className = CommonUtil.classNameNoPackage(this.getClass());
		String name;
		String map;
		Method setMethod;
		Method getMethod;
		Class<?> setClass;
		GXSimpleCollection currColl;
		if (isArrayObject)
		{
			map = getJsonMap(arrayItemName);
			setMethod = getMethod(SET_METHOD_NAME + className + "_" + (map != null? map : arrayItemName));
			getMethod = getMethod(GET_METHOD_NAME + className + "_" + (map != null? map : arrayItemName));
			if((setMethod != null) && (getMethod != null))
			{
				setClass = setMethod.getParameterTypes()[0];
				try
				{
					if(GXSimpleCollection.class.isAssignableFrom(setClass))
					{
						currColl = (GXSimpleCollection)getMethod.invoke(this, new Object[] {});
						currColl.clearCollection();
						collectionFromJSONArray((JSONArray)obj, currColl);
						setMethod.invoke(this, new Object[] { currColl });
					}
				}
				catch (java.lang.ClassCastException ex)
				{}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		else
		{
			if ((obj instanceof JSONObject) && !(obj instanceof JSONObjectWrapper))
				obj = new JSONObjectWrapper((JSONObject)obj);
			Iterator objIterator = getJSONObjectIterator((JSONObjectWrapper) obj);
			Iterator modifiedObjIterator = getFromJSONObjectOrderIterator(objIterator);
			while(modifiedObjIterator.hasNext())
			{
				name = (String)modifiedObjIterator.next();
				map = getJsonMap(name);
				setMethod = getMethod(SET_METHOD_NAME + className + "_" + (map != null? map : name));
				getMethod = getMethod(GET_METHOD_NAME + className + "_" + (map != null? map : name));
				if((setMethod != null) && (getMethod != null))
				{
					setClass = setMethod.getParameterTypes()[0];
					try
					{
						Object currObj = ((JSONObjectWrapper)obj).get(name);
						if(GXSimpleCollection.class.isAssignableFrom(setClass))
						{
							currColl = (GXSimpleCollection)getMethod.invoke(this, new Object[] {});
							currColl.clearCollection();
							if(currObj instanceof JSONArray)
							{
								collectionFromJSONArray((JSONArray)currObj, currColl);
								setMethod.invoke(this, new Object[] { currColl });
							}
						}
						else if(IGxJSONAble.class.isAssignableFrom(setClass))
						{
							IGxJSONAble innerObj = (IGxJSONAble)setClass.getConstructor(new Class[] { SpecificImplementation.Application.getModelContextClass() }).newInstance(new Object[] { context });
							innerObj.FromJSONObject(currObj);
							setMethod.invoke(this, new Object[] { innerObj });
						}
						else
						{
							if (getMethod.getName().startsWith("getgxTv_") && getMethod.isAnnotationPresent(GxUpload.class) && !((JSONObjectWrapper)obj).has(name + "_GXI") &&  currObj!=null )
							{
								if (currObj.getClass().equals(String.class) && ((String)currObj).startsWith(com.genexus.CommonUtil.FORMDATA_REFERENCE))
								{
									String varName = ((String)currObj).replace(com.genexus.CommonUtil.FORMDATA_REFERENCE, "");
									Method setMethod_blob = getMethod(SET_METHOD_NAME + className + "_" + name + "_setblob");
									currObj	= context.cgiGet(varName);
									if (setMethod_blob != null)
									{
										setMethod_blob.invoke(this, new Object[] { currObj, context.cgiGetFileName(varName), context.cgiGetFileType(varName)});
									}
								}
								else if (!currObj.equals(""))
								{
									Method setMethod_GXI = getMethod(SET_METHOD_NAME + className + "_" + name + "_gxi");
									if (setMethod_GXI!=null)
									{
										setMethod_GXI.invoke(this, new Object[] { "" });
									}
								}
							}
							if (setClass != null)
							{
								Object parmType = convertValueToParmType(currObj, setClass);
								if (parmType != null)
									setMethod.invoke(this, new Object[] { parmType });
							}
						}
					}
					catch (java.lang.ClassCastException ex)
					{}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}
	}

	private Iterator<Map.Entry<String, Object>> getJSONObjectIterator(JSONObjectWrapper obj) {
		if (SpecificImplementation.JsonSerialization == null)
			return obj.entrySet().iterator();

		return SpecificImplementation.JsonSerialization.getJSONObjectIterator(obj);
	}

        private Object convertValueToParmType(Object value, Class parmClass) throws Exception
        {
            if (parmClass.getName().equals("java.util.Date"))
            {
				if (value.toString().equals("null"))
					return null;
				String stringValue = value.toString();
				if (stringValue.contains("."))
					return GXutil.charToTimeREST(stringValue);
				else
					return localUtil.ctot(stringValue, 0);
            }
            return CommonUtil.convertObjectTo(value, parmClass);
        }

        private void collectionFromJSONArray(JSONArray jsonArray, GXSimpleCollection gxColl)
        {
            try
            {
                gxColl.clear();
                for(int i = 0; i < jsonArray.length(); i++)
                {
                    Object currObj = jsonArray.get(i);
					if ((currObj instanceof JSONObject) && !(currObj instanceof JSONObjectWrapper))
						currObj = new JSONObjectWrapper((JSONObject)currObj);
                    if(currObj instanceof JSONObjectWrapper || !gxColl.IsSimpleCollection())
                    {
                        Class<?> innerClass = gxColl.getElementsType();
						IGxJSONAble innerObj;
						if (GxSilentTrnSdt.class.isAssignableFrom(innerClass))
						{
							innerObj = (IGxJSONAble) innerClass.getConstructor(new Class[] {int.class}).newInstance(new Object[] {new Integer(-1)});
						}
						else
						{
							innerObj = (IGxJSONAble) innerClass.getConstructor(new Class[] {SpecificImplementation.Application.getModelContextClass()}).newInstance(new Object[] { context });
						}
                        innerObj.FromJSONObject(currObj);
                        gxColl.addBase(innerObj);
                    }
                    else
                    {
						if (gxColl.getElementsType() == java.math.BigDecimal.class)
							gxColl.addBase(DecimalUtil.stringToDec(jsonArray.get(i).toString()));
						else
                        	gxColl.addBase(currObj);
                    }
                }
            }
            catch (Exception ex) {ex.printStackTrace();}
        }

        // cache of methods for classes, inpruve perfomance, becuase each intance get all methods each time called.
        private static transient ConcurrentHashMap<String, ConcurrentHashMap<String, Method>> classesCacheMethods = new ConcurrentHashMap<>();
        // cache of methods names, inpruve perfomance.
        private static transient ConcurrentHashMap<String, String> toLowerCacheMethods = new ConcurrentHashMap<>();

        private transient ConcurrentHashMap<String, Method> classMethods;
        private Method getMethod(String methodName)
        {
        	String toLowerMethodName = (String)toLowerCacheMethods.get(methodName);
        	if (toLowerMethodName==null)
        	{
        		toLowerMethodName = methodName.toLowerCase();
        		toLowerCacheMethods.put(methodName, toLowerMethodName);
        	}
        	if (classMethods==null)
			{
        		Class thisClass = this.getClass();
        		//System.out.println("get methods from cache " + thisClass.getName());
        		classMethods = classesCacheMethods.get(thisClass.getName());
			}
			if (classMethods==null)
			{
				classMethods = new ConcurrentHashMap<>();
				Class thisClass = this.getClass();
				Method[] methods = thisClass.getMethods();
				for(int i=0; i<methods.length; i++)
				{
					classMethods.put(methods[i].getName().toLowerCase(),methods[i]);
				}
				//System.out.println("put methods in cache " + thisClass.getName());
				classesCacheMethods.put(thisClass.getName(), classMethods);
			}
            return classMethods.get(toLowerMethodName);
        }
	public String toJSonString()
	{
		return toJSonString(true);
	}

	public String toJSonString(boolean includeState, boolean includeNoInitialized)
	{
		return GetJSONObject(includeState, includeNoInitialized).toString();
	}

	public String toJSonString(boolean includeState)
	{
		return ToJavascriptSource(includeState);
	}

	public boolean fromJSonFile(AbstractGXFile xml, GXBaseCollection<SdtMessages_Message> messages)
	{
		return fromJSonString(xml.readAllText(""), messages);
	}

	public boolean fromJSonFile(AbstractGXFile xml)
	{
		return fromJSonString(xml.readAllText(""));
	}


	public boolean fromJSonString(String s)
	{
		return fromJSonString(s, null);
	}
	public boolean fromJSonString(String s, GXBaseCollection<SdtMessages_Message> messages)
	{
		try
		{
			jsonObj = new JSONObjectWrapper(s);
			FromJSONObject(jsonObj);
			return true;
		}
		catch (JSONException ex)
		{
			CommonUtil.ErrorToMessages("fromxml error", ex.getMessage(), messages);
			return false;
		}
		catch (Exception ex)
		{
			CommonUtil.ErrorToMessages("fromxml error", ex.getMessage(), messages);
			return false;
		}
	}
	public String ToJavascriptSource(boolean includeState)
	{
		return GetJSONObject(includeState).toString();
	}

	public String ToJavascriptSource()
	{
		return ToJavascriptSource(true);
	}
	protected void addExternalSoapHandler(String serviceName, Object objProvider)
	{
		if (SpecificImplementation.GXXMLSerializable != null)
			SpecificImplementation.GXXMLSerializable.addExternalSoapHandler(remoteHandle, context, serviceName, objProvider);
	}

	public void setSoapheadersraw(String soapHeaderRaw)
	{
		this.soapHeaderRaw = soapHeaderRaw;
	}
}
