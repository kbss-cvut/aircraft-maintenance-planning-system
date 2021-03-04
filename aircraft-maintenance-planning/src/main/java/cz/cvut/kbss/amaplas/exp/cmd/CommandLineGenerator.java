package cz.cvut.kbss.amaplas.exp.cmd;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class CommandLineGenerator {
    public static <T> String generate(T t, String where, String[] args){

        return String.format("");
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        Process p = Runtime.getRuntime().exec("java -version");
        p.waitFor();
        IOUtils.copy(p.getInputStream(), System.out);
        IOUtils.copy(p.getErrorStream(), System.out);
        System.out.flush();
        System.out.println("end");

    }
}
