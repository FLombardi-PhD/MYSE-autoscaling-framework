package midlab.myse.ann;

import java.util.*;
import java.io.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ANN main class of MYSE
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
public class ANN {

	/**
	 * Logger
	 */
	private static final Logger log4j = LogManager.getLogger("midlab");
	
	private double scale;
	private int timeAggregation;
	private double max;
	private double EPS;
	@SuppressWarnings("unused")
	private double momentum;
	private int iter;
	private int inp, outp; // num of input and output nodes
	@SuppressWarnings("unused")
	private int[] hid;     // num of neurons in hidden layers
	private int[] layers;  // num of neurons total inp-hid-out
	
	private Network net;
	
	private String globalPath;
	private String path;
	
	/**
	 * Get the scale value of the ANN
	 * @return the scale value
	 */
	public double getScale(){
		return this.scale;
	}
		
	
	/**
	 * Build an ANN with the BackPropagation learning algorithm parameters
	 * @param EPS: is the leraning rate
	 * @param momentum: momentum term
	 * @param iter: default backpropagation iteration
	 * @param inp: number of input nodes
	 * @param hid: array of hidden layers
	 * @param outp: number of output nodes
	 * @param timeAggregation: is the value (in minutes) of dataset granularity (e.g. =15 means 4 entry per hour)
	 * @param scale: is the scale of the dataset. Default value is 1.0
	 * @throws IOException
	 */
	public ANN(double EPS, double momentum, int iter, int inp, int[] hid, int outp, int timeAggregation, double scale, String globalPath) throws IOException{
		
		// offline version:
		this.globalPath = "C:/Users/Utente/Dropbox/MYSE/TEST/";
		// online version example: this.globalPath = "/home/myse/MYSE/TEST/";
		
		path = globalPath+System.currentTimeMillis()+"/";
		
		// create the directory
		boolean success = (new File(path)).mkdir();
		if(success){
			log4j.debug("directory "+path+" successful created.");
				
			// ann parameters
			this.EPS = EPS;
			this.momentum = momentum;
			this.iter = iter;
			this.inp = inp;
			this.hid = hid;
			this.outp = outp;
			this.timeAggregation = timeAggregation;
			this.scale = scale;
			
			
			// building layer
			layers = new int[hid.length+2];
			layers[0] = inp;
			layers[layers.length-1] = outp;
			for(int i=1; i<=hid.length; ++i){
				layers[i] = hid[i-1];
			}	
		
			// check layers
			String layersString = "";
			for(int i=0; i<layers.length; ++i){
				layersString += layers[i];
				if(i!=layers.length-1) layersString += "-";
			}
			log4j.debug("creating the ANN "+layersString);			
			
			// building ann
			this.net = new Network(EPS, momentum, layers, layers.length);

			log4j.debug("ANN created successfully.");
			printAnn();
		}
		else{
			log4j.error("could not create directory "+path+".");
			this.net = new Network(EPS, momentum, layers, layers.length);
		}
		
	}
	
	public String printAnn() throws FileNotFoundException{
		String pathAnn = path+"ANN_";
		for(int i=0; i<layers.length; ++i){
			if(i!=layers.length-1) pathAnn+=layers[i]+"-";
			else pathAnn+=layers[i]+".txt";
		}
		
		log4j.debug("updating "+pathAnn+"...");
		this.net.printNet(pathAnn);
		
		log4j.debug("ANN updated successfully.");
		return pathAnn;
	}

	/**
	 * Randomize the weights of the ann
	 * @throws FileNotFoundException
	 */
	public void randomizeWeight() throws FileNotFoundException{
		log4j.debug("randomizing ANN weights...");
		this.net.randomizeWeightAndBiases();
		
		log4j.debug("ANN weights randomized successfully.");
		printAnn();
	}

		
	/**
	 * Get the max value
	 * @return a double with the max observed value
	 */
	public double getMax(){
		return this.max;
	}
	
