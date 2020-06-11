package app.prj.vehicleparking.adapters;

import com.google.gson.Gson;

public class Vehicle {

    public static final String TABLE_REGISTER ="register";
    public static final String VEHICLE_NO ="vehicle_no";
    public static final String NAME ="name";
    public static final String MOBILE ="mobile";
    public static final String REGEX_VEHICLE_PATTERN = "\\w{2}\\d{2}\\w{1,2}\\d{4}";
    private String vehicleNo="",name="",mobile="";

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo==null?"":vehicleNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name==null?"":name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile==null?"":mobile;
    }
    public boolean isEmpty(){
        return vehicleNo.isEmpty();
    }

    @Override
    public String toString() {
        return "Vehicle No: "+vehicleNo+",Name: "+name+",Mobile: "+mobile;
    }
    public String toJson(){
        return new Gson().toJson(this);
    }
    public static Vehicle buildFromJson(String json){
        return new Gson().fromJson(json,Vehicle.class);
    }
}
