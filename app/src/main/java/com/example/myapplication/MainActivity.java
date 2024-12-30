package com.example.myapplication;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Context;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;

public class MainActivity extends AppCompatActivity {

    private static final int FILECHOOSER_RESULTCODE = 1; // Код для выбора файла
    private ValueCallback<Uri[]> uploadMessageArray; // Для передачи выбранного изображения (новый вариант)

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDatabaseEnabled(true);

        // Устанавливаем WebChromeClient для обработки выбора файлов
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (uploadMessageArray != null) {
                    uploadMessageArray.onReceiveValue(null); // Сбрасываем предыдущее значение
                }
                uploadMessageArray = filePathCallback; // Сохраняем новое значение

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Позволяем выбор нескольких изображений
                startActivityForResult(Intent.createChooser(intent, "Выберите изображения"), FILECHOOSER_RESULTCODE);
                return true;
            }
        });

        webView.loadUrl("http://a95040ph.beget.tech");

        WebViewClient webViewClient = new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        };
        webView.setWebViewClient(webViewClient);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (uploadMessageArray != null) {
                Uri[] results = null;
                if (resultCode == RESULT_OK && data != null) {
                    // Проверяем, выбраны ли несколько изображений
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        results = new Uri[count];
                        for (int i = 0; i < count; i++) {
                            results[i] = data.getClipData().getItemAt(i).getUri(); // Получаем URI каждого изображения
                        }
                    } else if (data.getData() != null) {
                        results = new Uri[]{data.getData()}; // Если выбрано одно изображение
                    }
                }
                uploadMessageArray.onReceiveValue(results); // Передаем результат обратно в WebView для API 21 и выше
                uploadMessageArray = null; // Обнуляем переменную для предотвращения утечек памяти
            }
        }
    }
}