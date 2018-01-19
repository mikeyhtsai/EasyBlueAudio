package com.cnss.audiotest.a2dp;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaRouter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteDiscoveryFragment;
import android.support.v7.media.MediaRouteSelector;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class BTActivity extends AppCompatActivity {

    TextView connectStatusText;
    private boolean mPlaying = false;
    private MediaPlayer mediaPlayer = null;
    TextView mStatusView=null;
    public static final String LOG_TAG = "EasyBt";
    private MediaRouteSelector mSelector;
    private String mA2dpDevName=null;
    private BtReceiver mReceiver=null;
    private boolean mRegisteredReceiver = false;
    private BroadcastReceiver mBroadcastReceiver;

    private static final String DISCOVERY_FRAGMENT_TAG = "DiscoveryFragment";

    public static final class DiscoveryFragment extends MediaRouteDiscoveryFragment {
        private static final String TAG = "DiscoveryFragment";
        private android.support.v7.media.MediaRouter.Callback mCallback;

        public DiscoveryFragment() {
            mCallback = null;
        }


        public void setCallback(android.support.v7.media.MediaRouter.Callback cb) {
            mCallback = cb;
        }

        @Override
        public android.support.v7.media.MediaRouter.Callback onCreateCallback() {
            return mCallback;
        }

        @Override
        public int onPrepareCallbackFlags() {
            // Add the CALLBACK_FLAG_UNFILTERED_EVENTS flag to ensure that we will
            // observe and log all route events including those that are for routes
            // that do not match our selector.  This is only for demonstration purposes
            // and should not be needed by most applications.
            return super.onPrepareCallbackFlags() | MediaRouter.CALLBACK_FLAG_UNFILTERED_EVENTS;
        }
    }
    private final  android.support.v7.media.MediaRouter.Callback mMediaRouterCB = new android.support.v7.media.MediaRouter.Callback() {
        // Return a custom callback that will simply log all of the route events
        // for demonstration purposes.


        @Override
        public void onRouteSelected(android.support.v7.media.MediaRouter router, android.support.v7.media.MediaRouter.RouteInfo route) {
            super.onRouteSelected(router, route);
            Log.d(LOG_TAG, "onRouteSelected: route=" + route);
            Log.d(LOG_TAG, "Selected Device = "+ route.getName());
            mStatusView.setText(route.getName()+"\nSelected");
            mA2dpDevName = route.getName();
         }

        @Override
        public void onRouteUnselected(android.support.v7.media.MediaRouter router, android.support.v7.media.MediaRouter.RouteInfo route) {
            super.onRouteUnselected(router, route);
            Log.d(LOG_TAG, "onRouteUnselected: route=" + route);
        }

        @Override
        public void onRouteAdded(android.support.v7.media.MediaRouter router, android.support.v7.media.MediaRouter.RouteInfo route) {
            super.onRouteAdded(router, route);
            Log.d(LOG_TAG, "onRouteAdded: route=" + route);
            mStatusView.setText(route.getName()+"\nAdded");
        }

        @Override
        public void onRouteRemoved(android.support.v7.media.MediaRouter router, android.support.v7.media.MediaRouter.RouteInfo route) {
            super.onRouteRemoved(router, route);
            Log.d(LOG_TAG, "onRouteRemoved: route=" + route);
            mStatusView.setText(route.getName()+"\nRemoved");
        }


    };

    private final OnAudioFocusChangeListener mAfChangeListener = new OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                Log.d(LOG_TAG, "onAudioFocusChange: LOSS_TRANSIENT");
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                Log.d(LOG_TAG, "onAudioFocusChange: AUDIOFOCUS_GAIN");
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                Log.d(LOG_TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS");
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.cnss.audiotest.a2dp.R.layout.activity_bt);
        mStatusView = (TextView)findViewById(com.cnss.audiotest.a2dp.R.id.Status);
        connectStatusText = (TextView) findViewById(com.cnss.audiotest.a2dp.R.id.connectedDeviceName);

 //       mSelector =
 //               new MediaRouteSelector.Builder().addControlCategory(MediaControlIntent
 //                       .CATEGORY_LIVE_AUDIO).addControlCategory(MediaControlIntent
 //                       .CATEGORY_LIVE_VIDEO).addControlCategory(MediaControlIntent
 //                       .CATEGORY_REMOTE_PLAYBACK).addControlCategory(SampleMediaRouteProvider
 //                       .CATEGORY_SAMPLE_ROUTE).build();

        // Add a fragment to take care of media route discovery.
        // This fragment automatically adds or removes a callback whenever the activity
        // is started or stopped.
        FragmentManager fm = getSupportFragmentManager();
        DiscoveryFragment fragment =
                (DiscoveryFragment) fm.findFragmentByTag(DISCOVERY_FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new DiscoveryFragment();
            fragment.setCallback(mMediaRouterCB);
 //           fragment.setRouteSelector(mSelector);
            fm.beginTransaction().add(fragment, DISCOVERY_FRAGMENT_TAG).commit();
        } else {
            fragment.setCallback(mMediaRouterCB);
//            fragment.setRouteSelector(mSelector);
        }
        Log.v(LOG_TAG, LOG_TAG+"Creating main activity, start btThread");
        updateConnectionStatus(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);


        mReceiver = new BtReceiver();
        if (!mRegisteredReceiver) {
            try {
                registerReceiver(mReceiver, filter);
                mRegisteredReceiver = true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Unable to register BT_event receiver", e);
            }
        }

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(LOG_TAG, "Received event from local broadcast " + intent.getAction());
                updateConnectionStatus(false);
                String action = intent.getAction();
                if (action.equals(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED)) {
                    int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_NOT_PLAYING);
                    if (state == BluetoothA2dp.STATE_PLAYING) {
                        mStatusView.setText(mA2dpDevName + "\nStreaming");
                    }
                     else {
                        mStatusView.setText(mA2dpDevName + "\nStopped");
                    }

                }
            }
        };

        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
        mgr.registerReceiver(mBroadcastReceiver, new IntentFilter(filter));

    }

    public void updateConnectionStatus(boolean inited){

        BTConnectThread bt2 = new BTConnectThread(this, inited);
        Thread btThread2 = new Thread(bt2);
        btThread2.start();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.cnss.audiotest.a2dp.R.menu.menu_bt, menu);
        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.cnss.audiotest.a2dp.R.id.action_help:
                Intent intent = new Intent(this, Help.class);
                startActivity(intent);
                // Help option clicked.
                return true;

            case com.cnss.audiotest.a2dp.R.id.action_exit:
                finish();
                System.exit(0);
                // Exit option clicked.
                return true;

            case com.cnss.audiotest.a2dp.R.id.action_bt:
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) //Wrong platform
                    return true;

                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                    Log.v(LOG_TAG, LOG_TAG + " :Turn Off BT");
                }
                else {
                    mBluetoothAdapter.enable();
                    Log.v(LOG_TAG, LOG_TAG + " :Turn ON BT");
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public void onPriorityButtonClick(View v) {
        Intent intent = new Intent(this, PriorityActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
        mgr.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(this);
        mgr.unregisterReceiver(mBroadcastReceiver);
    }

    public void onFAButtonClick(View v) throws InterruptedException {
        BTConnectThread obj=new BTConnectThread(this,false);
        Thread btThread = new Thread(obj);
        btThread.start();
        updateConnectionStatus(true);
    }

    public void onPlayButtonClick(View v) {
//     String fName = "my_endless_love.mp3";
//     int resID=getResources().getIdentifier(fName, "raw", getPackageName());

     TextView playView =  (TextView) findViewById(com.cnss.audiotest.a2dp.R.id.play);
     if (mediaPlayer == null) {
         mediaPlayer = MediaPlayer.create(this, com.cnss.audiotest.a2dp.R.raw.my_endless_love);
     }

     if (mPlaying == false) {
         Log.v(LOG_TAG, LOG_TAG+" :Start playing My_endless_love" );
         mediaPlayer.start();
         mPlaying = true;


         playView.setText(com.cnss.audiotest.a2dp.R.string.Playing);
         mediaPlayer.setLooping(true);
     }
     else {
         Log.v(LOG_TAG, LOG_TAG+" :Pause playing My_endless_love" );
         mediaPlayer.pause();

         mPlaying = false;
         playView.setText(com.cnss.audiotest.a2dp.R.string.Paused);

     }

    }
}
