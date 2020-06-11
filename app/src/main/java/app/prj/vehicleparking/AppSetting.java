package app.prj.vehicleparking;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSetting {
    private SharedPreferences pref;
    private String serverAddress;

    public AppSetting(Context context) {
        pref=context.getSharedPreferences("settings",Context.MODE_PRIVATE );
        load();
    }
    private void load(){
        serverAddress=pref.getString("serverAddress", "");
    }
    public void save(){
        SharedPreferences.Editor pref=this.pref.edit();
        pref.putString("serverAddress", serverAddress);
        pref.commit();
    }
    public void clear(){
        pref.edit().clear().commit();
    }
    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
}
