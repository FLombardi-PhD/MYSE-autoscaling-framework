package midlab.myse.start;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import midlab.myse.ann.ANN;
import midlab.myse.ann.Tools;
import midlab.myse.decider.Decider;
import midlab.myse.event.EventListener;
import midlab.myse.utils.PropFileManager;
import name.pachler.nio.file.ClosedWatchServiceException;
import name.pachler.nio.file.Path;
import name.pachler.nio.file.StandardWatchEventKind;
import name.pachler.nio.file.WatchEvent;
import name.pachler.nio.file.WatchKey;
import name.pachler.nio.file.WatchService;
import name.pachler.nio.file.ext.Bootstrapper;

/**
 * Start the execution of MYSE
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
public class Starter {
	
	/**
	 * Logger
	 */
	private static final Logger log4j = LogManager.getLogger("midlab");
	
	/**
	 * Define if the Starter instance is a simulation or not
	 */
	private static boolean isSimulation;
	
	/**
	 * Define if the Starter is in monitoring phase
	 * TODO: put in a config file
	 */
	private static boolean started = false;
	
	/**
	 * Iteration of Starter
	 */
	private static int iteration = 0;
	
	/**
	 * Sleep before to enter the loop
	 * TODO: put in a config file
	 */
	
	private static long sleepBeforeLoop = 10000;
	
	/**
	 * Sleep in the watch events
	 * TODO: put in a config file
	 */
	private static long sleepWatch = 500;
	
	/**
	 * Sleep between watch events
	 * TODO: put in a config file
	 */
	private static long sleepBetweenEvents = 15000;
	
	
	/**
	 * Maximum iteration instead of the infinite loop in wathing events
	 * TODO: put in a config file
	 */
	private static int maxIteration = 1300;
	
	
	/**
	 * Sliding window lenght
	 * TODO: currently not used, to finish the implementation
	 * TODO: put in a config file
	 */
	@SuppressWarnings("unused")
	private static int slidingWindowLength = 10;
	
	/**
	 * Return the status of the Starter
	 * @return true if the Starter is completely started, false otherwise
	 */
	public static boolean getStarted(){
		return started;
	}
	
	/**
	 * Return the iteration of Starter
	 * @return an int which represent the iteration
	 */
	public static int getIteration(){
		return iteration;
	}

	/**
	 * Return whether the Starter instance is a simulation or not
	 * @return a boolean that is true if it is a simulation, false otherwise
	 */
	public static boolean isSimulation(){
		return isSimulation;
	}
	
	/**
	 * Start the execution of MYSE
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException {
		
		System.out.println("\n*************** MYSE STARTER ***************");
		
		// reading the properties files
		PropFileManager propManConfig = null;
		PropFileManager propManAnn = null;
		try {
			propManConfig = new PropFileManager("config.properties");
			propManAnn = new PropFileManager("ann.properties");
		} catch (IOException e) {
			e.printStackTrace();
			log4j.error("Cannot find properties file. Exiting...");
			System.exit(1);
		}
		
				
		// setting the boolean that indicates wheter the Starter is a simulation or not
		if(propManConfig.getProperty("simulation").equalsIgnoreCase("true")){
			isSimulation = true;
			log4j.info("SIMULATION MODE!");
		}
		else if(propManConfig.getProperty("simulation").equalsIgnoreCase("false")) isSimulation = false;
		else{
			log4j.error("Error defining the propery 'simulation' in properties.config. It must contain true/false value. Exiting...", new Exception());
			System.exit(1);
		}
		
		// reading the globalPath and the testPath that will be listen from JPathWatch
		String globalPath = propManConfig.getProperty("globalPath");
		String trainingPath = propManConfig.getProperty("trainingPath");
		String testPath = propManConfig.getProperty("testPath");
		String trainingFile = propManConfig.getProperty("training");
		String testFile = propManConfig.getProperty("test");
		String replicaFile = propManConfig.getProperty("replica");
		
		// Files for writing the training and test that will be passed from an external client
		File training = new File(trainingPath+trainingFile);
		File test = new File(testPath+testFile);
		
		// PrintWriter for writing in a file the replicas decided each iteration
		PrintWriter writerReplica = null;
		try {
			writerReplica = new PrintWriter(globalPath+replicaFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			log4j.error("Cannot find replica file. Exiting...");
			System.exit(1);
		}
				
		// ANN parameters
		double EPS = Double.parseDouble(propManAnn.getProperty("EPS"));
		double momentum = Double.parseDouble(propManAnn.getProperty("momentum"));
		int iter = Integer.parseInt(propManAnn.getProperty("iter"));
		int inp= Integer.parseInt(propManAnn.getProperty("inp"));
		int outp= Integer.parseInt(propManAnn.getProperty("outp"));
		String[] hidString = propManAnn.getProperty("hid").split(",");
		int[] hid = new int[hidString.length];
		for(int i=0; i<hid.length; i++){
			hid[i] = Integer.parseInt(hidString[i]);
		}
		
		// dataset time aggregation, dataset scale, polling and sliding window parameters
		int timeAggregation = Integer.parseInt(propManAnn.getProperty("timeAggregation"));
		double scale = Double.parseDouble(propManAnn.getProperty("scale"));
		int pollingInterval = Integer.parseInt(propManConfig.getProperty("pollingInterval"));
		int window = Integer.parseInt(propManConfig.getProperty("window"));
		log4j.debug("time aggragation in minutes = "+timeAggregation);
		log4j.debug("dataset scale = "+scale);
		log4j.debug("polling interval = "+pollingInterval);
		log4j.debug("sliding window dimension = "+window);
		
		// Decider parameters
		int maxAvailableServers = Integer.parseInt(propManConfig.getProperty("maxAvailableServers"));
		double QoS = Double.parseDouble(propManConfig.getProperty("QoS"));
		double mu = Double.parseDouble(propManConfig.getProperty("mu"));
		double deciderScale = Double.parseDouble(propManConfig.getProperty("deciderScale"));
		log4j.debug("max available servers = "+maxAvailableServers);
		log4j.debug("QoS = "+QoS);
		log4j.debug("mu = "+mu);
		log4j.debug("Decier scale = "+deciderScale);
		Decider decider = new Decider(maxAvailableServers,QoS,mu,timeAggregation,deciderScale);
		
		// building the ANN
		ANN mlp = null;
		try {
			// TODO: check line below, last input is different from the used one
			// mlp = new ANN(EPS, momentum, iter, inp, hid, outp, timeAggregation, scale, true);
			mlp = new ANN(EPS, momentum, iter, inp, hid, outp, timeAggregation, scale, globalPath);
			mlp.randomizeWeight();
		} catch (IOException e1){
		} catch (NullPointerException e1){
			e1.printStackTrace();
			log4j.error("An error occured creating the ANN. Exiting...");
			System.exit(1);
		}
				
		// starting the thread of event listening
		EventListener t = new EventListener(isSimulation,globalPath,training,test,window);
		t.start();
		
		// wait a while before entering in loop
		Thread.sleep(sleepBeforeLoop);
		
		// writing head of replica file
		writerReplica.println("\"Previous Forecasted\"\t\"Previous Load\"\t\"MYSE Configuration\"\t\"Optimal Configuration\"\t\"Load Error\"\t\"Conf Error\"");
		
		log4j.debug("entering in loop..");
		monitoring(mlp,decider,training,test,testPath,writerReplica,pollingInterval);
		
		// end of infinite loop, closing the writer of replicas
		writerReplica.close();
	}
	
	/**
	 * Start the monitoring loop
	 * @param mlp the MultiLayerPerceptron
	 * @param training training file
	 * @param test test file
	 * @param testPath path to be monitored
	 * @param writerReplica the writer on which writing replicas number
	 * @param pollingInterval polling interval for the testPath folder
	 * @throws InterruptedException 
	 */
	@SuppressWarnings("rawtypes")
	private static void monitoring(ANN mlp, Decider decider, File training, File test, String testPath, PrintWriter writerReplica, int pollingInterval) throws InterruptedException{
		
		// TODO: fix following ANN parameters
		
		// forecasting variables
		double[] forecastedArray = null;
		long forecastedValue = 0;
		long forecastedPrevious = 5; //5 because I know from used dataset that the first value of previous traffic in the test set is 10
		
		long loadPrevious = 0;
		long errorPrevious = 0;
		// long forecastedNext = 0; for multi-output ANN (not used now)
		
		int conf = 0;               // computed configuration
		int confActual = 0;         // optimal config of the previos iteration
		int confPredicted = 0;      // estimated config of previous iteration
		int confErrorPrevious = 0;  // config error
		
		// configuring JPathWatch
		// TODO: use variable 'mill'
		@SuppressWarnings("unused")
		long mill = Bootstrapper.getDefaultPollingInterval();
		Bootstrapper.setDefaultPollingInterval(pollingInterval);
		boolean bool = Bootstrapper.isForcePollingEnabled();
		if(!bool) Bootstrapper.setForcePollingEnabled(true);
		mill = Bootstrapper.getDefaultPollingInterval();
		
		WatchService watchService = Bootstrapper.newWatchService();
		Path watchedPath = Bootstrapper.newPath(new File(testPath));
		
		log4j.debug("monitoring "+testPath);
		
		// TODO: use variable 'key'
		@SuppressWarnings("unused")
		WatchKey key = null;
		try{
			key = watchedPath.register(watchService, StandardWatchEventKind.ENTRY_MODIFY);
		} catch(UnsupportedOperationException uox){
			log4j.error("unsupported watching event");
		} catch(IOException iox){
			log4j.error("IO errors");
		}
		
		// set the Starter is ready for entering in loop
		started = true;
		System.out.println();
		
		// entering in the infinite loop
		for(int j=0; j<maxIteration; ++j){
			
			//sync the iteration of Starter with the EventGenerator
			iteration = j;
			
			log4j.info("\n*************** ITERATION "+j+"***************");
		
			// JPath 
			WatchKey signalledKey = null;
			try{
				// take() lock the thread till a file in the watchedPath is modified
				signalledKey = watchService.take();
			} catch(InterruptedException ix){
				// ignore the exception if some other thread has called the interrupt method on watch service
				continue;
			} catch(ClosedWatchServiceException cwse){
				log4j.error("watch service closed, terminating.", cwse);
				break;
			}
			
			// get the event list from object key
			List<WatchEvent<?>> list = signalledKey.pollEvents();
			
			// reset the event list from object key
			signalledKey.reset();
			
			// print what's happen in the monitored directory
			for(WatchEvent e : list){
				String message = "";
				if(e.kind() == StandardWatchEventKind.ENTRY_MODIFY){
					Path context = (Path)e.context();
					message = context.toString()+" modified";
					log4j.debug(message);
					log4j.info("new set received. Running the learning algorithm..");
					
					// now we learn myse with the new training set
				
					// training phase
					try {
						mlp.train(training.getAbsolutePath());
					} catch(NullPointerException npe){ 
						log4j.error("Something wrong filling training set.. retry");
						Thread.sleep(sleepWatch);
						try {
							forecastedArray = mlp.forecast(test.getAbsolutePath(), 1);
						} catch (IOException e1) { e1.printStackTrace();
						}
					} catch (IOException e1) {
						log4j.error("Unable to train the network");
						e1.printStackTrace();
					}	
					
					// forecasting phase
					try {
						forecastedArray = mlp.forecast(test.getAbsolutePath(), 1);
					} catch (NullPointerException enp){
						log4j.error("Something wrong filling testset.. retry");
						Thread.sleep(sleepWatch);
						try {
							forecastedArray = mlp.forecast(test.getAbsolutePath(), 1);
						} catch (IOException e1) { e1.printStackTrace();
						}
					} catch (IOException e1) {
						System.err.println("Unable to fill test set or forecast");
						e1.printStackTrace();
					}
					forecastedValue = Math.round(forecastedArray[0]*mlp.getMax());
					log4j.info("forecasted unscaled load="+forecastedValue+". Triggering to Decider..");
					
					/* TODO: to implement multiple ANN output:
					 * if(outp>1) {
					 * 		for (int i=0; i<outp; ++i) {
					 * 				forecastedNext = Math.round(forecastedArray[i]*mlp.getMax());
					 * 		}
					 * 	}
					 */
					
					// calculating the error committed the previous iteration
					try {
						loadPrevious = getPreviousLoad(test)*(long)mlp.getScale();
					} catch (NoSuchElementException nsee){
						log4j.error("Something wrong getting the previous load value.. retry");
						Thread.sleep(500);
						try {
							loadPrevious = getPreviousLoad(test)*(long)mlp.getScale();
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
					} catch (FileNotFoundException e1) {
						log4j.error("Unable to get previous load");
						e1.printStackTrace();
					}
					confActual = decider.mmsQueueingModel(loadPrevious,false);
					confPredicted = decider.mmsQueueingModel(forecastedPrevious,false);
					errorPrevious = forecastedPrevious-loadPrevious;
					confErrorPrevious = confPredicted-confActual;
									
					/* TODO: check the following try-catch to read constant rows of training file:
					 */
					try {
						Tools.countRowsFile(training.getPath());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					conf = decider.mmsQueueingModel(forecastedValue,true);
					log4j.info("\n\tRESULTS:"
							+ "\n\tprevious load = "+loadPrevious+"; previous forecasted = "+forecastedPrevious+"; previous error = "+errorPrevious+";"
							+ "\n\t"+"previous conf = "+confPredicted+"; previous optimal conf = "+conf+"; conf error = "+confErrorPrevious		
							+ "\n\n\t"+"next load forecasted = "+forecastedValue+";\n\tconfiguration: "+conf+""
											+ "\n"/*+" Prev2 = "+nextForecasted+"\n"*/);
				
					writerReplica.println(forecastedPrevious+"\t"+loadPrevious+"\t"+confPredicted+"\t"+confActual+"\t"+errorPrevious+"\t"+confErrorPrevious);
					writerReplica.flush();
					forecastedPrevious = forecastedValue;					
				}
				else if(e.kind() == StandardWatchEventKind.OVERFLOW){
					message = "OVERFLOW: too many events than it is possible to monitor.";
					log4j.error(message);
				}
				
			}
			
			// end cycle jpath events
			
			Thread.sleep(sleepBetweenEvents);
		}
	}
	
	/**
	 * Returns the previous value of observed load
	 * @param test the File with the test set
	 * @return a long representing the previous value of load
	 * @throws FileNotFoundException
	 */
	private static long getPreviousLoad(File test) throws FileNotFoundException{
		@SuppressWarnings("resource")
		Scanner scanTest = new Scanner(test);
		scanTest.next(); scanTest.next();
		scanTest.next(); scanTest.next();
		return scanTest.nextInt(); 
		
	}
	
}

/* TODO: to implement algorithm with sliding windows:
for(int j=0; j<maxIteration; ++j){
	
	/** from here ....
	
	// reinitialize training and test set
	wTraining = new PrintWriter(training);
	wTest = new PrintWriter(test);
	
	// set the indexes
	end = j;
	if(j < slidingWindowLength) start = 0;
	else start = end-slidingWindowLength;
	
	// cycle to write training and test set
	for(int i=start; i<end; ++i){
		wTraining.println(arrayHistory[i]); // write elements in the training
		log4j.info("training += " + i);
	}
	wTest.println(arrayHistory[end]); // write the next element to forecast
	log4j.info("test = " + end);
	
	// once written training and test it is possible to close the writers
	wTraining.close();
	wTest.close();
	
	/** ... to here, it will go in a separated thread which simulates jmx/httperf load injection
	
	// start training phase
	mlp.train(training.getAbsolutePath());	
	
	// then start forecasting phase
	log4j.info("[Starter.main]: iteration "+(j+1));
	forecastedArray = mlp.forecast(test.getAbsolutePath(), 1);
	forecastedValue = Math.round(forecastedArray[0]*mlp.getMax());
	 * TODO: to implement multiple ANN output:
	 * if(outp>1) {
	 * 		for (int i=0; i<outp; ++i) {
	 * 				forecastedNext = Math.round(forecastedArray[i]*mlp.getMax());
	 * 		}
	 * 	}
	 *
	
	// compute error of previous iteration
	previousLoad = getPreviousLoad(test);
	confActual = Decider.mmsQueueingModel(previousLoad,false);
	confPredicted = Decider.mmsQueueingModel(previousForecasted,false);
	errorPrevious = previousForecasted - trafficoPrec;
	errorServerPrevious = confPredicted - confActual;
				
		
	//Tools.countRowsFile(training.getPath());
	conf = Decider.mmsQueueingModel(forecastedValue,true);
	System.out.println("\tconf: "+conf);
	wReplica.println(previstoPrec+"\t"+trafficoPrec+"\t"+confPredicted+"\t"+confActual);
	wReplica.flush();
	previousLoad = forecastedValue;
	
	Thread.sleep(sleepBetweenEvents);
}
*/