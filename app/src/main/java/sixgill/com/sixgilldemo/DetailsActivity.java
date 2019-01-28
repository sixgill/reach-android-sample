package sixgill.com.sixgilldemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sixgill.protobuf.Ingress;
import com.sixgill.sync.sdk.Reach;
import com.sixgill.sync.sdk.ReachCallback;

import sixgill.com.sixgilldemo.adapter.EventsAdapter;

import static sixgill.com.sixgilldemo.MainActivity.storeName;

public class DetailsActivity extends AppCompatActivity {
    private boolean receiverAttached = false;
    EventsAdapter adapter = null;
    private boolean sdkDisabled = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ListView detailsList = findViewById(R.id.eventsList);
        adapter = new EventsAdapter(this);
        detailsList.setAdapter(adapter);

        // enable the SDK in debug mode set to true (optionally you can turn it off to prevent logging)
        Reach.enable(this, true, new ReachCallback() {
            @Override
            public void onReachSuccess() {
                // setup a local broadcast receiver to get events from SDK
                LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DetailsActivity.this);
                manager.registerReceiver(mEventReceiver, new IntentFilter(Reach.EVENT_BROADCAST));
                receiverAttached = true;
                Toast.makeText(DetailsActivity.this, "SDK Started", Toast.LENGTH_LONG).show();
                TextView status = findViewById(R.id.status);
                status.setVisibility(View.GONE);
                LinearLayout detailsBox = findViewById(R.id.detailsBox);
                detailsBox.setVisibility(View.VISIBLE);

                //save the status that SDK was enabled, so on next app start we skip the login screen
                SharedPreferences.Editor editor = getSharedPreferences(storeName, MODE_PRIVATE).edit();
                editor.putInt("running", 1);
                editor.apply();
                sdkDisabled = false;
            }

            @Override
            public void onReachFailure(String s) {
                Toast.makeText(DetailsActivity.this, "Failed to enable the SDK", Toast.LENGTH_LONG).show();
                TextView status = findViewById(R.id.status);
                status.setText(R.string.failed);
                sdkDisabled = true;
            }
        });

        final Button stopSdk = findViewById(R.id.stopSdk);
        stopSdk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sdkDisabled) {
                    // stop the SDK
                    Reach.disable(DetailsActivity.this);
                    //save the status that SDK was stopped by user
                    SharedPreferences.Editor editor = getSharedPreferences(storeName, MODE_PRIVATE).edit();
                    editor.putInt("running", 0);
                    editor.apply();
                    sdkDisabled = true;
                    stopSdk.setText(R.string.enable_sdk);
                } else {
                    Reach.enable(DetailsActivity.this);
                    //save the status that SDK was stopped by user
                    SharedPreferences.Editor editor = getSharedPreferences(storeName, MODE_PRIVATE).edit();
                    editor.putInt("running", 1);
                    editor.apply();
                    sdkDisabled = false;
                    stopSdk.setText(R.string.stop_sdk);
                }
            }
        });

        Button forceSensor = findViewById(R.id.forcesensor);
        forceSensor.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Reach.forceSensorUpdate(DetailsActivity.this);
                Toast.makeText(DetailsActivity.this, "Event will be generated in 20 seconds", Toast.LENGTH_SHORT).show();
            }
        });

        Button clearList = findViewById(R.id.clearlist);
        clearList.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                adapter.clearEvents();
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(receiverAttached) {
            // unregister the local broadcast receiver to prevent memory leak
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mEventReceiver);
        }
    }

    BroadcastReceiver mEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String encodedEvent = bundle.getString(Reach.EVENT_DATA);
                if(encodedEvent != null) {
                    byte[] b = Base64.decode(encodedEvent, Base64.DEFAULT);
                    Ingress.Event event = Ingress.Event.parseFrom(b);
                    if(adapter != null) {
                        adapter.addEvent(event);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };
}
