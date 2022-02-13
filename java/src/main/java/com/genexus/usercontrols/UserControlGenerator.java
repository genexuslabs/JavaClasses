package com.genexus.usercontrols;
import java.io.*;
import java.util.Date;

import com.genexus.ModelContext;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.util.GXMap;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public class UserControlGenerator 
{
	public static final ILogger logger = LogManager.getLogger(UserControlGenerator.class);
	private String controlType;
	private long lastRenderTime = 0;
	private Mustache mustache;
	
	public UserControlGenerator(String controlType)
	{
		this.controlType = controlType;
	}
	
	public String render(String internalName, GXMap propbag)
	{
		File template = new File(getTemplateFile(this.controlType));
		if (!template.exists())
			return "";

		if (getTemplateDateTime() > lastRenderTime)
		{
			MustacheFactory mf = new DefaultMustacheFactory();
			Reader reader = null;
			try
			{
				reader = new InputStreamReader(new FileInputStream(getTemplateFile(this.controlType)), "utf-8");
				mustache = mf.compile(reader, getTemplateFile(this.controlType));
			}
			catch (Exception e)
			{
				mustache = mf.compile(getTemplateFile(this.controlType));
			}
			finally
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					logger.error("Failed to render UserControl ", e);
					return "";
				}
			}
			lastRenderTime = new Date().getTime();
		}
		
		StringWriter stringWriter = new StringWriter();
		mustache.execute(stringWriter, propbag);
		return stringWriter.toString();
	}
	
	private String getTemplateFile(String controlType)
	{
		return ModelContext.getModelContext().getHttpContext().getDefaultPath() + File.separator + "WEB-INF" + File.separator + "gxusercontrols" + File.separator + controlType + ".view";
	}
	
	private long getTemplateDateTime()
	{
		return new File(getTemplateFile(this.controlType)).lastModified();
	}
}