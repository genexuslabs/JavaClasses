package com.genexus.agent;

import com.genexus.GXProcedure;
import com.genexus.ModelContext;
import com.genexus.util.CallResult;
import com.genexus.util.saia.OpenAIResponse;

import java.util.ArrayList;

public final  class Assistant extends GXProcedure
{
	public Assistant(int remoteHandle )
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
		ArrayList messages = new ArrayList();;
		if (AV3Parameter1.equals("chat")) {
			OpenAIResponse.Message message = new OpenAIResponse.Message();
			message.setRole("user");
			message.setContent("Dime el clima en Lima - Peru");
			messages.add(message);
			message = new OpenAIResponse.Message();
			message.setRole("assistant");
			message.setContent("El clima actual en Lima, Perú, es soleado con una temperatura de 20.9°C (69.6°F). La dirección del viento es del suroeste (SSW) a 15.1 km/h (9.4 mph), y la humedad relativa es del 68%. La presión atmosférica es de 1013 mb. La visibilidad es de 10 km y el índice UV es de 12.5.");
			messages.add(message);
			message = new OpenAIResponse.Message();
			message.setRole("user");
			message.setContent("Que me puedes contar de la ciudad que te pedi el clima previamente?");
			messages.add(message);
		}
		else {
			Gxproperties.set("&Parameter1", AV3Parameter1);
			Gxproperties.set("&Parameter2", AV4Parameter2);
			Gxproperties.set("$context", "Los Angeles");
		}
		AV5OutputVariable = callAgent( "The weatherman", Gxproperties, messages, new CallResult()) ;
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


