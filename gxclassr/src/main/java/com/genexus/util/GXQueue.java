package com.genexus.util;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.genexus.Application;
import com.genexus.ModelContext;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

public class GXQueue implements MessageListener{
  public final int ERROR = 1;
  public static final ILogger logger = LogManager.getLogger(GXQueue.class);

  private int errCode;
  private String errDescription = "";

  private static Hashtable<QueueSession, QueueSession> qsessions = new Hashtable<>();
  private static Hashtable<TopicSession, TopicSession> tsessions = new Hashtable<>();
  private static final String QUEUE  = "Queue";
  private static final String TOPIC = "Topic";

  private String provider;
  private String user;
  private String password;
  private boolean eof;
  private Enumeration messages;
  private String query;
  private boolean browse;
  private String type;
  private String durable;
  private String clientId;
  private String subscriptionName;
  private String driver;
  private String url;
  private String jndiID;
  private String queueName;
  private String queueAutoCommit;

  private javax.jms.Queue queue;
  private QueueSession qsession;
  private QueueConnection qconnection;
  private QueueReceiver reciver;

  private Topic topic;
  private TopicSession tsession;
  private TopicConnection tconnection;
  private TopicSubscriber suscriber;
  private Vector<GXQueueMessage> topicMessages = new Vector<>();

  public GXQueue() {
	Application.usingQueue = true;
    this.query = "";
    this.browse = false;
  }

  public void setProvider(String provider)
  {
    this.provider = provider;
  }

