package com.genexus.db.driver;

import com.genexus.CommonUtil;
import java.util.Date;

public class JDBCLogConfig
{
	public static final int LEVEL_NAMESPACE  	= 0;
	public static final int LEVEL_DATASOURCE    = 1;
	public static final int LEVEL_CONNECTION 	= 2;

	String fileName;
	boolean enabled;
	boolean uniqueFileName;
	int detail; 
	boolean buffered;
	String path;
	int level;

	public JDBCLogConfig(	String fileName,
							boolean enabled,
							boolean uniqueFileName,
							int detail,
							boolean buffered,
							String path,
							int level)
	{
		this.fileName		= fileName;
		this.enabled		= enabled;
		this.uniqueFileName	= uniqueFileName;
		this.detail			= detail;
		this.buffered		= buffered;
		this.path			= path;
		this.level			= level;
	}

	public int getLevel()
	{
		return level;
	}

	public void setNamespaceName(String name)
	{
		this.namespaceName = name;
	}
	
	public void setDataSourceName(String name)
	{
		this.dataSourceName = name;
	}

	public void setConnectionName(String name)
	{
		this.connectionName = name;
	}

	private String namespaceName  = "";
	private String dataSourceName = "";
	private String connectionName = "";
	
	public String getFileName()
	{
		if	(uniqueFileName)
		{
			String name = "gx_" + CommonUtil.getMMDDHHMMSS(new Date());

			if	(level >= LEVEL_NAMESPACE )
			{
				name += "_" + namespaceName;
			}

			if	(level >= LEVEL_DATASOURCE)
			{
				name += "_" + dataSourceName;
			}

			if	(level >= LEVEL_CONNECTION && connectionName.length() > 0)
			{
				name += "_" + connectionName;
			}

			if	(path.trim().length() > 0)
			{
				name = CommonUtil.addLastPathSeparator(path) + name;
			}

			return name + ".log";
		}
		else
		{
			return fileName;
		}
	}
}

