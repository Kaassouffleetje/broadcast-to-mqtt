package pixento.nl.broadcasttomqtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RetryBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "RetryBroadcastReceiver";

    /**
     * This function is called if a retry broadcast is sent, so we are retrying to send the queue
     * of messages in MqttConnection
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Forward the intent to the service
        Intent retry_intent = new Intent(context, MqttBroadcastService.class);
        retry_intent.putExtra("retry_sending", true);
        context.startService(retry_intent);
    }
}
