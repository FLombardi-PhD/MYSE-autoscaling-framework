package midlab.myse.event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The EventGenerator is the the thread created by the EventListener that communicates with a
 * ClientEventGenerator to receive periodically values of training and test, then writes them to the
 * training and test file so as the JPathWatch library notice the event and train the ANN.
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
public class EventGenerator extends Thread{

	/**
	 * Logger
	 */
	private static final Logger log4j = LogManager.getLogger("midlab");
	
	/**
	 * the client socket for communicating with a ClientEventGenerator.
	 * TODO: define it as a protocol
	 */
	private Socket socket;
	
	/**
	 * the training file writer
	 */
	private PrintWriter wTraining;
	
	/**
	 * the test file writer
	 */
	private PrintWriter wTest;
	
	/**
	 * Build a thread instance of EventGenerator
	 * @param socket the client socket 
	 * @param wTraining
	 * @param wTest
	 */
	public EventGenerator(Socket socket, PrintWriter wTraining, PrintWriter wTest){
		this.socket = socket;
		this.wTraining = wTraining;
		this.wTest = wTest;
	}
	
	/**
	 * start the EventGenerator thread
	 */
	@Override
	public void run(){
		
		BufferedReader in = null;
		
		try{
			
			//inputStream of socket between ClientEventGenerator and this thread where read training\test value
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			//SYNC1: waiting for training transmission
			String training = in.readLine();
			log4j.debug("new training received.");
			log4j.debug("training = ["+training+"]");
			
			//SYNC2: waiting for test transmission
			String test = in.readLine();
			log4j.debug("test     = ["+test+"]");
			
			//here we have the new value of training and test, so we write them to the files
			
			//TODO: test if automation of path is working (before was defined as below)
			//wTraining = new PrintWriter(new File("/home/lombardi01/MYSE/TEST/training-test/realtime/training/training.txt"));
			
			//TODO: insert the sliding window in the online algorithm either
			wTraining.println(training);
			wTraining.flush();
			
			//TODO: test if automation of path is working (before was defined as below)
			//wTest = new PrintWriter(new File("/home/lombardi01/MYSE/TEST/training-test/realtime/test/test.txt"));
			wTest.println(test);
			wTest.flush();
			
			//TODO: to check: is it necessary to close the training?
			//wTraining.close();
			wTest.close();
			in.close();
			socket.close();
			
		} catch(IOException e5){
			log4j.error("an IO error occured", e5);
		}
		
	}
	
}