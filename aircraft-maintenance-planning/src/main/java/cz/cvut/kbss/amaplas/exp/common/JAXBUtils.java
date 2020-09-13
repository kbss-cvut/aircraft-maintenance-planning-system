package cz.cvut.kbss.amaplas.exp.common;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;

public class JAXBUtils {
    public static <T> T loadData(String filePath, Class<T> cls, String schemaPath, XmlPreprocessor preprocessor) throws IOException {
        try(InputStream is = new FileInputStream(filePath)) {
            return loadData(filePath, is, cls, schemaPath, preprocessor);
        }
    }

    public static <T> T loadData(String filePath, InputStream inputStream, Class<T> cls, String schemaPath, XmlPreprocessor preprocessor){
        JAXBContext jaxbContext;
        try
        {
            Document doc = parseDocumentUnsafe(filePath, inputStream, schemaPath);
            if(preprocessor != null){
                preprocessor.transform(doc);
            }

            jaxbContext = JAXBContext.newInstance(cls);

//            XMLEntityManager
//            Binder<Node> binder = jaxbContext.createBinder();

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

//            jaxbUnmarshaller.setListener(new Unmarshaller.Listener() {
//                @Override
//                public void beforeUnmarshal(Object target, Object parent) {
//                    super.beforeUnmarshal(target, parent);
//                }
//
//                @Override
//                public void afterUnmarshal(Object target, Object parent) {
//                    super.afterUnmarshal(target, parent);
//                }
//            });
            T ret = (T) jaxbUnmarshaller.unmarshal(doc.getDocumentElement());

            return ret;
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Document parseDocumentUnsafe(String filePath, InputStream inputStream, String schemaPath) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        setSchema(schemaPath, dbf, db);

        return db.parse(inputStream);
    }

    public static void setSchema(String schemaPath, DocumentBuilderFactory dbf, DocumentBuilder db){
        if(schemaPath != null){
            Schema schema = loadSchema(schemaPath);
            dbf.setSchema(schema);
        }else{
            db.setEntityResolver(
                    new EntityResolver() {
                        @Override
                        public InputSource resolveEntity(String publicId, String systemId)
                                throws SAXException, IOException {
                            if (systemId != null) {
                                return new InputSource(new StringReader(""));
                            } else {
                                return null;
                            }
                        }
                    }
            );
            dbf.setValidating(false);
        }
    }

    public static Schema loadSchema(String schemaFileName) {
        Schema schema = null;
        try {
            //// 1. Lookup a factory for the W3C XML Schema language
            String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
            SchemaFactory factory = SchemaFactory.newInstance(language);

            /*
             * 2. Compile the schema. Here the schema is loaded from a java.io.File, but
             * you could use a java.net.URL or a javax.xml.transform.Source instead.
             */
            schema = factory.newSchema(new File(schemaFileName));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return schema;
    }
}
