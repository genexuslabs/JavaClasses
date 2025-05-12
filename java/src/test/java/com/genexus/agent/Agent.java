package com.genexus.agent;

import com.genexus.*;
import com.genexus.util.CallResult;
import com.genexus.util.ChatResult;
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
			message.setStringContent("Dime el clima en Lima - Peru");
			messages.add(message);
			message = new OpenAIResponse.Message();
			message.setRole("assistant");
			message.setStringContent("El clima actual en Lima, Perú, es soleado con una temperatura de 20.9°C (69.6°F). La dirección del viento es del suroeste (SSW) a 15.1 km/h (9.4 mph), y la humedad relativa es del 68%. La presión atmosférica es de 1013 mb. La visibilidad es de 10 km y el índice UV es de 12.5.");
			messages.add(message);
			message = new OpenAIResponse.Message();
			message.setRole("user");
			message.setStringContent("Que me puedes contar de la ciudad que te pedi el clima previamente?");
			messages.add(message);
			AV5OutputVariable = callAgent( "The weatherman", Gxproperties, messages, new CallResult()) ;
		}
		else if (AV3Parameter1.equals("eval_image")) {
			OpenAIResponse.StructuredContent content = new OpenAIResponse.StructuredContent();
			ArrayList<OpenAIResponse.StructuredContentItem> items = new ArrayList<>();
			OpenAIResponse.StructuredContentItem contentItem = new OpenAIResponse.StructuredContentItem();
			contentItem.setType("text");
			contentItem.setText("De que se trata esta imagen");
			items.add(contentItem);
			OpenAIResponse.StructuredContentItem contentItem1 = new OpenAIResponse.StructuredContentItem();
			contentItem1.setType("image_url");
			OpenAIResponse.StructuredContentItem.ImageUrl imageURL = new OpenAIResponse.StructuredContentItem.ImageUrl();
			imageURL.setUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg");
			contentItem1.setImage_url(imageURL);
			items.add(contentItem1);
			content.setItems(items);
			OpenAIResponse.Message message = new OpenAIResponse.Message();
			message.setRole("user");
			message.setStructuredContent(content);
			messages.add(message);
			AV5OutputVariable = callAgent( "The weatherman", Gxproperties, messages, new CallResult()) ;
		}
		else if (AV3Parameter1.equals("chat_stream")) {
			OpenAIResponse.Message message = new OpenAIResponse.Message();
			message.setRole("user");
			message.setStringContent("Dime el clima en Lima - Peru");
			messages.add(message);
			message = new OpenAIResponse.Message();
			message.setRole("assistant");
			message.setStringContent("El clima actual en Lima, Perú, es soleado con una temperatura de 20.9°C (69.6°F). La dirección del viento es del suroeste (SSW) a 15.1 km/h (9.4 mph), y la humedad relativa es del 68%. La presión atmosférica es de 1013 mb. La visibilidad es de 10 km y el índice UV es de 12.5.");
			messages.add(message);
			message = new OpenAIResponse.Message();
			message.setRole("user");
			message.setStringContent("Que me puedes contar de la ciudad que te pedi el clima previamente?");
			messages.add(message);
			ChatResult chatResult = chatAgent( "The weatherman", Gxproperties, messages, new CallResult()) ;
			while (chatResult.hasMoreData()) {
				System.out.print(chatResult.hasMoreData());
			}
		}
		else if (AV3Parameter1.equals("toolcall")) {
			OpenAIResponse.Message message = new OpenAIResponse.Message();
			message.setRole("user");
			message.setStringContent("Necesito nombre y descripcion del producto 1779");
			messages.add(message);
			AV5OutputVariable = callAgent( "ProductInfo", Gxproperties, messages, new CallResult()) ;
			message = new OpenAIResponse.Message();
			message.setRole("assistant");
			message.setStringContent(AV5OutputVariable);
			messages.add(message);
			message = new OpenAIResponse.Message();
			message.setRole("user");
			message.setStringContent("Quiero que traduzcas la descripcion del producto que me habias enviado previamente");
			messages.add(message);
			AV5OutputVariable = callAgent( "ProductInfo", Gxproperties, messages, new CallResult()) ;
		}
		else if (AV3Parameter1.equals("toolcall_stream")) {
			OpenAIResponse.Message message = new OpenAIResponse.Message();
			message.setRole("user");
			message.setStringContent("Necesito nombre y descripcion del producto 1779");
			messages.add(message);
			ChatResult chatResult = chatAgent( "ProductInfo", Gxproperties, messages, new CallResult()) ;
			while (chatResult.hasMoreData()) {
				System.out.print(chatResult.hasMoreData());
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


