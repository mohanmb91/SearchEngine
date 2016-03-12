package cs3.calstatela.edu.SearchEngine.Model;

import java.util.ArrayList;

public class PositionTFweightageModel {
	double weightage;
	double termFreq;
	ArrayList<Integer> Position = new ArrayList<Integer>();
	String title;
	
	public PositionTFweightageModel(double weightage, double termFreq, ArrayList<Integer> position, String title) {
		super();
		this.weightage = weightage;
		this.termFreq = termFreq;
		Position = position;
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public double getWeightage() {
		return weightage;
	}
	public void setWeightage(double weightage) {
		this.weightage = weightage;
	}
	public double getTermFreq() {
		return termFreq;
	}
	public void setTermFreq(double termFreq) {
		this.termFreq = termFreq;
	}
	public ArrayList<Integer> getPosition() {
		return Position;
	}
	public void setPosition(ArrayList<Integer> position) {
		Position = position;
	}
}
