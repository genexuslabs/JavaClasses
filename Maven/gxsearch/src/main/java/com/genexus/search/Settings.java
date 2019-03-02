package com.genexus.search;
import com.genexus.Preferences;
import java.io.*;
import com.genexus.ModelContext;


public class Settings
{
	private static Settings m_instance = new Settings();
	public static Settings getInstance(){return m_instance;}

	private String m_indexFolder;
	private String m_lockFolder;
	private File m_stopWords;
	private int m_optimizeThreshold = 500;
	private int m_maxQueueSize = 200;
	private String m_analyzer;
	
	public static final String WhitespaceAnalyzer = "WhitespaceAnalyzer";
	public static final String SimpleAnalyzer = "SimpleAnalyzer";
	public static final String StopAnalyzer = "StopAnalyzer";
	public static final String StandardAnalyzer = "StandardAnalyzer";

        private Settings(){

            m_indexFolder = Preferences.getDefaultPreferences().getLUCENE_INDEX_DIRECTORY();
            File index = new File(m_indexFolder);
            if (!index.isAbsolute() && !ModelContext.getModelContext().getHttpContext().getDefaultPath().equals("")){
                m_indexFolder = ModelContext.getModelContext().getHttpContext().getDefaultPath() + File.separator + m_indexFolder;
            }
            m_lockFolder = new File(m_indexFolder + File.separator + "Lock").getAbsolutePath();
            File dir = new File(m_lockFolder);
            if (!dir.exists()){
               dir.mkdirs();
            }
            
			String stopWords = "stopwords.txt";
			if(new File(ModelContext.getModelContext().getHttpContext().getDefaultPath() + File.separator + "WEB-INF").isDirectory())
			{
				stopWords = ModelContext.getModelContext().getHttpContext().getDefaultPath() + File.separator + "WEB-INF" + File.separator + stopWords;
			}

            File stopWordsFile = new File(stopWords);
            if (stopWordsFile.exists())
            {
                m_stopWords = stopWordsFile;
            }
				String maxQueueSize = Preferences.getDefaultPreferences().getINDEX_QUEUE_MAX_SIZE();
				try
				{
					if (maxQueueSize!=null && maxQueueSize.length()>0)
						m_maxQueueSize = Integer.parseInt(maxQueueSize);
				}catch (Exception e)
				{
					System.err.println("Error reading INDEX_QUEUE_MAX_SIZE:" + e.toString());
				}
			m_analyzer = Preferences.getDefaultPreferences().getLUCENE_ANALYZER();
        }

        public File getStopWordsFile(){
        
        	return m_stopWords;
        }
	public String getIndexFolder()
	{
		return this.m_indexFolder;
	}

	public String getLockFolder()
	{
		return this.m_lockFolder;
	}

	public String getAnalyzer()
	{
		return m_analyzer;
	}
	
	public int getOptimizeThreshold()
	{
		return this.m_optimizeThreshold;
	}
	public int getMaxQueueSize()
	{
		return this.m_maxQueueSize;
	}
	public void setIndexFolder(String folder)
	{
		this.m_indexFolder = folder;
	}

	public void setLockFolder(String folder)
	{
		this.m_lockFolder = folder;
	}

	public void setOptimizeThreshold(int threshold)
	{
		this.m_optimizeThreshold = threshold;
	}
}
