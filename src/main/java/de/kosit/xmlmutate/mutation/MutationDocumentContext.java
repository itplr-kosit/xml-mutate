// Generated by delombok at Fri Aug 13 16:07:39 CEST 2021
package de.kosit.xmlmutate.mutation;

import de.kosit.xmlmutate.runner.DocumentParser;
import de.kosit.xmlmutate.runner.SavingMode;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * Context of the Mutation including all data necessary to determine where in the document the mutation takes place.
 *
 * @author Andreas Penski
 */
public class MutationDocumentContext {
    private final Path documentPath;
    private final ProcessingInstruction pi;
    private final DocumentFragment originalFragment;

    private final Map<String, Set<String>> schematronFailures;
    private Node specificTarget;
    private SavingMode savingMode;

    /**
     * Constructor.
     *
     * @param pi the {@link ProcessingInstruction} linked to this context,
     * @param path name of the mutation
     */
    public MutationDocumentContext(@Nonnull final ProcessingInstruction pi, @Nonnull final Path path) {
        this(pi, path, SavingMode.SINGLE);
    }

    /**
     * Constructor.
     *
     * @param pi the {@link ProcessingInstruction} linked to this context,
     * @param path name of the mutation
     */
    public MutationDocumentContext(@Nonnull final ProcessingInstruction pi, @Nonnull final Path path, final SavingMode savingMode) {
        this(pi, path, savingMode, Map.of());
    }

    public MutationDocumentContext(@Nonnull final ProcessingInstruction pi, @Nonnull final Path path,
        final SavingMode savingMode, final Map<String, Set<String>> schematronFailures) {
        if (pi == null) {
            throw new java.lang.NullPointerException("pi is marked non-null but is null");
        }
        if (path == null) {
            throw new IllegalArgumentException("Valid path must be specified");
        }
        this.pi = pi;
        this.documentPath = path;
        this.originalFragment = createFragment();
        this.savingMode = savingMode;
        this.schematronFailures = schematronFailures;
    }

    /**
     * Returns the name of the document.
     *
     * @return the document name
     */
    public String getDocumentName() {
        return this.documentPath.getFileName().toString();
    }

    /*
     * Dies ist z.B. dann nötig, wenn das eigentlich Zielelement aus dem Dokument entfernt wird oder sich die Struktur
     * anderweitig ändert.
     */
    /**
     * Specific target DOM element.
     *
     * @param element the DOM node which is target of the mutation
     */
    public void setSpecificTarget(final Node element) {
        this.specificTarget = element;
    }

    /**
     * Calculate the line number of the MutationDocumentContext. Depending on the {@link Document} parsing, the line number might
     * not be availabe.
     *
     * @see DocumentParser
     * @return line number or -1 if not available
     */
    public int getLineNumber() {
        final Object userData = this.pi.getUserData(DocumentParser.LINE_NUMBER_KEY_NAME);
        return userData != null ? Integer.parseInt(userData.toString()) : -1;
    }

    /**
     * Gets nesting level of the {@link ProcessingInstruction}.
     *
     * @return Nesting Level
     */
    public int getLevel() {
        int level = 0;
        Node current = this.pi;
        while ((current = current.getParentNode()) != null) {
            level++;
        }
        return level;
    }

    /**
     * Gets the target Element. Default is the next sibling {@link Element} following this {@link ProcessingInstruction}.
     *
     * @return target node
     */
    public Node getTarget() {
        if (this.specificTarget == null) {
            return findTarget();
        }
        return this.specificTarget;
    }

    private Element findTarget() {
        Node sibling = this.pi;
        while ((sibling = sibling.getNextSibling()) != null) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) sibling;
            }
        }
        return null;
    }

    private DocumentFragment createFragment() {
        final DocumentFragment fragment = this.pi.getOwnerDocument().createDocumentFragment();
        final Node targetElement = getTarget();
        if (targetElement != null) {
            fragment.appendChild(targetElement.cloneNode(true));
            return fragment;
        }
        return null;
    }

    /**
     * Get the DOM Document.
     *
     * @return Document
     */
    public Document getDocument() {
        return this.pi.getOwnerDocument();
    }

    /**
     * Get parent Element. If the {@link ProcessingInstruction} is preceeding the Root Element, the parent will be null.
     *
     * @return parent Element
     */
    public Element getParentElement() {
        final Node parentNode = getPi().getParentNode();

        return parentNode instanceof Element element ? element : null;
    }

    /**
     * Creates a SHALLOW copy of this MutationDocumentContext
     *
     * @return a clone of this
     */
    public MutationDocumentContext cloneContext() {
        return new MutationDocumentContext(getPi(), getDocumentPath(), getSavingMode());
    }

    public Path getDocumentPath() {
        return this.documentPath;
    }

    public ProcessingInstruction getPi() {
        return this.pi;
    }

    public DocumentFragment getOriginalFragment() {
        return this.originalFragment;
    }

    public Node getSpecificTarget() {
        return this.specificTarget;
    }

    public SavingMode getSavingMode() {
        return this.savingMode;
    }

    public void setSavingMode(final SavingMode savingMode) {
        this.savingMode = savingMode;
    }

    public Map<String, Set<String>> getSchematronFailures() {
        return schematronFailures;
    }
}
