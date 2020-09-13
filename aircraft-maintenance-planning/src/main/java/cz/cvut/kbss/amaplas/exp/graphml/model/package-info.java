@XmlSchema(
        namespace="http://graphml.graphdrawing.org/xmlns",
        xmlns={
                @XmlNs(prefix="", namespaceURI = "http://graphml.graphdrawing.org/xmlns"),
                @XmlNs(prefix="y", namespaceURI="http://www.yworks.com/xml/graphml")
        }
        ,
        elementFormDefault= XmlNsForm.QUALIFIED
)
package cz.cvut.kbss.amaplas.exp.graphml.model;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;