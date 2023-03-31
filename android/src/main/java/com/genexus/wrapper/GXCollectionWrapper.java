package com.genexus.wrapper;

import java.util.List;

public class GXCollectionWrapper<T> {

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
