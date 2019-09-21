package com.example.webviewtest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private WebView m_WebView;
    private WebSettings m_WebSettings;
    private Button m_SearchButton;
    private EditText m_UrlText;
    private ProgressBar progressBar;

    private static final String APP_CACHE_DIRNAME = "/webcache";//缓存目录
    private static final int  NORMAL_MODE = 0;
    private static final int   NO_PICTURE_MODE = 1;
    private static final int  REPLACEMENT_MODE = 2;
    private static final int  NO_CACHE_MODE= 3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setConfiguration();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
       getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
        case R.id.id_alert_item:
        m_WebView.loadUrl("file:///android_asset/index.html");
        break;
        case R.id.id_certificate_item:
            m_WebView.loadUrl("https://badssl.com");
        break;
        case R.id.id_intercept_item:
            setWebViewClient(NO_PICTURE_MODE);
            setCache(NO_CACHE_MODE);
            m_WebView.loadUrl("http://www.sina.cn");
        break;
        case R.id.id_replace_item:
            setWebViewClient(REPLACEMENT_MODE);
            setCache(NO_CACHE_MODE);
            m_WebView.loadUrl("http://www.baidu.com");
        break;
        case R.id.id_cache_item:
            setCache(NO_CACHE_MODE);
            break;
        case R.id.id_normal_item:
            setWebViewClient();
            setCache(NORMAL_MODE);
            break;
            case R.id.action_refresh:
                m_WebView.reload();
                break;
        default:
        break;

    }
    return true;

}
    private void init()
    {
        m_UrlText = (EditText) findViewById(R.id.urltext);
        m_WebView = (WebView) findViewById(R.id.webview);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        m_WebSettings = m_WebView.getSettings();
        m_WebView.loadUrl("http://www.sina.cn/");

    }
    private void setConfiguration()
    {
        m_WebSettings.setJavaScriptEnabled(true);
        m_WebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        m_WebSettings.setEnableSmoothTransition(true);
        m_WebSettings.setBuiltInZoomControls(true);//支持缩放
        m_WebSettings.setDisplayZoomControls(false);//隐藏缩放按钮
        m_WebSettings.setDomStorageEnabled(true);
        setWebViewClient();
        setCache(NORMAL_MODE);

//        //监听webview,将当前页面的url显示在Edittext内
//        m_WebView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                m_UrlText.setText(m_WebView.getUrl().toString());
//                return false;
//            }
//        });

        m_WebView.setWebChromeClient(new  MyWebChromeClient()
        {
            //处理webview弹窗问题
            public boolean onJsAlert(WebView view, String url, String message, JsResult result)
            {
                return  super.onJsAlert(view,url,message,result);
            }
            //webview加载进度条
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress==100)
                    progressBar.setVisibility(View.GONE);
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }
        });
        m_SearchButton = (Button) findViewById(R.id.searchbutton);
        //监听Button按钮,进行搜索
        m_SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

        //监听Edittext,回车进行搜索
        m_UrlText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH||actionId ==EditorInfo.IME_ACTION_DONE
                        ||(event != null &&KeyEvent.KEYCODE_ENTER == event.getKeyCode()&& KeyEvent.ACTION_DOWN ==event.getAction()))
                {
                    search();
                }
                return false;
            }
        });
