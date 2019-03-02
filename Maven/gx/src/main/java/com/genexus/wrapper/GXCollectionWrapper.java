package com.genexus.wrapper;

import org.simpleframework.xml.*;
import java.util.*;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

@Root
@XmlRootElement
public class GXCollectionWrapper<T> {

    @ElementList(inline = true)
    @XmlAnyElement(lax = true)
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
