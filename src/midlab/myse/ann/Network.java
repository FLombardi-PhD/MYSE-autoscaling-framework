package midlab.myse.ann;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * Network class of ANN of MYSE
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
class Network {
	private Random random = new Random();
	private double learningRate; // learning param
	private double momentum;     // learning param
	private Layer[] layers;      // layers array
	private int totLayers;       // number of layers
	private int[] neuronInLayer; // array containing the number of neurons on layer i-th
	private double max;          // max traffic
	
	@SuppressWarnings("unused")
	private double[] netInputs;  // unuseful array with dimension equal to number of input nodes
	
	@SuppressWarnings("unused")
	private double[] netOutputs; // unuseful array with dimension equal to number of output nodes
	
	
	public Network(double learningRate, double momentum, int[] layers, int totLayers) {
		random = new Random();
		if (totLayers < 2)
			throw new IllegalArgumentException("layer's count cannot be less than 2");
		this.learningRate = learningRate;
		this.momentum = momentum;
		neuronInLayer = new int[totLayers];
		this.layers = new Layer[totLayers];
		for (int i = 0; i < totLayers; i++) {
			neuronInLayer[i] = layers[i];
			this.layers[i] = new Layer(layers[i],i); //'i' is the id of the layer
		}
		netInputs = new double[layers[0]];
		netOutputs = new double[layers[totLayers - 1]];
		this.totLayers = totLayers;
	}

	public void setMax(double max){
		this.max = max;
	}
	
	public void setInputs(double[] inputs) {
		for (int i = 0; i < neuronInLayer[0]; i++) {
			layers[0].getNeuron(i).setValue(inputs[i]);
		}
	}

	public void randomizeWeightAndBiases() {
		for (int i = 0; i < totLayers; i++) {
			for (int j = 0; j < neuronInLayer[i]; j++) {
				if (i != (totLayers - 1)) {
					layers[i].getNeuron(j).setDendrites(neuronInLayer[i + 1]);
					for (int k = 0; k < neuronInLayer[i + 1]; k++) {
						layers[i].getNeuron(j).getDendrite(k)
								.setWeight(getRand());
					}
				}
				if (i != 0) {
					layers[i].getNeuron(j).setBias(getRand());
				}
			}
		}
	}

	public double[] getOutput() {
		double[] outputs = new double[neuronInLayer[totLayers - 1]];
		for (int i = 1; i < totLayers; i++) {
			for (int j = 0; j < neuronInLayer[i]; j++) {
				layers[i].getNeuron(j).setValue(0);
				// add to neuron 'j' at layer 'i' the weights of all neurons connected to previous layer i-1
				for (int k = 0; k < neuronInLayer[i - 1]; k++) {
					layers[i].getNeuron(j).setValue(
							layers[i].getNeuron(j).getValue()
									+ layers[i - 1].getNeuron(k).getValue()
									* layers[i - 1].getNeuron(k).getDendrite(j).getWeight());
				}
				// add to neuron 'j' at layer 'i' the weights of bias
				layers[i].getNeuron(j).setValue(
						layers[i].getNeuron(j).getValue()
								+ layers[i].getNeuron(j).getBias());
				
				// apply sigmoid function
				layers[i].getNeuron(j).setValue(
						limiter(layers[i].getNeuron(j).getValue()));
			}
		}
		for (int i = 0; i < neuronInLayer[totLayers - 1]; i++) {
			outputs[i] = layers[totLayers - 1].getNeuron(i).getValue();
		}
		return outputs;
	}

	
	
	public void update() {
		getOutput();
	}

	public double limiter(double value) {
		// sigmoid function
		double result = (1.0 / (1 + Math.exp(-value)));
		
		/* for hyperbolic tangent instead of sigmoid:
		 * double exp = Math.exp(value);
		 * double expN = Math.exp(-value);
		 * double result = ((exp - expN)/(exp + expN));
		 */
		
		return result;
	}

	public double getRand() {
		return 2 * random.nextDouble() - 1;
	}

