package sixgill.com.sixgilldemo;

import android.Manifest;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.sixgill.sync.sdk.Reach;
import com.sixgill.sync.sdk.ReachCallback;
import com.sixgill.sync.sdk.ReachConfig;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private boolean useProductionEndpoint = true;
    private boolean enableNetworking = false;
    public static String storeName = "DEMO-SIXGILL-ANDROID";
    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //check is SDK was started earlier or not
        SharedPreferences prefs = getSharedPreferences(storeName, MODE_PRIVATE);
        int running = prefs.getInt("running", 0);

        if(running == 1) {
            //SDK was running earlier, skip login screen
            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_main);
        String[] items = new String[]{"https://sense-ingress-api.sixgill.com", "https://edge-ingress.staging.sixgill.io"};
        Spinner dropdown = findViewById(R.id.api_selector);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setSelection(0);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                useProductionEndpoint = position == 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        CheckBox sendToSixgill = findViewById(R.id.sendToSixgill);
        sendToSixgill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableNetworking = isChecked;
            }
        });

        //Request permissions
        RequestPermission(new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void RequestPermission(String[] permissions){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean granted = true;
            for (String perm : permissions) {
                if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                }
            }

            if (!granted) {
                requestPermissions(permissions, 1);
            }
        }
    }

    private void login() {
        EditText editText = findViewById(R.id.api_key);
        if(TextUtils.isEmpty(editText.getText())) {
            editText.setError("API Key is required");
            return;
        }
        String apiKey = editText.getText().toString();

        EditText phoneText = findViewById(R.id.phone);
        if(TextUtils.isEmpty(phoneText.getText())){
            phoneText.setError("Phone number is required");
            return;
        }
        String phone = phoneText.getText().toString();
        Map<String, String> aliases = new HashMap<>();
        aliases.put("phone", phone);

        ReachConfig config = new ReachConfig();
        if (useProductionEndpoint) {
            config.setIngressURL("https://sense-ingress-api.sixgill.com");
        } else {
            config.setIngressURL("https://edge-ingress.staging.sixgill.io");
        }
        config.setSendEvents(enableNetworking);
        config.setAliases(aliases);

        //custom notification builder for sticky notification
        Notification.Builder stickyBuilder = new Notification.Builder(getApplicationContext())
                .setContentTitle("Reach SDK") // custom sticky notification title
                .setContentText("This shows demo of sticky notification") // custom sticky notification body
                .setSmallIcon(R.drawable.sticky_notification_icon) // custom icon for sticky notifications
                .setAutoCancel(true);
        config.setStickyNotificationBuilder(stickyBuilder);

        // custom notification builder for any notification
        Notification.Builder notification = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.notification_icon) // custom icon for normal notifications
                .setAutoCancel(true);
        config.setNotificationBuilder(notification);

        progressDialog = ProgressDialog.show(this, "Wait",
                "Initializing the SDK, Please wait...", true);
        progressDialog.setCancelable(false);
        Reach.initWithAPIKey(this, apiKey, config, new ReachCallback() {
            @Override
            public void onReachSuccess() {
                // successfully registered the SDK
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                startActivity(intent);
                progressDialog.cancel();
            }

            @Override
            public void onReachFailure(String s) {
                // failed to register the SDK
                Toast.makeText(MainActivity.this, "Failed to register the SDK", Toast.LENGTH_LONG).show();
                progressDialog.cancel();
            }
        });
    }
}
