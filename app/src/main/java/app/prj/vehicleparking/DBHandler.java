package app.prj.vehicleparking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import app.prj.vehicleparking.adapters.Vehicle;

public class DBHandler extends SQLiteOpenHelper {

    public DBHandler(Context context) {
        super(context, "vehicle.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE "+Vehicle.TABLE_REGISTER +" ("+Vehicle.VEHICLE_NO +" TEXT  PRIMARY KEY,"+Vehicle.NAME +" TEXT,"+Vehicle.MOBILE +" TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE "+Vehicle.TABLE_REGISTER);
        onCreate(db);
    }

    public long addVehicle(Vehicle vhl){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(Vehicle.VEHICLE_NO, vhl.getVehicleNo());
            values.put(Vehicle.MOBILE, vhl.getMobile());
            values.put(Vehicle.NAME, vhl.getName());
            return db.insert(Vehicle.TABLE_REGISTER, null, values);
        }catch (Exception ex){
            ex.printStackTrace();
            return 0;
        }
    }

    public long editVehicle(Vehicle vhl){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(Vehicle.VEHICLE_NO, vhl.getVehicleNo());
            values.put(Vehicle.MOBILE, vhl.getMobile());
            values.put(Vehicle.NAME, vhl.getName());
            return db.update(Vehicle.TABLE_REGISTER, values,Vehicle.VEHICLE_NO +"=?",new String[]{vhl.getVehicleNo()});
        }catch (Exception ex){
            ex.printStackTrace();
            return 0;
        }
    }

    public Vehicle getVehicle(String vehNo){
        Vehicle vhl=new Vehicle();
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(Vehicle.TABLE_REGISTER,new String[] {Vehicle.VEHICLE_NO,Vehicle.NAME,Vehicle.MOBILE},Vehicle.VEHICLE_NO +"=?",new String[]{vehNo},null,null,null);
            if(cursor.moveToFirst())
                {
                    vhl.setVehicleNo(cursor.getString(cursor.getColumnIndex(Vehicle.VEHICLE_NO)));
                    vhl.setName(cursor.getString(cursor.getColumnIndex(Vehicle.NAME)));
                    vhl.setMobile(cursor.getString(cursor.getColumnIndex(Vehicle.MOBILE)));
                }
            cursor.close();
        }catch (Exception ex){ex.printStackTrace();}
        return vhl;
    }

    public List<Vehicle> getAllVehicles(){
        List<Vehicle> all=new ArrayList<>();
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(Vehicle.TABLE_REGISTER,new String[] {Vehicle.VEHICLE_NO,Vehicle.NAME,Vehicle.MOBILE},null,null,null,null,Vehicle.VEHICLE_NO +" ASC");
            if(cursor.moveToFirst())
             do{
                Vehicle vhl=new Vehicle();
                vhl.setVehicleNo(cursor.getString(cursor.getColumnIndex(Vehicle.VEHICLE_NO)));
                vhl.setName(cursor.getString(cursor.getColumnIndex(Vehicle.NAME)));
                vhl.setMobile(cursor.getString(cursor.getColumnIndex(Vehicle.MOBILE)));
                all.add(vhl);
            }while(cursor.moveToNext());
            cursor.close();
        }catch (Exception ex){ex.printStackTrace();}
        return all;
    }
}
