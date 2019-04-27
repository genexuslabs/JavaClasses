/*
               File: SdtMessages_Message
        Description: Messages
             Author: GeneXus Java Generator version 15_0_0-93885
       Generated on: October 21, 2015 14:52:17.73
       Program type: Callable routine
          Main DBMS: sqlserver
 */
package com.genexus ;

import com.genexus.ModelContext;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.xml.GXXMLSerializable;
import com.genexus.xml.XMLReader;

public final  class SdtMessages_Message extends GXXMLSerializable implements Cloneable, java.io.Serializable
{
	public SdtMessages_Message( )
	{
		this(SpecificImplementation.Application.createModelContext(SdtMessages_Message.class));
	}

	public SdtMessages_Message( ModelContext context )
	{
		super( context, "SdtMessages_Message");
	}

	public SdtMessages_Message( int remoteHandle ,
			ModelContext context )
	{
		super( remoteHandle, context, "SdtMessages_Message");
	}

	public SdtMessages_Message( StructSdtMessages_Message struct )
	{
		this();
		setStruct(struct);
	}

	private static java.util.HashMap mapper = new java.util.HashMap();
	static
	{
	}

	public String getJsonMap( String value )
	{
		return (String) mapper.get(value);
	}

	public short readxml( XMLReader oReader ,
			String sName )
	{
		short GXSoapError = 1 ;
		sTagName = oReader.getName() ;
		if ( oReader.getIsSimple() == 0 )
		{
			GXSoapError = oReader.read() ;
			nOutParmCount = (short)(0) ;
			while ( ( ( CommonUtil.strcmp(oReader.getName(), sTagName) != 0 ) || ( oReader.getNodeType() == 1 ) ) && ( GXSoapError > 0 ) )
			{
				readOk = (short)(0) ;
				if ( CommonUtil.strcmp2( oReader.getLocalName(), "Id") )
				{
					gxTv_SdtMessages_Message_Id = (String) oReader.getValue() ;
					if ( GXSoapError > 0 )
					{
						readOk = (short)(1) ;
					}
					GXSoapError = oReader.read() ;
				}
				if ( CommonUtil.strcmp2( oReader.getLocalName(), "Type") )
				{
					gxTv_SdtMessages_Message_Type = (byte)(CommonUtil.lval( (String) oReader.getValue())) ;
					if ( GXSoapError > 0 )
					{
						readOk = (short)(1) ;
					}
					GXSoapError = oReader.read() ;
				}
				if ( CommonUtil.strcmp2( oReader.getLocalName(), "Description") )
				{
					gxTv_SdtMessages_Message_Description = (String) oReader.getValue() ;
					if ( GXSoapError > 0 )
					{
						readOk = (short)(1) ;
					}
					GXSoapError = oReader.read() ;
				}
				nOutParmCount = (short)(nOutParmCount+1) ;
				if ( readOk == 0 )
				{
					context.setSOAPErrMsg(context.getSOAPErrMsg() + "Error reading " + sTagName + CommonUtil.newLine( ) );
					context.setSOAPErrMsg(context.getSOAPErrMsg() + "Message: " + oReader.readRawXML()) ;
					GXSoapError = (short)(nOutParmCount*-1) ;
				}
			}
		}
		return GXSoapError ;
	}

	public void writexml( com.genexus.xml.XMLWriter oWriter ,
			String sName ,
			String sNameSpace )
	{
		writexml(oWriter, sName, sNameSpace, true);
	}

