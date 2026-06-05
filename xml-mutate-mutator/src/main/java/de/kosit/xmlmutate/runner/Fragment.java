package de.kosit.xmlmutate.runner;

import org.w3c.dom.Node;

public interface Fragment {

    Node getContentNode();

    String getComment();

    void setComment(String comment);
}
