package pl.JavaPatcher.example;

import pl.JavaPatcher.Patcher;
import pl.JavaPatcher.annotations.Patch;
import pl.JavaPatcher.annotations.Postfix;
import pl.JavaPatcher.annotations.Prefix;

@Patch("pl.JavaPatcher.example.MainClass")
public class MainPatch extends Patcher {

    @Prefix("main")
    public static void mainPrefix() {
        System.out.println("MAIN PREFIX");
    }

    @Postfix("main")
    public static void mainPostfix() {
        System.out.println("MAIN POSTFIX");
    }

}