	public double sigmaWeightDelta(int layerNumber, int neuronNumber) {
		double result = 0;
		for (int i = 0; i < neuronInLayer[layerNumber + 1]; i++) {
			result = result + layers[layerNumber].getNeuron(neuronNumber).getDendrite(i).getWeight() * layers[layerNumber + 1].getNeuron(i).getDelta();
		}
		return result;
	}

	public void train(double[] inputs, double[] outputs) {
		
		printArray(inputs);
		
		double target, actual, delta;
		setInputs(inputs);
		update();
		for (int i = totLayers - 1; i > 0; i--) {
			for (int j = 0; j < neuronInLayer[i]; j++) {
				if (i == totLayers - 1) {
					target = outputs[j];
					actual = layers[i].getNeuron(j).getValue();
					
					// if using sigmoid
					delta = (target - actual) * actual * (1 - actual);
					
					/* if using hyperbolic tangent
					 * delta = (target - actual) * actual * (1 - actual);
					 */
					
					System.out.println("ACTUAL="+actual+", TARGET="+target+", DELTA="+delta);
					
					layers[i].getNeuron(j).setDelta(delta);
					for (int k = 0; k < neuronInLayer[i - 1]; k++) {
						double dendriteWeight = layers[i - 1].getNeuron(k).getDendrite(j).getWeight();
						dendriteWeight += delta*learningRate*layers[i - 1].getNeuron(k).getValue();
						layers[i - 1].getNeuron(k).getDendrite(j).setWeight(dendriteWeight);
					}
					layers[i].getNeuron(j).setBias(layers[i].getNeuron(j).getBias()+delta*learningRate*1);
				} else {
					actual = layers[i].getNeuron(j).getValue();
					delta = actual * (1 - actual) * sigmaWeightDelta(i, j);
					for (int k = 0; k < neuronInLayer[i - 1]; k++) {
						double dendriteWeight = layers[i - 1].getNeuron(k).getDendrite(j).getWeight();
						dendriteWeight += delta*learningRate*layers[i - 1].getNeuron(k).getValue();
						layers[i - 1].getNeuron(k).getDendrite(j).setWeight(dendriteWeight);
					}
					if (i != 0) {
						layers[i].getNeuron(j).setBias(layers[i].getNeuron(j).getBias()+delta*learningRate*1);
					}
				}
			}
		}
	}

