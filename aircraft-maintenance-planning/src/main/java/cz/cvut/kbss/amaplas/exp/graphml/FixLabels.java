package cz.cvut.kbss.amaplas.exp.graphml;

import cz.cvut.kbss.amaplas.exp.common.XmlPreprocessor;
import org.eclipse.persistence.internal.oxm.NamespaceResolver;
import org.eclipse.persistence.platform.xml.XMLNamespaceResolver;
import org.eclipse.persistence.platform.xml.jaxp.JAXPNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import java.util.Collection;
import java.util.List;

public class FixLabels extends XmlPreprocessor {

    public static final String TEXT_WRAPPER = "tmptxt";

    @Override
    public void transformImpl(Document d) {
        wrapNodes(
                d, "/:graphml/:graph/:node/:data/y:ShapeNode/y:NodeLabel[not(@hasText)]/text()",
                TEXT_WRAPPER);
        wrapNodes(d, "/:graphml/:graph/:edge/:data/y:PolyLineEdge/y:EdgeLabel/text()", TEXT_WRAPPER);
    }


    public void wrapNodes(Document d, String xpath, String wrapperLocalName){
        // select all nodes to be wrapped
        List<Node> nodes = selectList(xpath, d.getDocumentElement());
        wrapNodes(nodes, wrapperLocalName);
    }

    public void wrapNodes(Collection<Node> nodes, String wrapperLocalName){
        for(Node n : nodes) {
            wrapNode(n, wrapperLocalName);
        }
    }

    public void wrapNode(Node toBeWrapped, String wrapperLocalName){
        Document d = toBeWrapped.getOwnerDocument();
        Node parent = toBeWrapped.getParentNode();
        parent.removeChild(toBeWrapped);
        Node wrapper = d.createElementNS(parent.getNamespaceURI(), wrapperLocalName);
        wrapper.appendChild(toBeWrapped);
        parent.appendChild(wrapper);
    }
}
