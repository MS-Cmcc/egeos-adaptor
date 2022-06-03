package cmcc.opa.egeos.adaptor.model;

import java.util.ArrayList;

import org.json.JSONArray;

public class EosJsonData {

    private ArrayList wmss;
    private String status;
    private String acquisitionId;

    public ArrayList getWmss() {
        return wmss;
    }
    public void setWmss(ArrayList wmss) {
        this.wmss = wmss;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getAcquisitionId() {
        return acquisitionId;
    }
    public void setAcquisitionId(String acquisitionId) {
        this.acquisitionId = acquisitionId;
    }
}
