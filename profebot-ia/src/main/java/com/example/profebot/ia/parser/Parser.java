package com.example.profebot.ia.parser;

import com.example.profebot.ia.parser.tree.*;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.op.Op;
import org.yaml.snakeyaml.parser.ParserException;

import java.text.ParseException;
import java.util.LinkedList;

public class Parser {

    LinkedList<Token> tokens;
    Token lookahead;

    public ExpressionNode parse(TreeNode<Op<Double>> expression) throws ParserException, ParseException {
        return this.parse(this.getAsInfix(expression));
    }

    public String getAsInfix(TreeNode<Op<Double>> expression){
        try{
            return Double.valueOf(expression.toString()).toString();
        }catch (Exception e){
            String currentExpression = expression.getValue().name();
            switch (currentExpression){
                case "ADD":
                    return this.sumOrMinusAsInfix(expression, "+");
                case "SUB":
                    return this.sumOrMinusAsInfix(expression, "-");
                case "MUL":
                    return this.mulOrDivOperation(expression, "*");
                case "DIV":
                    return this.mulOrDivOperation(expression, "/");
                case "POW":
                    return this.powOperation(expression);
                case "SQRT":
                    return this.functionOperation(expression, "sqrt");
                case "LN":
                    return this.functionOperation(expression, "ln");
                case "LOG":
                    return this.functionOperation(expression, "log");
                case "LOG2B":
                    return this.functionOperation(expression, "log2b");
                case "SIN":
                    return this.functionOperation(expression, "sin");
                case "COS":
                    return this.functionOperation(expression, "cos");
                case "TAN":
                    return this.functionOperation(expression, "tan");
                case "INTEGRAL":
                    return this.functionOperation(expression, "int");
                case "DERIVATIVE":
                    return this.functionOperation(expression, "dx");
                default:
                    return currentExpression;
            }
        }
    }

    private String sumOrMinusAsInfix(TreeNode<Op<Double>> expression, String operator){
        String sum = "";
        Integer childCount = expression.childCount();
        for(int i = 0; i < childCount ; i++){
            sum += this.getAsInfix(expression.getChild(i)) + operator;
        }
        return sum.substring(0, sum.length() - 1);
    }

    private String mulOrDivOperation(TreeNode<Op<Double>> expression, String operator){
        String result = "";
        Integer childCount = expression.childCount();
        for(int i = 0; i < childCount ; i++){
            result += "(" + this.getAsInfix(expression.getChild(i)) + ")" + operator;
        }
        return result.substring(0, result.length() - 1);
    }

    private String powOperation(TreeNode<Op<Double>> expression){
        String result = "";
        Integer childCount = expression.childCount();
        for(int i = 0; i < childCount ; i++){
            result += "(" + this.getAsInfix(expression.getChild(i)) + ")" + "^";
        }
        return result.substring(0, result.length() - 1);
    }

    private String functionOperation(TreeNode<Op<Double>> expression, String operator){
        String result = "";
        Integer childCount = expression.childCount();
        for(int i = 0; i < childCount ; i++){
            result += operator + "(" + this.getAsInfix(expression.getChild(i)) + ")";
        }
        return result;
    }

    public ExpressionNode parse(String expression) throws ParserException, ParseException{
        Tokenizer tokenizer = Tokenizer.getExpressionTokenizer();
        tokenizer.tokenize(this.cleanFormatOf(expression));
        LinkedList<Token> tokens = tokenizer.getTokens();
        return this.parse(tokens);
    }

    public String cleanFormatOf(String expression){
        String expressionCleaned = expression;

        expressionCleaned = replaceComplexOperatorsNames(expressionCleaned);
        expressionCleaned = addMultiplicationSymbols(expressionCleaned);

        return expressionCleaned
                .replaceAll("e", "2.718281828459045235360")
                .replaceAll("pi", "3.14159265358979323846")
                .replaceAll("\\)x", ")*x")
                .replaceAll("\\)\\(", ")*(")
                .replaceAll("x\\(", "x*(")
                .replaceAll("dx\\*\\(", "dx(");
    }

    private String replaceComplexOperatorsNames(String expression) {
        return expression
                .replaceAll("derivative", "dx")
                .replaceAll("integral", "int");
    }

    private String addMultiplicationSymbols(String expression){
        for(int i = 0 ; i <= 9 ; i++){
            expression = expression
                    .replaceAll(i + "\\(", i + "*(")
                    .replaceAll(i + "sqrt", i + "*sqrt")
                    .replaceAll(i + "sin", i + "*sin")
                    .replaceAll(i + "cos", i + "*cos")
                    .replaceAll(i + "tan", i + "*tan")
                    .replaceAll(i + "ln", i + "*ln")
                    .replaceAll(i + "log", i + "*log")
                    .replaceAll(i + "log2b", i + "*log2b")
                    .replaceAll(i + "dx", i + "*dx")
                    .replaceAll(i + "int", i + "*int")
                    .replaceAll(i + "x", i + "*x");
        }
        return expression;
    }

