package com.genexus.assistant;

import com.genexus.*;

public final  class Assistant extends GXProcedure
{
	public Assistant( int remoteHandle )
	{
		super( remoteHandle , new ModelContext( Assistant.class ), "" );
	}

	public void execute( String aP0 ,
						 String aP1 ,
						 String[] aP2 )
	{
		execute_int(aP0, aP1, aP2);
	}

	private void execute_int( String aP0 ,
							  String aP1 ,
							  String[] aP2 )
	{
		AV3Parameter1 = aP0;
		AV4Parameter2 = aP1;
		this.aP2 = aP2;
		privateExecute();
	}

	protected void privateExecute( )
	{
		Gxproperties = new com.genexus.util.GXProperties();
		Gxproperties.set("&Parameter1", AV3Parameter1);
		Gxproperties.set("&Parameter2", AV4Parameter2);
		Gxproperties.set("$context", "Los Angeles");
		AV5OutputVariable = callAssistant( "The weatherman", Gxproperties, null) ;
		cleanup();
	}

	protected void cleanup( )
	{
		this.aP2[0] = AV5OutputVariable;
	}

	public void initialize( )
	{
	}

	String AV3Parameter1 ;
	String AV4Parameter2 ;
	String AV5OutputVariable ;
	com.genexus.util.GXProperties Gxproperties ;
	String[] aP2 ;
}


