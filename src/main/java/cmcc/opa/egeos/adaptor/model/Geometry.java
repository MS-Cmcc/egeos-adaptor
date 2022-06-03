package cmcc.opa.egeos.adaptor.model;

import java.util.ArrayList;

public class Geometry{
    public String type;
    public ArrayList<ArrayList<ArrayList<ArrayList<Double>>>> coordinates;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<ArrayList<ArrayList<ArrayList<Double>>>> getCoordinates() {
        return this.coordinates;
    }

    public void setCoordinates(ArrayList<ArrayList<ArrayList<ArrayList<Double>>>> coordinates) {
        this.coordinates = coordinates;
    }


}