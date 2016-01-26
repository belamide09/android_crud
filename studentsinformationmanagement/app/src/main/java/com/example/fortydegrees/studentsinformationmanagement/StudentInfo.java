package com.example.fortydegrees.studentsinformationmanagement;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class StudentInfo extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST = 1;
    JSONObject student;
    ProgressDialog pDialog;
    Bitmap bitmap;
    ImageView student_image;
    EditText name,course,yr_level;

    String img_string = "";
    String updateUrl = "http://10.0.2.2/update_student.php";

    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        name = (EditText)findViewById(R.id.txt_name);
        course = (EditText)findViewById(R.id.txt_course);
        yr_level = (EditText)findViewById(R.id.txt_yr_level);
        student_image = (ImageView)findViewById(R.id.student_image);

        Bundle bundle= getIntent().getExtras();
        try {
            student = new JSONObject(bundle.getString("student"));
            name.setText(student.getString("name"));
            course.setText(student.getString("course"));
            yr_level.setText(student.getString("yr_level"));

            String image_location = "http://10.0.2.2/img/emptyprofile.jpg";
            if(student.getString("image") != "null"){
                image_location = "http://10.0.2.2/uploads/"+student.getString("image");
            }
            new LoadImage().execute(image_location);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void UpdateOnclick(View view){
        AlertDialog.Builder dialog = new AlertDialog.Builder(StudentInfo.this);
        dialog.setTitle("Confirmation");
        dialog.setMessage("Are you sure you want to add this student");
        dialog.setPositiveButton("Yes", updateStudentConfirmation);
        dialog.setNegativeButton("No Way!!!", updateStudentConfirmation);
        dialog.show();
    }

    DialogInterface.OnClickListener updateStudentConfirmation = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE)
            UpdateStudent();
        }
    };

    private void UpdateStudent(){
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        StringRequest request = new StringRequest(Request.Method.POST, updateUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    Boolean result = Boolean.valueOf(json.getString("result").toString());
                    if (result){
                        AlertMessage("Success","Successfully updated student");
                    }else{
                        AlertMessage("Failed","Failed updating ");
                    }
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
            protected Map<String,String> getParams() throws AuthFailureError{
                Map<String,String> parameters = new HashMap<String,String>();
                try {
                    parameters.put("id",student.getString("id").toString());
                    parameters.put("name",name.getText().toString());
                    parameters.put("course",course.getText().toString());
                    parameters.put("yr_level",yr_level.getText().toString());
                    parameters.put("image",img_string);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return parameters;
            }
        };
        requestQueue.add(request);
    }

    public void Browse(View view) {
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

    private String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(StudentInfo.this);
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
                Toast.makeText(StudentInfo.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void AlertMessage(String title,String message){
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
    }

}
