# JavaPatcher

JavaPatcher is a simple Harmony-like library, where you can patch existing 
methods in other Java applications, using annotations such as `Prefix` and `Postfix`.

## Getting started

1. Attach `JavaPatcher.jar` into your project as a library
2. Write patcher
3. Build jar
4. Run program with command:
```shell
java -javaagent:JavaPatcher.jar=YourCompiledPatcher.jar -jar Application.jar
```

## Example

```java
import pl.JavaPatcher.annotations.Postfix;
import pl.JavaPatcher.annotations.Prefix;

@Patch("pl.JavaPatcherExample.MainClass")
public class MainPatch {

    @Prefix(value = "main", arguments = "java.lang.String[]")
    public static void mainPrefix() {
        System.out.println("Main method started its work!");
    }

    @Postfix(value = "main", arguments = "java.lang.String[]")
    public static void mainPostfix() {
        System.out.println("Main method finished its work!");
    }

}
```

```java

@Patch("pl.JavaPatcherExample.SomeClass")
public class SomeClassPatch {

    @Prefix(value = "example", arguments = "int")
    public static void examplePrefix(Object instance) {
        System.out.println("Example prefix, with instance: " + instance.hashCode());
    }

}
```

## Plans for the future

- Modifying passed arguments
- Ability to block the original method from `Postfix`