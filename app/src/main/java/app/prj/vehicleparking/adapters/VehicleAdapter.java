package app.prj.vehicleparking.adapters;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.regex.Pattern;

import app.prj.vehicleparking.DBHandler;
import app.prj.vehicleparking.R;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {
    List<Vehicle> vehicles;
    public VehicleAdapter(List<Vehicle> vehicles){
        this.vehicles=vehicles;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder viewHolder, final int position) {
        final Vehicle vhl=vehicles.get(position);
        viewHolder.tvVehicleNo.setText(vhl.getVehicleNo());
        viewHolder.tvName.setText(vhl.getName());
        viewHolder.tvMobile.setText(vhl.getMobile());
        viewHolder.bEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            showUpdateDialog(view.getContext(), vhl,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    class VehicleViewHolder extends RecyclerView.ViewHolder{
        AppCompatTextView tvVehicleNo,tvName,tvMobile;
        AppCompatImageButton bEdit;
        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVehicleNo=(AppCompatTextView)itemView.findViewById(R.id.tvVehicleNo);
            tvName=(AppCompatTextView)itemView.findViewById(R.id.tvName);
            tvMobile=(AppCompatTextView)itemView.findViewById(R.id.tvMobile);
            bEdit=(AppCompatImageButton)itemView.findViewById(R.id.bEdit);
        }
    }

    void showUpdateDialog(final Context context, final Vehicle vhl, final int position){
        View customView=LayoutInflater.from(context).inflate(R.layout.dialog_register,null,false);

        final Dialog dialog=new AlertDialog.Builder(context)
                .setTitle("Update Vehicle")
                .setView(customView)
                .setCancelable(false).show();
        final TextInputLayout tVehicleNo =((TextInputLayout)customView.findViewById(R.id.tVehicleNo));
        tVehicleNo.getEditText().setText(vhl.getVehicleNo());
        final TextInputLayout tName =((TextInputLayout)customView.findViewById(R.id.tName));
        tName.getEditText().setText(vhl.getName());
        final TextInputLayout tMobNo =((TextInputLayout)customView.findViewById(R.id.tMobNo));
        tMobNo.getEditText().setText(vhl.getMobile());
        final AppCompatButton bOk =((AppCompatButton)customView.findViewById(R.id.bOk));
        bOk.setText("Save");
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
                if(!Pattern.compile(Vehicle.REGEX_VEHICLE_PATTERN,Pattern.CASE_INSENSITIVE).matcher(vehNo).matches()){
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
                    long idx = new DBHandler(context).editVehicle(vhl);
                    if (idx > 0) {
                        Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show();
                        vehicles.set(position, vhl);
                        VehicleAdapter.this.notifyDataSetChanged();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }
}
