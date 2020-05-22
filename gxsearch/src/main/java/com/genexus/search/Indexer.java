package com.genexus.search;


import java.io.*;
import java.util.Date;
import com.genexus.util.GXFile;
import com.genexus.GxSilentTrnSdt;
import com.genexus.IGxSilentTrn;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import java.util.Vector;


public class Indexer
{
	private static Indexer m_instance = null;;
	private ActionBuffer m_buffer = new ActionBuffer(Settings.getInstance().getMaxQueueSize());
    private Analyzer m_analyzer = CreateAnalyzer();
	private Thread m_workerThread;
	private Object m_lock = new Object();
	private IndexWorker worker;
	
	static
	{
		GXFile.CleanUp = new CleanUpFile();
	}
	
    public static Analyzer CreateAnalyzer()
    {
    	try{
			String analyzer = Settings.getInstance().getAnalyzer();
			if (analyzer==null || analyzer.equals("") || analyzer.equalsIgnoreCase(Settings.StandardAnalyzer))
			{
				if (Settings.getInstance().getStopWordsFile() == null)
					return new StandardAnalyzer();
				else 
					return new StandardAnalyzer(Settings.getInstance().getStopWordsFile());
			}
			else if (analyzer.equalsIgnoreCase(Settings.WhitespaceAnalyzer))
			{
				return new WhitespaceAnalyzer();
			}
			else if (analyzer.equalsIgnoreCase(Settings.StopAnalyzer))
			{
				return new StopAnalyzer();
			}
			else
			{
				if (Settings.getInstance().getStopWordsFile() == null)
					return new StopAnalyzer();
				else 
					return new StopAnalyzer(Settings.getInstance().getStopWordsFile());
			}
    	}catch(Exception ex){
    		return new StandardAnalyzer();
    	}
    }

    public static Indexer getInstance() {
		if (m_instance == null)
			m_instance = new Indexer();
		return m_instance; }
    
	private Indexer()
	{
		worker = new IndexWorker(m_buffer);
		m_workerThread = new Thread(worker);
		m_workerThread.start();
	}

	public boolean insertContent(Object obj, GXContentInfo contentInfo)
	{
		addAction(new Action(Action.INSERT, getIndexRecord(obj, contentInfo)));
		return true;
	}

	public boolean updateContent(Object obj, GXContentInfo contentInfo)
	{
		addAction(new Action(Action.UPDATE, getIndexRecord(obj, contentInfo)));
		return true;
	}


	public boolean removeContent(Object obj)
	{
		if (obj instanceof String)
			addAction(new Action(Action.DELETE, new IndexRecord((String)obj)));
		else
			addAction(new Action(Action.DELETE, getIndexRecord(obj, new GXContentInfo())));
		return true;
	}

	public boolean removeEntity(String entityType)
	{
		addAction(new Action(Action.DELETE, new IndexRecord(null, entityType, null, null, null, null)));
		return true;
	}
	
	private void addAction(Action action)
	{
		m_buffer.addAction(action);
		synchronized(m_lock)
		{
			if (!m_workerThread.isAlive())
			{				
				m_workerThread = new Thread(worker);
				m_workerThread.start();
			}
		}
	}

        private IndexRecord getIndexRecord(Object obj, GXContentInfo contentInfo)
        {
			if (contentInfo == null) contentInfo = new GXContentInfo();
            IndexRecord ir = new IndexRecord();
            if (obj instanceof GXFile)
            {
                GXFile file = (GXFile)obj;
                ir.setUri(file.getAbsoluteName());

                ir.setContent(DocumentHandler.getText(file.getAbsoluteName(), file.getExt()).toLowerCase());
                ir.setEntity(contentInfo.getType() == null ? file.getClass().getName() : contentInfo.getType());
                ir.setTitle(contentInfo.getTitle() == null ? file.getName() : contentInfo.getTitle());
                ir.setViewer(contentInfo.getViewer() == null ? file.getName() : contentInfo.getViewer());
				ir.setKeys(contentInfo.getKeys() == null || contentInfo.getKeys().size()==0 ? new Vector<String>() : contentInfo.getKeys());
            }
            else if (obj instanceof GxSilentTrnSdt)
            {
                IGxSilentTrn bc = ((GxSilentTrnSdt)obj).getTransaction();
                GXContentInfo info = bc.getContentInfo();
                ir.setUri(info.getId());
                ir.setContent(IndexRecord.processContent(bc.toString()));
                ir.setEntity(contentInfo.getType() == null ? info.getType() : contentInfo.getType());
                ir.setTitle(contentInfo.getTitle() == null ? info.getTitle() : contentInfo.getTitle());
                ir.setViewer(contentInfo.getViewer() == null ? info.getViewer() : contentInfo.getViewer());
				ir.setKeys(contentInfo.getKeys() == null || contentInfo.getKeys().size() == 0 ? info.getKeys() : contentInfo.getKeys());
            }
            else if (obj instanceof String)
            {
                ir.setUri(contentInfo.getId() == null ? "" : contentInfo.getId());
                ir.setContent(IndexRecord.processContent((String)obj) );
                ir.setEntity(contentInfo.getType() == null ? "" : contentInfo.getType());
                ir.setTitle(contentInfo.getTitle() == null ? "" : contentInfo.getTitle());
                ir.setViewer(contentInfo.getViewer() == null ? "" : contentInfo.getViewer());
				ir.setKeys(contentInfo.getKeys() == null || contentInfo.getKeys().size() == 0 ? new Vector<String>() : contentInfo.getKeys());
            }
            else
            {
                ir = null;
            }
            return ir;
        }

