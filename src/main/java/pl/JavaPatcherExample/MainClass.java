package pl.JavaPatcherExample;

public class MainClass {

    public int test = 1;

    public static void main(String[] args) {
        System.out.println("HELLO WORLD!");
        new MainClass().example(new String[0][]);
    }

    private Object example(String[][] arg) {
        return new MainPatch();
    }

}