  public void setUser(String user)
  {
    this.user = user;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public void setQuerystring(String query)
  {
    this.query = query;
  }

  public void setBrowse(boolean browse)
  {
    this.browse = browse;
  }

  public boolean eof()
  {
      return eof;
  }

  public GXQueueMessage first()
  {
    if (browse && type.equals(QUEUE))
    {
      messages = null;
      GXQueueMessage result = new GXQueueMessage();
      browse();
      if (messages.hasMoreElements()) {
        Message message = (Message) messages.nextElement();
        if (message instanceof TextMessage)
        {
          TextMessage txtMessage = (TextMessage) message;
          try
          {
            result.setText(txtMessage.getText());
            result.setPriority(txtMessage.getJMSPriority());
            result.setCorrelationId(txtMessage.getJMSCorrelationID());
            result.setMessageID(txtMessage.getJMSMessageID());
            GXProperties msgProps = new GXProperties();
            Enumeration props = txtMessage.getPropertyNames();
            while (props.hasMoreElements())
            {
              String propName = (String)props.nextElement();
              String propValue = txtMessage.getStringProperty(propName);
              msgProps.add(propName, propValue);
            }
            result.setProperties(msgProps);
          }
          catch (JMSException e)
          {
            handleJMSException("GXQueue First method error: ", e);
            return null;
          }
        }
        eof = false;
        return result;
      }
      else
      {
        eof = true;
        return null;
      }
    }
    else
    {
      try {
        if (type.equals(QUEUE))
        {
          reciver = qsession.createReceiver(queue, query);
          qconnection.start();
        }
        return receive();
      }
      catch (JMSException e) {
        handleJMSException("GXQueue First method error: ", e);
        return null;
      }
    }
  }

  public GXQueueMessage next()
  {
    if (browse && type.equals(QUEUE))
    {
      GXQueueMessage result = new GXQueueMessage();
      if (messages.hasMoreElements()) {
        Message message = (Message) messages.nextElement();
        if (message instanceof TextMessage)
        {
          TextMessage txtMessage = (TextMessage) message;
          try
          {
            result.setText(txtMessage.getText());
            result.setPriority(txtMessage.getJMSPriority());
            result.setCorrelationId(txtMessage.getJMSCorrelationID());
			result.setMessageID(txtMessage.getJMSMessageID());
            GXProperties msgProps = new GXProperties();
            Enumeration props = txtMessage.getPropertyNames();
            while (props.hasMoreElements())
            {
              String propName = (String)props.nextElement();
              String propValue = txtMessage.getStringProperty(propName);
              msgProps.add(propName, propValue);
            }
            result.setProperties(msgProps);
          }
          catch (JMSException e)
          {
            handleJMSException("GXQueue Next method error: ", e);
            return null;
          }
        }
        eof = false;
        return result;
      }
      else
      {
        eof = true;
        return null;
      }
    }
    else
      return receive();
  }

  private boolean readProvider()
  {
	String configurationFile = "jms.xml";
	String path = ModelContext.getModelContext().getHttpContext().getDefaultPath();
	if (!path.equals(""))
	{
		char alternateSeparator = File.separatorChar == '/' ? '\\' : '/';
		path = path.replace(alternateSeparator, File.separatorChar) + File.separatorChar;
		if(new File(path + "WEB-INF" + File.separatorChar + "jms.xml").exists())
		{
			configurationFile = path + "WEB-INF" + File.separatorChar + "jms.xml";
		}
	}
    com.genexus.xml.XMLReader oReader = new com.genexus.xml.XMLReader();
    oReader.open(configurationFile);
    oReader.readType(1, "Name");
    while (!provider.trim().equals(oReader.getValue()))
    {
      oReader.readType(1, "Name");
      if (oReader.getEof())
      {
        oReader.close();
        return false;
      }
    }
        oReader.readType(1, "User");
        user = oReader.getValue();
        oReader.readType(1, "Password");
        password = oReader.getValue();
        oReader.readType(1, "Type");
        type =  oReader.getValue();
        short ok = oReader.readType(1, "Durable");
        if (ok == 1)
        {
        	durable =  oReader.getValue().toUpperCase();
        	oReader.readType(1, "ClientID");
        	clientId =  oReader.getValue();
        	oReader.readType(1, "SubscriptionName");
        	subscriptionName =  oReader.getValue();
        }
        else
        {
        	durable = "NO";
        }
        oReader.readType(1, "Factory");
        driver = oReader.getValue();
        oReader.readType(1, "URL");
        url = oReader.getValue();
        oReader.readType(1, "JNDI_ID");
        jndiID = oReader.getValue();
        oReader.readType(1, "Queue_Name");
        queueName = oReader.getValue();
        oReader.readType(1, "Queue_AutoCommit");
        queueAutoCommit = oReader.getValue().toUpperCase();
        oReader.close();
        return true;
  }

  public boolean connect()
  {
    if (!readProvider())
    {
      errCode = ERROR;
      errDescription = "GXQueue Connect method error: Provider doesn't exist";
      logger.error(errDescription);
      return false;
    }

    InitialContext ctx = null;
    qsession = null;
    tsession = null;

    Hashtable<String, String> env = new Hashtable<>();
    env.put(Context.SECURITY_PRINCIPAL, user);
    env.put(Context.SECURITY_CREDENTIALS, password);
    env.put(Context.PROVIDER_URL, url);
    env.put(Context.INITIAL_CONTEXT_FACTORY, driver);

    try
    {
      ctx = new InitialContext(env);
      if (type.equals(QUEUE))
      {
        QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup(jndiID);
        queue = (javax.jms.Queue)ctx.lookup(queueName);
        qconnection = factory.createQueueConnection();
        if (queueAutoCommit.equals("NO"))
        {
          qsession = qconnection.createQueueSession(true,
              qsession.AUTO_ACKNOWLEDGE);
          qsessions.put(qsession, qsession);
        }
        else
          qsession = qconnection.createQueueSession(false,
              qsession.AUTO_ACKNOWLEDGE);
      }
      else
      {
          TopicConnectionFactory tfactory = (TopicConnectionFactory) ctx.lookup(jndiID);
          topic = (Topic)ctx.lookup(queueName);
          tconnection = tfactory.createTopicConnection();
          if (!durable.equals("NO"))
          {
          	tconnection.setClientID(clientId);
          }
          if (queueAutoCommit.equals("NO"))
          {
            tsession = tconnection.createTopicSession(true,
                tsession.AUTO_ACKNOWLEDGE);
            tsessions.put(tsession, tsession);
          }
          else
            tsession = tconnection.createTopicSession(false,
                tsession.AUTO_ACKNOWLEDGE);
          if (durable.equals("NO"))
        	  suscriber = tsession.createSubscriber(topic, query, false);
          else
          {
        	  suscriber = tsession.createDurableSubscriber(topic, subscriptionName, query, false);
          }
          suscriber.setMessageListener(this);
          tconnection.start();
      }
    }
    catch (NamingException e)
    {
      handleJMSException("GXQueue Connect method error: ", e);
      return false;
    }
    catch (JMSException e)
    {
      handleJMSException("GXQueue Connect method error: ", e);
      return false;
    }

    return true;
  }

  public String send(GXQueueMessage qmessage)
  {
    TextMessage message;
    int index;
    int priority = qmessage.getPriority();
    String text = qmessage.getText();
    String correlationId = qmessage.getCorrelationId();
    String messageId = qmessage.getMessageID();
    GXProperties properties = qmessage.getProperties();
    try
    {
      if (type.equals(QUEUE))
      {
        QueueSender sender = qsession.createSender(queue);
        sender.setPriority(priority);
        message = qsession.createTextMessage();
        message.setText(text);
        message.setJMSCorrelationID(correlationId);
        message.setJMSMessageID(messageId);
        message.setText(text);
        if (properties != null)
        {
          index = 0;
          while (index < properties.count()) {
            message.setStringProperty(properties.item(index).name,
                                      properties.item(index).value);
            index++;
          }
        }
        sender.send(message);
      }
      else
      {
          TopicPublisher publisher = tsession.createPublisher(topic);
          message = tsession.createTextMessage();
          message.setText(text);
          message.setJMSPriority(priority);
		  message.setJMSCorrelationID(correlationId);
		  message.setJMSMessageID(messageId);
          if (properties != null)
          {
            index = 0;
            while (index < properties.count())
            {
              message.setStringProperty(properties.item(index).name, properties.item(index).value);
              index ++;
            }
          }
          publisher.publish(message);
      }
      return message.getJMSMessageID();
    }
    catch (JMSException e)
    {
      handleJMSException("GXQueue Send method error: ", e);
      return "";
    }
  }

  public GXQueueMessage receive()
  {
    Message message;
    TextMessage txtMessage;
    String strProperties;
    GXQueueMessage result = new GXQueueMessage();
    try
    {
      if (type.equals(QUEUE))
      {
        message= reciver.receiveNoWait();
        if (message != null)
        {
          if (message instanceof TextMessage)
          {
            txtMessage = (TextMessage) message;
            result.setText(txtMessage.getText());
            result.setPriority(txtMessage.getJMSPriority());
            result.setCorrelationId(txtMessage.getJMSCorrelationID());
            result.setMessageID(txtMessage.getJMSMessageID());
            GXProperties msgProps = new GXProperties();
            Enumeration props = txtMessage.getPropertyNames();
            while (props.hasMoreElements())
            {
              String propName = (String)props.nextElement();
              String propValue = txtMessage.getStringProperty(propName);
              msgProps.add(propName, propValue);
            }
            result.setProperties(msgProps);
          }
        }
        else
        {
          eof = true;
          return result;
        }
      }
      else
      {
          if (topicMessages.size() > 0)
          {
            result = topicMessages.elementAt(0);
            topicMessages.removeElementAt(0);
          }
          else
          {
            eof = true;
            return result;
          }
      }
      eof = false;
      return result;
    }
    catch (JMSException e)
    {
      handleJMSException("GXQueue Receive method error: ", e);
      return result;
    }
  }

  public void onMessage(Message message)
  {
    try
    {
      GXQueueMessage result = new GXQueueMessage();
      if (message instanceof TextMessage) {
        TextMessage txtMessage = (TextMessage) message;
        result.setText(txtMessage.getText());
        result.setPriority(txtMessage.getJMSPriority());
        result.setCorrelationId(txtMessage.getJMSCorrelationID());
        result.setMessageID(txtMessage.getJMSMessageID());
        GXProperties msgProps = new GXProperties();
        Enumeration props = txtMessage.getPropertyNames();
        while (props.hasMoreElements()) {
          String propName = (String) props.nextElement();
          String propValue = txtMessage.getStringProperty(propName);
          msgProps.add(propName, propValue);
        }
        result.setProperties(msgProps);
        topicMessages.addElement(result);
      }
    }
    catch (JMSException e)
    {
      handleJMSException("GXQueue OnMessage method error: ", e);
    }
  }


 public void browse()
  {
    try
    {
      if (type.equals(QUEUE))
      {
        QueueBrowser browser;
        browser = qsession.createBrowser(queue, query);
        qconnection.start();
        messages = browser.getEnumeration();
      }
    }
    catch (JMSException e)
    {
      handleJMSException("GXQueue Browse method error: ", e);
    }
  }

  public byte disconnect()
  {
    try
    {
      if (type.equals(QUEUE))
      {
        if (queueAutoCommit.equals("NO"))
          qsessions.remove(qsession);
        qsession.close();
        qconnection.close();
      }
      else
      {
        if (queueAutoCommit.equals("NO"))
          tsessions.remove(tsession);
        tsession.close();
        tconnection.close();
      }
    }
    catch (JMSException e)
    {
      handleJMSException("GXQueue Disconnect method error: ", e);
      return 0;
    }
    return 1;
  }

  static public void commitAll()
  {
    for(Enumeration enumera = qsessions.keys(); enumera.hasMoreElements();)
    {
      try
      {
        ( (QueueSession) enumera.nextElement()).commit();
      }
      catch (JMSException e)
      {
        logger.error("GXQueue CommitAll method error: ", e);
      }
    }

    for(Enumeration enumera = tsessions.keys(); enumera.hasMoreElements();)
    {
      try
      {
        ( (TopicSession) enumera.nextElement()).commit();
      }
      catch (JMSException e)
      {
        logger.error("GXQueue CommitAll method error: ", e);
      }
    }
  }

  static public void rollbackAll()
  {
    for(Enumeration enumera = qsessions.keys(); enumera.hasMoreElements();)
    {
      try
      {
        ( (QueueSession) enumera.nextElement()).rollback();
      }
      catch (JMSException e)
      {
        logger.error("GXQueue RollbackAll method error: ", e);
      }
    }

    for(Enumeration enumera = tsessions.keys(); enumera.hasMoreElements();)
    {
      try
      {
        ( (TopicSession) enumera.nextElement()).rollback();
      }
      catch (JMSException e)
      {
        logger.error("GXQueue RollbackAll method error: ", e);
      }
    }
  }

  public void commit()
  {
    if (queueAutoCommit.equals("NO"))
    {
      try {
        if (type.equals(QUEUE))
          qsession.commit();
        else
          tsession.commit();
      }
      catch (JMSException e) {
        handleJMSException("GXQueue Commit method error: ", e);
      }
    }
  }

  public void rollback()
  {
    if (queueAutoCommit.equals("NO"))
    {
      try {
        if (type.equals(QUEUE))
          qsession.rollback();
        else
          tsession.rollback();
      }
      catch (JMSException e) {
        handleJMSException("GXQueue Rollback method error: ", e);
      }
    }
  }

  private void handleJMSException(String methodName, Exception e)
  {
    errCode = ERROR;
    errDescription = e.getMessage();
    logger.error(methodName, e);
  }

  public short getErrCode()
  {
    return (short) errCode;
  }

  public String getErrDescription()
  {
    return errDescription;
  }
}
