package com.example.fortydegrees.chatapplication;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.EditText;
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

    TableLayout table;
    int user_id = 1;
    JSONObject selected_room;
    TableRow selected_row;
    EditText room_name;

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

        room_name = (EditText)findViewById(R.id.room_name);
        table = (TableLayout)findViewById(R.id.table_rooms);

        mSocket.connect();
        mSocket.on("returnRooms", returnRooms);
        mSocket.on("responseJoinRoom", responseJoinRoom);
        mSocket.on("responseCreateRoom", responseCreateRoom);
        mSocket.on("AppendRoom", AppendRoomListener);
        GetRooms();
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

    private Emitter.Listener responseJoinRoom = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            try {
                JSONObject result = new JSONObject(args[0].toString());
                if (result.getString("success").toString() == "true") {
                    RedirectToChat(selected_room);
                }else{
                    AlertMessage("Error","Failed to join the selected room");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener responseCreateRoom = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            try {
                JSONObject result = new JSONObject(args[0].toString());
                if (result.getString("success") == "true") {
                    JSONObject room = new JSONObject(result.getJSONObject("result").toString());
                    RedirectToChat(room);
                }else{
                    AlertMessage("Error","Failed to join the selected room");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener AppendRoomListener = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            try {
                final JSONObject result = new JSONObject(args[0].toString());
                JSONObject room = result.getJSONObject("result");
                if (result.getString("success") == "true") {
                    AppendRoom(room);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void displayRooms(JSONArray students) throws JSONException {
        table.removeAllViews();
        for(int x = 0 ; x < students.length(); x++){
            FrameLayout container = new FrameLayout(ChatRoom.this);
            final JSONObject room = students.getJSONObject(x);

            final TableRow row = new TableRow(ChatRoom.this);;
            row.setPadding(20, 20, 20, 20);

            TextView name = new TextView(ChatRoom.this);
            name.setWidth(600);
            name.setPadding(10, 0, 10, 0);
            name.setText(room.getString("room_name"));

            TextView join = new TextView(ChatRoom.this);
            join.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            join.setBackgroundColor(Color.parseColor("#ffffff"));
            join.setPadding(5, 5, 5, 5);
            join.setWidth(200);
            join.setText("Join");

            join.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    selected_row = row;
                    selected_room = room;
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ChatRoom.this);
                    dialog.setTitle("Confirmation");
                    dialog.setMessage("Are you sure you want to join this room");
                    dialog.setPositiveButton("Yes", joinRoomConfirmation);
                    dialog.setNegativeButton("No", joinRoomConfirmation);
                    dialog.show();
                }
            });

            row.addView(name);
            row.addView(join);
            table.addView(row);
        }
    }

    DialogInterface.OnClickListener joinRoomConfirmation = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE){
                try {
                    selected_room.put("user_id", user_id);
                    mSocket.emit("join_room", selected_room);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void CreateRoom(View view){
        AlertDialog.Builder dialog = new AlertDialog.Builder(ChatRoom.this);
        dialog.setTitle("Confirmation");
        dialog.setMessage("Are you sure you want to add this student");
        dialog.setPositiveButton("Yes", CreateRoomConfirmation);
        dialog.setNegativeButton("No", CreateRoomConfirmation);
        dialog.show();
    }

    DialogInterface.OnClickListener CreateRoomConfirmation = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            if(which == DialogInterface.BUTTON_POSITIVE){
                try {
                    JSONObject newRoom = new JSONObject();
                    newRoom.put("room_name",room_name.getText().toString());
                    newRoom.put("user_id",user_id);
                    mSocket.emit("create_room",newRoom);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private  void AppendRoom(final JSONObject room){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    table = (TableLayout)findViewById(R.id.table_rooms);

                    final TableRow row = new TableRow(ChatRoom.this);
                    row.setPadding(20, 20, 20, 20);

                    TextView name = new TextView(ChatRoom.this);
                    name.setWidth(600);
                    name.setPadding(10, 0, 10, 0);
                    name.setText(room.getString("room_name"));
                    TextView join = new TextView(ChatRoom.this);
                    join.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    join.setBackgroundColor(Color.parseColor("#ffffff"));
                    join.setPadding(5, 5, 5, 5);
                    join.setWidth(200);
                    join.setText("Join");

                    join.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selected_row = row;
                            selected_room = room;
                            AlertDialog.Builder dialog = new AlertDialog.Builder(ChatRoom.this);
                            dialog.setTitle("Confirmation");
                            dialog.setMessage("Are you sure you want to join this room");
                            dialog.setPositiveButton("Yes", joinRoomConfirmation);
                            dialog.setNegativeButton("No", joinRoomConfirmation);
                            dialog.show();
                        }
                    });

                    row.addView(name);
                    row.addView(join);

                    table.addView(row);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void RedirectToChat(JSONObject room){
        Intent intent = new Intent(ChatRoom.this, Chat.class);
        intent.putExtra("room", room.toString());
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void AlertMessage(String title,String message){
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
    }
}
