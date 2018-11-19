package sixgill.com.sixgilldemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sixgill.protobuf.Ingress;

import java.util.ArrayList;
import java.util.List;

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
            convertView = layoutInflater.inflate(R.layout.event_details, null);
            holder = new ViewHolder();
            holder.timestamp = convertView.findViewById(R.id.timestamp);
            holder.latlng = convertView.findViewById(R.id.latlng);
            holder.activity = convertView.findViewById(R.id.activity);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Ingress.Event e = events.get(position);
        holder.timestamp.setText(String.valueOf(e.getTimestamp()));

        if(e.getLocationsCount() > 0) {
            Ingress.Location location = e.getLocations(0);
            float latitude = location.getLatitude();
            float longitude = location.getLongitude();
            String value = String.valueOf(latitude) + ", " + String.valueOf(longitude);
            holder.latlng.setText(value);
        } else {
            holder.latlng.setText(R.string.no_location);
        }

        if(e.getActivitiesCount() > 0) {
            Ingress.Activity activity = e.getActivities(0);
            holder.activity.setText(activity.getType());
        } else {
            holder.activity.setText(R.string.no_activity);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView timestamp;
        TextView latlng;
        TextView activity;
    }
}
