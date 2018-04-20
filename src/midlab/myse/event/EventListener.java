package midlab.myse.event;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The EventListener is started by the MYSE Starter and allow a Client/Server communication between 
 * the EventGenerator (Server) and the ClientEventGenerator (Client).
 * The EventGenerator instance is a thread started by the EventListener.
 * The ClientEventGenerator is a java program that real-time communicates the new training and test to
 * the EventGenerator. The latter (EventGenerator) writes this values in the training and test files,
 * so as the JPathWatch library notice that change that run a new ANN learning.
 * The current version works with 2 different EventGenerator: ONLINE is for a real working, OFFLINE simulates
 * the event by reading an history file.
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
public class EventListener extends Thread{

	/**
	 * Logger
	 */
	private static final Logger log4j = LogManager.getLogger("midlab");
	
	/**
	 * Boolean to define if it is a local or remote run.
	 */
	private boolean simulation;
	
	/**
	 * GlobalPath of MYSE instance
	 */
	private String globalPath;
	
	/**
	 * File for writing the training
	 */
	private File training;
	
	/**
	 * File for writing the test
	 */
	private File test;
	
	/**
	 * Dimension of sliding window
	 */
	private int window;
	
	/**
	 * Port on which run the server socket
	 * TODO: put in a config file
	 */
	private int socketPort = 6565;
	
	/**
	 * Returns the online/offline execution
	 */
	public EventListener(boolean simulation, String globalPath, File training, File test, int window){
		this.simulation = simulation;
		this.globalPath = globalPath;
		this.training = training;
		this.test = test;
		this.window = window;
	}
	
	/**
	 * Start the execution of the thread that creates a ServerSocket for accepting connection from a
	 * client (EventGenerator or EventGeneratorSimulator)
	 */
	@Override
	public void run(){
	
		/*
		 * creating the writer where the EventGenerator thread will write on training and test file. Such
		 * communication could could be OFFLINE (EventGeneratorOFFLine) by reading an history file as simulation
		 * or ONLINE, by communicating with a thread instance of midlabmyse.httperfStart.start.ClientEventGenerator.java
		*/

		//writer initialization
		PrintWriter wTraining = null;
		PrintWriter wTest = null;
		try {
			wTraining = new PrintWriter(training);
			wTest = new PrintWriter(test);
			
		} catch (FileNotFoundException e1) { e1.printStackTrace();
		}
		
		//creating the ServerSocket
		ServerSocket serverSocket = null;
		int portServerSocket = socketPort;
		try {
        	serverSocket = new ServerSocket(portServerSocket);
        } catch (IOException e) {
            log4j.error("impossible to listen the port: " + portServerSocket);
            System.exit(1);
        }
        
        //creating the ClientSocket
        Socket clientSocket = null;
        try {
        	log4j.debug("waiting for client connection..");
        	//infinite loop for waiting an EventGenerator thread
        	if(simulation) new EventGeneratorSimulator(globalPath,training,test,window).start();
        	else{
        		while (true) {
            		clientSocket = serverSocket.accept();
            		new EventGenerator(clientSocket,wTraining,wTest).start();
            		Thread.sleep(MIN_PRIORITY);
            	}
            }	
        } catch (IOException e) {
            log4j.error("Failed to accept a client socket. Exiting..", e);
            System.exit(1);
        } catch (InterruptedException e) {
        	e.printStackTrace();
        }
	}
	
}
