package com.genexus.xml;

import com.genexus.util.GXProperties;

import org.simpleframework.xml.*;
import org.simpleframework.xml.strategy.*;
import org.simpleframework.xml.stream.*;
import org.simpleframework.xml.convert.*;

public class GXStateInterceptor implements Visitor {

    GXProperties stateAttributes;

    GXStateInterceptor(GXProperties stateAtts) {
        stateAttributes = stateAtts;
    }

    @Override
    public void read(Type field, NodeMap<InputNode> node) throws Exception {
    }

    @Override
    public void write(Type field, NodeMap<OutputNode> node) throws Exception {
        OutputNode opNode = node.getNode();
        if (stateAttributes != null && stateAttributes.containsKey(opNode.getName())) {
            opNode.remove();
        }
        opNode.getAttributes().remove("class");
    }
}
