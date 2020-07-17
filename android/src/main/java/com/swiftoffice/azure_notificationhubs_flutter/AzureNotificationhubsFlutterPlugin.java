package com.swiftoffice.azure_notificationhubs_flutter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class AzureNotificationhubsFlutterPlugin extends BroadcastReceiver implements FlutterPlugin, MethodCallHandler {

    private static Context applicationContext;
    private MethodChannel channel;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        onAttachedToEngine(binding.getApplicationContext(), binding.getBinaryMessenger());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        LocalBroadcastManager.getInstance(binding.getApplicationContext()).unregisterReceiver(this);
    }

    private void onAttachedToEngine(Context context, BinaryMessenger binaryMessenger) {
        applicationContext = context;
        channel = new MethodChannel(binaryMessenger, "azure_notificationhubs_flutter");
        channel.setMethodCallHandler(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NotificationService.ACTION_TOKEN);
        intentFilter.addAction(NotificationService.ACTION_REMOTE_MESSAGE);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(applicationContext);
        manager.registerReceiver(this, intentFilter);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("configure")) {
            registerWithNotificationHubs();
            NotificationService.createChannelAndHandleNotifications(applicationContext);
        } else {
            result.notImplemented();
        }
    }

    public void registerWithNotificationHubs() {
        Intent intent = new Intent(applicationContext, RegistrationIntentService.class);
        applicationContext.startService(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (action.equals(NotificationService.ACTION_TOKEN)) {
            String token = intent.getStringExtra(NotificationService.EXTRA_TOKEN);
            channel.invokeMethod("onToken", token);
        } else if (action.equals(NotificationService.ACTION_REMOTE_MESSAGE)) {
            RemoteMessage message = intent.getParcelableExtra(NotificationService.EXTRA_REMOTE_MESSAGE);
            Map<String, Object> content = NotificationService.parseRemoteMessage(message);
            channel.invokeMethod("onMessage", content);
        }
    }
}