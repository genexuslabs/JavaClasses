package com.genexus.util;

import com.genexus.ModelContext;
import com.genexus.util.*;
import com.genexus.Application;
import com.genexus.internet.*;
import java.util.*;
import java.io.File;
import javax.naming.*;
import javax.jms.*;

public class GXQueue implements MessageListener{
  private static Hashtable qsessions = new Hashtable();
  private static Hashtable tsessions = new Hashtable();
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
  private Vector topicMessages = new Vector();

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

  public void setBrowse(byte browse)
  {
    if (browse == 0)
      this.browse = false;
    else
      this.browse = true;
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
            System.err.println(e);
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
        System.err.println(e);
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
            System.err.println(e);
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

  public byte connect()
  {
    if (!readProvider())
    {
      System.err.println("Provider doesn't exist");
      return 0;
    }

    InitialContext ctx = null;
    qsession = null;
    tsession = null;

    Hashtable env = new Hashtable();
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
      System.err.println(e);
      return 0;
    }
    catch (JMSException e)
    {
      System.err.println(e);
      return 0;
    }

    return 1;
  }

  public String send(GXQueueMessage qmessage)
  {
    TextMessage message;
    int index;
    int priority = qmessage.getPriority();
    String text = qmessage.getText();
    GXProperties properties = qmessage.getProperties();
    try
    {
      if (type.equals(QUEUE))
      {
        QueueSender sender = qsession.createSender(queue);
        sender.setPriority(priority);
        message = qsession.createTextMessage();
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
      System.err.println(e);
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
        message= reciver.receive(1);
        if (message != null)
        {
          if (message instanceof TextMessage)
          {
            txtMessage = (TextMessage) message;
            result.setText(txtMessage.getText());
            result.setPriority(txtMessage.getJMSPriority());
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
            result = (GXQueueMessage) topicMessages.elementAt(0);
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
      System.err.println(e);
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
        System.err.println(e);
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
      System.err.println(e);
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
      System.err.println(e);
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
        System.err.println(e);
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
        System.err.println(e);
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
        System.err.println(e);
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
        System.err.println(e);
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
        System.err.println(e);
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
        System.err.println(e);
      }
    }
  }
}
