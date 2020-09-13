package cz.cvut.kbss.amaplas.exp.common;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;

public class ResourceUtils {

    public static String loadResource(String name){
        try {
            return IOUtils.toString(ResourceUtils.class.getResource(name), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
