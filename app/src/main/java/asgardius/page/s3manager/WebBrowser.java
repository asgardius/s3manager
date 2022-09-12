package asgardius.page.s3manager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebBrowser extends AppCompatActivity {

    private WebView npw;
    String webURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_browser);
        //This initializes webview object
        try {
            webURL = getIntent().getStringExtra("web_url");
            npw =(WebView)findViewById(R.id.webview);
            npw.setWebViewClient(new MyBrowser());
            npw.loadUrl(webURL);
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