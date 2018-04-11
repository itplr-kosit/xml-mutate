package de.kosit.xmlmutate.mutator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * package de.kosit.xmlmutate.mutator;Mutator
 * 
 * 
 */
public interface Mutator {
    

    public String getName();

    public Document execute(Element context);
}