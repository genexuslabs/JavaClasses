package com.genexus.performance;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;

import com.genexus.Application;

public class DataStoreProviderInfo
{
	private long sentenceCount;
	private long sentenceSelectCount;
	private long sentenceUpdateCount;
	private long sentenceDeleteCount;
	private long sentenceInsertCount;
	private long sentenceCallCount;	
	private long sentenceDirectSQLCount;
	private Hashtable sentenceInfo = new Hashtable();		
	
	private String name;
	
  public DataStoreProviderInfo(String name)
  {
	  this.name = name;
  }
  
  public String getName()
  {
	  return name;
  }
    
  public long getSentenceCount()
  {
	  return sentenceCount;
  }
  
  public void incSentenceCount()
  {
	  sentenceCount ++;
  }
  
  public long getSentenceSelectCount()
  {
	  return sentenceSelectCount;
  }
  
  public void incSentenceSelectCount()
  {
	  sentenceSelectCount ++;
  }  
  
  public long getSentenceUpdateCount()
  {
	  return sentenceUpdateCount;
  }
  
  public void incSentenceUpdateCount()
  {
	  sentenceUpdateCount ++;
  }  
  
  public long getSentenceDeleteCount()
  {
	  return sentenceDeleteCount;
  }
  
  public void incSentenceDeleteCount()
  {
	  sentenceDeleteCount ++;
  }  
  
  public long getSentenceInsertCount()
  {
	  return sentenceInsertCount;
  }
  
  public void incSentenceInsertCount()
  {
	  sentenceInsertCount ++;
  }  
  
  public long getSentenceCallCount()
  {
	  return sentenceCallCount;
  }  
  
  public void incSentenceCallCount()
  {
	  sentenceCallCount ++;
  }  
  
  public long getSentenceDirectSQLCount()
  {
	  return sentenceDirectSQLCount;
  }
  
  public void incSentenceDirectSQLCount()
  {
	  sentenceDirectSQLCount ++;
  }  
	
  public void dump(PrintStream out)
  {
	  out.println("\tDataStoreProvider : " + name);
	  out.println("\tNumber of sentences : " + sentenceCount);
	  out.println("\tNumber of select sentences : " + sentenceSelectCount);
	  out.println("\tNumber of update sentences : " + sentenceUpdateCount);
	  out.println("\tNumber of delete sentences : " + sentenceDeleteCount);
	  out.println("\tNumber of insert sentences : " + sentenceInsertCount);
	  out.println("\tNumber of CALL sentences : " + sentenceCallCount);
	  out.println("\tNumber of direct SQL sentences : " + sentenceDirectSQLCount);
	  out.println("");
	  out.println("");
	  for (Enumeration en = sentenceInfo.elements(); en.hasMoreElements(); )
	  {
			SentenceInfo sInfo = (SentenceInfo) en.nextElement();
			sInfo.dump(out);
			out.println("");
			out.println("");			
	  }
  }
  
  public void dump(com.genexus.xml.XMLWriter writer)
  {
	  writer.writeStartElement("DataStoreProvider");
	  writer.writeAttribute("Name", name);
	  writer.writeElement("Total_SQLStatementCount", sentenceCount);
	  writer.writeElement("Select_SQLStatementCount", sentenceSelectCount);
	  writer.writeElement("Update_SQLStatementCount", sentenceUpdateCount);			
	  writer.writeElement("Delete_SQLStatementCount", sentenceDeleteCount);			
	  writer.writeElement("Insert_SQLStatementCount", sentenceInsertCount);
	  writer.writeElement("StoredProcedureCount", sentenceCallCount);		
	  writer.writeElement("SQLCommandCount", sentenceDirectSQLCount);	  
	  for (Enumeration en = sentenceInfo.elements(); en.hasMoreElements(); )
	  {
			SentenceInfo sInfo = (SentenceInfo) en.nextElement();
			sInfo.dump(writer);
	  }	  
	  writer.writeEndElement();
  }
  
  public SentenceInfo addSentenceInfo(String key, String sqlSentence)
  {
	  if (!sentenceInfo.containsKey(key))
	  {
		  SentenceInfo sInfo = new SentenceInfo(sqlSentence);
		  sentenceInfo.put(key, sInfo);
		  if (Application.isJMXEnabled())
		  {
				  SentenceJMX.CreateSentenceJMX(this, key);
		  }
	  }
	  return (SentenceInfo) sentenceInfo.get(key);
  }	
	
  public SentenceInfo getSentenceInfo(String key)
  {
	  return (SentenceInfo) sentenceInfo.get(key);
  }  
}
