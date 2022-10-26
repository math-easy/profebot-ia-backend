package com.example.profebot.ia.parser.tree;

import java.util.*;
import java.util.function.*;
import org.springframework.expression.EvaluationException;

public class AdditionExpressionNode extends SequenceExpressionNode {
    public AdditionExpressionNode() {
    }

    public AdditionExpressionNode(final List<Term> terms) {
        this.terms = terms;
    }

    public AdditionExpressionNode(final ExpressionNode a, final boolean positive) {
        super(a, positive);
    }

    @Override
    public int getType() {
        return 3;
    }

    @Override
    public double getValue() throws EvaluationException {
        double sum = 0.0;
        for (final Term t : this.terms) {
            if (t.positive) {
                sum += t.expression.getValue();
            }
            else {
                sum -= t.expression.getValue();
            }
        }
        return sum;
    }

    @Override
    public Integer getLevel() {
        return this.getLevelFromBases(0, 3);
    }

    @Override
    public Integer getToken() {
        if (this.hasVariable()) {
            return 9;
        }
        if (this.allPositives()) {
            return 1;
        }
        return 2;
    }

    @Override
    public Boolean isLineal() {
        return this.terms.stream().allMatch(Term::isLineal);
    }

    @Override
    public Integer getDegree() {
        return this.terms.stream().map(Term::getDegree).reduce(0, (max, nextDegree) -> Math.max(max, nextDegree));
    }

    @Override
    public AdditionExpressionNode newSequenceWithTerms(final List<Term> terms) {
        return new AdditionExpressionNode(terms);
    }
}
