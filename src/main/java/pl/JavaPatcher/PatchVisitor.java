package pl.JavaPatcher;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.util.List;

public class PatchVisitor extends MethodVisitor {

    private final List<Method> prefixes;
    private final List<Method> postfixes;

    public PatchVisitor(int api, MethodVisitor methodVisitor, List<Method> prefixMethods, List<Method> postfixMethods) {
        super(api, methodVisitor);
        this.prefixes = prefixMethods;
        this.postfixes = postfixMethods;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        for(Method prefix : prefixes) {
            setupMethod(prefix);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if(opcode >= 172 && opcode <= 177) {
            for(Method postfix : postfixes) {
                setupMethod(postfix);
            }
        }
        super.visitInsn(opcode);
    }

    private void setupMethod(Method method) {
        String ownerClass = method.getDeclaringClass().getName().replace('.', '/');
        String methodName = method.getName();
        if(method.getParameterCount() > 0 && Object.class.equals(method.getParameterTypes()[0])) {
            System.out.println("---- Patch with instance for: " + methodName);
            super.visitVarInsn(Opcodes.ALOAD, 0);
            super.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    ownerClass,
                    methodName,
                    "(Ljava/lang/Object;)V",
                    false
            );
        } else {
            super.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    ownerClass,
                    methodName,
                    "()V",
                    false
            );
        }
    }

}
