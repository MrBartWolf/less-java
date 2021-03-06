package com.github.lessjava.types.ast;

public class ASTVoidMethodCall extends ASTStatement {
    public ASTMethodCall methodCall;

    public ASTVoidMethodCall(ASTMethodCall methodCall) {
        this.methodCall = methodCall;
    }

    @Override
    public String toString() {
        return String.format("%s.%s", methodCall.invoker, methodCall.funcCall);
    }

    @Override
    public void traverse(ASTVisitor visitor) {
        visitor.preVisit(this);
        methodCall.traverse(visitor);
        visitor.postVisit(this);
    }
}
