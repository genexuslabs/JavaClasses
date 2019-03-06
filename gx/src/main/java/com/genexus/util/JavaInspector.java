package com.genexus.util;

import java.lang.reflect.*;
import java.util.jar.*;
import java.io.*;
import com.genexus.xml.XMLWriter;

public class JavaInspector 
{
	public static void main(String args[])
	{
		new JavaInspector().execute(args);
	}
		
	private void execute(String args[])
	{	
		try 
		{
			XMLWriter writer = new XMLWriter();
			writer.setEncoding("UTF8");
			writer.xmlStart("JavaInspector.xml");
			writer.writeStartDocument("UTF-8");
            writer.writeStartElement("ArrayOfClassDefinition");
				writer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				writer.writeAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");			
			
				System.out.println("Inspection proccess started");
				if (args[0].toUpperCase().endsWith(".JAR") || args[0].toUpperCase().endsWith(".ZIP"))
				{
					processJarFile(writer, args[0]);
				}
				else
				{
					System.out.print("Inspecting " + args[0] + "...");
					printClass(writer, Class.forName(args[0]));
				}
				
			writer.writeEndElement();	
			writer.close();
			System.out.println("Inspection proccess succeeded");
         }
         catch (Throwable e) 
		 {
			 System.out.println("failed");
			 System.out.println("Error: " + e.toString());
			 System.out.println("Inspection proccess failed");
         }
	}
	
	private void processJarFile(XMLWriter writer, String jarFile) throws Throwable
	{
		System.out.println("Inspecting " + jarFile);
		JarInputStream jis = new JarInputStream(new FileInputStream(jarFile), true); 
		JarEntry je;
		while ((je = jis.getNextJarEntry()) != null) 
		{ 
			String classMain = je.getName();
			if(classMain.endsWith(".class"))
			{ 
				classMain = classMain.replace('/', '.').substring(0, classMain.length() -6);
				System.out.print("Inspecting " + classMain + "...");
				Class cl = Class.forName(classMain, false, this.getClass().getClassLoader());
				printClass(writer, cl);
			}
		}
	}
	
	private void printClass(XMLWriter writer, Class cls)
	{
		Package pkg = cls.getPackage();
		String className = cls.getName();
		if (pkg != null)
		{
			className = className.replaceFirst(pkg.getName() + ".", "");
		}		
		writer.writeStartElement("ClassDefinition");
			writer.writeElement("Name", className);
			if (pkg != null)
			{			
				writer.writeElement("PackageName", pkg.getName());
			}
			else
			{
				writer.writeElement("PackageName", "");
			}
			printConstructors(writer, cls);
			printAttributes(writer, cls);
			printMethods(writer, cls);			
		writer.writeEndElement();
		System.out.println("success");
	}
	
	private void printConstructors(XMLWriter writer, Class cls)
	{
		writer.writeStartElement("Constructors");
		Constructor constructor[] = cls.getConstructors();
		for (int i= 0; i < constructor.length; i++)
		{
			Constructor construct = constructor[i];
			writer.writeStartElement("Constructor");
				String defaultParametersString = buildDefaultParametersInit(construct.getParameterTypes());
				writer.writeElement("DefaultParametersString", defaultParametersString);
				printParameters(writer, construct.getParameterTypes());
			writer.writeEndElement();
		}
		writer.writeEndElement();					
	}
	
	private String buildDefaultParametersInit(Class pvec[])
	{
		String returnValue = "";
		for (int j = 0; j < pvec.length; j++)
		{
			if (pvec[j].isPrimitive())
			{
				returnValue += getPrimitiveDefaultValue(pvec[j].getName());
			}
			else
			{
				returnValue += "null";
			}
			if (j < pvec.length -1)
			{
				returnValue += " ,";
			}
		}
		return returnValue;
	}
	
	private String getPrimitiveDefaultValue(String value)
	{
		if (value.equals("char"))
		{
			return "";
		}
		else if (value.equals("byte") || value.equals("short") || value.equals("int") || value.equals("long") || value.equals("float") || value.equals("double"))
		{
			return "0";
		}
		else if (value.equals("boolean"))
		{
			return "false";
		}
		return "";
	}
	
	private void printAttributes(XMLWriter writer, Class cls)
	{
		writer.writeStartElement("Attributes");
			Field fieldlist[] = cls.getDeclaredFields();
			for (int i= 0; i < fieldlist.length; i++) 
			{
				Field fld = fieldlist[i];
				int mod = fld.getModifiers();
				if (Modifier.isPublic(mod) && !Modifier.isFinal(mod))
				{
					writer.writeStartElement("Attribute");
						writer.writeElement("Name", fld.getName());
						writer.writeStartElement("Type");
							this.printType(writer, fld.getType());				
						writer.writeEndElement();	
						writer.writeElement("AccessType", "member");					
					writer.writeEndElement();						
				}
			}
		writer.writeEndElement();
	}	
	
	private void printMethods(XMLWriter writer, Class cls)
	{
		Method methlist[] = cls.getDeclaredMethods();
		writer.writeStartElement("Methods");
		for (int i = 0; i < methlist.length;i++) 
		{
			Method m = methlist[i];
			int mod = m.getModifiers();
			if (Modifier.isPublic(mod))	
			{
				writer.writeStartElement("Method");
					writer.writeElement("Name", m.getName());
					if (Modifier.isStatic(mod))
					{
						writer.writeElement("IsStatic", "true");
					}
					else
					{
						writer.writeElement("IsStatic", "false");
					}
					printParameters(writer, m.getParameterTypes());
					writer.writeStartElement("ReturnType");
						printType(writer, m.getReturnType());
					writer.writeEndElement();					
				
					Class evec[] = m.getExceptionTypes();
					if (evec.length == 0)
					{
						writer.writeElement("Exceptions", "No");
					}
					else
					{
						writer.writeElement("Exceptions", "Yes");
					}
				
				writer.writeEndElement();				
			}
		}
		writer.writeEndElement();
	}
	
