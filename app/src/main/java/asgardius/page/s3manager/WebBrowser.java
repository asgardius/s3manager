package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebBrowser extends AppCompatActivity {

    private WebView browser;
    WebSettings webSettings;
    String webURL, pagetitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_browser);
        //This initializes webview object
        try {
            webURL = getIntent().getStringExtra("web_url");
            pagetitle = getIntent().getStringExtra("title");
            getSupportActionBar().setTitle(pagetitle);
            browser =(WebView)findViewById(R.id.webview);
            webSettings = browser.getSettings();
            webSettings.setJavaScriptEnabled(true);
            browser.setWebViewClient(new MyBrowser());
            browser.loadUrl(webURL);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}