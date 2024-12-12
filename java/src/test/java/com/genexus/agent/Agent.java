package com.genexus.agent;

import com.genexus.*;
import com.genexus.util.CallResult;
import com.genexus.util.saia.OpenAIResponse;
import java.util.ArrayList;

public final  class Agent extends GXProcedure
{
	public Agent(int remoteHandle )
	{
		super( remoteHandle , new ModelContext( Agent.class ), "" );
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
			AV5OutputVariable = callAgent( "The weatherman", Gxproperties, messages, new CallResult()) ;
		}
		else if (AV3Parameter1.equals("chat_stream")) {
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
			AV5OutputVariable = callAgent( "The weatherman", true, Gxproperties, messages, new CallResult()) ;
			System.out.print(AV5OutputVariable);
			while (!isStreamEOF()) {
				System.out.print(readChunk());
			}
		}
		else if (AV3Parameter1.equals("toolcall")) {
			OpenAIResponse.Message message = new OpenAIResponse.Message();
			message.setRole("user");
			message.setContent("Necesito nombre y descripcion del producto 1779");
			messages.add(message);
			AV5OutputVariable = callAgent( "ProductInfo", Gxproperties, messages, new CallResult()) ;
			message = new OpenAIResponse.Message();
			message.setRole("assistant");
			message.setContent(AV5OutputVariable);
			messages.add(message);
			message = new OpenAIResponse.Message();
			message.setRole("user");
			message.setContent("Quiero que traduzcas la descripcion del producto que me habias enviado previamente");
			messages.add(message);
			AV5OutputVariable = callAgent( "ProductInfo", Gxproperties, messages, new CallResult()) ;
		}
		else if (AV3Parameter1.equals("toolcall_stream")) {
			OpenAIResponse.Message message = new OpenAIResponse.Message();
			message.setRole("user");
			message.setContent("Necesito nombre y descripcion del producto 1779");
			messages.add(message);
			AV5OutputVariable = callAgent( "ProductInfo", true, Gxproperties, messages, new CallResult()) ;
			System.out.print(AV5OutputVariable);
			while (!isStreamEOF()) {
				System.out.print(readChunk());
			}
		}
		else {
			Gxproperties.set("&Parameter1", AV3Parameter1);
			Gxproperties.set("&Parameter2", AV4Parameter2);
			Gxproperties.set("$context", "Los Angeles");
			AV5OutputVariable = callAgent( "The weatherman", Gxproperties, messages, new CallResult()) ;
		}
		cleanup();
	}

	protected void cleanup( )
	{
		this.aP2[0] = AV5OutputVariable;
	}

	protected String callTool(String name, String arguments) {
		switch (name) {
			case "TranslateDescription":
				return "The Panavox Television 80 inches are wonderful";
			case "GetProductName":
				return "Televisor Panavox 80 pulgadas";
			case "GetProductDescription":
				return "Flor de Televisor el Panavox de 80 pulgadas";
			default:
				return String.format("Unknown function %s", name);
		}
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


