package com.genexus.usercontrols;
import java.io.File;
import java.io.StringWriter;
import java.util.Date;
import com.github.mustachejava.*;

import com.genexus.ModelContext;
import com.genexus.util.GXMap;

public class UserControlGenerator 
{
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
			mustache = mf.compile(getTemplateFile(this.controlType));
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