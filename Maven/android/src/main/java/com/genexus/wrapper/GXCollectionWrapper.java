package com.genexus.wrapper;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

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