    public ExpressionNode parse(LinkedList<Token> tokens) throws ParseException, ParserException{
        this.tokens = (LinkedList<Token>)tokens.clone();
        lookahead = this.tokens.getFirst();

        ExpressionNode expr = expression();

        if (lookahead.token != Token.EPSILON){
            //System.out.println(expression);
            throw new ParseException("Unexpected symbol " + lookahead + " found", 0);
        }

        return expr;
    }

    private void nextToken() {
        tokens.pop();
        // at the end of input we return an epsilon token
        if (tokens.isEmpty()){
            lookahead = new Token(Token.EPSILON, "", -1);
        }
        else{
            lookahead = tokens.getFirst();
        }
    }

    private ExpressionNode expression() throws ParseException{
        // expression -> signed_term sum_op
        ExpressionNode expr = signedTerm();
        return sumOp(expr);
    }

    private ExpressionNode sumOp(ExpressionNode expr) throws ParseException{
        // sum_op -> PLUSMINUS term sum_op
        if (lookahead.token == Token.PLUSMINUS) {
            AdditionExpressionNode sum;
            if (expr.getType() == ExpressionNode.ADDITION_NODE){
                sum = (AdditionExpressionNode)expr;
            } else{
                sum = new AdditionExpressionNode(expr, true);
            }

            boolean positive = lookahead.sequence.equals("+");
            nextToken();
            ExpressionNode t = term();
            sum.add(t, positive);

            return sumOp(sum);
        }

        // sum_op -> EPSILON
        return expr;
    }

    private ExpressionNode signedTerm() throws ParseException{
        // signed_term -> PLUSMINUS term
        if (lookahead.token == Token.PLUSMINUS) {
            boolean positive = lookahead.sequence.equals("+");
            nextToken();
            ExpressionNode t = term();
            if (positive){
                return t;
            } else{
                return new AdditionExpressionNode(t, false);
            }
        }

        // signed_term -> term
        return term();
    }

    private ExpressionNode term() throws ParseException{
        // term -> factor term_op
        ExpressionNode f = factor();
        return termOp(f);
    }

    private ExpressionNode termOp(ExpressionNode expression) throws ParseException{
        // term_op -> MULTDIV factor term_op
        if (lookahead.token == Token.MULTDIV) {
            MultiplicationExpressionNode prod;

            if (expression.getType() == ExpressionNode.MULTIPLICATION_NODE){
                prod = (MultiplicationExpressionNode)expression;
            }
            else{
                prod = new MultiplicationExpressionNode(expression, true);
            }

            boolean positive = lookahead.sequence.equals("*");
            nextToken();
            ExpressionNode f = signedFactor();
            prod.add(f, positive);

            return termOp(prod);
        }

        // term_op -> EPSILON
        return expression;
    }

    private ExpressionNode signedFactor() throws ParseException{
        // signed_factor -> PLUSMINUS factor
        if (lookahead.token == Token.PLUSMINUS) {
            boolean positive = lookahead.sequence.equals("+");
            nextToken();
            ExpressionNode t = factor();
            if (positive){
                return t;
            }

            return new AdditionExpressionNode(t, false);
        }

        // signed_factor -> factor
        return factor();
    }

    private ExpressionNode factor() throws ParseException{
        // factor -> argument factor_op
        ExpressionNode a = argument();
        return factorOp(a);
    }

    private ExpressionNode factorOp(ExpressionNode expression) throws ParseException{
        // factor_op -> RAISED factor
        if (lookahead.token == Token.RAISED) {
            nextToken();
            ExpressionNode exponent = signedFactor();

            return new ExponentiationExpressionNode(expression, exponent);
        }

        // factor_op -> EPSILON
        return expression;
    }

    private ExpressionNode argument() throws ParseException{
        // argument -> FUNCTION argument
        if (lookahead.token == Token.FUNCTION) {
            int function = FunctionExpressionNode.stringToFunction(lookahead.sequence);
            nextToken();
            ExpressionNode expr = argument();
            return new FunctionExpressionNode(function, expr);
        } else if (lookahead.token == Token.OPEN_BRACKET){ // argument -> OPEN_BRACKET sum CLOSE_BRACKET
            nextToken();
            ExpressionNode expr = expression();
            if (lookahead.token != Token.CLOSE_BRACKET){
                throw new ParseException("Closing brackets expected: " + lookahead, 0);
            }
            nextToken();
            return expr;
        }

        // argument -> value
        return value();
    }

    private ExpressionNode value() throws ParseException{
        // argument -> NUMBER
        if (lookahead.token == Token.NUMBER) {
            ExpressionNode expr = new ConstantExpressionNode(lookahead.sequence);
            nextToken();
            return expr;
        }

        // argument -> VARIABLE
        if (lookahead.token == Token.VARIABLE) {
            ExpressionNode expr = new VariableExpressionNode(lookahead.sequence);
            nextToken();
            return expr;
        }

        if (lookahead.token == Token.EPSILON){
            throw new ParseException("Unexpected end of input", 0);
        } else{
            throw new ParseException("Unexpected symbol " + lookahead + " found", 0);
        }
    }
}