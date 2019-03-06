package com.genexus.util;

public class GXQueueMessage {

  String text;
  int priority;
  GXProperties properties;

  public GXQueueMessage() {
    priority = 4;
  }

  public String getText()
  {
    return text;
  }

  public void setText(String text)
  {
    this.text = text;
  }

  public int getPriority()
  {
    return priority;
  }

  public void setPriority(int priority)
  {
      this.priority = priority;
  }

  public GXProperties getProperties()
  {
    return properties;
  }

  public void setProperties(GXProperties properties)
  {
    this.properties = properties;
  }

}
