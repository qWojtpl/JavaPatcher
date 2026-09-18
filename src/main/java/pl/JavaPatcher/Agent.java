package pl.JavaPatcher;

import org.objectweb.asm.*;
import pl.JavaPatcher.annotations.Patch;
import pl.JavaPatcher.annotations.Postfix;
import pl.JavaPatcher.annotations.Prefix;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Agent {

    private static final List<Object> patchers = new ArrayList<>();

    public static void premain(String agentArgs, Instrumentation inst) {
        PatcherScanner.scanForPatchers();
        System.out.println("-- JavaPatcher active. Total of (" + patchers.size() + ") patchers.");

        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                for(Object patcher : patchers) {
                    if(!patcher.getClass().isAnnotationPresent(Patch.class)) {
                        System.out.println("--- Patcher not annotated with Patch annotation: " + patcher.getClass().getCanonicalName());
                        continue;
                    }
                    Patch patch = patcher.getClass().getAnnotation(Patch.class);
                    if(patch.value().replace(".", "/").equals(className)) {
                        System.out.println("--- Patching class " + className + " with patcher " + patcher.getClass().getCanonicalName());
                        return patchClass(classfileBuffer, patcher);
                    }
                }
                return classfileBuffer;
            }
        });
    }

    private static byte[] patchClass(byte[] originalBytes, Object patcher) {
        ClassReader reader = new ClassReader(originalBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);

                List<Method> prefixes = new ArrayList<>();
                List<Method> postfixes = new ArrayList<>();

                for(Method method : patcher.getClass().getMethods()) {
                    List<String> values = new ArrayList<>();
                    List<String[]> arguments = new ArrayList<>();
                    List<Boolean> isPrefix = new ArrayList<>();
                    if(method.isAnnotationPresent(Prefix.class)) {
                        Prefix prefix = method.getAnnotation(Prefix.class);
                        values.add(prefix.value());
                        arguments.add(prefix.arguments());
                        isPrefix.add(true);
                    }
                    if(method.isAnnotationPresent(Postfix.class)) {
                        Postfix prefix = method.getAnnotation(Postfix.class);
                        values.add(prefix.value());
                        arguments.add(prefix.arguments());
                        isPrefix.add(false);
                    }
                    for(int j = 0; j < values.size(); j++) {
                        if(values.get(j).equals(name)) {
                            StringBuilder argumentDescriptor = new StringBuilder("(");
                            for(int i = 0; i < arguments.get(j).length; i++) {
                                String argument = arguments.get(j)[i];
                                if(argument.endsWith("[]")) {
                                    while(argument.contains("[]")) {
                                        argumentDescriptor.append("[");
                                        argument = argument.replaceFirst("\\[]", "");
                                    }
                                }
                                if(argument.equalsIgnoreCase("boolean")) {
                                    argumentDescriptor.append("Z");
                                    continue;
                                } else if(argument.equalsIgnoreCase("byte")) {
                                    argumentDescriptor.append("B");
                                    continue;
                                } else if(argument.equalsIgnoreCase("char")) {
                                    argumentDescriptor.append("C");
                                    continue;
                                } else if(argument.equalsIgnoreCase("double")) {
                                    argumentDescriptor.append("D");
                                    continue;
                                }  else if(argument.equalsIgnoreCase("float")) {
                                    argumentDescriptor.append("F");
                                    continue;
                                } else if(argument.equalsIgnoreCase("int")) {
                                    argumentDescriptor.append("I");
                                    continue;
                                } else if(argument.equalsIgnoreCase("long")) {
                                    argumentDescriptor.append("J");
                                    continue;
                                }  else if(argument.equalsIgnoreCase("short")) {
                                    argumentDescriptor.append("S");
                                    continue;
                                } else {
                                    argumentDescriptor.append("L");
                                }
                                argumentDescriptor.append(argument.replace(".", "/")).append(";");
                            }
                            /*System.out.println("DESCRIPTOR: " + descriptor);
                            System.out.println("ARGUMENT DESCRIPTOR: " + argumentDescriptor);
                            System.out.println("CHECK: "+ descriptor.split("\\)")[0] + " == " + argumentDescriptor);
                            */
                            if(descriptor.split("\\)")[0].contentEquals(argumentDescriptor)) {
                                if(isPrefix.get(j)) {
                                    prefixes.add(method);
                                } else {
                                    postfixes.add(method);
                                }
                            }
                        }
                    }
                }

                if(prefixes.isEmpty() && postfixes.isEmpty()) {
                    return methodVisitor;
                }

                return new PatchVisitor(Opcodes.ASM9, methodVisitor, prefixes, postfixes);
            }
        };

        reader.accept(visitor, 0);
        return writer.toByteArray();
    }

    public static void registerPatcher(Object patcher) {
        patchers.add(patcher);
    }

}
