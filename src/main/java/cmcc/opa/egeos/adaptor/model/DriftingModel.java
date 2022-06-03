package cmcc.opa.egeos.adaptor.model;

public class DriftingModel {

    private String id;
    private EosJsonData json_data;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public EosJsonData getJson_data() {
        return json_data;
    }
    public void setJson_data(EosJsonData json_data) {
        this.json_data = json_data;
    }
    
}
