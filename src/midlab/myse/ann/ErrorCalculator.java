package midlab.myse.ann;

/**
 * ErrorCalculator class of ANN of MYSE
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
public class ErrorCalculator {
	
	/**
	 * Calculate the Mean Absolute Error (MAE)
	 * @param testset: is the test set with the real value
	 * @param prev: is the array containing the predicted value to be compared for the error calculation
	 * @return the double indicating the MAE
	 */
	public static double MAE(double[][] testset, double[] prev) {
		int count = 0;
		double sum = 0.0;
		for (int i = 0; i < testset.length; i++) {
			sum += Math.abs(testset[i][0] - prev[i]);
			count++;
		}
		return (sum/count);
	}
	
	/**
	 * Calculate the Root Mean Square Error (RMSE)
	 * @param testset: is the test set with the real value
	 * @param prev: is the array containing the predicted value to be compared for the error calculation
	 * @return the double indicating the RMSE
	 */
	public static double RMSE(double[][] testset, double[] prev) {
		int count = 0;
		double sum = 0;
		for (int i = 0; i < testset.length; i++) {
			sum += Math.pow(testset[i][0] - prev[i], 2);
			count++;
		}
		return Math.sqrt((sum/count));
	}
}

