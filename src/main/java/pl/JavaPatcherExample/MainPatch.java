package pl.JavaPatcherExample;

import pl.JavaPatcher.annotations.Patch;
import pl.JavaPatcher.annotations.Postfix;
import pl.JavaPatcher.annotations.Prefix;

@Patch("pl.JavaPatcherExample.MainClass")
public class MainPatch {

    @Prefix("main")
    public static void mainPrefix() {
        System.out.println("MAIN PREFIX");
    }

    @Postfix("main")
    public static void mainPostfix() {
        System.out.println("MAIN POSTFIX");
    }

    @Postfix("example")
    public static void examplePostfix(Object instance) {
        System.out.println(((MainClass) instance).test);
    }

}
