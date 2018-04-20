package midlab.myse.ann.test;

import java.io.IOException;

import midlab.myse.ann.ANN;

/**
 * Test class for ANN of MYSE
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
public class Test {

	public static void main(String[] args) throws IOException{
		
		ANN ann = createANN();
		
		testANN(ann);
	}
	
	private static ANN createANN() throws IOException{
		
		// ANN params
		int inp=5;
		int outp=1;
		int[] hid = {5};
		
		// learning params
		double EPS=0.25;
		double momentum=0.5;
		int iter = 90;
		
		// other params
		int timeAggregation = 60; // aggregation of 60 minutes
		double scale = 1.0;       // scaled output
		
		// create the network
		ANN ann = new ANN(EPS, momentum, iter, inp, hid, outp, timeAggregation, scale/*, false*/,"");
		
		return ann;
	}
	
	private static void testANN(ANN ann) throws IOException{
		ann.randomizeWeight();
		ann.train("C:\\Users\\myse\\MYSE\\test\\training-test\\training.txt");
		ann.test("C:\\Users\\myse\\MYSE\\test\\training-test\\test.txt");
	}
}
