package tfm.uniovi.pirateseas.controller.sensors.events;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.view.Gravity;
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
        private ImageView imgSensorThumbnail;
        private TextView txtSensorName;
    }

    public SensorEventAdapter(Context context, int resource, List<AppSensorEvent> objects) {
        super(context, resource, objects);
        mContext = context;
        resourceLayout = resource;
        mSensorEvents = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(resourceLayout, parent, false);
        }

        final AppSensorEventViewHolder viewHolder =new AppSensorEventViewHolder();
        viewHolder.imgSensorThumbnail = convertView.findViewById(R.id.imgSensorThumbnail);
        viewHolder.txtSensorName = convertView.findViewById(R.id.txtSensorName);

        final View.OnClickListener sensorClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEventDialog(viewHolder.txtSensorName.getText().toString(), mSensorEvents.get(position).getImageResource(), mSensorEvents.get(position).getMessageResource());
            }
        };

        viewHolder.imgSensorThumbnail.setImageResource(mSensorEvents.get(position).getSensorThumbnailResource());
        if(!mSensorEvents.get(position).isSensorAvailable()) {
            Drawable base = mContext.getDrawable(mSensorEvents.get(position).getSensorThumbnailResource());
            Drawable overlay = mContext.getDrawable(R.drawable.ic_sensor_layered);
            Drawable[] layers = {base, overlay};
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            layerDrawable.setLayerGravity(1, Gravity.END);
            viewHolder.imgSensorThumbnail.setImageDrawable(layerDrawable);
        }
        viewHolder.imgSensorThumbnail.setOnClickListener(sensorClickListener);

        viewHolder.txtSensorName.setText(mContext.getResources().getString(mSensorEvents.get(position).getSensorName()));
        viewHolder.txtSensorName.setTextColor(mSensorEvents.get(position).hasEvent()? Color.DKGRAY : Color.WHITE);
        viewHolder.txtSensorName.setPaintFlags(mSensorEvents.get(position).hasEvent()? (viewHolder.txtSensorName.getPaintFlags() & (~Paint.FAKE_BOLD_TEXT_FLAG)) : (viewHolder.txtSensorName.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG));
        viewHolder.txtSensorName.setOnClickListener(sensorClickListener);

        return convertView;
    }

    private void createEventDialog(String eventTitle, int imageResource, int message) {
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
