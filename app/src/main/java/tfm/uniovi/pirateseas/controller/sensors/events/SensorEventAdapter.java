package tfm.uniovi.pirateseas.controller.sensors.events;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.view.activities.SensorActivity;

public class SensorEventAdapter extends ArrayAdapter<AppSensorEvent> {

    private List<AppSensorEvent> mSensorEvents;
    private Context mContext;
    private int resourceLayout;

    private class AppSensorEventViewHolder {
        private TextView txtSensorName;
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
        viewHolder.txtEventName = convertView.findViewById(R.id.txtEventName);
        viewHolder.imgEventThumbnail = convertView.findViewById(R.id.imgEventThumbnail);

        final View.OnClickListener sensorClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEventDialog(viewHolder.txtSensorName.getText().toString(), 0, mContext.getString(R.string.toast_event_enabled,
                        viewHolder.txtSensorName.getText().toString(),
                        viewHolder.txtEventName.getText().toString()));
            }
        };

        View.OnClickListener eventClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEventDialog(viewHolder.txtEventName.getText().toString(),
                        mSensorEvents.get(position).getImageResource(),
                        mContext.getString(mSensorEvents.get(position).getMessageResource()));
            }
        };

        viewHolder.txtSensorName.setText(mSensorEvents.get(position).getSensorName());
        viewHolder.txtSensorName.setPaintFlags(mSensorEvents.get(position).isActive()? (viewHolder.txtSensorName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG) : (viewHolder.txtSensorName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)));
        viewHolder.txtSensorName.setOnClickListener(sensorClickListener);

        viewHolder.txtEventName.setText(mSensorEvents.get(position).getEventName());
        viewHolder.txtEventName.setOnClickListener(eventClickListener);

        viewHolder.imgEventThumbnail.setImageResource(mSensorEvents.get(position).getThumbnailResource());
        viewHolder.imgEventThumbnail.setOnClickListener(eventClickListener);

        return convertView;
    }

    private void createEventDialog(String eventTitle, int imageResource, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = ((SensorActivity)mContext).getLayoutInflater();
        View inflatedView = inflater.inflate(R.layout.event_dialog, null);
        TextView txtEventTitle = inflatedView.findViewById(R.id.txtEventTitle);
        txtEventTitle.setText(eventTitle);
        ImageView imgEvent = inflatedView.findViewById(R.id.imgEvent);
        imgEvent.setImageResource(imageResource);
        TextView txtEventMessage = inflatedView.findViewById(R.id.txtEventMessage);
        txtEventMessage.setText(message);
        Button btnEventDialogOk = inflatedView.findViewById(R.id.btnEventDialogOk);

        final AlertDialog dialog = builder.setView(inflatedView).create();
        btnEventDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
