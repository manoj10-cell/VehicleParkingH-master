package app.prj.vehicleparking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.CommonStatusCodes;

import org.apache.http.HttpResponse;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.prj.vehicleparking.adapters.Vehicle;

public class VehicleUserActivity extends AppCompatActivity {
    private final int RC_OCR_CAPTURE = 8889;
    private final int PERMISSIONS_REQUEST = 999;
    private final String TAG = getClass().getSimpleName();

    private TextInputLayout tVehicleNo;
    private AppCompatTextView tvDetail;
    private GPSTracker tracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_user);
        tVehicleNo = (TextInputLayout) findViewById(R.id.tVehicleNo);
        tvDetail=(AppCompatTextView)findViewById(R.id.tvDetail);
        try{
            tracker=new GPSTracker(this);
        }catch (Exception ex){
            Log.d(TAG, ex.getMessage()+"");
            ex.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_web: showServerAddressDialog();break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showServerAddressDialog(){
        String addr=new AppSetting(getApplicationContext()).getServerAddress();
        new MaterialDialog.Builder(this)
                .autoDismiss(false)
                .title("Server Address")
                .input("102.168.1.100:8084", addr, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                    }
                })
                .positiveText("Set")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String address=dialog.getInputEditText().getText().toString().trim();
                        if(address.isEmpty())Toast.makeText(getApplicationContext(),"Enter Server Address" , Toast.LENGTH_SHORT).show();
                        else {
                            AppSetting appSetting= new AppSetting(getApplicationContext());
                            appSetting.setServerAddress(address);
                            appSetting.save();
                            dialog.dismiss();
                        }
                    }
                })
                .negativeText("Cancel")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                }).show();
    }
    public void bScanOnClick(View view) {
        tVehicleNo.getEditText().setText("");
        if (hasPermission(true)) {
            startScan();
        }
    }
    public void bSendOnClick(View view) {
        if (!hasPermission(true)) {
            return;
        }
        String vehNo = tVehicleNo.getEditText().getText().toString().replaceAll("[^a-zA-Z0-9]","").toUpperCase();
        if (vehNo.isEmpty() || !Pattern.compile(Vehicle.REGEX_VEHICLE_PATTERN, Pattern.CASE_INSENSITIVE).matcher(vehNo).matches()) {
            tVehicleNo.setError("Enter Valid Vehicle No");
            tVehicleNo.setErrorEnabled(true);
        } else {
            tVehicleNo.setError("");
            tVehicleNo.setErrorEnabled(false);
            Vehicle vhl=new DBHandler(getApplicationContext()).getVehicle(vehNo);
            if(!vhl.isEmpty()){
                tvDetail.setText(vhl.toString().replace(",", "\n"));
                SmsManager.getDefault().sendTextMessage(vhl.getMobile(), null, getString(R.string.sms) + " " + vhl.getVehicleNo()+"\ngps:"+tracker.getLatitude()+","+tracker.getLongitude(), null, null);
                Toast.makeText(getApplicationContext(), "sent SMS to " + vhl.getMobile(), Toast.LENGTH_SHORT).show();
            }else{
                tvDetail.setText("");
                new ApacheRest(ApacheRest.Method.GET, "http://"+new AppSetting(getApplicationContext()).getServerAddress()+"/VehicleParking/api/vehicle")
                        .addQueryParam("vehicleNo",vehNo )
                        .showProgress(true)
                        .setOnResponseListener(new ApacheRest.OnResponseListener() {
                            @Override
                            public void onResponse(String responseContent, HttpResponse httpResponse) {
                                try{
                                    JSONObject jRes=new JSONObject(responseContent);
                                    if(jRes.getBoolean("status")){
                                        Vehicle vhl=Vehicle.buildFromJson(jRes.optJSONObject("data").toString());
                                        tvDetail.setText(vhl.toString().replace(",", "\n"));

                                        Toast.makeText(getApplicationContext(), "sent SMS to " + vhl.getMobile(), Toast.LENGTH_SHORT).show();
                                        SmsManager.getDefault().sendTextMessage(vhl.getMobile(), null, getString(R.string.sms) + " " + vhl.getVehicleNo()+"\ngps:"+tracker.getLatitude()+","+tracker.getLongitude(), null, null);
                                        new DBHandler(getApplicationContext()).addVehicle(vhl);
                                    }else{
                                        Toast.makeText(getApplicationContext(),jRes.getString("error")+"" ,Toast.LENGTH_SHORT ).show();
                                    }
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                    Toast.makeText(getApplicationContext(), ex.getMessage()+"", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onError(Exception error) {
                                Toast.makeText(getApplicationContext(),error.getMessage()+"" ,Toast.LENGTH_SHORT ).show();
                            }
                        }).connect();

            }

        }
    }


    void startScan() {
        Intent intent = new Intent(this, OcrCaptureActivity.class);
        intent.putExtra(OcrCaptureActivity.AutoFocus, true);
        intent.putExtra(OcrCaptureActivity.UseFlash, false);
        startActivityForResult(intent, RC_OCR_CAPTURE);
    }

    private boolean hasPermission(boolean canRequest) {
        boolean has = true;
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.SEND_SMS,Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
        for (String permission : permissions)
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                has = false;
                break;
            }
        if (!has && canRequest) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST);
        }
        return has;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (!hasPermission(false)) {
                Snackbar.make(tVehicleNo, "Need Pemission for OCR", Snackbar.LENGTH_INDEFINITE).setAction("Grant", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hasPermission(true);
                    }
                }).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_OCR_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String text = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
                    try {
                        text = text.replaceAll("[^a-zA-Z0-9]", "");

                        Matcher m = Pattern.compile(Vehicle.REGEX_VEHICLE_PATTERN, Pattern.CASE_INSENSITIVE).matcher(text);
                        if (m.find()) {
                            String vehNo=m.group().toUpperCase();
                            tVehicleNo.getEditText().setText(vehNo);
                            Vehicle vh=new DBHandler(getApplicationContext()).getVehicle(vehNo);
                            if(!vh.isEmpty()){
                                tvDetail.setText(vh.toString().replace(",", "\n"));
                            }else tvDetail.setText("");
                        }else{
                            tVehicleNo.getEditText().setText("");
                            tvDetail.setText("");
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    Log.d(TAG, "Text read: " + text);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.ocr_failure), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "No Text captured, intent data is null");
                }
            } else {
                Toast.makeText(getApplicationContext(), String.format(getString(R.string.ocr_error), CommonStatusCodes.getStatusCodeString(resultCode)), Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