	public void trainMomentum(double[] inputs, double[] outputs) {
		
		printArray(inputs);
		
		double target, actual, delta, beta;
		setInputs(inputs);
		update();
		
		//double dendriteWeightPrec = 0.0;
		beta = 0.5;
		
		for (int i = totLayers - 1; i > 0; i--) {
			for (int j = 0; j < neuronInLayer[i]; j++) {
				if (i == totLayers - 1) {
					target = outputs[j];
					actual = layers[i].getNeuron(j).getValue();

					// if using sigmoid
					delta = (target - actual) * actual * (1 - actual);
					
					/* if using hyperbolic tangent
					 * delta = (target - actual) * actual * (1 - actual);
					 */
					
					System.out.println("ACTUAL="+actual+", TARGET="+target+", DELTA="+delta);
					
					layers[i].getNeuron(j).setDelta(delta);
					for (int k = 0; k < neuronInLayer[i - 1]; k++) {
						
						// load Wkj(t) and deltaWkj(t-1)
						double dendriteWeight = layers[i - 1].getNeuron(k).getDendrite(j).getWeight();
						double deltaWkjPrec = layers[i - 1].getNeuron(k).getDendrite(j).getDeltaWeightPrec();
						
						// update the weight Wkj(t+1) = Wkj(t) + deltaWkj(t) + beta*deltaWkj(t-1)
						dendriteWeight += delta*learningRate*layers[i - 1].getNeuron(k).getValue() + beta*deltaWkjPrec;
						
						// set deltaWkj(t) for next iteration and the weight Wkj(t+1)
						layers[i - 1].getNeuron(k).getDendrite(j).setDeltaWeightPrec(delta*learningRate*layers[i - 1].getNeuron(k).getValue());
						layers[i - 1].getNeuron(k).getDendrite(j).setWeight(dendriteWeight);
						
					}
					
					// set the new bias
					layers[i].getNeuron(j).setBias(layers[i].getNeuron(j).getBias() + delta*learningRate*1 + beta*layers[i].getNeuron(j).getDeltaBiasPrec());
					layers[i].getNeuron(j).setDeltaBiasPrec(delta*learningRate*1);
				} else {
					actual = layers[i].getNeuron(j).getValue();
					delta = actual * (1 - actual) * sigmaWeightDelta(i, j);
					for (int k = 0; k < neuronInLayer[i - 1]; k++) {
						
						// load Wkj(t) and deltaWkj(t-1)
						double dendriteWeight = layers[i - 1].getNeuron(k).getDendrite(j).getWeight();
						double deltaWkjPrec = layers[i - 1].getNeuron(k).getDendrite(j).getDeltaWeightPrec();
						
						// update the weight Wkj(t+1) = Wkj(t) + deltaWkj(t) + beta*deltaWkj(t-1)
						dendriteWeight += delta*learningRate*layers[i - 1].getNeuron(k).getValue() + beta*deltaWkjPrec;
						
						// set the deltaWkj(t) for next iterationa and the weight Wkj(t+1)
						layers[i - 1].getNeuron(k).getDendrite(j).setDeltaWeightPrec(delta*learningRate*layers[i - 1].getNeuron(k).getValue());
						layers[i - 1].getNeuron(k).getDendrite(j).setWeight(dendriteWeight);
					}
					if (i != 0) {
						
						// set the new bias
						layers[i].getNeuron(j).setBias(layers[i].getNeuron(j).getBias() + delta*learningRate*1 + beta*layers[i].getNeuron(j).getDeltaBiasPrec());
						layers[i].getNeuron(j).setDeltaBiasPrec(delta*learningRate*1);
					}
				}
			}
		}
	}
	
	
	public void trainResilient(double[] inputs, double[] outputs) {
		
		printArray(inputs);
		
		double niPlus = 1.5;
		double niMinus = 0.5;
		double deltaMax = 50.0;
		double deltaMin = 1/(Math.E*Math.E*Math.E*Math.E*Math.E*Math.E);
		
		@SuppressWarnings("unused")
		double target, actual, delta, deltaEoverW, deltaEoverWjk, deltaJk, deltaWjk, deltaWjkPrec;
		setInputs(inputs);
		update();
		
		//double dendriteWeightPrec = 0.0;
		learningRate = 1.0;
		
		for (int i = totLayers - 1; i > 0; i--) {
			for (int j = 0; j < neuronInLayer[i]; j++) {
				if (i == totLayers - 1) {  // if it is the last layer, thus the output neuron
					target = outputs[j];
					actual = layers[i].getNeuron(j).getValue();
					delta = (target - actual) * actual * (1 - actual);
					deltaEoverW = delta*actual;
					
					System.out.println("ACTUAL="+actual+", TARGET="+target+", DELTA="+delta);
					
					layers[i].getNeuron(j).setDelta(delta);
					
					for (int k = 0; k < neuronInLayer[i - 1]; k++) {
						
						// load Wkj(t) and deltaWkj(t-1)
						double dendriteWeight = layers[i - 1].getNeuron(k).getDendrite(j).getWeight();
						
						// that's correct even though deltaEoverWjk is propery of neuron k and not of dendrite
						// but it is stored a copy of such a value in each dendrite
						double deltaEoverWjkPrec = layers[i].getNeuron(j).getDeltaEoverWPrec();
						
						double deltaJkPrec = layers[i - 1].getNeuron(k).getDendrite(j).getDeltaJkPrec();
						double c = deltaEoverW*deltaEoverWjkPrec;
						
						if(c>0){
							deltaJk = Math.min((deltaJkPrec*niPlus), deltaMax);
							deltaWjk = (-1.0*(Math.signum(deltaEoverW)))*deltaJk;
							dendriteWeight += deltaWjk;
							
							deltaEoverWjk = deltaEoverW;
						}
						if(c<0){
							deltaJk = Math.max((deltaJkPrec*niMinus), deltaMin);
							dendriteWeight -= deltaJkPrec;
							deltaEoverWjk = 0;
						}
						else{
							deltaJk = deltaJkPrec;
							deltaWjk = (-1.0*(Math.signum(deltaEoverW)))*deltaJk;
							dendriteWeight += deltaWjk;
							
							deltaEoverWjk = deltaEoverW;
						}
						
						// update deltaEoverWjk
						layers[i - 1].getNeuron(k).getDendrite(j).setDeltaWeightPrec(deltaEoverWjk);
						
						// update deltaJkPrec = deltaJk
						layers[i - 1].getNeuron(k).getDendrite(j).setDeltaJkPrec(deltaJk);
						
						// set deltaWkj(t) for next iteration and the weight Wkj(t+1)
						layers[i - 1].getNeuron(k).getDendrite(j).setWeight(dendriteWeight);
						
					}
					
					// set the new bias
					double deltaEoverWjkPrec = layers[i].getNeuron(j).getDeltaEoverWPrec();
					double c = deltaEoverW*deltaEoverWjkPrec;
					double deltaJkPrec = layers[i].getNeuron(j).getDeltaJkPrec();
					double biasWeight = layers[i].getNeuron(j).getBias();
					if(c>0){
						deltaJk = Math.min((deltaJkPrec*niPlus), deltaMax);
						deltaWjk = (-1.0*(Math.signum(deltaEoverW)))*deltaJk;
						biasWeight += deltaWjk;
						
						deltaEoverWjk = deltaEoverW;
					}
					if(c<0){
						deltaJk = Math.max((deltaJkPrec*niMinus), deltaMin);
						biasWeight -= deltaJkPrec;
						deltaEoverWjk = 0;
					}
					else{
						deltaJk = deltaJkPrec;
						deltaWjk = (-1.0*(Math.signum(deltaEoverW)))*deltaJk;
						biasWeight += deltaWjk;
						
						deltaEoverWjk = deltaEoverW;
					}
					layers[i].getNeuron(j).setDeltaBiasPrec(layers[i].getNeuron(j).getBias());
					layers[i].getNeuron(j).setBias(biasWeight);
				}
				else { // else = if instead it is not the last layer (i.e. the output)
					actual = layers[i].getNeuron(j).getValue();
					delta = actual * (1 - actual) * sigmaWeightDelta(i, j);
					deltaEoverW = delta*actual;
					for (int k = 0; k < neuronInLayer[i - 1]; k++) {
						
						// load Wkj(t) and deltaWkj(t-1)
						double dendriteWeight = layers[i - 1].getNeuron(k).getDendrite(j).getWeight();
						
						// that's correct even though deltaEoverWjk is propery of neuron k and not of dendrite
						// but it is stored a copy of such a value in each dendrite
						double deltaEoverWjkPrec = layers[i].getNeuron(j).getDeltaEoverWPrec();
						
						double deltaJkPrec = layers[i - 1].getNeuron(k).getDendrite(j).getDeltaJkPrec();
						double c = deltaEoverW*deltaEoverWjkPrec;
						
						if(c>0){
							deltaJk = Math.min((deltaJkPrec*niPlus), deltaMax);
							deltaWjk = (-1.0*(Math.signum(deltaEoverW)))*deltaJk;
							dendriteWeight += deltaWjk;
							
							deltaEoverWjk = deltaEoverW;
						}
						if(c<0){
							deltaJk = Math.max((deltaJkPrec*niMinus), deltaMin);
							dendriteWeight -= deltaJkPrec;
							deltaEoverWjk = 0;
						}
						else{
							deltaJk = deltaJkPrec;
							deltaWjk = (-1.0*(Math.signum(deltaEoverW)))*deltaJk;
							dendriteWeight += deltaWjk;
							
							deltaEoverWjk = deltaEoverW;
						}
						
						// update deltaEoverWjk
						layers[i - 1].getNeuron(k).getDendrite(j).setDeltaWeightPrec(deltaEoverWjk);
						
						// update deltaJkPrec = deltaJk
						layers[i - 1].getNeuron(k).getDendrite(j).setDeltaJkPrec(deltaJk);
						
						// set deltaWkj(t) for next iterationa and the weight Wkj(t+1)
						layers[i - 1].getNeuron(k).getDendrite(j).setWeight(dendriteWeight);						
						
					}
					
					if (i != 0) {
						
						// set the new bias
						double deltaEoverWjkPrec = layers[i].getNeuron(j).getDeltaEoverWPrec();
						double c = deltaEoverW*deltaEoverWjkPrec;
						double deltaJkPrec = layers[i].getNeuron(j).getDeltaJkPrec();
						double biasWeight = layers[i].getNeuron(j).getBias();
						if(c>0){
							deltaJk = Math.min((deltaJkPrec*niPlus), deltaMax);
							deltaWjk = (-1.0*(Math.signum(deltaEoverW)))*deltaJk;
							biasWeight += deltaWjk;
							
							deltaEoverWjk = deltaEoverW;
						}
						if(c<0){
							deltaJk = Math.max((deltaJkPrec*niMinus), deltaMin);
							biasWeight -= deltaJkPrec;
							deltaEoverWjk = 0;
						}
						else{
							deltaJk = deltaJkPrec;
							deltaWjk = (-1.0*(Math.signum(deltaEoverW)))*deltaJk;
							biasWeight += deltaWjk;
							
							deltaEoverWjk = deltaEoverW;
						}
						layers[i].getNeuron(j).setDeltaBiasPrec(layers[i].getNeuron(j).getBias());
						layers[i].getNeuron(j).setBias(biasWeight);
					}
				}
			}
		}
	}

