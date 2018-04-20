package midlab.myse.ann;

/**
 * Dendrite class of ANN of MYSE
 * 
 * @author Federico Lombardi - Sapienza University of Rome
 *
 */
class Dendrite {
	private double weight;
	private double weightPrec;
	private double deltaJkPrec;
	private int pointsTo;

	public Dendrite() {
		weight = 0;
		pointsTo = 0;
	}

	public void setWeight(double weight) {
		// TODO: check to remove it: this.weightPrec = this.weight;
		this.weight = weight;
	}

	public void setDeltaWeightPrec(double weight) {
		//// TODO: check to remove it: this.weightPrec = this.weight;
		this.weightPrec = weight;
	}
	
	public void setDeltaJkPrec(double weight) {
		//// TODO: check to remove it: this.weightPrec = this.weight;
		this.deltaJkPrec = weight;
	}
	
	public void setPointsTo(int pointsTo) {
		this.pointsTo = pointsTo;
	}

	public double getWeight() {
		return weight;
	}
	
	public double getDeltaWeightPrec() {
		return weightPrec;
	}
	
	public double getDeltaJkPrec() {
		return deltaJkPrec;
	}
	
	public int getPointsTo() {
		return pointsTo;
	}
}
