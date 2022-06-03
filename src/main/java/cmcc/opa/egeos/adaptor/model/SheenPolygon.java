package cmcc.opa.egeos.adaptor.model;

import java.util.ArrayList;

public class SheenPolygon {
	private String type;
	private ArrayList<ArrayList<ArrayList<ArrayList<Float>>>> coordinates;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
//	public ArrayList<ArrayList<Float>> getCoordinates() {
//		return coordinates;
//	}
//	public void setCoordinates(ArrayList<ArrayList<Float>> coordinates) {
//		this.coordinates = coordinates;
//	}
	public ArrayList<ArrayList<ArrayList<ArrayList<Float>>>> getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(ArrayList<ArrayList<ArrayList<ArrayList<Float>>>> coordinates) {
		this.coordinates = coordinates;
	}

	
	
}
