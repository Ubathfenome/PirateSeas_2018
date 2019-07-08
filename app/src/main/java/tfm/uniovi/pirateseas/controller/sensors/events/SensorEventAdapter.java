package tfm.uniovi.pirateseas.controller.sensors.events;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.view.activities.SensorActivity;

public class SensorEventAdapter extends ArrayAdapter<AppSensorEvent> {

    private List<AppSensorEvent> mSensorEvents;
    private Context mContext;
    private int resourceLayout;

    private class AppSensorEventViewHolder {
        private TextView txtSensorName;
        private CheckBox chkActive;
        private TextView txtEventName;
        private ImageView imgEventThumbnail;
    }

    public SensorEventAdapter(Context context, int resource, List<AppSensorEvent> objects) {
        super(context, resource, objects);
        mContext = context;
        resourceLayout = resource;
        mSensorEvents = objects;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(resourceLayout, parent, false);
        }

        final AppSensorEventViewHolder viewHolder =new AppSensorEventViewHolder();
        viewHolder.txtSensorName = convertView.findViewById(R.id.txtSensorName);
        viewHolder.chkActive = convertView.findViewById(R.id.chkActive);
        viewHolder.txtEventName = convertView.findViewById(R.id.txtEventName);
        viewHolder.imgEventThumbnail = convertView.findViewById(R.id.imgEventThumbnail);

        final View.OnClickListener sensorClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mSensorEvents.get(position).isActive()) {
                    Toast.makeText(mContext, mContext.getString(R.string.toast_event_enabled,
                            viewHolder.txtSensorName.getText().toString(),
                            viewHolder.txtEventName.getText().toString()),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.toast_event_disabled,
                            viewHolder.txtSensorName.getText().toString(),
                            viewHolder.txtEventName.getText().toString()),
                            Toast.LENGTH_LONG).show();
                }
            }
        };

        View.OnClickListener eventClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                LayoutInflater inflater = ((SensorActivity)mContext).getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.event_dialog, null))
                        .setPositiveButton(R.string.command_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create().show();
            }
        };

        viewHolder.txtSensorName.setText(mSensorEvents.get(position).getSensorName());
        viewHolder.txtSensorName.setOnClickListener(sensorClickListener);
        viewHolder.chkActive.setChecked(mSensorEvents.get(position).isActive());
        viewHolder.chkActive.setEnabled(false);
        viewHolder.chkActive.setOnClickListener(sensorClickListener);
        viewHolder.txtEventName.setText(mSensorEvents.get(position).getEventName());
        viewHolder.txtEventName.setOnClickListener(eventClickListener);
        viewHolder.imgEventThumbnail.setImageResource(mSensorEvents.get(position).getThumbnailResource());
        viewHolder.imgEventThumbnail.setOnClickListener(eventClickListener);

        return convertView;
    }
}
