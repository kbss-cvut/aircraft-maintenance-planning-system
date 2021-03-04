package cz.cvut.kbss.amaplas.exp.cmd;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;

public class GenerateRunScript {
    private static final Logger LOG = LoggerFactory.getLogger(GenerateRunScript.class);

    public static long creationTime(File f){
        try{
            return Files.readAttributes(f.toPath(), BasicFileAttributes.class).creationTime().toMillis();
        }catch (IOException e){
            LOG.error("Error reading file attributes of file \"{}\"", f.getAbsolutePath());
        }
        return Long.MAX_VALUE;
    }

    public static File findJarFile() throws IOException{
        File target = new File("./target");
        if (!target.exists()) {
            LOG.error("Aborting script generation. Cannot find target folder, \"{}\" does not exist",
                    target.getCanonicalPath());
            return null;
        }


        long time = System.currentTimeMillis();
        return Arrays.stream(target.listFiles())
                .filter(f -> f.getName().endsWith("jar"))
                .sorted(Comparator.comparing(f -> time - creationTime(f)))
                .findFirst().orElse(null);
    }

    public static String readClassPath(){
        String classpathFile = "./target/classpath.txt";
        try (Reader r = new FileReader(classpathFile)) {
            return IOUtils.toString(r);
        } catch (FileNotFoundException e) {
            LOG.error("Could not find classpath file \"{}\"", classpathFile, e);
        } catch (IOException e) {
            LOG.error("Error reading Classpath from file \"{}\"", classpathFile, e);
        }
        return null;
    }

    /**
     * This method fails because the mvn command is not recognized when executed via Runtime.getRuntime.exec(...)
     * @return
     */
    @Deprecated
    public static String buildClassPath(){
        String fileName = "tmp.cls";
        try {
            // build the classpath
            Process p = Runtime.getRuntime().exec("mvn dependency:build-classpath -Dmdep.outputFile=tmp.cls");
            p.waitFor();
            // get the class path
            String classpath = IOUtils.toString(new File("./tmp.cls").toURI(), "UTF-8");
            new File("tmp.cls").delete();
            return classpath;
        } catch (IOException e) {
            LOG.error("could not build classpath");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        String jarName = null;
        if(args.length > 0)
            jarName = args[0];

        File jarFile = (jarName == null) ?
                findJarFile() :
                new File("./target", jarName);

        // build calsspath
        String classpath = readClassPath();

        if(jarFile == null || classpath == null)
            return;
        // add jar file to classpath
        classpath = jarFile.getCanonicalPath() + ";" + classpath;
        // build java command
        String cmd = String.format(
                "@echo off\n" +
                "java -classpath %s %s\n" +
                "@echo off", classpath, "%*");

        File cmdFile = new File("./target", "run.bat");
        try(Writer w = new FileWriter(cmdFile)) {
            IOUtils.write(cmd, w);
        }
//        System.out.println(root.getCanonicalPath());
    }

}
