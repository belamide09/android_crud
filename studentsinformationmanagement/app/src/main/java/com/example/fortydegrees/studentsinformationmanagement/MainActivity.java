package com.example.fortydegrees.studentsinformationmanagement;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    String showUrl = "http://10.0.2.2/show_students.php";
    String deleteUrl = "http://10.0.2.2/delete_student.php";
    Button add_student;
    RequestQueue requestQueue;
    TableLayout table_students;
    EditText search;

    JSONObject selected_student;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        add_student = (Button)findViewById(R.id.update_student);
        search = (EditText)findViewById(R.id.txt_search);
        table_students = (TableLayout)findViewById(R.id.table_students);
        displayRecords();

        add_student.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewStudent.class);
                startActivity(intent);
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                displayRecords();
            }
        });
    }

    private void displayRecords(){
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,
                showUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject result = new JSONObject(response);
                    JSONArray students = result.getJSONArray("Students");
                    appendStudents(students);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                AlertMessage("Error",error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("name", search.getText().toString());
                return parameters;
            }
        };;
        requestQueue.add(jsonObjectRequest);
    }

    private void appendStudents(JSONArray students_arr) throws JSONException {
        JSONArray students = students_arr;

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relativeLayout);
        table_students.removeAllViews();

        TableRow headers = new TableRow(this);

        TextView tv_name_header = new TextView(this);
        tv_name_header.setWidth(300);
        tv_name_header.setGravity(Gravity.CENTER);
        tv_name_header.setTextColor(0XFF00FF00);

        TextView tv_course_header = new TextView(this);
        tv_course_header.setWidth(200);
        tv_course_header.setGravity(Gravity.CENTER);
        tv_course_header.setTextColor(0XFF00FF00);

        TextView tv_yr_level_header = new TextView(this);
        tv_yr_level_header.setWidth(200);
        tv_yr_level_header.setGravity(Gravity.CENTER);
        tv_yr_level_header.setTextColor(0XFF00FF00);

        tv_name_header.setText("Name");
        tv_course_header.setText("Course");
        tv_yr_level_header.setText("Year Level");

        headers.addView(tv_name_header);
        headers.addView(tv_course_header);
        headers.addView(tv_yr_level_header);

        table_students.addView(headers);

        for (int i = 0; i < students.length(); i++) {
            final JSONObject student = students.getJSONObject(i);

            TableRow tr = new TableRow(this);

            TextView tv_name = new TextView(this);
            tv_name.setWidth(300);
            tv_name.setGravity(Gravity.CENTER);

            TextView tv_course = new TextView(this);
            tv_course.setWidth(200);
            tv_course.setGravity(Gravity.CENTER);

            TextView tv_yr_level = new TextView(this);
            tv_yr_level.setWidth(200);
            tv_yr_level.setGravity(Gravity.CENTER);

            Button btn_view = new Button(this);
            Button btn_delete = new Button(this);
            btn_view.setWidth(100);
            btn_delete.setWidth(100);

            tv_name.setText(student.getString("name"));
            tv_course.setText(student.getString("course"));
            tv_yr_level.setText(student.getString("yr_level"));
            btn_view.setText("View");
            btn_delete.setText("Delete");

            tr.addView(tv_name);
            tr.addView(tv_course);
            tr.addView(tv_yr_level);
            tr.addView(btn_view);
            tr.addView(btn_delete);

            btn_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, StudentInfo.class);
                    intent.putExtra("student", student.toString());
                    startActivity(intent);
                }
            });
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selected_student = student;
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("Confirmation");
                    dialog.setMessage("Are you sure you want to delete this student");
                    dialog.setPositiveButton("Yes", deleteStudentConfirmation);
                    dialog.setNegativeButton("No Way!!!", deleteStudentConfirmation);
                    dialog.show();
                }
            });

            table_students.addView(tr);
        }
    }

    DialogInterface.OnClickListener deleteStudentConfirmation = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE)
                DeleteStudent();
        }
    };

    private void DeleteStudent(){
        StringRequest request = new StringRequest(Request.Method.POST, deleteUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    Boolean result = Boolean.valueOf(json.getString("result").toString());
                    if (result) {
                        AlertMessage("Sucess", "Successfully deleted selected student");
                        displayRecords();
                    } else {
                        AlertMessage("Failed", "Failed deleting selected student");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AlertMessage("Error", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                try {
                    parameters.put("id", selected_student.getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return parameters;
            }
        };
        requestQueue.add(request);
    }

    private void AlertMessage(String title,String message){
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
