package com.example.profebot.ia.algorithm;

import static com.example.profebot.ia.config.GeneticAlgorithmConfig.CHROMOSOME;
import static com.example.profebot.ia.config.GeneticAlgorithmConfig.INITIAL_POPULATION_SIZE;
import static com.example.profebot.ia.config.GeneticAlgorithmConfig.MIN_ITERATIONS;
import static com.example.profebot.ia.config.GeneticAlgorithmConfig.MUTATION_PROB;

import com.example.profebot.ia.fitness.NeuralNetworkSimilarExpressionCalculator;
import com.example.profebot.ia.fitness.ProceduralSimilarExpressionCalculator;
import com.example.profebot.ia.fitness.SimilarExpressionCalculator;
import com.example.profebot.ia.parser.Parser;
import io.jenetics.Genotype;
import io.jenetics.Mutator;
import io.jenetics.Phenotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.ext.SingleNodeCrossover;
import io.jenetics.ext.util.TreeNode;
import io.jenetics.prog.ProgramGene;

import java.time.Duration;

public class GeneticAlgorithm {

  // Define the structure of solutions (max tree depth, operations and terminals to consider, etc)
  private static final Codec<ProgramGene<Double>, ProgramGene<Double>> CODEC = Codec.of(
      Genotype.of(CHROMOSOME),
      Genotype::getGene
  );
  // Define the fitness function
  // static final SimilarExpressionCalculator SIMILAR_EXPRESSION_CALCULATOR = new NeuralNetworkSimilarExpressionCalculator(EXPRESSION);
  private static SimilarExpressionCalculator SIMILAR_EXPRESSION_CALCULATOR;

  public GeneticAlgorithm(String candidate) {
    SIMILAR_EXPRESSION_CALCULATOR = new ProceduralSimilarExpressionCalculator(candidate);
  }

  public GeneticAlgorithm(String candidate, Boolean useNeuralNetworkFitness) {
    SIMILAR_EXPRESSION_CALCULATOR = useNeuralNetworkFitness ? new NeuralNetworkSimilarExpressionCalculator(candidate) :
        new ProceduralSimilarExpressionCalculator(candidate);
  }

  private static final Double fitnessFunction(final ProgramGene<Double> expression) {
    String otherExpression = new Parser().getAsInfix(TreeNode.ofTree(expression));
    return SIMILAR_EXPRESSION_CALCULATOR.similarityWith(otherExpression);
  }

  public static void showGeneration(EvolutionResult<ProgramGene<Double>, Double> generation) {
    TreeNode bestCandidate = TreeNode.ofTree(generation.getBestPhenotype().getGenotype().getGene());
    String candidateAsInfix = new Parser().getAsInfix(bestCandidate);

    System.out.println(
        "Generation: " + generation.getGeneration() + "; " +
            "Best fitness: " + SIMILAR_EXPRESSION_CALCULATOR.similarityWith(candidateAsInfix) + "; " +
            "Best genotype: " + candidateAsInfix);
  }

  public String getExpressionMostSimilar() {
    Engine<ProgramGene<Double>, Double> engine = Engine.builder(GeneticAlgorithm::fitnessFunction, CODEC)
        .alterers(
            new Mutator<>(MUTATION_PROB),
            new SingleNodeCrossover<>())
        .populationSize(INITIAL_POPULATION_SIZE)
        .executor(Runnable::run)
        .maximizing()
        .build();

    Phenotype<ProgramGene<Double>, Double> bestExpression = engine.stream()
        .limit(Limits.byExecutionTime(Duration.ofSeconds(1)))
        .peek(GeneticAlgorithm::showGeneration)
        .collect(EvolutionResult.toBestPhenotype());

    TreeNode bestCandidate = TreeNode.ofTree(bestExpression.getGenotype().getGene());
    return new Parser().getAsInfix(bestCandidate);
  }

  public SimilarExpressionCalculator getSimilarExpressionCalculator() {
    return SIMILAR_EXPRESSION_CALCULATOR;
  }
}