	/**
	 * Set the max value
	 * @param output: is the matrix
	 */
	public void setMax(double[][] output){
		double newMax = 0.0;
		for(int i=0; i<output.length; ++i){
			if(output[i][0]>newMax) newMax = output[i][0];
		}
		this.max = newMax;
		this.net.setMax(newMax);
	}
	
	
	/**
	 * Get the path
	 * @return the path of the specific ann
	 */
	public String getPath(){
		return this.path;
	}
	
	/**
	 * Get the global path
	 * @return the global path of the project
	 */
	public String getGlobalPath(){
		return this.globalPath;
	}
	
	/**
	 * Allow to change the iteration of the backpropagation algorithm
	 * @param iter: is the number of iteration to set
	 */
	public void setIter(int iter){
		this.iter = iter;
	}
	
	
	/**
	 * Forecast the next value of the test set given in input
	 * @param fileTest: is the test set
	 * @param dimTest: is the dimension of the test set
	 * @return an array which is the output layer
	 * @throws IOException
	 */
	@SuppressWarnings("unused") // TODO: unused some dobule[][] array; check to remove them
	public double[] forecast(String fileTest, int dimTest) throws IOException{
		Object[] testSet = fillTestSet(fileTest, dimTest);
		double[][] testInput = (double[][]) testSet[0];
		double[][] testOutput = (double[][]) testSet[1];     // unused
		double[][] testOutputReal = (double[][]) testSet[2]; // unused
		double[][] testInputReal = (double[][]) testSet[3];  // unused
		
		net.setInputs(testInput[0]); // testInput[0] contain the only row to forecast in the test set
		double[] outputs = net.getOutput();
		
		String day = "";
		String date = "";
		String month = "";
		String hour = "";
		String value = "";
		for(int i=0; i<testInput[0].length; ++i){
			value = String.valueOf(Math.round(maxMinDenormalization(i, testInput[0][i])));
			if(value.contains("10")){
				if(i!=2)value.replaceAll(".0", "");
			}
			else value.replaceAll(".0", "");
			if(i==0){
				if(value.equals("0")) day = "sunday";
				if(value.equals("1")) day = "monday";
				if(value.equals("2")) day = "thusday";
				if(value.equals("3")) day = "wednesday";
				if(value.equals("4")) day = "thursday";
				if(value.equals("5")) day = "friday";
				if(value.equals("6")) day = "saturday";				
			}
			if(i==1) date = value;
			if(i==2){
				if(value.equals("1")) month = "jan";
				if(value.equals("2")) month = "feb";
				if(value.equals("3")) month = "mar";
				if(value.equals("4")) month = "apr";
				if(value.equals("5")) month = "may";
				if(value.equals("6")) month = "jun";
				if(value.equals("7")) month = "jul";
				if(value.equals("8")) month = "aug";
				if(value.equals("9")) month = "sep";
				if(value.equals("10")) month = "opt";
				if(value.equals("11")) month = "nov";
				if(value.equals("12")) month = "dec";
			}
			if(i==3) hour = value;
			System.out.println("[ANN.forecast]: forecasting "+value);
			
		}
		log4j.info("forecasting "+day+", "+date+"/"+month+" h: "+hour+":00");
		return outputs;
	}
	
	
	/**
	 * Training of the ann
	 * @param fileTraining: is the training set file
	 * @throws IOException
	 */
	// WARNING: to switch according to which dataset to use: fillGenericTrain or fillTrain
	public void train(String fileTraining) throws IOException{
			
		log4j.info("TRAINING");
		
		log4j.debug("counting rows of "+fileTraining+".. ");
		int dimTraining = Tools.countRowsFile(fileTraining);
		log4j.debug("done. Counted "+dimTraining);
		
		/* TODO: SWITCH HERE!
		 * Object[] trainingIO = fillTrainingSet(fileTraining, dimTraining);
		 * Object[] trainingIO = fillGenericTrainingSet(fileTraining, dimTraining);
		 */
		
		Object[] trainingIO = fillTrainingSet(fileTraining, dimTraining);
		
		double[][] trainingInputs = (double[][]) trainingIO[0];
		double[][] trainingOutputs = (double[][]) trainingIO[1];
		
		@SuppressWarnings("unused") //TODO: to check whether to remove it
		double[][] trainingOutputsVero = (double[][]) trainingIO[2];
		
		// training phase
		log4j.info("start training...");
		for (int i = 0; i <= iter; i++) {
			if(i==iter) log4j.debug(i+" training iterations done. Training complete. New max="+max);
			for (int j = 0; j < dimTraining; j++) {
				net.trainMomentum(trainingInputs[j], trainingOutputs[j]);
			}
		}
		printAnn();
	}
	
		
	/**
	 * Test the ann accuracy on a test set and print for each output node a file containing the accuracy
	 * of prediction in all the entry of the test set
	 * @param fileTest: is the test set file
	 * @throws IOException
	 */
	// WARNING: to switch according to which dataset to use: fillGenericTest or fillTest
	public void test(String fileTest) throws IOException{
		
		System.out.println("\n***************** TEST *****************");
		
		System.out.print("[ANN.test]:  counting rows of "+fileTest+".. ");
		int dimTest = Tools.countRowsFile(fileTest);
		System.out.println(dimTest);
		
		String resultFilename = printFileName();
		PrintWriter[] resultTest = new PrintWriter[outp];
		String pathTest = path;
		for(int i=0; i<outp; ++i){
			String s = "TEST-"+resultFilename;
			s = s.replaceFirst(".txt", "-out"+i);
			
			@SuppressWarnings("unused") //TODO: check whether to remove it
			boolean success = (new File(pathTest)).mkdir();
			
			resultTest[i] = new PrintWriter(pathTest+s+".txt");
			resultTest[i].println(s);
			resultTest[i].println("Predicted Value"+"\t"+"Real Value"+"\t"+"Error"+"\t"+"Predicted lambda"+"\t"+"Real lambda");
		}
		
		/* TODO: switch here
		 * Object[] testSet = fillTestSet(fileTest, dimTest);
		 * Object[] testSet = fillGenericTestSet(fileTest, dimTest);
		 */
		Object[] testSet = fillTestSet(fileTest, dimTest);
		
		double[][] testInput = (double[][]) testSet[0];
		double[][] testOutput = (double[][]) testSet[1];
		double[][] testOutputReal = (double[][]) testSet[2];
		double[][] testInputReal = (double[][]) testSet[3];
		
		// double[] outputs; // TODO: to extend with multiple outputs
		
		// variable sfor computing errors
		
		double[][] outputPrev = new double[outp][testOutput.length];
		
		//int contTest = 0; // TODO: check whether to remove it
		
		long forecastedTraffic = 0;
		long realTraffic = 0;
		long errorTraffic = 0;
				
		// TESTING
		System.out.println("[ANN.test]:  start testing...");
		for (int j = 0; j < testOutput.length; j++) { // TODO: change '2' with 'dimTraining'; currently is necessary only the first value
			System.out.println("[ANN.test]:  test case " + (j + 1));
			net.setInputs(testInput[j]); 
			double[] outputs = net.getOutput();
			
			for (int i = 0; i < inp; i++) {
				System.out.println("\t\tinput " + (i) + " : " + testInputReal[j][i] + " ("+testInput[j][i]+")");
			}
			for (int i = 0; i < outp; i++) {
				System.out.println("\t\tpredicted_output "+i+" = "+(outputs[i]*max)+"; actual_output = "+testOutput[j][i]*max+" ("+testOutputReal[j][i]+")");
				
				forecastedTraffic = Math.round(outputs[i]*max);
				realTraffic = Math.round(testOutput[j][i]*max); // changed: j+i and 0
				errorTraffic = Math.abs(realTraffic-forecastedTraffic);
				
				resultTest[i].print(forecastedTraffic+"\t"+realTraffic+"\t"+errorTraffic+"\t");
				
				// TODO: check wheter to remove: double lambdaReal = (double)((double)realTraffic/(double)3600.0);
				
				double lambdaReal = (double)((double)realTraffic/(double)60.0*(double)timeAggregation);
				double lambdaPrev = (double)((double)forecastedTraffic/(double)60.0*(double)timeAggregation);
				
				resultTest[i].println(lambdaPrev+"\t"+lambdaReal);
				errorTraffic = realTraffic-forecastedTraffic;
				
				outputPrev[i][j] = outputs[i];
			}
			System.out.println();
		}
		
		System.out.println("[ANN.test]:  test complete!");
		
		for(int i=0; i<outp; ++i){
			double MAE = ErrorCalculator.MAE(testOutput, outputPrev[i]);
			double RMSE = ErrorCalculator.RMSE(testOutput, outputPrev[i]);
			System.out.println("\t\tMAE="+MAE);
			System.out.println("\t\tRMSE"+RMSE);
			System.out.println();
			resultTest[i].println("\nMAE="+MAE);
			resultTest[i].println("RMSE="+RMSE);
			resultTest[i].close();
		}
		
	}
	
	
	/**
	 * Fill the training set taking as input a file
	 * @param fileTraining: is the training set file
	 * @param dimTraining: is the dimension of the training set
	 * @return an Object[] with 3 element: i=0 normalized input; i=1 normalized output; i=2 real output 
	 * @throws IOException
	 */
	private Object[] fillTrainingSet(String fileTraining, int dimTraining) throws IOException{
		
		int timeSeries = 1; // number of timeseries parameters passed as input
		
		// TODO: WARNING: currently it works only with timeSeries = 1
		
		// TODO: check if scan and reader below are necessary
		@SuppressWarnings({ "resource", "unused" })
		Scanner scan = new Scanner(new File(fileTraining));
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(new FileReader(fileTraining));
		
		double[][] input = new double[dimTraining][inp];   // normalized input of training set
		double[][] output = new double[dimTraining][outp]; // NON-normalized output of training set
			
		log4j.debug("filling training set...");
		
		// cycle to compute max and normalized first three input
		String row;
		String[] s;
		for(int i=0; i<input.length; i++){
			
			/* step 1: read a row
			 * step 2: split input and output
			 * step 3: first n-1 raws are input, the last one is the output
			 */
			
			row = reader.readLine();
			s = row.split("\t");
			for(int j=0; j<input[0].length; j++){
				/* normalized NON-timeseries variables (i.e. non previous traffic)
				 * and insert them in the input matrix
				 */
				if(j<input[0].length-timeSeries)
					input[i][j] = maxMinNormalization(j, Double.parseDouble(s[j]));
			}	
			// insert the output in the output matrix
			output[i][0] = Double.parseDouble(s[s.length-1])*scale;
		}
		
		// multioutput version
		for(int j=0; j<output.length; ++j){
			for(int k=1; k<outp; ++k){ // set (if any) other outputs as previous output
				if((j+k)<output.length){
					System.out.println("j="+j+"; k="+k+"; out.length="+output.length);
					double outSucc = output[j+k][0];
					output[j][k] = outSucc;
				}
			}
		}
		setMax(output);
		
		// cycle to normalize only input traffic
		reader = new BufferedReader(new FileReader(fileTraining));
		for(int i=0; i<input.length; i++){
			row = reader.readLine();
			s = row.split("\t");
			input[i][input[0].length-timeSeries] = 
					maxMinNormalization(4, Double.parseDouble(s[input[0].length-timeSeries]));
		}
		
		Object[] trainingIO = new Object[3]; // LEGENDA: i=0 input, i=1 nomalized output, i=2 real output
		trainingIO[0] = input;                          // normalized input
		trainingIO[1] = outputNormalization(4, output); // normalized output
		trainingIO[2] = output;                         // NON-normalized output
		
		log4j.debug("training set filled.");
		return trainingIO;
	}
	
	
	/**
	 * Fill the test set taking as input a file
	 * @param fileTestSet: the test set file
	 * @param dimTest: the dimension of the test set
	 * @return an Object[] with 4 element: i=0 normalized input; i=1 normalized output; i=2 real output; i=3 real input
	 * @throws IOException
	 */
	public Object[] fillTestSet(String fileTestSet, int dimTest) throws IOException{
		
		// TODO: check if scan and reader below are necessary
		@SuppressWarnings({ "resource", "unused" })
		Scanner scan = new Scanner(new File(fileTestSet));
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(new FileReader(fileTestSet));
		
		double[][] inputReal = new double[dimTest][inp]; // NON-normalized input of test set
		double[][] input = new double[dimTest][inp];     // normalized input of test set
		double[][] output = new double[dimTest][outp];   // NON-normalized output of test set
				
		log4j.debug("filling test set...");
		
		String row;
		String[] s;
		for(int i=0; i<input.length; i++){
			row = reader.readLine();
			s = row.split("\t");
			for(int j=0; j<input[0].length; j++){
				inputReal[i][j] = Double.parseDouble(s[j]);
				if(j<4) input[i][j] = maxMinNormalization(j, inputReal[i][j]);
				if(j==4)
					input[i][j] = maxMinNormalization(j, inputReal[i][j]);	
			}
			output[i][0] = Double.parseDouble(s[s.length-1])*scale;
		}
		
		// multioutput version: TODO: currently does not work as test here has dimension always 1. CHECK!!
		for(int j=0; j<output.length; ++j){
			for(int k=1; k<outp; ++k){ // set (if any) other output as previous output
				System.out.println("test: "+" j="+j+"; k="+k+"; out.length="+output.length);
				if((j+k)<output.length){
					System.out.println("test: "+" j="+j+"; k="+k+"; out.length="+output.length);
					double outSucc = output[j+k][0];
					output[j][k] = outSucc;
				}
			}
		}
				
				
		Object[] testIO = new Object[4];
		testIO[0] = input;
		if(max==0.0) setMax(output);
		testIO[1] = outputNormalization(4, output);
		testIO[2] = output;
		testIO[3] = inputReal;
		System.out.println("ANN_MLP.fillTestSet:  OUTPUT: "+output[0][0]);
		log4j.debug("test set filled.");
		return testIO;
	}
	
