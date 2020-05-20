package tfm.uniovi.pirateseas.utils.persistence;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FontAdapter extends ArrayAdapter<String> {
    private Typeface font;
    private Context context;
    private int idListView;
    private int idTextView;
    private String[] values;

    public FontAdapter(Context context, int idListView, int idTextView, String[] stringArray, Typeface customFont) {
        super(context, idListView, idTextView, stringArray);
        this.font = customFont;
        this.context = context;
        this.values = stringArray;
        this.idListView = idListView;
        this.idTextView = idTextView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(idListView,parent, false);
        TextView textView = rowView.findViewById(idTextView);

        textView.setTypeface(font); // set typeface here
        textView.setText(values[position]);

        return rowView;
    }
}
