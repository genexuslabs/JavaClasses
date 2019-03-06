package com.genexus.wrapper;

import org.simpleframework.xml.*;
import java.util.*;

@Root
public class GXCollectionWrapper<T> {

    @ElementList(inline = true)
    List<T> list;

    public GXCollectionWrapper() {
    }

    public GXCollectionWrapper(List<T> listItems) {
        list = listItems;
    }

    public GXCollectionWrapper(Object obj) {
        list = (List) obj;
    }
}
