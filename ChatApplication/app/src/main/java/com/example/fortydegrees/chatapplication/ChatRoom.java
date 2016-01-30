package com.example.fortydegrees.chatapplication;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import  com.github.nkzawa.emitter.Emitter;
import  com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import android.widget.RelativeLayout.LayoutParams;

public class ChatRoom extends AppCompatActivity {


    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://10.0.2.2:3000");
        } catch (URISyntaxException e) {
            AlertMessage("Error",e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTitle("Chat Room List");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSocket.connect();
        GetRooms();
        mSocket.on("returnRooms", returnRooms);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void GetRooms(){
        mSocket.emit("get_rooms");
    }

    // Emit event listeners
    private Emitter.Listener returnRooms = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            try {
                JSONArray results = (JSONArray) args[0];
                displayRooms(results);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void displayRooms(JSONArray students) throws JSONException {
        TableLayout table = (TableLayout)findViewById(R.id.table_rooms);
        table.removeAllViews();
        for(int x = 0 ; x < students.length(); x++){
            FrameLayout container = new FrameLayout(ChatRoom.this);
            JSONObject room = students.getJSONObject(x);

            TableRow row = new TableRow(ChatRoom.this);;
            row.setPadding(20, 20, 20, 20);

            TextView name = new TextView(ChatRoom.this);
            name.setWidth(600);
            name.setPadding(10, 0, 10, 0);
            name.setText(room.getString("room_name"));

            Button join = new Button(ChatRoom.this);
            join.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            join.setWidth(80);
            join.setHeight(50);
            join.setText("Join");

            row.addView(name);
            row.addView(join);
            table.addView(row);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GetRooms();
    }

    private void AlertMessage(String title,String message){
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
    }
}
