package com.hornmicro.bootstrap;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.jdt.internal.jarinjarloader.RsrcURLStreamHandlerFactory;

class SWTBootstrap {
    static final String MAIN_CLASS = "SWTBootstrap-MainClass";
    static final String SWT_VERSION = "SWTBootstrap-SWTVersion";
    static final String CLASS_PATH = "SWTBootstrap-Class-Path";
    
    static final String INTERNAL_URL_PROTOCOL_WITH_COLON = "rsrc:";
    static final String PATH_SEPARATOR = "/";
    static final String JAR_INTERNAL_URL_PROTOCOL_WITH_COLON = "jar:rsrc:";
    static final String JAR_INTERNAL_SEPARATOR = "!/";
    
    String[] args;
    String version;
    
    public SWTBootstrap(String[] args) {
        this.args = args;
    }    
    
    public ClassLoader getClassloader(String[] classes) throws Exception {
        URLClassLoader cl = (URLClassLoader) getClass().getClassLoader();
        URL.setURLStreamHandlerFactory(new RsrcURLStreamHandlerFactory(cl));
        URL[] rsrcUrls = new URL[classes.length + 1];
        for (int i = 0; i < classes.length; i++) {
            String rsrcPath = classes[i];
            if (rsrcPath.endsWith(PATH_SEPARATOR)) {
                rsrcUrls[i] = new URL(INTERNAL_URL_PROTOCOL_WITH_COLON + rsrcPath);
            } else {
                rsrcUrls[i] = new URL(JAR_INTERNAL_URL_PROTOCOL_WITH_COLON + rsrcPath + JAR_INTERNAL_SEPARATOR);
            }
        }
        
        // Add the SWT platform jar
        //rsrcUrls[classes.length] = new URL("rsrc:"+getSwtJarName());
        rsrcUrls[classes.length] = new URL(JAR_INTERNAL_URL_PROTOCOL_WITH_COLON + getSwtJarName() + JAR_INTERNAL_SEPARATOR);
        return new URLClassLoader(rsrcUrls, null);
    }
    
    protected String getSwtJarName() {
        String osName = System.getProperty("os.name").toLowerCase();
        String os = null;
        String arch = System.getProperty("os.arch").toLowerCase().contains("64") ? "64" : "32";

        if(osName.contains("win")) {
            os = "win";
        } else if (osName.contains("mac")) {
            os = "osx";
        } else if (osName.contains("linux") || osName.contains("nix")) {
            os = "linux";
        }

        if (os == null) {
            throw new RuntimeException("Unknown OS name: " + osName);
        }
        return "swt-"+ os +"-"+ arch +"-"+ version +".jar";
    }
    
    public void run() throws Exception {
        Class<?> clazz = this.getClass();
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            throw new RuntimeException("This must be run within a jar");
        }
        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        Attributes mainAttributes = new Manifest(new URL(manifestPath).openStream()).getMainAttributes();
        String mainClass = mainAttributes.getValue(MAIN_CLASS);
        version = mainAttributes.getValue(SWT_VERSION);
        
        String classString = mainAttributes.getValue(CLASS_PATH);
        if(classString == null) {
            classString = "";
        }
        ClassLoader cl = getClassloader(classString.split(" "));
        Thread.currentThread().setContextClassLoader(cl);
        Class<?> c = Class.forName(mainClass, true, cl);
        Method main = c.getMethod("main", new Class[] {args.getClass()});
        main.invoke(null, new Object[] { args });
    }
    
    public static void main(String[] args) throws Exception {
        new SWTBootstrap(args).run();
    }
}