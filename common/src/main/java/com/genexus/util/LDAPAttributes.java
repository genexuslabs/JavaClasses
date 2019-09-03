package com.genexus.util;

import java.util.Vector;

public class LDAPAttributes {
  private Vector<LDAPAttribute> vector = new Vector<>();

  public LDAPAttributes() {
  }

  public void add(String name, String value)
  {
    LDAPAttribute ldapAttr = new LDAPAttribute();
    ldapAttr.name = name;
    ldapAttr.value = value;
    vector.add(ldapAttr);
  }

  public LDAPAttribute item(int i)
  {
    return vector.elementAt(i);
  }

  public int count()
  {
    return vector.size();
  }

  public void clear()
  {
    vector.removeAllElements();
  }

  class LDAPAttribute
  {
    public String name;
    public String value;
  }
}
