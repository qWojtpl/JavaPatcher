package pl.JavaPatcherExample;

import pl.JavaPatcher.annotations.Patch;
import pl.JavaPatcher.annotations.Postfix;
import pl.JavaPatcher.annotations.Prefix;

@Patch("pl.JavaPatcherExample.MainClass")
public class MainPatch {

    @Prefix(value = "main", arguments = { String[].class })
    public static void mainPrefix() {
        System.out.println("MAIN PREFIX");
    }

    @Postfix(value = "main", arguments = { String[].class })
    public static void mainPostfix() {
        System.out.println("MAIN POSTFIX");
    }

    @Prefix("example")
    public static boolean examplePrefix(Object instance) {
        System.out.println("EXAMPLE PREFIX");
        return ((MainClass) instance).test == 1;
    }


    @Postfix("example")
    public static void examplePostfix(Object instance) {
        System.out.println("EXAMPLE POSTFIX");
        System.out.println(((MainClass) instance).test);
    }

}
