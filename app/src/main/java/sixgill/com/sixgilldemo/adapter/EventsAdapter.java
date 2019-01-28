package sixgill.com.sixgilldemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sixgill.protobuf.Ingress;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import sixgill.com.sixgilldemo.R;

public class EventsAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private List<Ingress.Event> events = new ArrayList<>();

    public EventsAdapter(Context ctx){
        layoutInflater = LayoutInflater.from(ctx);
    }

    public void addEvent(Ingress.Event event){
        events.add(event);
    }

    public void clearEvents(){
        events.clear();
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.event_details, parent, false);
            holder = new ViewHolder();
            holder.timestamp = convertView.findViewById(R.id.timestamp);
            holder.latlng = convertView.findViewById(R.id.latlng);
            holder.activity = convertView.findViewById(R.id.activity);
            holder.errors = convertView.findViewById(R.id.errors);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Ingress.Event e = events.get(position);
        String timestamp = "Time: " + FormatTimestamp(e.getTimestamp());
        holder.timestamp.setText(timestamp);

        if(e.getLocationsCount() > 0) {
            Ingress.Location location = e.getLocations(0);
            float latitude = location.getLatitude();
            float longitude = location.getLongitude();
            String value = "Location: " + String.valueOf(latitude) + ", " + String.valueOf(longitude);
            holder.latlng.setText(value);
        } else {
            holder.latlng.setText(R.string.no_location);
        }
        if(e.getErrorCount() > 0){
            StringBuilder errorData = new StringBuilder();
            errorData.append("Errors:");
            List<Ingress.Error> errors = e.getErrorList();
            for(Ingress.Error err : errors){
                String errMsg = "Message: " + err.getErrorMessage() + ", Code: " + err.getErrorCode();
                errorData.append("\n").append(errMsg);
            }
            holder.errors.setText(errorData);
        } else {
            holder.errors.setText(R.string.no_error);
        }

        if(e.getActivitiesCount() > 0) {
            Ingress.Activity activity = e.getActivities(0);
            String activityText = "Activity: " + activity.getType();
            holder.activity.setText(activityText);
        } else {
            holder.activity.setText(R.string.no_activity);
        }
        return convertView;
    }

    private static String FormatTimestamp(long timestamp){
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a, MMM dd, yyyy", Locale.US);
        Date netDate = (new Date(timestamp));
        return sdf.format(netDate);
    }

    static class ViewHolder {
        TextView timestamp;
        TextView latlng;
        TextView activity;
        TextView errors;
    }
}
