package com.cnss.audiotest.a2dp;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import static com.cnss.audiotest.a2dp.BTDataPersistence.btDeviceNames;
import static com.cnss.audiotest.a2dp.BTDataPersistence.storePriority;


public class PriorityActivity extends AppCompatActivity {

    DynamicListView1 listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.cnss.audiotest.a2dp.R.layout.activity_priority);
        listCreate();
    }

    protected void listCreate(){
        StableArrayAdapter adapter = null;
        adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, btDeviceNames);
        listView = (DynamicListView1) findViewById(com.cnss.audiotest.a2dp.R.id.id_priorityList);
        listView.setCheeseList(btDeviceNames);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.cnss.audiotest.a2dp.R.menu.menu_priority, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.cnss.audiotest.a2dp.R.id.action_resetPriorityList:
                BTDataPersistence.resetPriorityList();
                listCreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onFAButtonClick2(View v) throws InterruptedException {
        Intent intentBluetooth = new Intent();
        intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intentBluetooth);
    }


    @Override
    protected void onPause(){
        storePriority(listView.mCheeseList,this);
        super.onPause();
    }


}




