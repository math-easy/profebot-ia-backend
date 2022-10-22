package com.example.profebotia;

import java.util.ArrayList;
import java.util.List;

public class GeneticAlgorithmExecutor {
    public static EquationsResponse execute(String aTermExpression, String aContextExpression, String root){
        return new EquationsResponse(getMostSimilarExpressionTo(aTermExpression, "".equals(aContextExpression) ? aTermExpression : aContextExpression), root);
    }

    private static List<ExpressionResponse> getMostSimilarExpressionTo(String aTermExpression, String aContextExpression){
        List<ExpressionResponse> responses = new ArrayList<>();
        responses.add(getNewExpressionFrom(aTermExpression));
        responses.add(getNewExpressionFrom(aContextExpression));
        responses.add(getNewExpressionFrom(aContextExpression));
        responses.add(getNewExpressionFrom(aContextExpression));
        return responses;
    }

    private static ExpressionResponse getNewExpressionFrom(String baseExpression){
        ExpressionResponse response = ExpressionResponse.empty();
        do {
            //response = executor.submit(new Task(baseExpression)).get(5, TimeUnit.SECONDS);
            // TODO: FIX
            response = new ExpressionResponse("x + 2 = 1",0.75);
        }while (!response.isValid());

        return response;
    }

/*    static class Task implements Callable<ExpressionResponse> {

        private String baseExpression;

        public Task(String aBaseExpression){
            baseExpression = aBaseExpression;
        }

        @Override
        public ExpressionResponse call() throws Exception {
            GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(this.baseExpression);
            String mostSimilarExpression = geneticAlgorithm.getExpressionMostSimilar();
            Double similarity = geneticAlgorithm.getSimilarExpressionCalculator().similarityWith(mostSimilarExpression);
            return new ExpressionResponse(mostSimilarExpression, similarity);
        }
    }*/
}
