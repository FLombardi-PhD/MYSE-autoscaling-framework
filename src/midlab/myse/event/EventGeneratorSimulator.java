package midlab.myse.event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import midlab.myse.ann.Tools;
import midlab.myse.start.Starter;

/**
 * The EventGeneratorSimulator is the the thread created by the EventListener and simulates real events.
 * Specifically, it read the history file a writes periodically a row of this file on a training set file
 * and in the same way writes the next value on the test set file, generating the reaction of the JPathWatch
 * library that start a new ANN learning
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
public class EventGeneratorSimulator extends Thread{

	/**
	 * Logger
	 */
	private static final Logger log4j = LogManager.getLogger("midlab");
	
	/**
	 * The globalPath of MYSE
	 */
	private String globalPath;
	
	/**
	 * The training file
	 */
	private File training;
	
	/**
	 * The test file
	 */
	private File test;
	
	/**
	 * Dimension of sliding window
	 */
	private int window;
	
	/**
	 * Sleep time just to simulate the arrival of new request (optionally, could be also 0)
	 * TODO: put in a config file
	 */
	private long simulationSleep = 1000;
	
	/**
	 * Build a thread instance of EventGenerator
	 * @param globalPath the global path of MYSE
	 */
	public EventGeneratorSimulator(String globalPath, File training, File test, int window){
		this.globalPath = globalPath;
		this.training = training;
		this.test = test;
		this.window = window;
		log4j.debug("thread created sucesfully.");
	}
	
	/**
	 * start the EventGenerator thread
	 */
	@SuppressWarnings("static-access")
	@Override
	public void run(){
		
		log4j.debug("thread started.");
		
		//reading history file
		File history = new File(globalPath+"slidingWindow/history.txt");
		BufferedReader rHistory = null;
		int historyFileDimension=0;
		try {
			rHistory = new BufferedReader(new FileReader(history));
			historyFileDimension = Tools.countRowsFile(history.getPath());
		} catch (FileNotFoundException e) { e.printStackTrace();
		} catch (IOException e) { e.printStackTrace();
		}
				
		//creating the writer for training and test file
		PrintWriter wTraining = null;
		PrintWriter wTest = null;
		
		//indexes for the sliding window
		int start = 0;
		int end = 0;
		
		//creating an array with each entry containing a row of the history file
		String[] arrayHistory = new String[historyFileDimension];
		for(int i=0; i<historyFileDimension; ++i){
			try {
				arrayHistory[i] = rHistory.readLine();
			} catch (IOException e) { e.printStackTrace();
			}
		}
		
		log4j.debug("checking for Starter initialization..");
		while(!Starter.getStarted()){
			try {
				this.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		log4j.debug("ok! Starter is in execution.");
		
		//events loop: each iteration is a simulation of an event (a row of history file)
		log4j.debug("entering in events loop..");
		for(int j=0; j<1300; ++j){
		
			log4j.debug("thread iteration = "+j);
			//reinitializing the writers for training and test
			try {
				while(j!=Starter.getIteration()){
					log4j.debug("loop.. thread iteration="+j+", while Starter iteration="+Starter.getIteration());
					if(j>Starter.getIteration()) Thread.sleep(1000);
					else break;
				}
				wTraining = new PrintWriter(training);
				wTest = new PrintWriter(test);
			} catch (FileNotFoundException e) { e.printStackTrace();
			} catch (InterruptedException e) { e.printStackTrace();
			}
			
			//setting the sliding window indexes
			end = j;
			if(j<window) start = 0;
			else start = end-window;
			
			//loop for writing on training file by reading the history array
			for(int i=start; i<end; ++i){
				wTraining.println(arrayHistory[i]);
				log4j.debug("training += "+i);
			}
			
			//writing the next element in history as a single row in test
			wTest.println(arrayHistory[end]);
			log4j.debug("test = "+"\t"+arrayHistory[end]);
		
			//closing the writer. Now the new files are officially written
			wTraining.close();
			wTest.close();
			log4j.debug("files written succcessfully");
			
			//this sleep is just for simulating the arrival of new request (optionally, could be also 0) 
			try {
				Thread.sleep(simulationSleep);
			} catch (InterruptedException e) { e.printStackTrace();
			}
		}
		try {
			rHistory.close();
		} catch (IOException e) { e.printStackTrace();
		}
	}

}
