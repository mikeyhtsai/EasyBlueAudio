package com.cnss.audiotest.a2dp;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class BtReceiver extends BroadcastReceiver {
    private static final String TAG = "EasyBt";
    private static boolean wait_for_gattdereg = false;
    private int state;
    private BluetoothAdapter mBluetoothAdapter;
    static boolean wait_for_bt = false;
    static boolean isBleEnabled = false;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        ContentResolver cr = context.getContentResolver();
        Log.d(TAG, "Receive Intent - Action" + action);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "Received ACTION_BOOT_COMPLETED");
            if (mBluetoothAdapter != null) {
                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    Log.d(TAG, "BT State if OFF when boot completed");
                }
            }
        }

       /* 1> if State changes from BT-ON to BLE-ALWAYS when MTP is
       ** still on pad, pad detection will be broadcasted to register
       ** a4wp service
       ** 2> Register A4WP if BT is turned-on.
       */
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            state = intent.getIntExtra
                    (BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (BluetoothAdapter.STATE_OFF == state) {
                Log.d(TAG, "BT State changed to OFF");
            } else if (BluetoothAdapter.STATE_ON == state) {
                Log.d(TAG, "BT State changed to ON");
            }

        }
        /* as we need to update activity GUI, so we send the intent back via localbroadcastmanager

         */
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(context);
        mgr.sendBroadcast(intent);

    }
}
