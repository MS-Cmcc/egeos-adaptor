package cmcc.opa.egeos.adaptor.model;

public class Feature{
    public String type;
    public String id;
    public Geometry geometry;
    public String geometry_name;
    public Properties properties;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Geometry getGeometry() {
        return this.geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getGeometry_name() {
        return this.geometry_name;
    }

    public void setGeometry_name(String geometry_name) {
        this.geometry_name = geometry_name;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    
}