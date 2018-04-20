package midlab.myse.ann;

/**
 * Layer class of ANN of MYSE
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
class Layer {
	private Neuron[] neurons;
	private int id;
	
	public Layer(int size, int id) { 
		this.id = id;
		String neuronIdString = "0"+id;
		int neuronId = 0;
		neurons = new Neuron[size];
		for (int i = 0; i < size; i++) {
			neuronIdString = "0"+id+i;
			neuronId = Integer.parseInt(neuronIdString);
			neurons[i] = new Neuron(1,neuronId); // create "size" neurons with 1 dendrite pointing to next layer
		}
	}

	public Neuron getNeuron(int index) {
		return neurons[index];
	}

	public void setNeuron(Neuron neuron, int index) {
		neurons[index] = neuron;
	}
	
	public int getId(){
		return id;
	}
}
