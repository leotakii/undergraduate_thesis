package auxs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.DoubleStream;

public class DE extends Metaheuristic { //Standard DE implementation
    
    
    
    // crossover probability [0.01,1]
    private double CROSSOVER_PROBABILITY;
    
    // differential weight [0.01,2]
    private double DIFFERENTIAL_WEIGHT;

       
    PrintWriter pw;  
 
  
    public DE(int maxIterations, int populationSize, double crossoverProbability, double differentialWeight, String fitnessFunc, boolean minimize) { 
    	
        this.random = new Random();
        this.MAX_ITERATIONS = maxIterations;
        this.population = new LinkedList<>();
        this.POPULATION_SIZE = populationSize;
        this.CROSSOVER_PROBABILITY = crossoverProbability;
        this.DIFFERENTIAL_WEIGHT = differentialWeight;
        this.FITNESS_FUNC = fitnessFunc;
        this.minimize = minimize;
                
    }
    
    public static void main(String[] args){
        
        // define lower/ upper bounds for each required dimensions

        // define lower/ upper bounds for 1st dimension
        double[] dimension1Bounds = new double[2];
        dimension1Bounds[0] = -5.12d;
        dimension1Bounds[1] = 5.12d;
       /* 
        // define lower/ upper bounds for 2nd dimension
        double[] dimension2Bounds = new double[2];
        dimension2Bounds[0] = 80.0d;
        dimension2Bounds[1] = 160.0d;
        
        // define lower/ upper bounds for 3rd dimension
        double[] dimension3Bounds = new double[2];
        dimension3Bounds[0] = 100.0d;
        dimension3Bounds[1] = 200.0d;
*/
        // add all dimension to a list, and this will be passed to DE
        List<double[]> dimensionList = new LinkedList<>();
        
        dimensionList.add(dimension1Bounds);
       // dimensionList.add(dimension2Bounds);
      //  dimensionList.add(dimension3Bounds);
      
        
        DE de = new DE(40, 20, 0.7, 1, "rastrigin",false);
        
        // start optimizing process and return the best candidate after number of specified iteration
        Individual bestIndividual = de.optimize(dimensionList);
        Double bestResult = new Double(Individual.fitnessFunction(bestIndividual, de.FITNESS_FUNC));
        String objective;
        
        System.out.println("Best combination found: "+de.FITNESS_FUNC+"(" + bestIndividual.getDataValue()[0] +") = "+ bestResult  );             
    }
    
    
        
