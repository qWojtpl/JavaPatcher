package pl.JavaPatcherExample;

import pl.JavaPatcher.annotations.Patch;
import pl.JavaPatcher.annotations.Postfix;
import pl.JavaPatcher.annotations.Prefix;

@Patch("pl.JavaPatcherExample.MainClass")
public class MainPatch {

    @Prefix(value = "main", arguments = { "java.lang.String[]" })
    public static void mainPrefix() {
        System.out.println("MAIN PREFIX");
    }

    @Postfix(value = "main", arguments = { "java.lang.String[]" })
    public static void mainPostfix() {
        System.out.println("MAIN POSTFIX");
    }

    @Prefix(value = "example", arguments = { "java.lang.String[][]" })
    public static void examplePrefix(Object instance) {
        System.out.println("EXAMPLE PREFIX");
    }

}
