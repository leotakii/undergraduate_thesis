/*
 * Copyright 2016 Jackson Antonio do Prado Lima <jacksonpradolima at gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.ufpr.inf.gres.irace.core;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 *
 * @author Jackson Antonio do Prado Lima <jacksonpradolima at gmail.com>
 * @version 1.0
 */
@Parameters(separators = "=")
public class HookRunCommands {

    @Parameter(names = {"-help", "-h"}, help = true)
    public boolean help;
    
    @Parameter(names = "--algorithm", description = "Metaheuristic (String)")
	public String algorithm;
    
    @Parameter(names = "--fitnessFunction", description = "Fitness function for the metaheuristic (String)")
    public String fitnessFunction;
    
    @Parameter(names = {"--directory", "-dir"}, description = "Directory name (String)", required = true)
    public String directory;
    
    @Parameter(names = {"--candidateId", "-ci"}, description = "ID of this execution. Used for differentiating the output files. (int)", required = true)
    public int candidateId;
    
    //parametros de desempenho
    @Parameter(names = "--populationSize", description = "Population size (int)", required = true)
    public int populationSize;

    @Parameter(names = "--maxEvaluations", description = "Maximum Evaluations (int)", required = true)
    public int maxEvaluations;
    
    @Parameter(names = "--c1", description = "C1 PSO (double)")
    public double c1;
    
    @Parameter(names = "--c2", description = "C2 PSO (double)")
    public double c2;
    
    @Parameter(names = "--wIn", description = "wIn PSO (double)")
    public double wIn;
    
    @Parameter(names = "--wF", description = "wF PSO (double)")
    public double wF;
    
    @Parameter(names = "--itReset", description = "Iterations until reset PSO (int)")
    public int itReset;
    
    @Parameter(names = "--pReset", description = "Probability to force reset PSO (double)")
    public double pReset;
    
    @Parameter(names = "--crossoverProbability", description = "Crossover Probability DE and GA (double)")
    public double crossoverProbability;

    @Parameter(names = "--mutationProbability", description = "Mutation Probability GA (double)")
    public double mutationProbability;
    
    @Parameter(names = "--selectionGA", description = "Selection algorithm GA (String)")
    public String selectionGA;
    
    @Parameter(names = "--crossoverGA", description = "Crossover algorithm GA (String)")
    public String crossoverGA;
    
    @Parameter(names = "--mutationGA", description = "Mutation algorithm GA (String)")
    public String mutationGA;
    
    @Parameter(names = "--elitistRateGA", description = "Elitist rate for GA (double)")
    public double elitistRateGA;
    
    @Parameter(names = "--differentialWeight", description = "Differential Weight DE (double)")
    public double differentialWeight;
    
    @Parameter(names = "--generationReplacement" , description = "Generation replacement e. g. Generational or SteadyState for GA (String)")
    public String generationReplacement;
    
    @Parameter(names = "--statistics" , description = "Obtain data for statistical analysis (boolean)")
    public boolean statistics;
    
    


//    @Parameter(names = "--archiveSize", description = " Archive Size in relation the Population Size (double)")
//    public double archiveSize;

    @Override
    public String toString() {
    	
    	switch (algorithm) {
    	case "GA":
    		return "--directory=" + directory + "\n"
    		+ "--algorithm=" + algorithm + "\n"
    		+ "--selectionGA=" + selectionGA + "\n"
    		+ "--crossoverGA=" + crossoverGA + "\n"
    		+ "--mutationGA=" + mutationGA + "\n"
    		+ "--elitistRateGA=" + elitistRateGA + "\n"
    		+ "--fitnessFunction=" + fitnessFunction + "\n"
    		+ "--candidateId=" + candidateId + "\n"
            + "--populationSize=" + populationSize + "\n"
            + "--maxEvaluations=" + maxEvaluations + "\n"
            + "--crossoverProbability=" + crossoverProbability + "\n"
            + "--mutationProbability=" + mutationProbability + "\n"
    		+ "--generationReplacement=" + generationReplacement + "\n";

    	case "DE":
    		return "--directory=" + directory + "\n"
    		+ "--algorithm=" + algorithm + "\n"
    		+ "--fitnessFunction=" + fitnessFunction + "\n"
    		+ "--candidateId=" + candidateId + "\n"
            + "--populationSize=" + populationSize + "\n"
            + "--maxEvaluations=" + maxEvaluations + "\n"
            + "--crossoverProbability=" + crossoverProbability + "\n"
            + "--differentialWeight=" + differentialWeight + "\n";
    		
    	case "PSO":
    		return "--directory=" + directory + "\n"
    		+ "--algorithm=" + algorithm + "\n"
    		+ "--fitnessFunction=" + fitnessFunction + "\n"
    		+ "--candidateId=" + candidateId + "\n"
            + "--populationSize=" + populationSize + "\n"
            + "--maxEvaluations=" + maxEvaluations + "\n"
            + "--c1=" + c1 + "\n"
            + "--c2=" + c2 + "\n"
            + "--wIn=" + wIn + "\n"
            + "--wF=" + wF + "\n"
            + "--itReset=" + itReset + "\n"
            + "--pReset=" + pReset + "\n";

    		
    	default:
    		return "ERRO NOS PARÂMETROS";
    	}
        
    }
}
