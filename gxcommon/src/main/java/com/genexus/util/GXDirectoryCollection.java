package com.genexus.util;

import java.util.Vector;

public class GXDirectoryCollection {
  private Vector<GXDirectory> vector = new Vector<>();

  public GXDirectoryCollection() {
  }

  public void add(GXDirectory file)
  {
    vector.addElement(file);
  }

  public GXDirectory item(int idx)
  {
          return vector.elementAt(idx - 1);
  }

  public int getItemCount()
  {
          return vector.size();
  }
}