	/**
	 * Denormalize a value
	 * @param type: type of value to denormalize: 0=week day, 1=month day, 2=month, 3=hour, 4=load, 5=time aggregation
	 * @param value: the value to denormalize
	 * @return: the denormalized value
	 */
	public double maxMinDenormalization(int type, double value){
		
		double denormValue = 0.0;
		if(type==0){
			denormValue = (value*6.0);
		}
		if(type==1){
			denormValue = (value*31.0);
		}
		if(type==2){
			denormValue = (value*12.0);
		}
		if(type==3){
			denormValue = (value*23.0);
		}
		if(type==4){
			denormValue = (value*getMax());
		}
		if(type==5){
			denormValue = (value*(60/timeAggregation));
		}
		return denormValue;
	}
	
	/**
	 * Normalize a value
	 * @param type: type of value to normalize: 0=week day, 1=month day, 2=month, 3=hour, 4=load, 5=time aggregation
	 * @param value: the value to normalize
	 * @return: the normalized value
	 */
	public double maxMinNormalization(int type, double value){
		// Normalization [0, 1]
		/** LEGENDA TYPES:
		 * Type = 0: input0 - weekDay (from [0, 6]  to [0, 1])
		 * Type = 1: input1 - day     (from [1, 31] to [0, 1])
		 * Type = 2: input2 - month   (from [1, 12] to [0, 1])
		 * Type = 3: input3 - hour    (from [0, 23] to [0, 1])
		 * Type = 4: input4\output - traffico (from [0, max] to [0, 1])
		 * Type = 5: time aggregation (from [0, 60/timeAggregation] to [0, 1]
		 *****************************************************/
		
		double normValue = 0.0;
		if(type==0){
			normValue = (value/6.0);
		}
		if(type==1){
			normValue = (value/31.0);
		}
		if(type==2){
			normValue = (value/12.0);
		}
		if(type==3){
			normValue = (value/23.0);
		}
		if(type==4){
			normValue = (value/getMax());
		}
		if(type==5){
			normValue = (value/(60/timeAggregation));
		}
		return normValue;
	}
	
