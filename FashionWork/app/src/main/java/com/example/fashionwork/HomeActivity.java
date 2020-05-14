package com.example.fashionwork;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentResolver.SCHEME_CONTENT;

public class HomeActivity extends AppCompatActivity {

    public String IPV4Addr,Portno;
    Button connectButton,uploadButton;
    public String selectedImagePath;
    Uri currImageURI,uri;
    Bitmap bitmap;
    String searchQuery;
    Boolean hasUploaded;
    String amazonUrl;
    TextView responseText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        connectButton = findViewById(R.id.connectButton);
        uploadButton = findViewById(R.id.getImage);
        searchQuery = "";
        hasUploaded = false;

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hasUploaded){
                    Toast.makeText(HomeActivity.this, "Upload a photo first!", Toast.LENGTH_SHORT).show();
                } else {
                    connectServer();
                }
            }
        });
    }

    public void setAmazonUrl(){
        amazonUrl = "https://www.amazon.in/s?k="+searchQuery+"&ref=nb_sb_noss_2";
    }

    public void connectServer(){
        IPV4Addr = "192.168.0.178";
        Portno = "5000";
        String postURL = "http://"+IPV4Addr+":"+Portno+"/";

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch(Exception e) {

        }

   //    Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath,options);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        byte[] byteArray = stream.toByteArray();

        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .build();

        TextView respText = findViewById(R.id.httpmessage);
        respText.setText("Please wait....");

        postRequest(postURL,postBodyImage);
    }

    public void selectImage(View v) {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 0);
    }


    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {

        super.onActivityResult(reqCode,resCode,data);

        if(resCode == RESULT_OK && data != null) {
            uri = data.getData();
            hasUploaded=true;

            File file = new File(uri.getPath());
            final String[] split = file.getPath().split(":");
            selectedImagePath = split[0];

            for(int i=1;i<split.length;i++){
                selectedImagePath = selectedImagePath+"/"+split[i];
            }

            ContentResolver resolver = getApplicationContext().getContentResolver();
            try {
                selectedImagePath = getRealPathFromURI(uri);
            } catch(Exception e){

            }
            TextView imgPath = findViewById(R.id.imageText);
            imgPath.setText(selectedImagePath);
            imgPath.setTextColor(Color.RED);
            //Toast.makeText(getApplicationContext(), selectedImagePath, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(HomeActivity.this, "Doesnt work bro", Toast.LENGTH_SHORT).show();
        }
    }


    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    public void postRequest(String postURL,RequestBody postBody){

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
            .url(postURL)
            .post(postBody)
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.httpmessage);
                        responseText.setText("Failed to connect to server!");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        responseText = findViewById(R.id.httpmessage);

                        try{
                            responseText.setText(response.body().string());
                            searchQuery = responseText.getText().toString();
                            goToAmazon();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void goToAmazon(){
        setAmazonUrl();
        Intent intent = new Intent(HomeActivity.this,WebActivity.class);
        intent.putExtra("query",amazonUrl);
        startActivity(intent);
    }


}