//

    }
    private  void setCache( int type)
    {
        //设置缓存模式
        if (type == NO_CACHE_MODE)
        {
            m_WebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            return;
        }

        if (type == NORMAL_MODE)
        {
            m_WebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        //设置缓冲区
        m_WebSettings.setAppCacheMaxSize(20*1024*1024);

        //开启DOM storage缓存
        m_WebSettings.setDomStorageEnabled(true);
        //开启database storage缓存
        m_WebSettings.setDatabaseEnabled(true);
        String cacheDirPath = getFilesDir().getAbsoluteFile()+APP_CACHE_DIRNAME;
        Log.i("cachePath",cacheDirPath);
        //设置缓存路径
        m_WebSettings.setAppCachePath(cacheDirPath);
        m_WebSettings.setAppCacheEnabled(true);
        Log.i("databasePath",m_WebSettings.getDatabasePath());
    }


    private void  setWebViewClient(){
        //解决不用系统浏览器打开,直接显示在当前WebView
        //除HTTP和HTTPS其他访问类型问题
        m_WebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView wv, String url) {
                if (url == null)
                    return false;
                try {

                    if (url.startsWith("http:") || url.startsWith("https")) {
                        m_WebView.loadUrl(url);
                        return false;
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }

                } catch (Exception e) {
                    return true;
                }


            }
            public void onReceivedSslError(WebView view,final SslErrorHandler handler,
                                           SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setIcon(R.drawable.warning);
                builder.setCancelable(false);
                String message = "";
                switch (error.getPrimaryError()) {
                    case SslError.SSL_UNTRUSTED:
                        message = "当前网站证书不受信任:\r" +"SSL_UNTRUSTED\n" ;
                        break;
                    case SslError.SSL_EXPIRED:
                        message = "证书时间已过期:\n"+"SSL_EXPIRED\n";
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = "证书和当前网站不匹配:\n"+"SSL_IDMISMATCH\n";
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = "证书已失效:\n"+"SSL_NOTYETVALID\n";
                        break;
                    case SslError.SSL_DATE_INVALID:
                        message = "证书日期无效:\n"+"SSL_DATE_INVALID\n";
                        break;
                    case SslError.SSL_INVALID:
                        message = "证书不可用:\n"+"SSL_INVALID\n";
                    default:
                        message = "证书错误";
                        break;
                }
                message += "是否继续访问?";

                builder.setTitle("警告");
                builder.setMessage(message);

                builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                builder.setNegativeButton("继续访问", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }

        });
    }

    private void  setWebViewClient(final int type)
    {


        m_WebView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView wv, String url) {
                if (url == null)
                    return false;
                try {

                    if (url.startsWith("http:") || url.startsWith("https")) {
                        m_WebView.loadUrl(url);
                        return false;
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }

                } catch (Exception e) {
                    return true;
                }


            }


            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                String sourceFile = "";
                String targetFile = "";
                String gifImage = "#############";
                String pngImage = "#############";
                if (type == NO_PICTURE_MODE)
                {
                    sourceFile ="";
                    targetFile = ".jpg";
                    gifImage = ".gif";
                    pngImage = ".png";

                }
                if(type == REPLACEMENT_MODE)
                {
                    sourceFile ="img/vivo.png";
                    targetFile = "plus_logo_web.png";
                }
                if (url.contains(targetFile)||url.contains(gifImage)||url.contains(pngImage)) {

                    InputStream is = null;


                    try {
                        is =getApplicationContext().getAssets().open(sourceFile);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    WebResourceResponse response = new WebResourceResponse("image/png",
                            "utf-8", is);

                    System.out.println("旧API");
                    return response;
                }

                return super.shouldInterceptRequest(view, url);
            }


            // API21以上用shouldInterceptRequest(WebView view, WebResourceRequest request)
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

                String sourceFile = "";
                String targetFile = "";
                String gifImage = "#############";
                String pngImage = "#############";
                if (type == NO_PICTURE_MODE)
                {
                    sourceFile ="";
                    targetFile = ".jpg";
                    gifImage = ".gif";
                    pngImage = ".png";

                }
                if(type == REPLACEMENT_MODE)
                {
                    sourceFile ="img/vivo.png";
                    targetFile = "plus_logo_web.png";
                }
                if (request.getUrl().toString().contains(targetFile)||request.getUrl().toString().contains(gifImage)
                ||request.getUrl().toString().contains(pngImage)) {

                    InputStream is = null;

                    try {
                        is = getApplicationContext().getAssets().open(sourceFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //步骤4:替换资源

                    WebResourceResponse response = new WebResourceResponse("image/png",
                            "utf-8", is);
                    // 参数1：http请求里该图片的Content-Type,此处图片为image/png
                    // 参数2：编码类型
                    // 参数3：存放着替换资源的输入流（上面创建的那个）

                    return response;
                }
                return super.shouldInterceptRequest(view, request);
            }
        });

    }


    //点击系统返回返回上一页
    public  boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK&&m_WebView.canGoBack())
        {
            m_WebView.goBack();
            return true;
        }
        return  super.onKeyDown(keyCode, event);
    }
    public boolean search()
    {
//        if (m_UrlText.getText().toString().length() == 0)
//        {
//            return false;
//        }
        InputMethodManager imm =(InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(m_UrlText.getWindowToken(),0);
        String url = m_UrlText.getText().toString();
        if (Patterns.WEB_URL.matcher(url).matches() || URLUtil.isValidUrl(url))
        {
            if(url.indexOf("file:///")==0 || url.indexOf("http://") == 0 || url.indexOf("https://")==0)
            {

            }
            else
            {
                url = "http://" + url;

            }
       }

//
        else
        {
            url = "http://www.baidu.com.cn/s?wd="  + m_UrlText.getText().toString()+"&cl=3";
        }
        m_WebSettings.setJavaScriptEnabled(true);
        m_WebView.loadUrl(url);
        return  true;

    }
}
