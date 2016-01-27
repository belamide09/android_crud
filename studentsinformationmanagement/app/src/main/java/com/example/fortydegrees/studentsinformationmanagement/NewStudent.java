package com.example.fortydegrees.studentsinformationmanagement;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.SSLCertificateSocketFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class NewStudent extends AppCompatActivity {

    EditText name, course, yr_level;
    private int PICK_IMAGE_REQUEST = 1;
    ImageView student_image;
    ProgressDialog pDialog;

    Bitmap bitmap;

    String img_string = "";

    Button add_student;

    RequestQueue requestQueue;

    String addUrl = "http://10.0.2.2/add_student.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_student);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        student_image = (ImageView) findViewById(R.id.student_image);
        add_student = (Button) findViewById(R.id.update_student);

        new LoadImage().execute("http://10.0.2.2/img/emptyprofile.jpg");
    }

    public void AddStudentOnClick(View view){
        AlertDialog.Builder dialog = new AlertDialog.Builder(NewStudent.this);
        dialog.setTitle("Confirmation");
        dialog.setMessage("Are you sure you want to add this student");
        dialog.setPositiveButton("Yes", addStudentConfirmation);
        dialog.setNegativeButton("No Way!!!", addStudentConfirmation);
        dialog.show();
    };

    DialogInterface.OnClickListener addStudentConfirmation = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE)
            AddStudent();
        }
    };

    public void Browse(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = imageReturnedIntent.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                img_string = getStringImage(selectedImage);
                student_image.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void AddStudent() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        name = (EditText) findViewById(R.id.txt_name);
        course = (EditText) findViewById(R.id.txt_course);
        yr_level = (EditText) findViewById(R.id.txt_yr_level);

        StringRequest request = new StringRequest(Request.Method.POST, addUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    Boolean result = Boolean.valueOf(json.getString("result").toString());
                    if (result) {
                        name.setText("");
                        course.setText("");
                        yr_level.setText("");
                        img_string = "";
                        new LoadImage().execute("http://10.0.2.2/img/emptyprofile.jpg");

                        AlertDialog.Builder dialog = new AlertDialog.Builder(NewStudent.this);
                        dialog.setTitle("Confirmation");
                        dialog.setTitle("Success");
                        dialog.setMessage("Successfully added new student.\n Would you like to go back to student list?");
                        dialog.setPositiveButton("Yes", redirectConfirmation);
                        dialog.setNegativeButton("No Way!!!", redirectConfirmation);
                        dialog.show();

                    } else {
                        AlertMessage("Failed", "Failed adding new student");
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
                parameters.put("name", name.getText().toString());
                parameters.put("course", course.getText().toString());
                parameters.put("yr_level", yr_level.getText().toString());
                parameters.put("image", img_string);
                return parameters;
            }
        };
        requestQueue.add(request);
    }

    DialogInterface.OnClickListener redirectConfirmation = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
        if(which == DialogInterface.BUTTON_POSITIVE){
            Intent intent = new Intent(NewStudent.this,MainActivity.class);
            startActivity(intent);
        }
        }
    };

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(NewStudent.this);
            pDialog.setMessage("Loading Image ....");
            pDialog.show();
        }
        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {

            if(image != null){
                student_image.setImageBitmap(image);
                pDialog.dismiss();
            }else{
                pDialog.dismiss();
                Toast.makeText(NewStudent.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


    private void AlertMessage(String title,String message){
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
    }

    private void addTest() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;
        try {
            //constants
            URL url = new URL("https://english.fdc-inc.com/api/users/update");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("users_api_token", "1234567890");
            jsonObject.put("users_username", "test");
            String message = jsonObject.toString();

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(message.getBytes().length);

            if (conn instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConn = (HttpsURLConnection) conn;
                httpsConn.setSSLSocketFactory(SSLCertificateSocketFactory.getInsecure(0, null));
                httpsConn.setHostnameVerifier(new AllowAllHostnameVerifier());
            }

            //make some HTTP header nicety
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            //open
            conn.connect();

            //setup send
            os = new BufferedOutputStream(conn.getOutputStream());
            os.write(message.getBytes());
            //clean upg
            os.flush();
            //do something with response
            is = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                result.append(line);
            }
            AlertMessage("Result",result.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {                }

    }

}