	/**
	 * Call the maxMinNormalization() method to normalize the output value
	 * @param type: type of value to normalize; for an output value choose type=4 or type=5 if using a time aggregation different from default one (60)
	 * @param output
	 * @return the normalized output value
	 */
	private double[][] outputNormalization(int type, double[][] output){
		double[][] norm = new double[output.length][this.outp];
		for(int i=0; i<output.length; ++i){
			for(int j=0; j<output[0].length; ++j){
				norm[i][j] = maxMinNormalization(type, output[i][j]);
			}
		}
		return norm;
	}
	
	
	/**
	 * Give a name to the ann for creating the related txt file
	 * @return the ann txt file name
	 */
	private String printFileName(){
		String nomeFile = "";
		for(int i=0; i<layers.length; ++i){
			nomeFile+=layers[i]+"-";
		}
		nomeFile+="EPS="+EPS+"-IT="+iter+"-TA="+timeAggregation+".txt";
		return nomeFile;
	}	
	

	/**
	 * Calculate the error according to the change of the number of the BackPropagation iterations
	 * @param fileTest: the test set on which calculate the errors
	 * @param iterationError: print the MAE and the RMSE of the test set
	 * @param testInput: the normalized input value of the test set
	 * @param testInputReal: the real input value of the test set
	 * @param testOutput: the normalized output value of the test set
	 * @param testOutputReal: the real output value of the test set
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void errorChangingNumberIteration(String fileTest, PrintWriter iterationError, double[][] testInput, double[][] testInputReal, double[][] testOutput, double[][] testOutputReal) throws FileNotFoundException, IOException{
		
		// retry to fill
		
		/* TODO: switch here:
		 * Object[] testSet = fillTestSet(fileTest, Tools.countRowsFile(fileTest));
		 * Object[] testSet = fillGenericTestSet(fileTest, dimTest);
		 */
		Object[] testSet = fillTestSet(fileTest, Tools.countRowsFile(fileTest));
		
