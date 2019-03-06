package com.genexus.util;

import java.util.Vector;

import com.genexus.common.classes.AbstractGXFile;

public class GXFileCollection {
  private Vector<AbstractGXFile> vector = new Vector<AbstractGXFile>();

  public GXFileCollection() {
  }

  public void add(AbstractGXFile file)
  {
    vector.addElement(file);
  }

  public AbstractGXFile item(int idx)
  {
          Object o = vector.elementAt(idx - 1);
          return (AbstractGXFile) o;
  }

  public int getItemCount()
  {
          return vector.size();
  }
}
