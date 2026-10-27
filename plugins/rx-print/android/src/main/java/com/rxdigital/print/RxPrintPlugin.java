package com.rxdigital.print;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * window.print() tidak berfungsi di WebView Android.
 * Plugin ini merender HTML struk di WebView terpisah lalu membuka dialog cetak Android
 * (bisa ke printer WiFi/Bluetooth yang punya print service, atau "Simpan sebagai PDF").
 */
@CapacitorPlugin(name = "RxPrint")
public class RxPrintPlugin extends Plugin {

    // Disimpan sebagai field agar WebView tidak dibuang GC sebelum proses cetak selesai.
    private WebView printView;

    @PluginMethod
    public void print(final PluginCall call) {
        final String html = call.getString("html");
        final String name = call.getString("name", "Struk");
        if (html == null || html.isEmpty()) {
            call.reject("Isi struk kosong");
            return;
        }
        final Activity act = getActivity();
        act.runOnUiThread(() -> {
            try {
                final WebView wv = new WebView(act);
                wv.getSettings().setJavaScriptEnabled(false);
                wv.setWebViewClient(new WebViewClient() {
                    private boolean started = false;

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if (started) return;
                        started = true;
                        try {
                            PrintManager pm = (PrintManager) act.getSystemService(Context.PRINT_SERVICE);
                            PrintDocumentAdapter adapter = view.createPrintDocumentAdapter(name);
                            pm.print(name, adapter, new PrintAttributes.Builder().build());
                            call.resolve();
                        } catch (Exception e) {
                            call.reject("Gagal membuka dialog cetak: " + e.getMessage());
                        }
                    }
                });
                printView = wv;
                wv.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
            } catch (Exception e) {
                call.reject("Gagal menyiapkan struk: " + e.getMessage());
            }
        });
    }

    @PluginMethod
    public void share(final PluginCall call) {
        final String text = call.getString("text", "");
        final String title = call.getString("title", "Bagikan struk");
        final Activity act = getActivity();
        act.runOnUiThread(() -> {
            try {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_SUBJECT, title);
                send.putExtra(Intent.EXTRA_TEXT, text);
                Intent chooser = Intent.createChooser(send, title);
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                act.startActivity(chooser);
                call.resolve();
            } catch (Exception e) {
                call.reject("Gagal membagikan: " + e.getMessage());
            }
        });
    }
}