		testInput = (double[][]) testSet[0];
		testOutput = (double[][]) testSet[1];
		testOutputReal = (double[][]) testSet[2];
		testInputReal = (double[][]) testSet[3];
		
		// variables to compte the error
		double[] outputs;
		double[] outputPrev = new double[testOutput.length];
		long forecastedTraffic = 0;
		long realTraffic = 0;
		long errorTraffic = 0;
		
		@SuppressWarnings("unused") // TODO: check whether to remove it
		long erroreTrafficAbs = 0;
		
		// cycle fr eac record in the test set
		for (int j = 0; j < testOutput.length; j++) { // change '2' with dimTraining (see similar comment above)
			System.out.println("Case number: " + (j + 1));			
			net.setInputs(testInput[j]);
			outputs = net.getOutput();
			for (int i = 0; i < inp; i++) {	}
			for (int i = 0; i < outp; i++) {
				forecastedTraffic = Math.round(outputs[i]*max);
				realTraffic = Math.round(testOutput[j+i][0]*max);
				errorTraffic = forecastedTraffic-realTraffic;
				erroreTrafficAbs = Math.abs(errorTraffic);
				outputPrev[j] = outputs[i];
				System.out.println("STAMPA: "+forecastedTraffic+" "+testOutput[j+i][0]*max+" ");
			}
		}
		
