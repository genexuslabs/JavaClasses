package com.genexus;
import java.util.Vector;

public class GXBCLevelCollection<T extends GxSilentTrnSdt> extends GXBCCollection<T> 
{
	public GXBCLevelCollection()
	{
	}

	public GXBCLevelCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace)
	{
		this(elementsType, elementsName, containedXmlNamespace, (int)-1);
	}

	public GXBCLevelCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace, int remoteHandle)
	{
		this.elementsType = elementsType;
		this.elementsName = elementsName;
		xmlElementsName = elementsName;
		this.containedXmlNamespace = containedXmlNamespace;
		this.remoteHandle = remoteHandle;
	}
	public GXBCLevelCollection( Class<T> elementsType ,
			String elementsName ,
			String containedXmlNamespace ,
			Vector<T> data )
	{
		super(elementsType, elementsName, containedXmlNamespace, data);
	}

	public GXBCLevelCollection( Class<T> elementsType ,
			String elementsName ,
			String containedXmlNamespace ,
			Vector<T> data ,
			int remoteHandle )
	{
		super(elementsType, elementsName, containedXmlNamespace, data, remoteHandle);
	}

	public boolean add(T item )
	{
		return super.addElementTrn( item) ;
	}

	public byte removeItem( int idx )
	{
		return (byte)(super.removeElementTrn((double)idx)) ;
	}

	public void clear( )
	{
		int idx ;
		idx = this.getItemCount() ;
		while ( idx >= 0 )
		{
			this.removeItem( idx) ;//GXBaseCollection.remove
			idx = (int)(idx-1) ;
		}
	}
}