	private void printParameters(XMLWriter writer, Class pvec[])
	{
		writer.writeStartElement("Parameters");
		for (int j = 0; j < pvec.length; j++)
		{
			writer.writeStartElement("Parameter");
				writer.writeElement("Name", "param" + j);
				writer.writeStartElement("Type");
					boolean isArray = this.printType(writer, pvec[j]);				
				writer.writeEndElement();	
				if (isArray)
					writer.writeElement("InOut", "inout");
				else
					writer.writeElement("InOut", "in");
			writer.writeEndElement();
		}
		writer.writeEndElement();
	}
	
	private boolean printType(XMLWriter writer, Class classType)	
	{	
		boolean isArray = false;
		if (classType.getName().charAt(0) == '[')
			isArray = true;
		GXType gxt = new GXType(classType, isArray);		
		writer.writeElement("Name", classType.getName());
		writer.writeElement("Type", gxt.type);
		writer.writeElement("Length", gxt.length);
		writer.writeElement("Decimals", gxt.decimals);
		writer.writeElement("Sign", gxt.sign);
		writer.writeElement("PlatformType", gxt.platformType);
		writer.writeElement("IsCollection", gxt.isCollection);
		return isArray;
	}	
	
	class GXType
	{
		String name;
		String type;
		int length;
		int decimals;
		int sign;
		String isCollection;
		String platformType;		
		
		public GXType(Class returnType, boolean isArray)
		{
			isCollection = "false";
			if (!isArray)
				platformType = returnType.getName();
			else
			{
				platformType = getArrayType(returnType.getName());
			}
			if (returnType == Character.class || returnType.getName().equals("char") || returnType.getName().equals("[C"))
			{
				type = "char";
				length = 1;
				decimals = 0;
				sign = 0;
			}
			else if (returnType == String.class || returnType.getName().equals("[Ljava.lang.String;"))
			{
				type = "char";
				length = 100;
				decimals = 0;
				sign = 0;
			}
			else if (returnType == Byte.class || returnType.getName().equals("byte") || returnType.getName().equals("[B"))
			{
				type = "int";
				length = 2;
				decimals = 0;
				sign = 0;
			}
			else if (returnType == Short.class || returnType.getName().equals("short") || returnType.getName().equals("[S"))
			{
				type = "int";
				length = 4;
				decimals = 0;
				sign = 0;
			}
			else if (returnType == Integer.class || returnType.getName().equals("int") || returnType.getName().equals("[I"))
			{
				type = "int";
				length = 9;
				decimals = 0;
				sign = 0;
			}
			else if (returnType == Long.class || returnType.getName().equals("long") || returnType.getName().equals("[J"))
			{
				type = "int";
				length = 12;
				decimals = 0;
				sign = 0;
			}
			else if (returnType == Float.class || returnType.getName().equals("float") || returnType.getName().equals("[F"))
			{
				type = "int";
				length = 19;
				decimals = 8;
				sign = 0;
			}
			else if (returnType == Double.class || returnType.getName().equals("double") || returnType.getName().equals("[D"))
			{
				type = "int";
				length = 19;
				decimals = 8;
				sign = 0;
			}
			else if (returnType == java.math.BigDecimal.class || returnType.getName().equals("[Ljava.math.BigDecimal;"))
			{
				type = "int";
				length = 19;
				decimals = 8;
				sign = 0;
			}
			else if (returnType == java.util.Date.class || returnType.getName().equals("[Ljava.util.Date;"))
			{
				type = "dtime";
				length = 0;
				decimals = 0;
				sign = 0;
			}
			else if (returnType == Boolean.class || returnType.getName().equals("boolean") || returnType.getName().equals("[Z"))
			{
				type = "bool";
				length = 0;
				decimals = 0;
				sign = 0;
			}
			else if (returnType == java.util.Vector.class || returnType == java.util.ArrayList.class)
			{
				type = "o(object)";
				length = 0;
				decimals = 0;
				sign = 0;
				isCollection = "true";
			}
			else if (returnType == Void.class || returnType.getName().equals("void"))
			{
				type = "void";
				length = 0;
				decimals = 0;
				sign = 0;
			}
		}
		
		private String getArrayType(String returnType)
		{
			String arrayType;
			
			if (returnType.substring(1).equals("Z"))
				arrayType = "boolean";
			else if (returnType.substring(1).equals("B"))
				arrayType = "byte";
			else if (returnType.substring(1).equals("C"))
				arrayType = "char";			
			else if (returnType.substring(1).equals("D"))
				arrayType = "double";
			else if (returnType.substring(1).equals("F"))
				arrayType = "float";						
			else if (returnType.substring(1).equals("I"))
				arrayType = "int";			
			else if (returnType.substring(1).equals("J"))
				arrayType = "long";			
			else if (returnType.substring(1).equals("S"))
				arrayType = "short";			
			else
				arrayType = returnType.substring(2, returnType.length() -1);
			
			return arrayType;
		}
	}
}
