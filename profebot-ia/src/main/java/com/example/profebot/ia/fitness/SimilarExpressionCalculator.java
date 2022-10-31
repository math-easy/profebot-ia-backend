package com.example.profebot.ia.fitness;


import com.example.profebot.ia.parser.Operator;
import com.example.profebot.ia.parser.tree.ExpressionNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SimilarExpressionCalculator {

    protected String originalExpression;

    public abstract Double similarityWith(String otherExpression);

    SimilarExpressionCalculator(String expression) {
        this.originalExpression = expression;
    }

    protected List<Operator> getListOfTokensOfNormalizedExpression(ExpressionNode expressionNode){
        return this.removeDuplicates(expressionNode.normalize().getListOfTokens());
    }

    protected List<Operator> removeDuplicates(List<Operator> operators){
        List<Operator> operatorsWithoutDuplicates = new ArrayList<>();

        for(Operator operator : operators){
            if(operatorsWithoutDuplicates.stream().noneMatch(operator1 -> operator1.equals(operator))){
                operatorsWithoutDuplicates.add(new Operator(operator.getOperator(), operator.getDegree()));
            }
        }

        return this.chooseTermWithVariableByTermWithVariableDegree(operators, operatorsWithoutDuplicates, true);
    }

    private List<Operator> chooseTermWithVariableByTermWithVariableDegree(List<Operator> reference, List<Operator> result, Boolean chooseMax){
        Integer maxDegree = reference.stream()
                .filter(operator -> operator.getOperator().equals(Operator.TERM_WITH_X_BY_TERM_WITH_X))
                .map(Operator::getDegree)
                .reduce(0, Math::max);

        Integer degreeToUse;

        if(chooseMax){
            degreeToUse = maxDegree;
        }else{
            degreeToUse = reference.stream()
                    .filter(operator -> operator.getOperator().equals(Operator.TERM_WITH_X_BY_TERM_WITH_X))
                    .map(Operator::getDegree)
                    .reduce(maxDegree, Math::min);
        }

        return result.stream()
                .map(operator ->
                        operator.getOperator().equals(Operator.TERM_WITH_X_BY_TERM_WITH_X) ?
                                new Operator(operator.getOperator(), degreeToUse) :
                                operator)
                .collect(Collectors.toList());
    }

}