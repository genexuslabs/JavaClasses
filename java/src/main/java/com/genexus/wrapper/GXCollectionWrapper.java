package com.genexus.wrapper;

import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

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

	@SuppressWarnings("unchecked")
    public GXCollectionWrapper(Object obj) {
        list = (List) obj;
    }
}
