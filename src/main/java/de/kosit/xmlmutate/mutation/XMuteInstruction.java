package de.kosit.xmlmutate.mutation;

import de.kosit.xmlmutate.expectation.ExpectedResult;
import de.kosit.xmlmutate.mutator.Mutator;
import de.kosit.xmlmutate.observation.OperationStatus;
import de.kosit.xmlmutate.observation.ParsingOperationStatus;
import de.kosit.xmlmutate.runner.DomFragment;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

public class XMuteInstruction {

    private static final Logger log = LoggerFactory.getLogger(XMuteInstruction.class);
    final OperationStatus status = new ParsingOperationStatus(OperationStatus.OK, "All good so far.");
    private final Properties props;
    private ProcessingInstruction pi = null;
    private boolean hasError = false;
    private Mutator mutator = null;
    private ExpectedResult schemaExpectation = ExpectedResult.UNDEFINED;


    public XMuteInstruction(ProcessingInstruction pi, Properties props) {
        this.pi = pi;
        this.props = props;
    }

    public String getProperty(String name) {
        if (props.containsKey(name)) {
            return this.props.getProperty(name);
        }
        return "";
    }

    public void addOperationStatus(OperationStatus status) {
        this.status.add(status);
    }

    public boolean hasError() {
        return this.status.isNotOK();
    }

    public void setMutator(Mutator m) {
        this.mutator = m;
    }

    public void setSchemaExpectation(ExpectedResult expectation) {
        if (expectation == null) {
            schemaExpectation = ExpectedResult.UNDEFINED;
        }
        schemaExpectation = expectation;
    }

    public ProcessingInstruction getPI() {
        return this.pi;
    }

    /**
     * 
     * @return A DomFragment with deep clones of the DOM Nodes
     */
    public DomFragment getTargetFragment() {
        Node sibling = this.pi;

        log.trace("Get target Element/Fragment of pi {}", sibling);

        DocumentFragment cloneFragment = pi.getOwnerDocument().createDocumentFragment();
        log.trace("Get target Element/Fragment of {}", sibling.getNodeName());
        while ((sibling = sibling.getNextSibling()) != null) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE) {
                log.trace("Create Clone Fragment with Element={}", sibling);
                cloneFragment.appendChild(sibling.cloneNode(true));

                return new DomFragment(cloneFragment);
            }
        }

        return new DomFragment(cloneFragment);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString() {
        return "XMuteInstruction [hasError=" + hasError + ", mutator=" + mutator.getPreferredName() + ", pi=" + pi
                + ", props=" + props
                + ", schemaExpectation=" + schemaExpectation + "]";
    }

}
