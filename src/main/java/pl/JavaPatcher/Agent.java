package pl.JavaPatcher;

import org.objectweb.asm.*;
import pl.JavaPatcher.annotations.Patch;
import pl.JavaPatcher.annotations.Postfix;
import pl.JavaPatcher.annotations.Prefix;
import pl.JavaPatcher.example.MainPatch;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

public class Agent {

    private static final List<Patcher> patchers = new ArrayList<>();

    public static void premain(String agentArgs, Instrumentation inst) {
        patchers.add(new MainPatch());
        System.out.println("-- JavaPatcher active. Total of (" + patchers.size() + ") patchers.");
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                for(Patcher patcher : patchers) {
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

    private static byte[] patchClass(byte[] originalBytes, Patcher patcher) {
        ClassReader reader = new ClassReader(originalBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);

                List<Method> prefixes = new ArrayList<>();
                List<Method> postfixes = new ArrayList<>();

                for(Method method : patcher.getClass().getMethods()) {
                    if(method.isAnnotationPresent(Prefix.class)) {
                        if(method.getAnnotation(Prefix.class).value().equals(name)) {
                            prefixes.add(method);
                        }
                    }
                    if(method.isAnnotationPresent(Postfix.class)) {
                        if(method.getAnnotation(Postfix.class).value().equals(name)) {
                            postfixes.add(method);
                        }
                    }
                }

                if(prefixes.isEmpty() && postfixes.isEmpty()) {
                    return methodVisitor;
                }

                return new MethodVisitor(Opcodes.ASM9, methodVisitor) {

                    @Override
                    public void visitCode() {
                        super.visitCode();
                        for(Method prefix : prefixes) {
                            String ownerClass = prefix.getDeclaringClass().getName().replace('.', '/');
                            String methodName = prefix.getName();
                            super.visitMethodInsn(
                                    Opcodes.INVOKESTATIC,
                                    ownerClass,
                                    methodName,
                                    "()V",
                                    false
                            );
                        }
                    }

                    @Override
                    public void visitInsn(int opcode) {
                        if(opcode >= 172 && opcode <= 177) {
                            for(Method postfix : postfixes) {
                                String ownerClass = postfix.getDeclaringClass().getName().replace('.', '/');
                                String methodName = postfix.getName();
                                super.visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        ownerClass,
                                        methodName,
                                        "()V",
                                        false
                                );
                            }
                        }
                        super.visitInsn(opcode);
                    }
                };
            }
        };

        reader.accept(visitor, 0);
        return writer.toByteArray();
    }

    public static void registerPatcher(Class<? extends Patcher> patcher) throws Exception {
        patchers.add((Patcher) patcher.getConstructors()[0].newInstance());
    }

}