	public void printNet(String filePath) throws FileNotFoundException{
		File fileRete = new File(filePath);
		PrintWriter pw = new PrintWriter(fileRete);
		
		pw.print("NEURAL NETWORK: ");
		for(int i=0; i<layers.length; ++i){
			if(i!=layers.length-1) pw.print(neuronInLayer[i]+"-");
			else pw.println(neuronInLayer[i]+"\n");
		}		
		pw.println("EPS="+learningRate);
		pw.println("BETA="+momentum+"\n");
		for (int i = 0; i < totLayers; i++) {
			pw.print("[LAYER "+i);
			if(i==0) pw.println(" : INPUT]\n");
			else if(i==totLayers-1) pw.println(" : OUTPUT]\n");
			else pw.println(" : HIDDEN]\n");
			for (int j = 0; j < neuronInLayer[i]; j++) {
				pw.println("\tNeuron "+layers[i].getNeuron(j).getId()+":\n"+
									"\t\tValues="+layers[i].getNeuron(j).getValue()*this.max+"\n"+
									"\t\tBias="+layers[i].getNeuron(j).getBias()+"\n"+
									"\t\tDelta="+layers[i].getNeuron(j).getDelta()+"\n"+
									"\t\tDendrites:");
				for(int k=0; k<layers[i].getNeuron(j).getDendrites().length; ++k){
					pw.println("\t\t\td"+k+": w="+layers[i].getNeuron(j).getDendrite(k).getWeight()+
								", pointsTo="+layers[i].getNeuron(j).getDendrite(k).getPointsTo());
				}
				pw.println();
			}
		}
		pw.close();
	}
	
	private static void printArray(double[] arr){
		for(int i=0; i<arr.length; ++i){
			if(i==0) System.out.print("[ ");
			if(i!=arr.length-1) System.out.print(arr[i]+", ");
			else System.out.println(arr[i]+" ]");
		}
	}
}