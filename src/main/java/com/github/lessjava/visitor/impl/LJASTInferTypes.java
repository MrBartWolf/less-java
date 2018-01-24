package com.github.lessjava.visitor.impl;

import java.util.Arrays;
import java.util.List;

import com.github.lessjava.types.Symbol;
import com.github.lessjava.types.ast.ASTAbstractFunction.Parameter;
import com.github.lessjava.types.ast.ASTArgList;
import com.github.lessjava.types.ast.ASTAssignment;
import com.github.lessjava.types.ast.ASTBinaryExpr;
import com.github.lessjava.types.ast.ASTConditional;
import com.github.lessjava.types.ast.ASTEntry;
import com.github.lessjava.types.ast.ASTExpression;
import com.github.lessjava.types.ast.ASTForLoop;
import com.github.lessjava.types.ast.ASTFunction;
import com.github.lessjava.types.ast.ASTFunctionCall;
import com.github.lessjava.types.ast.ASTList;
import com.github.lessjava.types.ast.ASTMap;
import com.github.lessjava.types.ast.ASTMethodCall;
import com.github.lessjava.types.ast.ASTProgram;
import com.github.lessjava.types.ast.ASTReturn;
import com.github.lessjava.types.ast.ASTSet;
import com.github.lessjava.types.ast.ASTUnaryExpr;
import com.github.lessjava.types.ast.ASTVariable;
import com.github.lessjava.types.inference.HMType;
import com.github.lessjava.types.inference.HMType.BaseDataType;
import com.github.lessjava.types.inference.impl.HMTypeBase;
import com.github.lessjava.types.inference.impl.HMTypeCollection;
import com.github.lessjava.types.inference.impl.HMTypeList;
import com.github.lessjava.types.inference.impl.HMTypeMap;
import com.github.lessjava.types.inference.impl.HMTypeSet;
import com.github.lessjava.types.inference.impl.HMTypeTuple;
import com.github.lessjava.types.inference.impl.HMTypeVar;
import com.github.lessjava.visitor.LJAbstractAssignTypes;

public class LJASTInferTypes extends LJAbstractAssignTypes {
    private HMType returnType;

    private static ASTProgram program;

    private List<Parameter> parameters;

    @Override
    public void preVisit(ASTProgram node) {
        super.preVisit(node);

        if (LJASTInferTypes.program == null) {
            LJASTInferTypes.program = node;
        }
    }

    @Override
    public void preVisit(ASTFunction node) {
        super.preVisit(node);

        this.returnType = null;
        this.parameters = node.parameters;
    }

    @Override
    public void postVisit(ASTFunction node) {
        super.postVisit(node);

        this.parameters = null;

        if (node.body == null) {
            return;
        }

        node.returnType = this.returnType == null ? HMTypeBase.VOID : unify(node.returnType, this.returnType);
        node.concrete = node.parameters.stream().noneMatch(p -> p.type instanceof HMTypeVar);
    }

    @Override
    public void preVisit(ASTForLoop node) {
        if (node.lowerBound == null) {
            node.var.type = ((HMTypeCollection) node.upperBound.type).elementType;
        } else {
            node.var.type = new HMTypeBase(BaseDataType.INT);
        }
    }

    @Override
    public void postVisit(ASTReturn node) {
        super.postVisit(node);

        if (node.value == null) {
            this.returnType = new HMTypeBase(BaseDataType.VOID);
        } else if (node.value instanceof ASTVariable) {
            ASTVariable var = (ASTVariable) node.value;

            if (var.index != null && var.type instanceof HMTypeCollection) {
                this.returnType = ((HMTypeCollection) var.type).elementType;
            } else {
                this.returnType = node.value.type;
            }
        } else {
            this.returnType = node.value.type;
        }
    }

    @Override
    public void preVisit(ASTBinaryExpr node) {
        super.preVisit(node);

        switch (node.operator) {
            case INVOKE:
                break;
            case INDEX:
                node.type = ((HMTypeCollection) node.leftChild.type).elementType;
                break;
            case ASGN:
            case ADDASGN:
            case SUBASGN:
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MOD:
                HMType unifiedType = unify(node.leftChild.type, node.rightChild.type);
                node.type = unifiedType != null ? unifiedType : node.type;
                break;
            case OR:
            case AND:
            case EQ:
            case NE:
            case LT:
            case GT:
            case LE:
            case GE:
                node.type = new HMTypeBase(BaseDataType.BOOL);
                break;
        }
    }

    @Override
    public void postVisit(ASTBinaryExpr node) {
        super.postVisit(node);

        ASTExpression leftChild, rightChild;

        leftChild = node.leftChild;
        rightChild = node.rightChild;

        leftChild.type = rightChild.type = unify(leftChild.type, rightChild.type, node.operator);
    }

