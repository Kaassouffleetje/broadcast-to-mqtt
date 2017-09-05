package pixento.nl.broadcasttomqtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static pixento.nl.broadcasttomqtt.MainActivity.bcPrefsKey;

/**
 * The BroadcastReceiver for all broadcasts we're subscribed to.
 * Forwards the broadcast messages using MQTT.
 */
public class SubBroadcastReceiver extends BroadcastReceiver {
    
    private static final String TAG = "SubBroadcastReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the action
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        // Get the preferences manager
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        
        // Get the broadcast items from preference manager
        Set<String> bcItemsSet = prefs.getStringSet(bcPrefsKey, new HashSet<String>());
        BroadcastItemList bcItems = new BroadcastItemList(bcItemsSet);
        
        // Find the broadcast item of the current intent, and get a reference
        BroadcastItem current = bcItems.search(action);
        current.count_executed++;
        current.last_executed = new Date();
        
        // Save the edited list
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(bcPrefsKey, bcItems.toStringSet());
        editor.commit();
        
        Log.v(TAG, "Received bc: " + action);
        
        // Create a JSON object with all data from the intent
        JSONObject payload = new JSONObject();
        try {
            // Add the action and alias
            payload.put("action", action);
            payload.put("alias", current.alias);
            
            // if no extras have been added to the intent, extras = null
            if(extras != null) {
                for (String key : extras.keySet()) {
                    payload.put(key, extras.get(key));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        // Get the MqttConnection instance and enqueue the message
        MqttConnection connection = MqttConnection.getInstance(context);
        connection.enqueue(payload);

        // Set an retry alarm in case some messages were not sent
        connection.setRetryAlarm(context);
    }
}
