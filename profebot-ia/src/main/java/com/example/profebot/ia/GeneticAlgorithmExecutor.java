package com.example.profebot.ia;

import com.example.profebot.ia.algorithm.GeneticAlgorithm;
import java.util.ArrayList;
import java.util.List;
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
        responses.add(getNewExpressionFrom(aContextExpression));
        responses.add(getNewExpressionFrom(aContextExpression));
        responses.add(getNewExpressionFrom(aContextExpression));
        return responses;
    }

    private static ExpressionResponse getNewExpressionFrom(String baseExpression){
        ExpressionResponse response;
        do {
          GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(baseExpression);
          String mostSimilarExpression = geneticAlgorithm.getExpressionMostSimilar();
          Double similarity = geneticAlgorithm.getSimilarExpressionCalculator().similarityWith(mostSimilarExpression);
          response = new ExpressionResponse(mostSimilarExpression, similarity);
        }while(!response.isValid());
        return response;
    }

}