    @Override
    public void preVisit(ASTUnaryExpr node) {
        super.preVisit(node);

        unify(node.type, node.child.type);
    }

    @Override
    public void postVisit(ASTUnaryExpr node) {
        super.postVisit(node);
    }

    @Override
    public void postVisit(ASTFunctionCall node) {
        super.postVisit(node);

        if (idFunctionMap.containsKey(node.getIdentifyingString())) {
            node.type = unify(node.type, idFunctionMap.get(node.getIdentifyingString()).returnType);
        }
    }

    @Override
    public void preVisit(ASTVariable node) {
        super.preVisit(node);

        if (parameters == null) {
            return;
        }

        for (Parameter p : parameters) {
            if (p.name.equals(node.name)) {
                node.type = unify(node.type, p.type);
            }
        }
    }

    @Override
    public void postVisit(ASTVariable node) {
        super.postVisit(node);

        List<Symbol> symbols = BuildSymbolTables.searchScopesForSymbol(node, node.name);

        if (symbols != null && !symbols.isEmpty()) {
            symbols.forEach(s -> node.type = unify(node.type, s.type));
        }

        node.isCollection = node.isCollection || node.type instanceof HMTypeCollection;
    }

    @Override
    public void preVisit(ASTList node) {
        super.preVisit(node);

        node.type = new HMTypeList(node.initialElements.type);
    }

    @Override
    public void preVisit(ASTSet node) {
        super.preVisit(node);

        node.type = new HMTypeSet(node.initialElements.type);
    }

    @Override
    public void preVisit(ASTMap node) {
        super.preVisit(node);

        if (node.initialElements.type instanceof HMTypeTuple) {
            node.type = new HMTypeMap((HMTypeTuple) node.initialElements.type);
        }
    }

    @Override
    public void preVisit(ASTEntry node) {
        List<HMType> types = Arrays.asList(new HMType[] { node.key.type, node.value.type });

        node.type = new HMTypeTuple(types);
    }

    @Override
    public void preVisit(ASTArgList node) {
        super.preVisit(node);

        node.isConcrete = node.isConcrete || node.type instanceof HMTypeBase;
    }

    @Override
    public void postVisit(ASTArgList node) {
        super.postVisit(node);

        if (!node.arguments.isEmpty()) {
            node.type.isConcrete = true;
            node.type = unify(node.type, node.arguments.get(0).type);
        }
    }

    @Override
    public void postVisit(ASTAssignment node) {
        super.postVisit(node);

        node.variable.type = unify(node.variable.type, node.value.type);

        if (node.value instanceof ASTVariable) {
            ASTVariable var = (ASTVariable) node.value;

            if (var.index != null) {
                node.variable.type = ((HMTypeCollection) var.type).elementType;
            }
        }

        node.type = node.variable.type;
    }

    @Override
    public void postVisit(ASTMethodCall node) {
        super.postVisit(node);

        // TODO: Better way??
        if (node.invoker.type instanceof HMTypeCollection) {
            HMTypeCollection t = (HMTypeCollection) node.invoker.type;

            if (node.funcCall.name.equals("add")) {
                t.elementType = unify(t.elementType, node.funcCall.arguments.get(0).type);
            } else if (node.funcCall.name.equals("insert")) {
                t.elementType = unify(t.elementType, node.funcCall.arguments.get(1).type);
            } else if (node.funcCall.name.equals("remove")) {
                if (!node.funcCall.arguments.isEmpty()) {
                    t.elementType = unify(t.elementType, node.funcCall.arguments.get(0).type);
                }
            } else if (node.funcCall.name.equals("put")) {
                if (!node.funcCall.arguments.isEmpty()) {
                    HMTypeTuple tuple = (HMTypeTuple) t.elementType;

                    HMType key = tuple.types.get(0);
                    HMType value = tuple.types.get(1);

                    key = unify(key, node.funcCall.arguments.get(0).type);
                    value = unify(value, node.funcCall.arguments.get(1).type);

                    t.elementType = new HMTypeTuple(Arrays.asList(new HMType[] {key, value}));
                }
            }
        }

        if (idFunctionMap.containsKey(node.getIdentifyingString())) {
            node.type = unify(node.type, idFunctionMap.get(node.getIdentifyingString()).returnType);
        }
    }

    @Override
    public void preVisit(ASTConditional node) {
        super.preVisit(node);

        node.condition.type = unify(node.condition.type, new HMTypeBase(BaseDataType.BOOL));
    }

}
