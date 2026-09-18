package pl.JavaPatcher;

import pl.JavaPatcher.annotations.Patch;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class PatcherScanner {

    public static void scanForPatchers() {
        System.out.println("-- Scanning for patchers...");

        try {
            URL jarUrl = PatcherScanner.class.getProtectionDomain().getCodeSource().getLocation();
            File jarFile = new File(jarUrl.toURI());

            if (jarFile.isDirectory()) {
                scanJarDirectory(jarFile, "");
            } else {
                scanJarFile(jarFile);
            }
        } catch(Exception ignored) {}
    }

    private static void scanJarDirectory(File directory, String path) {
        for(File file : directory.listFiles()) {
            if(file.isDirectory()) {
                scanJarDirectory(file, path + file.getName() + ".");
            } else if(file.getName().endsWith(".class")) {
                checkAndRegister(path + file.getName().substring(0, file.getName().length() - 6));
            }
        }
    }

    private static void scanJarFile(File jarFile) {
        try(JarInputStream inputStream = new JarInputStream(new FileInputStream(jarFile))) {
            JarEntry entry;
            while((entry = inputStream.getNextJarEntry()) != null) {
                if(entry.getName().endsWith(".class")) {
                    checkAndRegister(entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6));
                }
            }
        } catch(Exception ignored) {}
    }

    private static void checkAndRegister(String className) {
        try {
            if(className.contains("JavaPatcherExample") && !className.endsWith("Patch")) { // For debugging purposes
                return;
            }
            Class<?> clazz = Class.forName(className);
            if(!clazz.isInterface() && clazz.isAnnotationPresent(Patch.class)) {
                Agent.registerPatcher(clazz.getDeclaredConstructor().newInstance());
            }
        } catch(Exception ignored) {}
    }

}
