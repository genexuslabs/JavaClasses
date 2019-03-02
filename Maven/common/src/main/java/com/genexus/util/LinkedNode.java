/*
  File: LinkedNode.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  11Jun1998  dl               Create public version
  25may2000  dl               Change class access to public
*/

package com.genexus.util;

/** A standard linked list node used in various queue classes **/
public class LinkedNode { 
  protected Object value;
  protected LinkedNode next = null;
  public LinkedNode(Object x) { value = x; }
  public LinkedNode(Object x, LinkedNode n) { value = x; next = n; }
}
