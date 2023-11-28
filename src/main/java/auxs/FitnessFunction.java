package auxs;

public class FitnessFunction {

	public static double evaluate(Individual ind, String functionName) {
		switch (functionName) {
		case "rastrigin":
			return rastrigin(ind);
		}
		return Double.NaN;
	}

	private static double rastrigin(Individual ind) {
		// Rastrigin function
        double value = 10.0d * ind.getDataValue().length;
        
        for(int i = 0; i < ind.getDataValue().length; i++){
            value = value + Math.pow(ind.getDataValue()[i], 2.0) - 10.0 * Math.cos(2 * Math.PI * ind.getDataValue()[i]); 
        }
              
        return value;

	}
	
}
