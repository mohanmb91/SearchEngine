package cs3.calstatela.edu.SearchEngine.Model;

import java.util.ArrayList;
import java.util.List;

public class SearchModel {

	double score ;
	List<Integer> Position = new ArrayList<Integer>();
	
	public SearchModel(double score, List<Integer> position) {
		super();
		this.score = score;
		Position = position;
	}
	
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public List<Integer> getPosition() {
		return Position;
	}
	public void setPosition(List<Integer> position) {
		Position = position;
	}
	
}
