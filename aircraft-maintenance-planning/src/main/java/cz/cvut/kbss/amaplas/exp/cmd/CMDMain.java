package cz.cvut.kbss.amaplas.exp.cmd;

import cz.cvut.kbss.amaplas.exp.graphml.Graphml2Owl;
import org.apache.commons.io.IOUtils;
//import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Stream;

public class CMDMain {
    public static Class getCommand(String name) throws IOException {
//        Path p = new Path;
//        p.
//        CMDMain.class.getClassLoader().getResources("/");
        String fileName = name + ".class";
        ClassLoader cl = CMDMain.class.getClassLoader();
//        InputStream is = CMDMain.class.getResource("/").openStream();
//        String content = IOUtils.toString(is,"UTF-8");
        Stack<String> stack = new Stack<>();
        Map<String,String> nameMap = new HashMap<>();
//        Arrays.stream(content.split("\n")).forEach(stack::push);
        stack.push("/");
        while(!stack.isEmpty()){
            String resourcePath = stack.pop();
            String resourceName = nameMap.remove(resourcePath);
//            System.out.println(String.format("%s -> %s", resourcePath, resourceName));
            if(fileName.equals(resourceName)){
                try {
                    String clsCanName = resourcePath.substring(1, resourcePath.length()-6).replaceAll("/",".");
                    Class cls = cl.loadClass(clsCanName);
                    return cls;
                } catch (ClassNotFoundException e) {
                    // class was not found. continue searching
                }
            }
            // the current element is done
            URL rurl = CMDMain.class.getResource(resourcePath);
            if(rurl == null)
                continue;
            File f = new File(rurl.getFile());
            if(f.isDirectory()) {
                for (File c : f.listFiles()) {
                    String path = "";
                    if(resourcePath.equals("/")){
                        path =  resourcePath + c.getName();
                    }else {
                        path = String.join("/", resourcePath, c.getName());
                    }
                    stack.push(path);
                    nameMap.put(path, c.getName());
                }
            }
        }
//
//        Class cls = ClassUtils.resolveClassName("cz.cvut.kbss.amaplas.exp.graphml.Graphml2Owl", CMDMain.class.getClassLoader());
//        Class cls1 = ClassUtils.resolveClassName("Graphml2Owl", CMDMain.class.getClassLoader());
//        ClassUtils.forName()
//        CMDMain.class.getClassLoader().getResource("")
        return null;
    }

    public static void main(String[] args) throws Exception {
//        args = new String[]{"Graphml2Owl"};
        System.out.println("Running a command...");
        String clsName = args[0];
//        String clsName = "Graphml2Owl";
        Class cls = getCommand(clsName);
        if(cls != null) {
//            Method m = Graphml2Owl.class.getMethod("main1", String[].class);
//            Graphml2Owl.main(Arrays.copyOfRange(args, 1, args.length));
//            m.invoke(Graphml2Owl.class, (String[])args);

//            Method m = Graphml2Owl.class.getMethod("main");
//            m.invoke(Graphml2Owl.class);

//            Method m = cls.getMethod("main");
//            m.invoke(cls);
//
            Method m = cls.getMethod("main", String[].class);
            m.invoke(null, (Object)Arrays.copyOfRange(args, 1, args.length));


//            System.out.println(String.fo  rmat("%s found -> %s", clsName, cls.getCanonicalName()));
        }else{
            System.err.println(String.format("Could not find command \"%s\"", clsName));
        }
    }
}
