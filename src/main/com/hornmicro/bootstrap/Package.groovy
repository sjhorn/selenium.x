package com.hornmicro.bootstrap

import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

import com.hornmicro.selenium.SeleniumX



/*
 * This file packages the classes and jars into a single runnable jar. 
 * The techniques is based on the jar-in-jar code from eclipse jdt ui project
 * and the ideas from SWTJAR (https://github.com/mchr3k/swtjar)
 * 
 * The jars and manifest are designed around the behaviour of SWTBootstrap.
 * 
 * SWTBootstrap reads the manifest property SWTBootstrap.CLASS_PATH that contains a 
 * space separated list of jars embedded on the outer jar, each of these are added 
 * to the classpath prior to the SWTBootstrap.MAIN_CLASS static main method being called.
 * 
 * For our purposes the Groovy, Eclipse and SWT files are all added at runtime and
 * maintain their original jar structure. 
 */
class Package {
    static main(args) {
        new File("dist/Seleniumx.jar").withOutputStream { os ->

            Manifest manifest = new Manifest()
            def mainAttributes = manifest.mainAttributes
            mainAttributes.putValue(Attributes.Name.MANIFEST_VERSION as String, "1.0")
            [
                'Manifest-Version' : "1.0",
                (SWTBootstrap.MAIN_CLASS) : SeleniumX.class.name, 
                (SWTBootstrap.SWT_VERSION) : "3.7.1", 
                'Main-Class' : SWTBootstrap.class.name
            ].each { k,v -> 
                mainAttributes.putValue(k, v)
            }
            
            def libs = ["./", "."]
            new File("libs").eachFileRecurse { file ->
                if(!file.isDirectory() && !file.name.startsWith(".") && !file.path.startsWith("libs/swt")) {
                    libs << file.name
                }
            }
            mainAttributes.putValue(SWTBootstrap.CLASS_PATH, libs.join(" "))
            
            JarOutputStream jar = new JarOutputStream(os, manifest)
            new File("libs").eachFileRecurse { file ->
                if(!file.isDirectory() && !file.name.startsWith(".")) {
                    JarEntry jarEntry = new JarEntry(file.name)
                    jarEntry.setTime(file.lastModified())
                    jar.putNextEntry(jarEntry)
                    jar.write(file.readBytes())
                }
            }
            
            // Add our class files
            new File("bin").eachFileRecurse { file ->
                if(!file.isDirectory()) {
                    JarEntry jarEntry = new JarEntry(file.path.replaceAll(/^bin\//,""))
                    jarEntry.setTime(file.lastModified())
                    jar.putNextEntry(jarEntry)
                    jar.write(file.readBytes())
                }
            }
            
            // Add gfx/xml etc
            /*
            [
                "gfx/android.png", 
            ].each { String fileName ->
                File file = new File(fileName)
                JarEntry jarEntry = new JarEntry("com/hornmicro/spaceinvaders/${file.name}")
                jarEntry.setTime(file.lastModified())
                jar.putNextEntry(jarEntry)
                jar.write(file.readBytes())
            }
            */
            jar.close()
        }
    }
}