	public void writexml( com.genexus.xml.XMLWriter oWriter ,
			String sName ,
			String sNameSpace ,
			boolean sIncludeState )
	{
		if ( (CommonUtil.strcmp("", sName)==0) )
		{
			sName = "Message" ;
		}
		oWriter.writeStartElement(sName);
		if ( CommonUtil.strcmp(CommonUtil.left( sNameSpace, 10), "[*:nosend]") != 0 )
		{
			oWriter.writeAttribute("xmlns", sNameSpace);
		}
		else
		{
			sNameSpace = CommonUtil.right( sNameSpace, CommonUtil.len( sNameSpace)-10) ;
		}
		oWriter.writeElement("Id", CommonUtil.rtrim( gxTv_SdtMessages_Message_Id));
		if ( CommonUtil.strcmp(sNameSpace, "Genexus") != 0 )
		{
			oWriter.writeAttribute("xmlns", "Genexus");
		}
		oWriter.writeElement("Type", CommonUtil.trim( CommonUtil.str( gxTv_SdtMessages_Message_Type, 2, 0)));
		if ( CommonUtil.strcmp(sNameSpace, "Genexus") != 0 )
		{
			oWriter.writeAttribute("xmlns", "Genexus");
		}
		oWriter.writeElement("Description", CommonUtil.rtrim( gxTv_SdtMessages_Message_Description));
		if ( CommonUtil.strcmp(sNameSpace, "Genexus") != 0 )
		{
			oWriter.writeAttribute("xmlns", "Genexus");
		}
		oWriter.writeEndElement();
	}

	public void tojson( )
	{
		tojson( true) ;
	}

	public void tojson( boolean includeState )
	{
		AddObjectProperty("Id", gxTv_SdtMessages_Message_Id, false);
		AddObjectProperty("Type", gxTv_SdtMessages_Message_Type, false);
		AddObjectProperty("Description", gxTv_SdtMessages_Message_Description, false);
	}

	public String getgxTv_SdtMessages_Message_Id( )
	{
		return gxTv_SdtMessages_Message_Id ;
	}

	public void setgxTv_SdtMessages_Message_Id( String value )
	{
		gxTv_SdtMessages_Message_Id = value ;
	}

	public byte getgxTv_SdtMessages_Message_Type( )
	{
		return gxTv_SdtMessages_Message_Type ;
	}

	public void setgxTv_SdtMessages_Message_Type( byte value )
	{
		gxTv_SdtMessages_Message_Type = value ;
	}

	public String getgxTv_SdtMessages_Message_Description( )
	{
		return gxTv_SdtMessages_Message_Description ;
	}

	public void setgxTv_SdtMessages_Message_Description( String value )
	{
		gxTv_SdtMessages_Message_Description = value ;
	}

	public void initialize( int remoteHandle )
	{
		initialize( ) ;
	}

	public void initialize( )
	{
		gxTv_SdtMessages_Message_Id = "" ;
		gxTv_SdtMessages_Message_Description = "" ;
		sTagName = "" ;
	}

	public SdtMessages_Message Clone( )
	{
		return (SdtMessages_Message)(clone()) ;
	}

	public void setStruct( StructSdtMessages_Message struct )
	{
		setgxTv_SdtMessages_Message_Id(struct.getId());
		setgxTv_SdtMessages_Message_Type(struct.getType());
		setgxTv_SdtMessages_Message_Description(struct.getDescription());
	}

	public StructSdtMessages_Message getStruct( )
	{
		StructSdtMessages_Message struct = new StructSdtMessages_Message ();
		struct.setId(getgxTv_SdtMessages_Message_Id());
		struct.setType(getgxTv_SdtMessages_Message_Type());
		struct.setDescription(getgxTv_SdtMessages_Message_Description());
		return struct ;
	}


	public void sdttoentity( Object androidEntity )
   {
		SpecificImplementation.SdtMessages_Message.sdttoentity(this, androidEntity);
   }

   public void entitytosdt( Object androidEntity )
   {
	   SpecificImplementation.SdtMessages_Message.entitytosdt(androidEntity, this);
   }

	protected byte gxTv_SdtMessages_Message_Type ;
	protected short readOk ;
	protected short nOutParmCount ;
	protected String sTagName ;
	protected String gxTv_SdtMessages_Message_Id ;
	protected String gxTv_SdtMessages_Message_Description ;
}

