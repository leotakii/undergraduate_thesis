package auxs;

import java.util.List;
import java.util.Random;


public class Metaheuristic {
	protected Random random;	   // to generate random seed
	protected int MAX_ITERATIONS;  // number of iterations
	protected List<Individual> population; // list of generated individuals
	protected int POPULATION_SIZE; // population size to generate at the beginning 
	protected String FITNESS_FUNC; // name of the fitness function
	protected boolean minimize;    // for fitness comparison
	
	protected Individual bestIndividual(Individual a, Individual b) {
		int best = Individual.compare(a,b);
		if(minimize) {
			if(best == -1) {
				return a;
			}
			
			else {
				return b;
			}
		}
		
		else {
			if(best == 1) {
				return b;
			}
			
			else {
				return a;
			}
		}
		
		
	}
	


}
