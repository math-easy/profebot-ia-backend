package com.example.profebot.ia.parser.tree;

import com.example.profebot.ia.parser.Operator;
import org.springframework.expression.EvaluationException;

import java.util.List;

public class MultiplicationExpressionNode extends SequenceExpressionNode{

    public MultiplicationExpressionNode(ExpressionNode a, boolean positive) {
        super(a, positive);
    }

    public MultiplicationExpressionNode(List<Term> terms) {
        super();
        this.terms = terms;
    }

    public int getType() {
        return MULTIPLICATION_NODE;
    }

    public double getValue() throws EvaluationException {
        double prod = 1.0;
        for (Term t : terms) {
            if (t.positive){
                prod *= t.expression.getValue();
            }
            else{
                prod /= t.expression.getValue();
            }
        }
        return prod;
    }

    public Integer getDegree() {
        return this.terms.stream()
                .map(Term::getDegree)
                .reduce(0, (total, aDegree) -> total + aDegree);
    }

    public Integer getLevel(){
        return this.getLevelFromBases(1, 4);
    }

    public Integer getToken() {
        if(!this.hasVariable()){
            if(this.allPositives()){
                return Operator.N_BY_N;
            }

            return Operator.N_DIVIDED_N;
        }

        if(this.terms.size() == 2 && this.onlyOneTermIsVariable()){
            return Operator.X;
        }

        if(this.onlyOneTermIsVariable() || this.onlyOneTermIsLineal()){
            return Operator.N_BY_X;
        }

        if(this.onlyOneTermHasVariableAsFactor() && !this.onlyOneTermHasVariableAsDividend()){
            return Operator.BY_TERM_WITH_X;
        }

        if(this.onlyOneTermHasVariableAsDividend()){
            return Operator.TERM_WITH_X_DIVIDED_N;
        }

        if(this.towOrMoreTermsWithVariableAsFactors()){
            return Operator.TERM_WITH_X_BY_TERM_WITH_X;
        }

        if(this.anyTermWithVariableAsQuotient()){
            return Operator.DIVIDED_TERM_WITH_X;
        }

        return Operator.N;
    }

    private Boolean onlyOneTermIsVariable(){
        return this.terms.stream().filter(Term::hasVariable).count() == 1 &&
                this.terms.stream().filter(term -> term.isVariable() && term.positive).count() == 1;
    }

    private Boolean onlyOneTermIsLineal(){
        return this.terms.stream().filter(Term::hasVariable).count() == 1 &&
                this.terms.stream().filter(term -> term.hasVariable() && term.isLineal() && term.positive).count() == 1;
    }

    private Long countOfTermsWithVariableAsFactor(){
        return this.terms.stream().filter(term -> term.hasVariable() && term.positive).count();
    }

    private Boolean onlyOneTermHasVariableAsFactor() {
        return this.countOfTermsWithVariableAsFactor() == 1 && this.terms.stream().filter(term -> term.hasVariable() && !term.positive).count() == 0;
    }

    private Boolean onlyOneTermHasVariableAsDividend(){
        return this.onlyOneTermHasVariableAsFactor() &&
                this.terms.stream().anyMatch(term -> (term.isNumber() && !term.positive) || term.isFractionalNumber());
    }

    public Boolean isQuadraticX() {
        return (this.terms.stream().filter(Term::hasVariable).count() == 1 && this.terms.stream().filter(Term::isQuadraticX).count() == 1) ||
                (this.terms.stream().filter(Term::hasVariable).count() == 2 && this.terms.stream().filter(Term::isVariable).count() == 2);
    }

    private Boolean towOrMoreTermsWithVariableAsFactors(){
        return this.countOfTermsWithVariableAsFactor() >= 2;
    }

    private Boolean anyTermWithVariableAsQuotient(){
        return this.terms.stream().filter(term -> term.hasVariable() && !term.positive).count() >= 1;
    }

    public Boolean isLineal() {
        return this.terms.stream().filter(Term::hasVariable).count() <= 1 &&
                this.terms.stream().filter(term -> term.hasVariable() && term.isLineal()).count() == 1;
    }

    public Boolean isZero(){
        return this.terms.stream().anyMatch(Term::isZero);
    }

    public MultiplicationExpressionNode newSequenceWithTerms(List<Term> terms){
        return new MultiplicationExpressionNode(terms);
    }
}