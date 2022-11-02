package com.example.profebot.ia;

import com.example.profebot.ia.algorithm.GeneticAlgorithm;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.scheduling.config.Task;

public class GeneticAlgorithmExecutor {
    public static EquationsResponse execute(String aTermExpression, String aContextExpression, String root){
      List<ExpressionResponse> list = getMostSimilarExpressionTo(aTermExpression, "".equals(aContextExpression) ? aTermExpression : aContextExpression);
      EquationsResponse response = new EquationsResponse(list, root);
      return response;
    }

    private static List<ExpressionResponse> getMostSimilarExpressionTo(String aTermExpression, String aContextExpression){
        List<ExpressionResponse> responses = new ArrayList<>();
        responses.add(getNewExpressionFrom(aTermExpression));
        return responses;
    }

    private static ExpressionResponse getNewExpressionFrom(String baseExpression){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ExpressionResponse response = ExpressionResponse.empty();
        int times = 0;
        do {
            try {
                response = executor.submit(new Task(baseExpression)).get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.out.println("\n\n\n\nTimeout: " + e.getMessage() + "\n\n\n\n");
                for (Thread thread : Thread.getAllStackTraces().keySet()) {
                    if (thread.getName().contains("pool-") && thread.getName().contains("thread-")) {
                        System.out.println("Thread stopped: " + thread.getName());
                        thread.stop();
                    }
                }
            }
            times++;
        }while (!response.isValid() && times < 10);

        return response;
    }

    static class Task implements Callable<ExpressionResponse> {

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
    }

}
