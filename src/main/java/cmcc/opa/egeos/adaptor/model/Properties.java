package cmcc.opa.egeos.adaptor.model;

import java.util.Date;
import com.google.gson.annotations.SerializedName;

public class Properties{
    public int id;
    public double length_km;
    public double width_km;
    public double baric_lat;
    public double min_lat;
    public String possible_s;
    public int class_val;
    public String region_aff;
    public double max_lon;
    public double area_km;
    public String country_as;
    public double max_lat;
    public double min_lon;
    public double baric_lon;
    public String alarm_lev;
    @SerializedName("date-time") 
    public Date dateTime;
    public String name;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLength_km() {
        return this.length_km;
    }

    public void setLength_km(double length_km) {
        this.length_km = length_km;
    }

    public double getWidth_km() {
        return this.width_km;
    }

    public void setWidth_km(double width_km) {
        this.width_km = width_km;
    }

    public double getBaric_lat() {
        return this.baric_lat;
    }

    public void setBaric_lat(double baric_lat) {
        this.baric_lat = baric_lat;
    }

    public double getMin_lat() {
        return this.min_lat;
    }

    public void setMin_lat(double min_lat) {
        this.min_lat = min_lat;
    }

    public String getPossible_s() {
        return this.possible_s;
    }

    public void setPossible_s(String possible_s) {
        this.possible_s = possible_s;
    }

    public int getClass_val() {
        return this.class_val;
    }

    public void setClass_val(int class_val) {
        this.class_val = class_val;
    }

    public String getRegion_aff() {
        return this.region_aff;
    }

    public void setRegion_aff(String region_aff) {
        this.region_aff = region_aff;
    }

    public double getMax_lon() {
        return this.max_lon;
    }

    public void setMax_lon(double max_lon) {
        this.max_lon = max_lon;
    }

    public double getArea_km() {
        return this.area_km;
    }

    public void setArea_km(double area_km) {
        this.area_km = area_km;
    }

    public String getCountry_as() {
        return this.country_as;
    }

    public void setCountry_as(String country_as) {
        this.country_as = country_as;
    }

    public double getMax_lat() {
        return this.max_lat;
    }

    public void setMax_lat(double max_lat) {
        this.max_lat = max_lat;
    }

    public double getMin_lon() {
        return this.min_lon;
    }

    public void setMin_lon(double min_lon) {
        this.min_lon = min_lon;
    }

    public double getBaric_lon() {
        return this.baric_lon;
    }

    public void setBaric_lon(double baric_lon) {
        this.baric_lon = baric_lon;
    }

    public String getAlarm_lev() {
        return this.alarm_lev;
    }

    public void setAlarm_lev(String alarm_lev) {
        this.alarm_lev = alarm_lev;
    }

    public Date getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }


}