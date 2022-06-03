package cmcc.opa.egeos.adaptor.model;

import java.util.ArrayList;

public class EgeosEvent {
    
	public String type;
    public int totalFeatures;
    public ArrayList<Feature> features;
    public Crs crs;
	
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getTotalFeatures() {
		return this.totalFeatures;
	}

	public void setTotalFeatures(int totalFeatures) {
		this.totalFeatures = totalFeatures;
	}

	public ArrayList<Feature> getFeatures() {
		return this.features;
	}

	public void setFeatures(ArrayList<Feature> features) {
		this.features = features;
	}

	public Crs getCrs() {
		return this.crs;
	}

	public void setCrs(Crs crs) {
		this.crs = crs;
	}
	
}