    // DE constructor

    
    public Individual optimize(List<double[]> dimensionList){
         
        // generate population up to the define limit
        for(int i = 0; i < POPULATION_SIZE; i++){
            Individual individual = new Individual(dimensionList, this.random);
            population.add(individual);
            
        }       
                
       // try more than one iteration 
       for(int iterationCount = 0; iterationCount < this.MAX_ITERATIONS; iterationCount++){
           
            try {
                pw = new PrintWriter(new File("data/popoluation_" + Integer.toString(iterationCount) +".csv"));
            } catch (FileNotFoundException ex) {
                System.out.println("Oh no");
            }    
            
            
            for(int n = 0; n < dimensionList.size(); n++){      
                pw.write("v" + Integer.toString(n));
                pw.write(",");
            }
        
            pw.write("fValue");

            pw.write("\n");
        
            for (Individual individual : population) {
                pw.write(individual.toString());
                pw.write(",");
                pw.write(Double.toString(Individual.fitnessFunction(individual, FITNESS_FUNC)));
                pw.write("\n");
            }
            
            pw.flush();
            int loop = 0;
        
            // main loop for evolution
            while(loop < population.size()){       

	            Individual original = null;
	            Individual candidate = null;
	            boolean boundsHappy;
	
	            do{
	                boundsHappy = true;
	                // pick an agent from the the population
	                int x = loop;
	                int a,b,c = -1;
	
	                // pick three random agents from the population
	                // make sure that they are not identical to selected agent from
	                // the population 
	
	                do{
	                    a = random.nextInt(population.size());
	                }while(x == a);
	                do{
	                    b = random.nextInt(population.size());
	                }while(b==x || b==a);
	                do{
	                    c = random.nextInt(population.size());
	                }while(c == x || c == a || c == b);
	
	                // create three agent individuals
	                Individual individual1 = population.get(a);
	                Individual individual2 = population.get(b);
	                Individual individual3 = population.get(c);
	
	                // create a noisy random candidate
	                Individual noisyRandomCandidate = new Individual(dimensionList, this.random);
	
	                // mutation process
	                // if an element of the trial parameter vector is
	                // found to violate the bounds after mutation and crossover, it is reset in such a way that the bounds
	                // are respected (with the specific protocol depending on the implementation)
	                for(int n = 0; n < dimensionList.size(); n++){     
	                    noisyRandomCandidate.getDataValue()[n] = (individual1.getDataValue()[n] + DIFFERENTIAL_WEIGHT * (individual2.getDataValue()[n] - individual3.getDataValue()[n]));               
	                }           
	
	                // Create a trial candidate 
	                original = population.get(x);
	                candidate = new Individual(dimensionList, this.random);
	
	                // copy values from original agent to the candidate agent
	                for(int n = 0; n < dimensionList.size(); n++){             
	                    candidate.getDataValue()[n] = original.getDataValue()[n];
	                }  
	
	                // crossover process with the selected individual
	                // pick a random dimension, which definitely takes the value from the noisy random candidate
	                int R = random.nextInt(dimensionList.size());
	
	                for(int n = 0; n < dimensionList.size(); n++){
	
	                    double crossoverProbability = random.nextDouble();
	
	                    if(crossoverProbability < CROSSOVER_PROBABILITY || n == R){
	                        candidate.getDataValue()[n] = noisyRandomCandidate.getDataValue()[n];
	                    }
	
	                }
	
	                // check here if the trial candidate satisfies bounds for each value
	                for(int n = 0; n < dimensionList.size(); n++){ 
	                    if(candidate.getDataValue()[n] < dimensionList.get(n)[0] || candidate.getDataValue()[n] > dimensionList.get(n)[1]){
	                       boundsHappy = false;
	                    }
	                }
	
	            }while(boundsHappy == false);
	
	                //see if the candidate is better than original, if so replace it
		            if(this.minimize) {
		                if(Individual.fitnessFunction(original, FITNESS_FUNC) > Individual.fitnessFunction(candidate, FITNESS_FUNC)){
		                        population.remove(original);
		                        population.add(candidate);     
		                }
		                
		            }
		            
		            else {
		                if(Individual.fitnessFunction(original, FITNESS_FUNC) > Individual.fitnessFunction(candidate, FITNESS_FUNC)){
		                        population.remove(original);
		                        population.add(candidate);     
		                }
		                
		            }
		            loop++;
            }        
        }
        
       Individual bestFitness = new Individual(dimensionList, this.random);
   
       // selecting the final best agent from the the population
       for(int i = 0; i < population.size(); i++){
           Individual individual = population.get(i);
           	
           if(this.minimize) {
        	   if(Individual.fitnessFunction(bestFitness, FITNESS_FUNC) > Individual.fitnessFunction(individual, FITNESS_FUNC)){
                   
                   try {
                       bestFitness = (Individual) individual.clone();
                       //System.out.println(bestFitness.getFitness());
                   } catch (CloneNotSupportedException ex) {
                      
                   }
                }
           }
          
           else {
        	   if(Individual.fitnessFunction(bestFitness, FITNESS_FUNC) < Individual.fitnessFunction(individual, FITNESS_FUNC)){
                   
                   try {
                       bestFitness = (Individual) individual.clone();
                   } catch (CloneNotSupportedException ex) {
                      
                   }
                }
           }
            
       }
       
        
       return bestFitness;
    }
            
}