package app.prj.vehicleparking;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;


public class AdminLoginActivity extends AppCompatActivity {
    TextInputLayout tUsername, tPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        tUsername = (TextInputLayout) findViewById(R.id.tUsername);
        tPassword = (TextInputLayout) findViewById(R.id.tPassword);
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
    public void bLoginOnClick(View view) {
        final String un = tUsername.getEditText().getText().toString().trim();
        final String pw = tPassword.getEditText().getText().toString().trim();
        if (un.isEmpty()) {
            tUsername.setErrorEnabled(true);
            tUsername.setError("Enter Username");
        } else if (pw.isEmpty()) {
            tPassword.setErrorEnabled(true);
            tPassword.setError("Enter Password");
        } else if(un.equals("admin") && pw.equals("admin")){
            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
        }else{
            Toast.makeText(getApplicationContext(), "Wrong username or password", Toast.LENGTH_SHORT).show();
        }
    }
}
