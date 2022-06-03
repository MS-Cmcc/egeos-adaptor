package cmcc.opa.egeos.adaptor.model;

public class Crs{
    public String type;
    public Properties properties;  
     
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
}