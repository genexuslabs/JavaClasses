package com.genexus.xml;


import org.simpleframework.xml.*;
import org.simpleframework.xml.strategy.*;
import org.simpleframework.xml.stream.*;
import org.simpleframework.xml.convert.*;

public class GXTypeInterceptor implements Visitor {

    static final String name = "item";
    String root;

    public GXTypeInterceptor(String rootName) {
        root = rootName;
    }

    @Override
    public void read(Type field, NodeMap<InputNode> node) throws Exception {
    }

    @Override
    public void write(Type field, NodeMap<OutputNode> node) throws Exception {
        OutputNode opNode = node.getNode();
        if (opNode.isRoot()) {
            opNode.setName(root);
        }else{
            opNode.setName(name);
        }
            
        opNode.getAttributes().remove("class");
    }
}
