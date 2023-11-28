package auxs;

import java.util.List;
import java.util.Random;
import java.util.stream.DoubleStream;

public class Individual implements Cloneable {
    
    // each element a value from valid range for a given dimension
    private double[] dataValue;
    private double fitness;
       
    
    public Individual(List<double[]> dimensionIn, Random random){
        int noDimension = dimensionIn.size();       
        // initialize data vector
        dataValue = new double[noDimension];
             
        // for each dimension, create corresponding data point between given range
        for(int dimensionIndex = 0; dimensionIndex < noDimension; dimensionIndex++){
            
            double dimensionLowerBound = dimensionIn.get(dimensionIndex)[0];
            double dimensionUpperBound = dimensionIn.get(dimensionIndex)[1];        
            
            DoubleStream valueGenerator = random.doubles(dimensionLowerBound, dimensionUpperBound);
            
            dataValue[dimensionIndex] = valueGenerator.iterator().nextDouble();
            
            fitness = Double.NaN;
        }
    }
    
    @Override
    public String toString(){
        
        String string = "";
        
        for (int i = 0; i < dataValue.length; i++) {
            string += Double.toString(dataValue[i]);
            
            if((i + 1) != dataValue.length){
                string += ",";
            }
        }
        
        return string;
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static double fitnessFunction(Individual i, String functionName) {
    	if( Double.isNaN(i.fitness) ) {
    		i.setFitness(FitnessFunction.evaluate(i, functionName));
    	}
    	return i.getFitness();
    	
    }
    
    public static int compare(Individual a, Individual b) {
    	if(a.fitness < b.fitness) {
    		return -1;
    	}
    	else {
    		return 1;
    	}
    }
    
    public double[] getDataValue() {
    	return this.dataValue;
    }
    
    public double getFitness() {
    	return this.fitness;
    }
    
    public void setFitness(double fitness) {
    	this.fitness = fitness;
    }
}
