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
                        System.out.println("-X- Patcher not annotated with Patch annotation: " + patcher.getClass().getCanonicalName());
                        return classfileBuffer;
                    }
                    Patch patch = patcher.getClass().getAnnotation(Patch.class);
                    if(patch.value().replace(".", "/").equals(className)) {
                        System.out.println("-- Patching class " + className + " with patcher " + patcher.getClass().getCanonicalName());
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

                Type returnType = Type.getReturnType(descriptor);

                for(Method method : patcher.getClass().getMethods()) {
                    if(method.isAnnotationPresent(Prefix.class)) {
                        Prefix prefix = method.getAnnotation(Prefix.class);
                        if(prefix.value().equals(name) && descriptor.equals(Type.getMethodDescriptor(returnType,
                                Arrays.stream(prefix.arguments()).map(Type::getType).toArray(Type[]::new)))) {
                            prefixes.add(method);
                        }
                    }
                    if(method.isAnnotationPresent(Postfix.class)) {
                        Postfix postfix = method.getAnnotation(Postfix.class);
                        if(postfix.value().equals(name) && descriptor.equals(Type.getMethodDescriptor(returnType,
                                Arrays.stream(postfix.arguments()).map(Type::getType).toArray(Type[]::new)))) {
                            postfixes.add(method);
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
