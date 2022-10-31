package com.example.profebot.ia.parser.tree;

import com.example.profebot.ia.parser.ExpressionsWithArgumentStructures;
import com.example.profebot.ia.parser.Operator;

import java.util.*;
import java.util.stream.Collectors;

public abstract class SequenceExpressionNode extends AbstractExpressionNode implements ExpressionNode{

    protected List<Term> terms;

    public SequenceExpressionNode() {
        this.terms = new LinkedList<Term>();
    }

    public SequenceExpressionNode(ExpressionNode a, boolean positive) {
        this.terms = new LinkedList<Term>();
        this.terms.add(new Term(positive, a));
    }

    public void add(ExpressionNode a, boolean positive) {
        this.terms.add(new Term(positive, a));
    }

    public Boolean hasVariable() {
        return terms.stream().anyMatch(Term::hasVariable);
    }

    public Boolean isNumber() {
        return this.terms.stream().allMatch(Term::isNumber);
    }

    public Boolean isPositiveNumber() {
        try{
            return this.isNumber() && this.getValue() >= 0;
        }catch (Exception e){
            return false;
        }
    }

    public Boolean isEven(){
        try{
            return this.isNumber() && this.getValue() % 2 == 0;
        }catch (Exception e){
            return false;
        }
    }

    public Integer maxLevelAgainst(Integer level){
        return terms.stream().map(Term::getLevel).reduce(level, Math::max);
    }

    protected Integer getLevelFromBases(Integer lowerBase, Integer upperBase){
        Boolean existsTermWithVariable = terms.stream().anyMatch(Term::hasVariable);
        Integer base = existsTermWithVariable ? upperBase : lowerBase;
        return this.maxLevelAgainst(base);
    }

    private Boolean is(Integer number){
        if(this.hasVariable()){
            return false;
        }

        try{
            return this.getValue() == number;
        }catch (Exception e){
            return false;
        }
    }

    private Boolean isGreaterThan(Integer number, Boolean positive){
        if(this.hasVariable()){
            return false;
        }

        try{
            Double value = this.getValue();
            return Math.abs(value) >= number && (positive ? value >= 0 : value < 0);
        }catch (Exception e){
            return false;
        }
    }

    public Boolean isMinusOne(){
        return this.is(-1);
    }

    public Boolean isMinusN(){
        return this.isGreaterThan(2, false);
    }

    public Boolean isOne(){
        return this.is(1);
    }

    public Boolean isTwo(){
        return this.is(2);
    }

    public Boolean isZero(){
        return this.is(0);
    }

    public Boolean isN(){
        return this.isGreaterThan(2, true);
    }

    public Boolean isFractionalNumber(){
        if(this.hasVariable()){
            return false;
        }

        try{
            Double value = this.getValue();
            return Math.abs((value - value.intValue())) > 0;
        }catch (Exception e){
            return false;
        }
    }

    protected Boolean allPositives(){
        return this.terms.stream().allMatch(term -> term.positive);
    }

    public ExpressionsWithArgumentStructures getStructureOf(ExpressionsWithArgumentStructures expressionsWithArgumentStructures){
        for(Term term : this.terms){
            term.getStructureOf(expressionsWithArgumentStructures);
        }
        return expressionsWithArgumentStructures;
    }

    public List<Operator> getListOfTokens(){
        List<Operator> tokens = new ArrayList<>();
        tokens.add(Operator.newToken(this.getToken(), this.getDegree()));

        for(Term term : this.terms){
            tokens.addAll(term.getListOfTokens());
        }

        return tokens;
    }

    public ExpressionNode normalize(){
        this.terms = this.terms.stream().map(term -> new Term(term.positive, term.normalize())).collect(Collectors.toList());
        return this;
    }

    public Boolean contains(Integer operator){
        return this.getToken().equals(operator) || this.terms.stream().anyMatch(term -> term.contains(operator));
    }

    public ExpressionNode simplify() {
        if(this.isNumber()){
            try{
                return new ConstantExpressionNode(this.getValue());
            }catch (Exception e){
                return this.simplifySequenceWithVariables();
            }
        }
        return this.simplifySequenceWithVariables();
    }

    private ExpressionNode simplifySequenceWithVariables(){
        List<Term> termsSimplified = this.terms.stream().map(Term::simplify).collect(Collectors.toList());
        Map<String, Map<Term, Integer>> countOfTermsBySign = new HashMap<>();
        Integer count;

        // Detect the terms to cancel
        for(Term term : termsSimplified){
            Map<Term, Integer> value = new HashMap<>();
            count = 0;
            if(countOfTermsBySign.containsKey(term.getListOfTokens().toString())){
                Term termToTake = countOfTermsBySign.get(term.getListOfTokens().toString()).keySet().stream().findFirst().get();
                count = countOfTermsBySign.get(term.getListOfTokens().toString()).get(termToTake);
                countOfTermsBySign.remove(term.getListOfTokens().toString());
            }
            value.put(term, count + (term.positive ? 1 : -1));
            countOfTermsBySign.put(term.getListOfTokens().toString(), value);
        }

        // Take only terms with count != 0
        List<Term> newTerms = new ArrayList<>();
        for(String tokensKey : countOfTermsBySign.keySet()){
            Term newTerm = countOfTermsBySign.get(tokensKey).keySet().stream().findFirst().get();
            if(countOfTermsBySign.get(tokensKey).get(newTerm) != 0){
                newTerms.add(newTerm);
            }
        }

        return this.newSequenceWithTerms(newTerms);
    }

    public abstract SequenceExpressionNode newSequenceWithTerms(List<Term> terms);
}