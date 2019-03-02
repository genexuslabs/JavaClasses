package com.genexus.xml;


import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.*;
import org.simpleframework.xml.stream.*;
import org.simpleframework.xml.convert.*;

public class GXSDTInterceptor implements Visitor {

    String root;
    String name;
    String namespace;

    public GXSDTInterceptor(String rootName, String elementName, String containedXmlNamespace) {
        root = rootName;
        name = elementName;
        namespace = containedXmlNamespace;
    }

    @Override
    public void read(Type field, NodeMap<InputNode> node) throws Exception {
    }

    @Override
    public void write(Type field, NodeMap<OutputNode> node) throws Exception {
        OutputNode opNode = node.getNode();

        if (opNode.isRoot()) {
            opNode.setName(root);
            opNode.getNamespaces().setReference(namespace);
        } else if (opNode.getParent().isRoot()) {
            opNode.setName(name);
        }

        opNode.getAttributes().remove("class");
    }
}
