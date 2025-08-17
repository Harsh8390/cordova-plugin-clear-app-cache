package com.example.clearcache;  // ✅ match this with plugin.xml

import android.content.Context;
import android.os.Build;
import android.webkit.WebStorage;
import android.webkit.WebView;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class ClearAppCache extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if ("clearCache".equals(action)) {
            this.clearAllCache(callbackContext);
            return true;
        }

        if ("clearCacheSelective".equals(action)) {
            JSONObject options = args.optJSONObject(0);
            this.clearCacheSelective(options, callbackContext);
            return true;
        }

        if ("getCacheInfo".equals(action)) {
            this.getCacheInfo(callbackContext);
            return true;
        }

        return false;
    }

    private void clearAllCache(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                Context context = cordova.getActivity().getApplicationContext();

                clearWebViewCache();
                clearInternalCache(context);
                clearExternalCache(context);

                callbackContext.success("Cache cleared successfully (databases and preferences preserved)");

            } catch (Exception e) {
                callbackContext.error("Error clearing cache: " + e.getMessage());
            }
        });
    }

    private void clearCacheSelective(JSONObject options, CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                Context context = cordova.getActivity().getApplicationContext();

                if (options.optBoolean("webview", false)) {
                    clearWebViewCache();
                }
                if (options.optBoolean("internal", false)) {
                    clearInternalCache(context);
                }
                if (options.optBoolean("external", false)) {
                    clearExterna
