package com.genexus;
import com.genexus.ModelContext;
import com.genexus.common.interfaces.IPendingEventHelper;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.util.GXProperties;

import java.io.InputStream; 
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

import com.genexus.xml.GXXMLSerializer;

public class GxSilentTrnSdt extends com.genexus.xml.GXXMLSerializable
{
	static final ILogger logger = LogManager.getLogger(GxSilentTrnSdt.class);
	static final String SET_METHOD_PREFIX = "setgxTv_";
	static final String GET_METHOD_PREFIX = "getgxTv_";
	IPendingEventHelper pendingHelper;
	GXProperties dirties = new GXProperties();

	IGxSilentTrn trn;
	public GxSilentTrnSdt( ModelContext context, String type)
	{
		super( context, type);
	}

	public GxSilentTrnSdt( int remoteHandle, ModelContext context, String type)
	{
		super( remoteHandle, context, type);
	}

	public GxSilentTrnSdt()
	{
		super( SpecificImplementation.Application.getModelContext(), "");
	}

	public IGxSilentTrn getTransaction()
	{
		return trn;
	}

	public void setTransaction( IGxSilentTrn trn)
	{
		dirties.clear();
		this.trn = trn;
	}

	public void Save()
	{
		if (getTransaction() != null)
		{
		prePendingEvents();
			getTransaction().Save();
		postPendingEvents();
		}
	}

	public void Check()
	{
		if (getTransaction() != null)
			getTransaction().Check();
	}

	public void Delete()
	{
		if (getTransaction() != null)
		{
			getTransaction().SetMode("DLT");
			prePendingEvents();
			getTransaction().Save();
			postPendingEvents();
		}
	}

	private void prePendingEvents() 
	{
		if (SpecificImplementation.GXSilentTrnSdt != null)
		{
			pendingHelper = SpecificImplementation.GXSilentTrnSdt.CreatePendingEventHelper();
			if (pendingHelper != null)
				pendingHelper.prePendingEvents(this, trn);
		}
	}

	private void postPendingEvents() 
	{
	   if (pendingHelper!=null)
	   {
		   pendingHelper.postPendingEvents(this, trn);
	   }
	}

	public int Errors()
	{
		return  getTransaction().Errors();
	}

	public boolean Success()
	{
		if (getTransaction() == null)
			return false;
		return  (getTransaction().Errors() == 0);
	}

	public boolean Fail()
	{
		if (getTransaction() == null)
			return false;
		return  (getTransaction().Errors() == 1);
	}

	public String GetMode()
	{
		if (getTransaction() != null)
			return getTransaction().GetMode();
		return "";
	}

	public boolean fromxml(String sXML)
	{
		return fromxml( sXML, "" );
	}

	public boolean fromxml( String sXML, String sName )
	{
		return fromxml( sXML, null, "");
	}

	public boolean fromxml( String sXML, GXBaseCollection<SdtMessages_Message> messages, String sName )
	{
		try{
			if(SpecificImplementation.Application.getProperty("SIMPLE_XML_SUPPORT", "0").equals("1")) {
             	Class<?> me = getClass();
             	Object struct = me.getMethod("getStruct", new Class[]{}).invoke(this, (Object[]) null);
             	me.getMethod("setStruct", struct.getClass()).invoke(this, GXXMLSerializer.deserializeSimpleXml(struct, sXML));
				return true;
            }else{
				boolean result = super.fromxml(sXML, messages, sName);
				if (getTransaction() != null)
				{
					getTransaction().ReloadFromSDT();
				}
				return result;
			}
		}
		catch(Exception ex)
		{
			CommonUtil.ErrorToMessages("fromxml error", ex.getMessage(), messages);
			return false;
		}
	}

	public String getJsonMap(String value) {return null;}
	public void initialize() {}

	public GxSilentTrnSdt( int remoteHandle){ this(SpecificImplementation.Application.getModelContext(), ""); }
	public GXBaseCollection<SdtMessages_Message> GetMessages( )
	{
		short item = 1 ;
		GXBaseCollection<SdtMessages_Message> msgs = new GXBaseCollection<SdtMessages_Message>(SdtMessages_Message.class, "Messages.Message", "Genexus", remoteHandle) ;
		SdtMessages_Message m1 ;
		IGxSilentTrn trn = getTransaction() ;
		if ( trn != null )
		{
			com.genexus.internet.MsgList msgList = trn.GetMessages() ;
			while ( item <= msgList.getItemCount() )
			{
				m1 = new SdtMessages_Message(remoteHandle, context) ;
				m1.setgxTv_SdtMessages_Message_Id( msgList.getItemValue(item) );
				m1.setgxTv_SdtMessages_Message_Description( msgList.getItemText(item) );
				m1.setgxTv_SdtMessages_Message_Type( (byte)(msgList.getItemType(item)) );
				msgs.add(m1, 0);
				item = (short)(item+1) ;
			}
		}
		return msgs ;
	}
	
	@Override
	public boolean isVisitorStrategy(boolean includeState){
		return !includeState;
	}

	public void Load()
	{
		getTransaction().Load();
	}

	public void ForceCommitOnExit()
	{
		getTransaction().ForceCommitOnExit();
	}

	public boolean Insert()
	{
		boolean result = false;
		if (getTransaction() != null)
		{
			prePendingEvents();
			result = getTransaction().Insert();
			postPendingEvents();
		}
		return result;
	}

	public boolean Update()
	{
		boolean result = false;
		boolean isUpdate = GetMode().equalsIgnoreCase("UPD");
		if (getTransaction() != null)
		{
			if (isUpdate)
				prePendingEvents();
			result = getTransaction().Update();
			if (isUpdate)
				postPendingEvents();
		}
		return result;
	}

