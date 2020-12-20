package com.cientista1.sigaaunilab;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.time.LocalTime;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity {

    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;


    String baseUrl = "https://sig.unilab.edu.br/sigaa/mobile/touch/login.jsf";
    String ErrorPagePath = "file:///android_asset/error.html";

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    SwipeRefreshLayout swipeRefreshLayout;
    WebView web;
    Button btnprint,btnhome, btnback;
    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        editor = preferences.edit();
        String url = preferences.getString("url", baseUrl);
        swipeRefreshLayout = findViewById(R.id.swipe);
        web = findViewById(R.id.web);
        btnprint = findViewById(R.id.imprimir);
        btnhome = findViewById(R.id.home);
        btnback = findViewById(R.id.back);
        checkDownloadPermission();
        WebSettings  webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSavePassword(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        web.setWebViewClient(new Callback());
        web.setWebChromeClient(new WebChromeCallBack());
        web.setHorizontalScrollBarEnabled(false);
        web.setAlwaysDrawnWithCacheEnabled(true);
        web.loadUrl(url);
        web.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                //------------------------COOKIE!!------------------------
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                //------------------------COOKIE!!------------------------



                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Baixando o arquivo...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
                DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "O arquivo está sendo baixado", Toast.LENGTH_LONG).show();
            }

        });
        btnprint.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                createWebPrintJob(web);
            }
        });

        btnhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                web.loadUrl(getHomeUrl());
            }
        });

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                web.goBack();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(!DetectConnection.checkInternetConnection(getApplicationContext())){
                    web.loadUrl(ErrorPagePath);
                }
                if(web.getUrl().contains("unilab"))
                    web.reload();
                else
                    web.loadUrl(getHomeUrl());
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode == REQUEST_SELECT_FILE)
            {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        }
        else if (requestCode == FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
        else
            Toast.makeText(getApplicationContext().getApplicationContext(), "Falha ao fazer upload", Toast.LENGTH_LONG).show();
        super.onActivityResult(requestCode, resultCode, intent);
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        saveUrl();
        if(web.canGoBack())
            web.goBack();
        Toast.makeText(this, "Clica novamente para sair", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        saveUrl();
        super.onDestroy();

    }

    private void saveUrl(){
        editor.putString("url",getHomeUrl());
        editor.commit();
    }
    private void saveUrl(String url){
        editor.putString("url",url);
        editor.commit();
    }

    private String getHomeUrl(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return "https://sig.unilab.edu.br/sigaa/mobile/touch/menu.jsf?t=" + LocalTime.now();
        return "https://sig.unilab.edu.br/sigaa/mobile/touch/menu.jsf";
    }
    private String getHomeUrl(int i){
        return "https://sig.unilab.edu.br/sigaa/mobile/touch/menu.jsf";
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createWebPrintJob(WebView webView) {

        PrintManager printManager = (PrintManager) this
                .getSystemService(Context.PRINT_SERVICE);

        PrintDocumentAdapter printAdapter =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            printAdapter = webView.createPrintDocumentAdapter("Meu Documento");
        }

        String jobName = getString(R.string.app_name) + " Print Test";

        printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());
    }



    private class Callback extends WebViewClient {


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!DetectConnection.checkInternetConnection(getApplicationContext())) {
                web.loadUrl(ErrorPagePath);
            }

            if(!url.contains("sig.") && !url.contains("error.html") && !url.contains("unilab")){
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
                return true;
            }
            swipeRefreshLayout.setRefreshing(true);
            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            swipeRefreshLayout.setRefreshing(true);
            super.onPageStarted(view, url, favicon);
        }



        @Override
        public void onPageFinished(WebView view, String url) {
            swipeRefreshLayout.setRefreshing(false);
            CookieSyncManager.getInstance().sync();
            saveUrl();
            if(web.getUrl().contains("unilab"))
                swipeRefreshLayout.setEnabled(false);
            else
                swipeRefreshLayout.setEnabled(true);
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            //Clearing the WebView
            try {
                view.stopLoading();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                view.clearView();
            } catch (Exception e) {
                e.printStackTrace();
            }
            swipeRefreshLayout.setRefreshing((false));
            view.loadUrl(ErrorPagePath);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }


    }

    private class WebChromeCallBack extends WebChromeClient{
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }

        protected void openFileChooser(ValueCallback uploadMsg, String acceptType)
        {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
        }


        // For Lollipop 5.0+ Devices
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
        {
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }

            uploadMessage = filePathCallback;

            Intent intent = fileChooserParams.createIntent();
            try
            {
                startActivityForResult(intent, REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e)
            {
                uploadMessage = null;
                Toast.makeText(getApplicationContext().getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }

        //For Android 4.1 only
        protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
        {
            mUploadMessage = uploadMsg;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
        }

        protected void openFileChooser(ValueCallback<Uri> uploadMsg)
        {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }

    }

    private void checkDownloadPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Por favor, deia permissão para guardar ficheiros na memoria do celular.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }
}