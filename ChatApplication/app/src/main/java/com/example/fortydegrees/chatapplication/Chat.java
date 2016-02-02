package com.example.fortydegrees.chatapplication;

import android.app.AlertDialog;
import android.app.VoiceInteractor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Random;

public class Chat extends AppCompatActivity {

    JSONObject current_room;
    EditText txt_message;
    Socket mSocket;
    JSONObject user;
    TableLayout conversations;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            Random random = new Random();
            String [] names = {"Jacob","John","Roy","Mark","Jeff"};
            String name = names[random.nextInt(names.length-1)];
            mSocket = IO.socket("http://10.0.2.2:3000");

            user = new JSONObject();
            user.put("name",name);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Bundle bundle = getIntent().getExtras();
        try {
            current_room = new JSONObject(bundle.getString("room"));
            this.setTitle(current_room.getString("room_name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        conversations = (TableLayout)findViewById(R.id.conversations);
        txt_message = (EditText)findViewById(R.id.txt_message);

        mSocket.on("UpdateRoom",UpdateRoom);
        mSocket.on("ReceiveMessage",ReceiveMessage);
    }

    public void SendMessage(View view) {
        try {
            JSONObject message = new JSONObject();
            message.put("message",txt_message.getText());
            message.put("user",user);
            mSocket.emit("send_message", message);
            txt_message.setText("");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Event listener

    private Emitter.Listener UpdateRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject room = (JSONObject)args[0];
            try {
                if ( room.getString("id").toString() == current_room.getString("id").toString() ){
                    current_room = room;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener ReceiveMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject message = (JSONObject) args[0];
                        JSONObject user = message.getJSONObject("user");

                        TableRow row = new TableRow(Chat.this);

                        RelativeLayout container = new RelativeLayout(Chat.this);
                        TextView tv_message = new TextView(Chat.this);
                        tv_message.setText(user.getString("name") + " - " + message.getString("message"));
                        container.addView(tv_message);

                        row.addView(container);

                        conversations.addView(row);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private void AlertMessage(String title,String message){
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
    }

}
