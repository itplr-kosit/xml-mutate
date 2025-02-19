package de.kosit.xmlmutate.runner;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

/*
 * Basically a DocumentFragment guarantied to always have a parent node and all is cloned.
 * And a comment.
 */
public class DomFragment implements Fragment {

    private DocumentFragment fragment = null;
    private String comment = "";

    private DomFragment(){}

    public DomFragment(DocumentFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public Node getContentNode() {

        return this.fragment;
    }

    /**
     * @return the comment
     */
    @Override
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    @Override
    public void setComment(String comment) {
        this.comment = (comment == null) ? "" : comment;
    }

}
