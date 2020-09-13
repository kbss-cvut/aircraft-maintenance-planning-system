package cz.cvut.kbss.amaplas.exp.common;

import com.sun.org.apache.xml.internal.utils.PrefixResolverDefault;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XmlPreprocessor {
    protected XPathFactory xPathFactory = XPathFactory.newInstance();
    protected XPath x = xPathFactory.newXPath();


    public void transform(Document d){
        xpathNSAware(d);
        transformImpl(d);
    }

    protected void xpathNSAware(Document d){
        //see https://web.archive.org/web/20070328212209/http://blog.davber.com/2006/09/17/xpath-with-namespaces-in-java/
        PrefixResolverDefault prefixResolver = new PrefixResolverDefault(d.getDocumentElement());
        NamespaceContext namespaceContext = new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                return prefixResolver.getNamespaceForPrefix(prefix);
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return null;
            }
        };

//        NamespaceContext namespaceContext = new NamespaceContext() {
//            @Override
//            public String getNamespaceURI(String prefix) {
//                String uri = null;
//                switch (prefix){
//                    case "p1" : uri = "http://www.yworks.com/xml/graphml";break;
//                    case "" : uri = "http://graphml.graphdrawing.org/xmlns";
//                    default:
//                }
//                return uri;
//            }
//
//            @Override
//            public String getPrefix(String uri) {
//                String prefix = null;
//                switch (uri){
//                    case "http://www.yworks.com/xml/graphml" : prefix = "p1";break;
//                    case "http://graphml.graphdrawing.org/xmlns" : prefix = "";
//                    default:
//                }
//                return prefix;
//            }
//
//            @Override
//            public Iterator getPrefixes(String namespaceURI) {
//                return Arrays.asList("", "p1").iterator();
//            }
//        };
        x.setNamespaceContext(namespaceContext);
    }


    public void transformImpl(Document d){

    }


    public Double selectNumber(String xpath, Node root){
        try {
            return (Double) x.compile(xpath).evaluate(root, XPathConstants.NUMBER);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String selectString(String xpath, Node root){
        try {
            return (String) x.compile(xpath).evaluate(root, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean selectBoolean(String xpath, Node root){
        try {
            return (Boolean) x.compile(xpath).evaluate(root, XPathConstants.BOOLEAN);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Node selectSingle(String xpath, Node root){
        try {
            return (Node) x.compile(xpath).evaluate(root, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Node> selectList(String xpath, Node root){
        try {
            NodeList nl = (NodeList) x.compile(xpath).evaluate(root, XPathConstants.NODESET);
            List<Node> nodes = new ArrayList<>(nl.getLength());
            for(int i = 0; i < nl.getLength(); i++){
                nodes.add(nl.item(i));
            }
            return nodes;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

}
