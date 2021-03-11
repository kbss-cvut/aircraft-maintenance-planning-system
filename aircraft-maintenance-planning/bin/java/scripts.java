import java.io.*;
import java.util.Arrays;
import java.util.Properties;

public class scripts{

    public static String findRunCommand() throws IOException {
        return findRunCommand(".");
    }

    public static String findRunCommand(String from) throws IOException {
        File dir = new File(from).getCanonicalFile();
        while(dir != null && dir.exists()) {
            File pom = new File(dir, "pom.xml");
            File scriptsConfig = new File(dir, "scripts-config.properties");
            File run = null;
            if(!pom.exists() && scriptsConfig.exists()) {
                pom = getPomFromScriptConfigs(scriptsConfig);
            }

            if(pom.exists()) {
                run = new File(dir, "run.bat");
                if(!run.exists()){
                    File target = new File(pom.getParentFile(), "target");
                    if(target.exists())
                        run = new File(target, "run.bat");
                }
            }
            if(run != null)
                return run.getCanonicalFile().getParentFile().getPath();
            dir = dir.getParentFile();
        }
        return "";
    }

    public static File getPomFromScriptConfigs(File scriptsConfig){
        Properties props = readProperties(scriptsConfig);
        String dir = props.getProperty("scripts.root.dir");
        if(dir != null) {
            File pom = new File(dir, "pom.xml");
            if(pom.isAbsolute()){
                return pom;
            }else{
                return new File(new File(scriptsConfig.getParentFile(), dir), "pom.xml");
            }
        }
        return null;
    }

    public static Properties readProperties(File file){
        Properties p = new Properties();
        try(Reader r = new InputStreamReader(new FileInputStream(file))) {
            p.load(r);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    public static File find(File dir, String name){
        return Arrays.stream(dir.listFiles())
                .filter(f -> f.getName().equals(name))
                .findFirst()
                .orElse(null);

    }

    public static void setEnvVariable(){
        Process p = null;
        try {
//            p = Runtime.getRuntime().exec("set MY_VAR=100");
            p = Runtime.getRuntime().exec("help");
            p.waitFor();
            print(p.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void print(InputStream inputStream){
        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))){
            while(br.ready()){
                System.out.println(br.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
//        System.setProperty("myprop", "adsf");
        String out = findRunCommand();
        String path = System.getenv("path");
        if(path == null)
            System.out.print("exi");
        else if(path.contains(out)){
            System.out.print("__NOOP__");
            System.out.print(out);
        }else {

            System.out.print(out);
        }
        // DEBUG
//        System.err.println(System.getenv("path"));
//        System.out.println(out.length());

        System.out.flush();
    }
}