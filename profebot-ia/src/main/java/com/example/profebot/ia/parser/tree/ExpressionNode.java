package com.example.profebot.ia.parser.tree;

import com.example.profebot.ia.parser.ExpressionsWithArgumentStructures;
import com.example.profebot.ia.parser.Operator;

import java.util.List;

public interface ExpressionNode {
    public static final int VARIABLE_NODE = 1;
    public static final int CONSTANT_NODE = 2;
    public static final int ADDITION_NODE = 3;
    public static final int MULTIPLICATION_NODE = 4;
    public static final int EXPONENTIATION_NODE = 5;
    public static final int FUNCTION_NODE = 6;

    int getType();

    double getValue();

    Integer getLevel();

    Boolean hasVariable();

    ExpressionsWithArgumentStructures getStructureOf(final ExpressionsWithArgumentStructures p0);

    Integer getToken();

    Boolean isNumber();

    Boolean isLineal();

    Boolean isQuadraticX();

    Boolean isZero();

    Boolean isVariable();

    Boolean isMinusOne();

    Boolean isMinusN();

    Boolean isOne();

    Boolean isTwo();

    Boolean isFractionalNumber();

    Boolean isPositiveNumber();

    Boolean isEven();

    ExpressionNode normalize();

    Integer getDegree();

    List<Operator> getListOfTokens();

    Boolean contains(final Integer p0);

    double[] extractFeaturesForExpression();

    ExpressionNode simplify();
}