        public IndexWriter getWriter() {
            try {
                boolean create = true;
                String folder = Settings.getInstance().getIndexFolder();
                if (IndexReader.indexExists(folder))
                    create = false;

                return new IndexWriter(folder, this.m_analyzer, create);
            } catch (Exception ex) {
                return null;
            }
        }

        public static IndexReader getReader() {
            try {
                if (IndexReader.indexExists(Settings.getInstance().getIndexFolder())) {
                    return IndexReader.open(Settings.getInstance().getIndexFolder());
                }
            } catch (Exception ex) {

            }
            return null;
        }


	class IndexWorker implements Runnable
	{
		private ActionBuffer m_buffer;
		private int m_counter = 0;

		IndexWorker(ActionBuffer buffer)
		{
			this.m_buffer = buffer;
		}

		public void run()
		{
			int totsec = 0;
			while(totsec<3)
			{
				try
				{
					while(this.m_buffer.getCount() > 0)
					{
						index(this.m_buffer.getAction());
					}

					Thread.sleep(1000L);
					totsec=totsec+1;
				}
				catch(Exception ex)
				{
					System.err.println("IndexWorker error " +  ex.toString());
				}
			}
		}

		private void index(Action action)
		{
			switch(action.getActionType())
			{
				case Action.INSERT:
					insert(action.getRecord());
					break;
				case Action.DELETE:
					delete(action.getRecord());
					break;
				case Action.UPDATE:
					delete(action.getRecord());
					insert(action.getRecord());
					break;
			}
		}

		private void insert(IndexRecord record)
		{
			IndexWriter writer = Indexer.getInstance().getWriter();
			if(writer == null)
				return;

			try
			{
				Document doc = new Document();
				doc.add(new Field(IndexRecord.URIFIELD,record.getUri(),Field.Store.YES, Field.Index.UN_TOKENIZED));
				doc.add(new Field(IndexRecord.ENTITYFIELD,record.getEntity(),Field.Store.YES, Field.Index.UN_TOKENIZED));
				doc.add(new Field(IndexRecord.CONTENTFIELD,new StringReader(record.getContent())));
				doc.add(new Field(IndexRecord.TIMESTAMPFIELD, DateTools.dateToString(new Date(), DateTools.Resolution.MILLISECOND),Field.Store.YES,Field.Index.NO));
				doc.add(new Field(IndexRecord.VIEWERFIELD, record.getViewer(), Field.Store.YES, Field.Index.UN_TOKENIZED));
				doc.add(new Field(IndexRecord.TITLEFIELD, record.getTitle(), Field.Store.YES, Field.Index.UN_TOKENIZED));

				String[] keys = record.getKeys();
				for(int i=0;i<keys.length;i++)
				{
					doc.add(new Field(IndexRecord.KEYFIELDPREFIX+(i+1),keys[i],Field.Store.YES,Field.Index.NO));
				}
				writer.addDocument(doc);
				if(m_counter++ > Settings.getInstance().getOptimizeThreshold())
				{
					m_counter = 0;
					writer.optimize();
				}

			}catch(Exception ex)
			{

			}
			finally
			{
				try
				{
					writer.close();
					Searcher.getInstance().close();

				}catch(IOException ex)
				{

				}
			}
		}

		private void delete(IndexRecord record)
		{
			IndexReader reader = Indexer.getReader();
			if(reader == null)
				return;
			try
			{
				if (record.getUri() != null)
				{
					reader.deleteDocuments(new Term(IndexRecord.URIFIELD, record.getUri()));
				}
				else
				{
					reader.deleteDocuments(new Term(IndexRecord.ENTITYFIELD, record.getEntity()));
				}
			}
			catch(Exception ex)
			{
			}
			finally
			{
				try
				{
				reader.close();
				Searcher.getInstance().close();
				}
				catch(IOException ex)
				{
				}
			}
		}

	}
}
