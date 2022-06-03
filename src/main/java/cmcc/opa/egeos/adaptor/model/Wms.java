package cmcc.opa.egeos.adaptor.model;

public class Wms {
    private String layer;
    private String wmsUrl;
    private String wmsDescription;
    private String[] wmsDates;

    public String getLayer() {
        return layer;
    }
    public void setLayer(String layer) {
        this.layer = layer;
    }
    public String getWmsUrl() {
        return wmsUrl;
    }
    public void setWmsUrl(String wmsUrl) {
        this.wmsUrl = wmsUrl;
    }
    public String getWmsDescription() {
        return wmsDescription;
    }
    public void setWmsDescription(String wmsDescription) {
        this.wmsDescription = wmsDescription;
    }
    public String[] getWmsDates() {
        return wmsDates;
    }
    public void setWmsDates(String[] wmsDates) {
        this.wmsDates = wmsDates;
    }

}
