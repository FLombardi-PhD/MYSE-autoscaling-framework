package midlab.myse.decider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import midlab.myse.cm.AutoCM;

/**
 * Decider Algorithm based on Queuing Model
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
@SuppressWarnings("unused")
public class Decider {

	/**
	 * Logger
	 */
	private static final Logger log4j = LogManager.getLogger("midlab");
	
	/**
	 * The max number of servers available in the cluster
	 * TODO: put in a config file
	 */
	public int maxAvailableServers = 8;
		
	/**
	 * The QoS value the response time must be under of
	 */
	public double QoS;
	
	/**
	 * The number of client that could be served at time unit. E.g. mu=1.0 means 1.0 client/sec. It is the inverse of the response time
	 */
	public double mu;
	
	/**
	 * The aggregation time in minutes. E.g. 60 means a dataset structured in 1-hour entry (as Google Analytics)
	 */
	public int timeAggregation;
	
	/**
	 * Decider scale factor
	 */
	public double scale; //1000 for analytics trace, 1.0 for clarknet trace
	
	/**
	 * Variable to set whether to drain only the queue without QoS assurance
	 * TODO: put in a config file
	 */
	public boolean drainOnlyQueueInsteadOfQoS = false;
	
	/**
	 * Build an instance of Decider
	 * @param maxAvailableServers
	 * @param QoS
	 * @param mu
	 * @param timeAggregation
	 * @param scale
	 */
	public Decider(int maxAvailableServers, double QoS, double mu, int timeAggregation, double scale){
		this.maxAvailableServers = maxAvailableServers;
		this.QoS = QoS;
		this.mu = mu;
		this.timeAggregation = timeAggregation;
		this.scale = scale;
	}
	
	/**
	 * Calculates the optimal number of replicas given a prediction of a workload by applying the MMS Queuing Model
	 * @param prediction the predicted load value
	 * @param logging a boolean to decide wheter logging or not. E.g. for PREVIOUS LOAD it will be set to false, for ACTUAL LOAD to true
	 * @return the optimal number of replicas
	 */
	public int mmsQueueingModel(long prediction, boolean logging){
		
		//double scale = 1000; //1000 for analitics or 1.0 for clarknet
		double predictionScaled = prediction*scale;
		double lambda = (((double)predictionScaled)/(timeAggregation*60.0));
				
		int optimus = maxAvailableServers;
		
		double[] ro = new double[maxAvailableServers];
		double[] pZero = new double[maxAvailableServers];
		double[] nq = new double[maxAvailableServers];
		double[] tq = new double[maxAvailableServers];
		double[] n = new double[maxAvailableServers];
		double[] t = new double[maxAvailableServers];
		double[] responseTime = new double[maxAvailableServers];
		
		if(logging) log4j.debug("load="+predictionScaled+"; lambda="+lambda);
		
		//calculating the parameter for all the servers
		for(int i=0; i<maxAvailableServers; ++i){
			ro[i] = lambda/(mu*(i+1));
			pZero[i] = pZeroCalculate(lambda,mu,ro[i],i+1);
			nq[i] = nqCalculate(lambda, mu, ro[i], i+1, pZero[i]);
			tq[i] = tqCalculate(nq[i], lambda);
			n[i] = nCalculate(nq[i], lambda, mu);
			t[i] = tCalculate(tq[i], mu);
			responseTime[i] = responseTimeCalculate((i+1),lambda,mu,ro[i]);
			
			if(logging) log4j.debug("\n\t\texpected time with "+(i+1)+" server: "+t[i]+"\n" +
					/*"\t\texpected pkt in queue: "+n[i]+"\n" +*/
					"\t\tro = "+ro[i]+"\n" +
					/*"\t\tpZero = "+pZero[i] + 
					"\t\tlambda = "+lambda +*/
					"\t\tresponse time = "+responseTime[i]);
		}
		
		//searching the minimum number of replicas that ensure QoS
		for(int i=maxAvailableServers-1; i>=0; --i){
			if(responseTime[i]<=QoS && ro[i]<=1.0) {
				// if drainOnly we can guarantee only to drain the queue with possible long times (no QoS assurance)
				if(drainOnlyQueueInsteadOfQoS){
					if(ro[i-1]<1) optimus = i;
				}
				// otherwise, if we want to ensure QoS
				else optimus = i+1; 
			}
		}
		if(logging) log4j.info("optimal configuration = " + optimus);
		
		//now return 'optimus' to AutoCM to physically reconfigure the system
		//TODO to implement: AutoCM.newConfiguration(optimus);
		
		return optimus;
	}
	
	/**
	 * Calculates the p0 value
	 * @param lambda
	 * @param mu
	 * @param ro
	 * @param s number of servers
	 * @return the p0 value
	 */
	private static double pZeroCalculate(double lambda, double mu, double ro, int s){
		double denom = 0.0;
		for(int n=0; n<s; ++n){
			denom +=( (1.0/(factorial(n))) * (Math.pow(lambda/mu, n)) ) +
					( (1.0/(factorial(s))) * (Math.pow(lambda/mu, s)) * (1.0/(1.0-ro))); 
		}
		double pZero = (1.0)/denom;
		return pZero;
	}
	
	/**
	 * Calculates the number of clients in queue
	 * @param lambda
	 * @param mu
	 * @param ro
	 * @param s the number of servers
	 * @param pZero
	 * @return the clients in queue
	 */
	private static double nqCalculate(double lambda, double mu, double ro, int s, double pZero){
		double nq = (1.0/factorial(s)) * (Math.pow(lambda/mu, s)) * ( (ro/(1.0-Math.pow(1.0-ro, 2.0)) * pZero) );
		return nq;
	}
	
	/**
	 * Calculates the time in queue for each clients
	 * @param nq
	 * @param lambda
	 * @return the time in queue
	 */
	private static double tqCalculate(double nq, double lambda){
		double tq = nq/lambda;
		return tq;
	}
	
	/**
	 * Calculates the number of clients in the global system
	 * @param nq
	 * @param lambda
	 * @param mu
	 * @return the clients in the global system
	 */
	private static double nCalculate(double nq, double lambda, double mu){
		double n = nq + (lambda/mu);
		return n;
	}
	
	/**
	 * Calculates the time of each clients in the global system
	 * @param tq
	 * @param mu
	 * @return the time in te global system
	 */
	private static double tCalculate(double tq, double mu){
		double t = tq + (1.0/mu);
		return t;
	}
	
	/**
	 * Calculate a value named 'C' useful for the response time calculation
	 * @param c the number of servers
	 * @param lambda
	 * @param mu
	 * @param ro
	 * @return the value 'C'
	 */
	private static double cCalculate(int c, double lambda, double mu, double ro){
		double numerator = ( Math.pow(c*ro, c)/factorial(c) ) * ( 1/(1-ro) );
		double denominator = 0.0;
		for(int k=0; k<c; ++k){
			denominator += (Math.pow(c*ro, k)) + ( (Math.pow(c*ro, c)/factorial(c)) * (1/(1-ro)) );
		}
		double C = numerator/denominator;
		return C;
	}
	
	/**
	 * Calculate the response time for each clients in the system
	 * @param c the number of servers
	 * @param lambda
	 * @param mu
	 * @param ro
	 * @return the response time
	 */
	private static double responseTimeCalculate(int c, double lambda, double mu, double ro){
		double responseTime = (cCalculate(c,lambda,mu,ro) / ((c*mu)-lambda)) + (1/mu);
		return responseTime;
	}
	
	/**
	 * Calculates the factorial of the given int in input
	 * @param x the int on which calculate the factorial
	 * @return the factorial value
	 */
	private static int factorial(int x){
		int f=1;
		for(int i=1; i<=x; i=i+1) {
	    	f=f*i;
	    }
	    return f;
	}
	
}
