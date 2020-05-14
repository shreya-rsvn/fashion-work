package com.example.fashionwork;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

public class WebActivity extends AppCompatActivity {

    String queryUrl;
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Intent intent = getIntent();
        queryUrl = intent.getStringExtra("query");
        Toast.makeText(this, queryUrl, Toast.LENGTH_SHORT).show();

        webView = findViewById(R.id.webview);
        webView.loadUrl(queryUrl);
    }
}
