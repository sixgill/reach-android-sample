
# reach-android-sample

## Overview
This repository contains the sample app and source code to demonstrate the basic use of Sixgill Reach SDK. 
You can find the complete SDK documentation [here](https://github.com/sixgill/sense-docs/blob/master/guides/002-sdks/002-android-sdk.md)

*Reach SDK version: 1.2.9*
##### [Download sample app](https://github.com/sixgill/reach-android-sample/raw/master/android-sample-build.apk)

## Implementation details
Reach SDK requires some permissions in order to work properly. Skipping some of the permissions will disable the related feature, for example if you skip the location permission then SDK won't be able to collect the location of the user.
The permissions are added in the [AndroidManifest](https://github.com/sixgill/reach-android-sample/blob/initial-setup/app/src/main/AndroidManifest.xml)

Once the permission are added in the SDK, it's time to ask the user to grant those required permissions. See [MainActivity.java](https://github.com/sixgill/reach-android-sample/blob/initial-setup/app/src/main/java/sixgill/com/sixgilldemo/MainActivity.java#L87)
```java
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

//Request permissions
RequestPermission(new String[]{
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION
});
```

To start the SDK we need to pass some of the basic information:
- API Key
- Aliases (can be unique phone number)

You can turn off the functionality of the SDK to stop sending the events to Sixgill server (by default this is ON).

See [MainActivity.java](https://github.com/sixgill/reach-android-sample/blob/initial-setup/app/src/main/java/sixgill/com/sixgilldemo/MainActivity.java#L114)
```java
ReachConfig config = new ReachConfig();
config.setSendEvents(false);
```

Optionally you can also provide Notification builder to customize the notifications.
```java
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
```

Finally to start the SDK you can call the `initWithAPIKey` method of the SDK. 
```java
Map<String, String> aliases = new HashMap<>();
aliases.put("phone", phoneNumber);
// some additional information can be added to aliases as well
aliases.put("organization", "sixgill");

ReachConfig config = new ReachConfig();
if (useDevelopmentEndpoint) {
    config.setIngressURL("https://edge-ingress.staging.sixgill.io");
} else {
    config.setIngressURL("https://sense-ingress-api.sixgill.com");
}
config.setSendEvents(false);
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
```

Once the SDK is initialized, you need to enable the SDK to start collecting the events.

Once the SDK is enabled you can collect the events data by setting up the local broadcast. Do remember to unregister the broadcast listner to prevent memory leaks

See [DetailsActivity.java](https://github.com/sixgill/reach-android-sample/blob/initial-setup/app/src/main/java/sixgill/com/sixgilldemo/DetailsActivity.java)
```java
BroadcastReceiver mEventReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String encodedEvent = bundle.getString(Reach.EVENT_DATA);
            if(encodedEvent != null) {
                byte[] b = Base64.decode(encodedEvent, Base64.DEFAULT);
                Ingress.Event event = Ingress.Event.parseFrom(b);
            }
        }
    }
};

Reach.enable(this, true, new ReachCallback() {
    @Override
    public void onReachSuccess() {
        // setup a local broadcast receiver to get events from SDK
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DetailsActivity.this);
        manager.registerReceiver(mEventReceiver, new IntentFilter(Reach.EVENT_BROADCAST));
    }

    @Override
    public void onReachFailure(String s) {
        Toast.makeText(DetailsActivity.this, "Failed to enable the SDK", Toast.LENGTH_LONG).show();
    }
});

@Override
protected void onDestroy() {
    super.onDestroy();
    // unregister the local broadcast receiver to prevent memory leak
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mEventReceiver);
}
```

Finally to stop the SDK, you need to call `disable` method of SDK
```java
Reach.disable(DetailsActivity.this);
```

Reach SDK also provides you functionality to generate event on demand. 
```java
// forceSensorUpdate will generate the event on demand, minimum duration to generate an event is 20 seconds. Any on demand event generated can be received in the local broadcast listner, described above in this readme
Reach.forceSensorUpdate(DetailsActivity.this);
```

If any permission is missing, Reach SDK will generate error code and error message in the event itself. To check the errors, you can call `getErrorCount()` method and `getErrorList()` method
```java
Ingress.Event e = events.get(position);
 if(e.getErrorCount() > 0){
    List<Ingress.Error> errors = e.getErrorList();
    int errorCode = errors.get(0).getErrorCode();
    String errorMessage = errors.get().getErrorMessage();
}
```
