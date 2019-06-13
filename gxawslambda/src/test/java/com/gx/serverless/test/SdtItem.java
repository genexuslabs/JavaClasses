/*
               File: SdtItem
        Description: Item
             Author: GeneXus Java Generator version 15_0_11-123400
       Generated on: July 19, 2018 16:10:34.49
       Program type: Callable routine
          Main DBMS: SQL Server
*/
package com.gx.serverless.test;
import com.gx.serverless.*;
import com.genexus.*;
import com.genexus.xml.*;

public final  class SdtItem extends GXXMLSerializable implements Cloneable, java.io.Serializable
{
   public SdtItem( )
   {
      this(  new ModelContext(SdtItem.class));
   }

   public SdtItem( ModelContext context )
   {
      super( context, "SdtItem");
   }

   public SdtItem( int remoteHandle ,
                   ModelContext context )
   {
      super( remoteHandle, context, "SdtItem");
   }

   public SdtItem( StructSdtItem struct )
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

   public short readxml( com.genexus.xml.XMLReader oReader ,
                         String sName )
   {
      short GXSoapError = 1 ;
      formatError = false ;
      sTagName = oReader.getName() ;
      if ( oReader.getIsSimple() == 0 )
      {
         GXSoapError = oReader.read() ;
         nOutParmCount = (short)(0) ;
         while ( ( ( GXutil.strcmp(oReader.getName(), sTagName) != 0 ) || ( oReader.getNodeType() == 1 ) ) && ( GXSoapError > 0 ) )
         {
            readOk = (short)(0) ;
            if ( GXutil.strcmp2( oReader.getLocalName(), "ItemId") )
            {
               if ( GXutil.notNumeric( oReader.getValue()) )
               {
                  formatError = true ;
               }
               gxTv_SdtItem_Itemid = (short)(GXutil.lval( oReader.getValue())) ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "ItemName") )
            {
               gxTv_SdtItem_Itemname = oReader.getValue() ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            nOutParmCount = (short)(nOutParmCount+1) ;
            if ( ( readOk == 0 ) || formatError )
            {
               context.globals.sSOAPErrMsg = context.globals.sSOAPErrMsg + "Error reading " + sTagName + GXutil.newLine( ) ;
               context.globals.sSOAPErrMsg = context.globals.sSOAPErrMsg + "Message: " + oReader.readRawXML() ;
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
      if ( (GXutil.strcmp("", sName)==0) )
      {
         sName = "Item" ;
      }
      if ( (GXutil.strcmp("", sNameSpace)==0) )
      {
         sNameSpace = "ServerlessBasicTest" ;
      }
      oWriter.writeStartElement(sName);
      if ( GXutil.strcmp(GXutil.left( sNameSpace, 10), "[*:nosend]") != 0 )
      {
         oWriter.writeAttribute("xmlns", sNameSpace);
      }
      else
      {
         sNameSpace = GXutil.right( sNameSpace, GXutil.len( sNameSpace)-10) ;
      }
      oWriter.writeElement("ItemId", GXutil.trim( GXutil.str( gxTv_SdtItem_Itemid, 4, 0)));
      if ( GXutil.strcmp(sNameSpace, "ServerlessBasicTest") != 0 )
      {
         oWriter.writeAttribute("xmlns", "ServerlessBasicTest");
      }
      oWriter.writeElement("ItemName", GXutil.rtrim( gxTv_SdtItem_Itemname));
      if ( GXutil.strcmp(sNameSpace, "ServerlessBasicTest") != 0 )
      {
         oWriter.writeAttribute("xmlns", "ServerlessBasicTest");
      }
      oWriter.writeEndElement();
   }

   public void tojson( )
   {
      tojson( true) ;
   }

   public void tojson( boolean includeState )
   {
      AddObjectProperty("ItemId", gxTv_SdtItem_Itemid, false);
      AddObjectProperty("ItemName", gxTv_SdtItem_Itemname, false);
   }

   public short getgxTv_SdtItem_Itemid( )
   {
      return gxTv_SdtItem_Itemid ;
   }

   public void setgxTv_SdtItem_Itemid( short value )
   {
      gxTv_SdtItem_Itemid = value ;
   }

   public String getgxTv_SdtItem_Itemname( )
   {
      return gxTv_SdtItem_Itemname ;
   }

   public void setgxTv_SdtItem_Itemname( String value )
   {
      gxTv_SdtItem_Itemname = value ;
   }

   public void initialize( int remoteHandle )
   {
      initialize( ) ;
   }

   public void initialize( )
   {
      gxTv_SdtItem_Itemname = "" ;
      sTagName = "" ;
   }

   public SdtItem Clone( )
   {
      return (SdtItem)(clone()) ;
   }

   public void setStruct( StructSdtItem struct )
   {
      setgxTv_SdtItem_Itemid(struct.getItemid());
      setgxTv_SdtItem_Itemname(struct.getItemname());
   }

   public StructSdtItem getStruct( )
   {
      StructSdtItem struct = new StructSdtItem();
      struct.setItemid(getgxTv_SdtItem_Itemid());
      struct.setItemname(getgxTv_SdtItem_Itemname());
      return struct ;
   }

   protected short gxTv_SdtItem_Itemid ;
   protected short readOk ;
   protected short nOutParmCount ;
   protected String gxTv_SdtItem_Itemname ;
   protected String sTagName ;
   protected boolean formatError ;
}

