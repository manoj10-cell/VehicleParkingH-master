package app.prj.vehicleparking;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
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

public class HomeActivity extends AppCompatActivity {
    private final int RC_OCR_CAPTURE = 9003;
    private final int PERMISSIONS_REQUEST = 100;
    private final String TAG = getClass().getSimpleName();
    private final String REGEX_VEHICLE_NO = "\\w{2}\\d{2}\\w{1,2}\\d{4}";
    private TextInputLayout tVehicleNo;
    private AppCompatTextView tvDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        tVehicleNo = (TextInputLayout) findViewById(R.id.tVehicleNo);
    }

    public void bScanOnClick(View view) {
        tVehicleNo.getEditText().setText("");
        if (hasPermission(true)) {
            startScan();
        }
    }

    public void bRegisterOnClick(View view) {
        String vehNo = tVehicleNo.getEditText().getText().toString();
        if (vehNo.isEmpty() || !Pattern.compile(Vehicle.REGEX_VEHICLE_PATTERN, Pattern.CASE_INSENSITIVE).matcher(vehNo).matches()) {
            tVehicleNo.setError("Enter Valid Vehicle No");
            tVehicleNo.setErrorEnabled(true);
        } else {
            tVehicleNo.setError("");
            tVehicleNo.setErrorEnabled(false);
            Vehicle vhl = new Vehicle();
            vhl.setVehicleNo(vehNo);
            showRegisterDialog(vhl);
        }
    }

    void showRegisterDialog(final Vehicle vhl){
        View customView=LayoutInflater.from(this).inflate(R.layout.dialog_register,null,false);

        final Dialog dialog=new AlertDialog.Builder(this)
                .setTitle("Register Vehicle")
                .setView(customView)
                .setCancelable(false).show();
        final TextInputLayout tVehicleNo =((TextInputLayout)customView.findViewById(R.id.tVehicleNo));
        tVehicleNo.getEditText().setText(vhl.getVehicleNo());
        final TextInputLayout tName =((TextInputLayout)customView.findViewById(R.id.tName));
        final TextInputLayout tMobNo =((TextInputLayout)customView.findViewById(R.id.tMobNo));
        final AppCompatButton bOk =((AppCompatButton)customView.findViewById(R.id.bOk));
        final AppCompatButton bCancel =((AppCompatButton)customView.findViewById(R.id.bCancel));
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String vehNo=tVehicleNo.getEditText().getText().toString().trim();
                String mobNo=tMobNo.getEditText().getText().toString().trim();
                String name=tName.getEditText().getText().toString().trim();
                if(!Pattern.compile(REGEX_VEHICLE_NO,Pattern.CASE_INSENSITIVE).matcher(vehNo).matches()){
                    tVehicleNo.setErrorEnabled(true);
                    tVehicleNo.setError("Enter Valid Vehicle No");
                }else if(mobNo.length()>=10 && mobNo.length()<=13 && !Patterns.PHONE.matcher(mobNo).matches()){
                    tVehicleNo.setErrorEnabled(false);
                    tMobNo.setErrorEnabled(true);
                    tMobNo.setError("Enter Valid Mobile No");
                }else if(name.isEmpty()){
                    tVehicleNo.setErrorEnabled(false);
                    tMobNo.setErrorEnabled(false);
                    tName.setErrorEnabled(true);
                    tName.setError("Enter Name");
                }else {
                    Vehicle vhl = new Vehicle();
                    vhl.setVehicleNo(vehNo);
                    vhl.setMobile(mobNo);
                    vhl.setName(name);
                    long idx = new DBHandler(getApplicationContext()).addVehicle(vhl);
                    if (idx > 0) {
                        Toast.makeText(getApplicationContext(), "Registered", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getApplicationContext(), "Not Register", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }
    public void bViewRegisterOnClick(View view) {
        Intent it = new Intent(getApplicationContext(), ViewVehiclesActivity.class);
        startActivity(it);
    }

    void startScan() {
        Intent intent = new Intent(this, OcrCaptureActivity.class);
        intent.putExtra(OcrCaptureActivity.AutoFocus, true);
        intent.putExtra(OcrCaptureActivity.UseFlash, false);
        startActivityForResult(intent, RC_OCR_CAPTURE);
    }

    private boolean hasPermission(boolean canRequest) {
        boolean has = true;
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.SEND_SMS,Manifest.permission.READ_PHONE_STATE};
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
            if (hasPermission(false)) {
                startScan();
            } else {
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