		// compute error on predicted set
		double MAE = ErrorCalculator.MAE(testOutput, outputPrev);
		double RMSE = ErrorCalculator.RMSE(testOutput, outputPrev);
		iterationError.print(MAE+"\t"+RMSE+"\t");
		
	}

	/**
	 * Calculate the error according to the change of the training set dimension
	 * @param fileTest: the test set on which calculate the errors
	 * @param iterationError: print the MAE and the RMSE of the test set
	 * @param testInput: the normalized input value of the test set
	 * @param testInputReal: the real input value of the test set
	 * @param testOutput: the normalized output value of the test set
	 * @param testOutputReal: the real output value of the test set
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void errorChangingTrainingDimension(String fileTest, PrintWriter iterationError, double[][] testInput, double[][] testInputReal, double[][] testOutput, double[][] testOutputReal) throws FileNotFoundException, IOException{
		
		// retry to fill
		
		/* TODO: switch here:
		 * Object[] testSet = fillTestSet(fileTest, Tools.countRowsFile(fileTest));
		 * Object[] testSet = fillGenericTestSet(fileTest, dimTest);
		 */
		Object[] testSet = fillTestSet(fileTest, Tools.countRowsFile(fileTest));
		
		testInput = (double[][]) testSet[0];
		testOutput = (double[][]) testSet[1];
		testOutputReal = (double[][]) testSet[2];
		testInputReal = (double[][]) testSet[3];
		
		// variables for computing the error
		double[] outputs;
		double[] outputPrev = new double[testOutput.length];
		long forecastedTraffic = 0;
		long realTraffic = 0;
		long errorTraffic = 0;
		long erroreTrafficAbs = 0;
		
		for (int j = 0; j < testOutput.length; j++) { // change '2' with 'dimTraining' (see similar comment above)
			net.setInputs(testInput[j]);
			for(int k=0; k<testInput[j].length; k++){
				System.out.print(" "+testInput[j][k]+" - ");
			}
			System.out.println();
			
			outputs = net.getOutput();
			for (int i = 0; i < outp; i++) {
				forecastedTraffic = Math.round(outputs[i]*max);
				realTraffic = Math.round(testOutput[j][0]*max);
				errorTraffic = forecastedTraffic-realTraffic;
				erroreTrafficAbs = Math.abs(errorTraffic);
				outputPrev[j] = outputs[i];
				System.out.println("ANN_MLP.ErrorChangingTrainingDimension: FORECASTED="+forecastedTraffic);
			}
		}
		
		// compute errors on forecasted set
		double MAE = ErrorCalculator.MAE(testOutput, outputPrev);
		double RMSE = ErrorCalculator.RMSE(testOutput, outputPrev);
		
		iterationError.print(MAE+"\t"+RMSE+"\t"+erroreTrafficAbs+"\t"+forecastedTraffic+"\t"+realTraffic+"\t");
		
	}
	
	/**
	 * Find the minimum error given an array of errors
	 * @param MAEarr: the array containing the errors
	 * @return the index of the minimum error
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("unused") // TODO: check wheter it is necessary
	private int findMinValue(double[] MAEarr) throws FileNotFoundException{
		double min = 99999.0;
		int indexMin = -1;
		PrintWriter errore = new PrintWriter(path+"error.csv");
		for(int i=0; i<MAEarr.length; ++i){
			if(MAEarr[i]<min) indexMin = i;
			errore.println(MAEarr[i]);
		}
		errore.close();
		return indexMin;
	}
	
	/**
	 * Print an array
	 * @param arr: the array to print
	 */
	@SuppressWarnings("unused") // TODO: check wheter it is necessary
	private static void printArray(double[] arr){
		if(arr.length==1) System.out.println(arr[0]);
		else{
			for(int i=0; i<arr.length; ++i){
				if(i==0) System.out.print("[ "+arr[i]+" , ");
				else if(i==arr.length-1) System.out.println(arr[i]+" ]");
				else System.out.print(arr[i]+" , ");
			}
		}	
	}
}