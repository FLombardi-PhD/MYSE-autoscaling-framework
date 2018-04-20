package midlab.myse.ann;

/**
 * Neuron class of ANN of MYSE
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
class Neuron {
	private int id;
	private double value;
	private double bias;
	private double delta;
	private double deltaBiasPrec;
	private Dendrite[] dendrites;
	
	private double deltaEoverWPrec;
	private double deltaJkPrec;
	
	public Neuron(int dendritesCount, int id) {
		this.id = id;
		dendrites = new Dendrite[dendritesCount];
		for (int i = 0; i < dendrites.length; i++) {
			dendrites[i] = new Dendrite();
			dendrites[i].setPointsTo(i);
		}
		
		deltaEoverWPrec = 0.0;
		deltaJkPrec = 1.0;
		// TODO: check if 'value=0' can be removed; it is used to initialize a neuron to 0 once created (not necessary?)
		// value=0; 
	}

	public void setDendrites(int dendritesCount) {
		dendrites = new Dendrite[dendritesCount];
		for (int i = 0; i < dendrites.length; i++) {
			dendrites[i] = new Dendrite();
			dendrites[i].setPointsTo(i);
		}
	}

	public Dendrite getDendrite(int index) {
		return dendrites[index];
	}

	public Dendrite[] getDendrites() {
		return dendrites;
	}
	
	public double getDelta() {
		return delta;
	}

	public double getDeltaBiasPrec() {
		return deltaBiasPrec;
	}
	
	public double getDeltaEoverWPrec() {
		return deltaEoverWPrec;
	}
	
	public double getDeltaJkPrec() {
		return deltaJkPrec;
	}
	
	public void setDelta(double delta) {
		this.delta = delta;
	}

	public void setDeltaBiasPrec(double delta) {
		this.deltaBiasPrec = delta;
	}
	
	public void setDeltaEoverWPrec(double delta) {
		this.deltaEoverWPrec = delta;
	}
	
	public void setDeltaJkPrec(double delta) {
		this.deltaJkPrec = delta;
	}
	
	public int getId() {
		return id;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}

	public void setBias(double bias) {
		this.bias = bias;
	}

	public double getBias() {
		return bias;
	}
}
