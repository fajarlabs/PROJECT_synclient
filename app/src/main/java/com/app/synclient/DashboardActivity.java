package com.app.synclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DashboardActivity extends AppCompatActivity {

    private static Context context;

    public static Context getAppContext() {
        return DashboardActivity.context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // register context
        DashboardActivity.context = getApplicationContext();

        setContentView(R.layout.activity_dashboard);
        final WebView webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setLoadsImagesAutomatically(true);

        // cache
        // webView.getSettings().setAppCacheMaxSize(1024*1024*8);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setAppCachePath( getApplicationContext().getCacheDir().getAbsolutePath() );
        webView.getSettings().setAllowFileAccess( true );
        webView.getSettings().setAppCacheEnabled( true );
        webView.getSettings().setCacheMode( WebSettings.LOAD_DEFAULT ); // load online by default
        webSettings.setDomStorageEnabled(true);

        // Tiga baris di bawah ini agar laman yang dimuat dapat
        // melakukan zoom.
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        webView.requestFocusFromTouch();
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        // don't sleep always weakup
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if ( isNetworkAvailable() ) { // loading offline
            // webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            // webView.saveWebArchive(backupDestination);
        }

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        String webSource = "https://dragino.000webhostapp.com/assets.zip";
        GlobalLibrary.downloadZip(webSource, GlobalEnvironment.path.getAbsolutePath());
        GlobalLibrary.unpackZip(GlobalEnvironment.path.getAbsolutePath()+File.separator,"data.zip");

        // load first time
        File f = new File(GlobalEnvironment.path.getAbsolutePath()+File.separator+"index.html");
        try {
            webView.loadUrl(String.valueOf(f.toURI().toURL()));
            Log.d("DashboardActivity","Load new source data");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // refresh update web source
        final Handler h = new Handler();
        h.postDelayed(new Runnable()
        {
            private long time = 0;

            @Override
            public void run()
            {
                // do stuff then
                // can call h again after work!
                time += 30000;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        File f = new File(GlobalEnvironment.path.getAbsolutePath()+File.separator+"index.html");
                        try {
                            webView.loadUrl(String.valueOf(f.toURI().toURL()));
                            Log.d("DashboardActivity","Load new source data");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                });
                h.postDelayed(this, 30000);
            }
        }, 30000); // 1 second delay (takes millis)

        //webView.loadUrl(backupDestination);
        Log.d("DashboardActivity","F://"+GlobalEnvironment.path.getAbsolutePath()+File.separator+"index.html");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        // remove & auto hide bottom back, home, apps
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        startService(new Intent(this, BackgroundService.class));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService( CONNECTIVITY_SERVICE );
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
