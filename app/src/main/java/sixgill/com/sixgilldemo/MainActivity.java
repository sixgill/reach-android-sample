package sixgill.com.sixgilldemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private boolean useDevelopmentEndpoint = true;
    private boolean enableNetworking = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button askPermission = findViewById(R.id.askPermission);
        askPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePermissions();
            }
        });
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        CheckBox networking = findViewById(R.id.useNetworking);
        networking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableNetworking = isChecked;
            }
        });

        String[] items = new String[]{"Development", "Production"};
        Spinner dropdown = findViewById(R.id.api_selector);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setSelection(0);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                useDevelopmentEndpoint = position == 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void takePermissions() {
        //Request permissions
        RequestPermission(new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                PermissionsGranted();
                break;
        }
    }

    private void RequestPermission(String[] permissions){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean granted = true;
            for (String perm : permissions) {
                if (ContextCompat.checkSelfPermission(this, perm)
                        != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                }
            }

            if (!granted) {
                requestPermissions(permissions, 1);
            } else {
                PermissionsGranted();
            }
        }
    }

    private void PermissionsGranted() {
        CheckBox cb = findViewById(R.id.location);
        cb.setChecked(true);
        // all required permissions granted
        LinearLayout permissionBox = findViewById(R.id.permissionBox);
        permissionBox.setVisibility(View.GONE);

        LinearLayout loginBox = findViewById(R.id.loginBox);
        loginBox.setVisibility(View.VISIBLE);
    }

    private void login() {
        EditText editText = findViewById(R.id.api_key);
        String apiKey = editText.getText().toString();
        Map<String, String> aliases = new HashMap<>();
        aliases.put("phone", "0123456789");

        ReachConfig config = new ReachConfig();
        if (useDevelopmentEndpoint) {
            config.setIngressURL("https://edge-ingress.staging.sixgill.io");
        } else {
            config.setIngressURL("https://sense-ingress-api.sixgill.com");
        }
        config.setSendEvents(enableNetworking);
        config.setAliases(aliases);
        Reach.initWithAPIKey(this, apiKey, config, new ReachCallback() {
            @Override
            public void onReachSuccess() {
                // successfully registered the SDK
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                startActivity(intent);
            }

            @Override
            public void onReachFailure(String s) {
                // failed to register the SDK
                Toast.makeText(MainActivity.this, "Failed to register the SDK", Toast.LENGTH_LONG).show();
            }
        });
    }
}
