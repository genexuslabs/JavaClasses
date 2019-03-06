package com.genexus.util;

import java.util.Vector;

public class GXDirectoryCollection {
  private Vector vector = new Vector();

  public GXDirectoryCollection() {
  }

  public void add(GXDirectory file)
  {
    vector.addElement(file);
  }

  public GXDirectory item(int idx)
  {
          Object o = vector.elementAt(idx - 1);
          return (GXDirectory) o;
  }

  public int getItemCount()
  {
          return vector.size();
  }
}