	public boolean InsertOrUpdate()
	{
		boolean result = false;
		if (getTransaction() != null)
		{
			if (SpecificImplementation.SupportPending)
			{
			//if android call insert and if fail call update, to work ok with pending events.
			result = Insert();
			if (!result)
			{
				// call Update, update start cleaning BC previous errors.
				result = Update();
			}
			}
			else
				result = getTransaction().InsertOrUpdate();
		}
		return result;
	}

	public void SetDirty(String fieldName)
	{
		dirties.put(fieldName.toLowerCase(), "true");
	}
	@Override
	public boolean IsDirty(String fieldName)
	{
		if (dirties.containsKey(fieldName.toLowerCase()))
			return true;
		return false;
	}

	public GXProperties getMetadata()
	{
		return new GXProperties();
	}
        
        public Object[][] GetBCKey()
        {
            return null;
        }
        
	public void setvalue(String name, String value)
	{
		try
		{
			Class<?> me = getClass();
			String methodName = SET_METHOD_PREFIX + me.getSimpleName() + "_" + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
			for(java.lang.reflect.Method method : me.getMethods())
			{
				if(method.getName().equals(methodName))
				{
					Class [] parmTypes = method.getParameterTypes();
					if(parmTypes.length == 1)
					{
						if(GXSimpleCollection.class.isAssignableFrom(parmTypes[0]))
						{
							String methodNameGet = GET_METHOD_PREFIX + me.getSimpleName() + "_" + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
							GXSimpleCollection col = (GXSimpleCollection)me.getMethod(methodNameGet, new Class[]{}).invoke(this, (Object[])null);
							col.removeAllItems();
							col.fromJSonString(value);
							return;
						}
						Object obj = CommonUtil.convertObjectTo(value, parmTypes[0], true);
						method.invoke(this, new Object[]{obj});
						return;
					}
				}
			}
		}catch(Exception e)
		{
		}
	}
	public void copy(GxSilentTrnSdt source)
	{
		try
		{
			Class<?> me = getClass();
			HashMap<String, Method> setMethods = new HashMap<>();
			HashMap<String, Method> getMethods = new HashMap<>();
			for(java.lang.reflect.Method method : me.getDeclaredMethods())
			{
				String methodName = method.getName();
				if(methodName.startsWith(SET_METHOD_PREFIX))
				{
					setMethods.put(methodName, method);
				}else if (methodName.startsWith(GET_METHOD_PREFIX)){
					getMethods.put(methodName, method);
				}
			}
			for(java.lang.reflect.Method setMethod : setMethods.values()) {
				String getMethod = setMethod.getName().replace(SET_METHOD_PREFIX, GET_METHOD_PREFIX);
				if (getMethods.containsKey(getMethod)) {
					Object value = getMethods.get(getMethod).invoke(source, (Object[]) null);
					setMethod.invoke(this, new Object[]{value});
				}
			}

		}catch(Exception e)
		{
			logger.fatal(e.getMessage(), e);
		}
	}

	public String getvalue(String name)
	{
		try
		{
			Class<?> me = getClass();
			String methodName = GET_METHOD_PREFIX + me.getSimpleName() + "_" + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

			Object obj = me.getMethod(methodName, new Class[]{}).invoke(this, (Object[])null);
			if(obj instanceof GXSimpleCollection)
			{
				return ((GXSimpleCollection)obj).toJSonString();
			}
			else return CommonUtil.convertObjectTo(obj, String.class, true).toString();
		}catch(Exception e)
		{
			return "";
		}
	}

	static String GxSdtNameToJavaName(String GxFullSdtName)
	{
		String[] names = GxFullSdtName.split("\\\\");
		StringBuilder qualifiedName = new StringBuilder();
		if (names.length > 1)
		{
			for (int i = 0; i < names.length - 1; i++)
			{
				qualifiedName.append(names[i].toLowerCase());
				if (i < names.length - 1)
					qualifiedName.append('.');
			}
		}
		qualifiedName.append("Sdt");
		qualifiedName.append(names[names.length - 1]);
		return qualifiedName.toString();
	}

	public static GxSilentTrnSdt create(String name, String namespace, ModelContext context, int remoteHandle)
	{
		String classPackage = SpecificImplementation.Application.getPACKAGE();

		if	(!classPackage.equals(""))
			classPackage += ".";

		String fullName = ((namespace == null || namespace.equals("")) ? "" : namespace + ".") + GxSdtNameToJavaName(name);
		if (namespace == null || namespace.equals(""))
		{
			fullName = classPackage + fullName;
		}
		try
		{
			Class<?> bcClass = Class.forName(fullName);
			Object bc = bcClass.getConstructor(new Class[] {int.class, SpecificImplementation.Application.getModelContextClass() }).newInstance(new Object[] {new Integer(remoteHandle), context});
			return (GxSilentTrnSdt)bc;
		}catch(Throwable e)
		{
			return new GxSilentTrnSdt(remoteHandle);
		}
	}

	@SuppressWarnings("unchecked")
	public static GXBCCollection createCollection(String name, String namespace, ModelContext context, int remoteHandle)
	{
		String classPackage = SpecificImplementation.Application.getPACKAGE();

		if	(!classPackage.equals(""))
			classPackage += ".";

		String itemFullName = ((namespace == null || namespace.equals("")) ? "" : namespace + ".") + GxSdtNameToJavaName(name);
		if (namespace == null || namespace.equals(""))
		{
			itemFullName = classPackage + itemFullName;
		}

		try
		{
			Class itemClass = Class.forName(itemFullName);
			GXBCCollection collection = new GXBCCollection(itemClass, name, namespace, new Integer(remoteHandle));
			return collection;
		}catch(Throwable e)
		{
			return null;
		}
	}
}
