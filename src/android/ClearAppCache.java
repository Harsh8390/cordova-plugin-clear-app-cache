package com.yourcompany.plugins;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.webkit.WebStorage;
import android.webkit.WebView;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;

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
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    Context context = cordova.getActivity().getApplicationContext();
                    
                    // Clear WebView cache
                    clearWebViewCache();
                    
                    // Clear application cache
                    clearApplicationCache(context);
                    
                    // Clear internal cache
                    clearInternalCache(context);
                    
                    // Clear external cache
                    clearExternalCache(context);
                    
                    // Note: Databases and shared preferences are preserved by default
                    // They can be cleared optionally using clearCacheSelective method
                    
                    callbackContext.success("Cache cleared successfully (databases and preferences preserved)");
                    
                } catch (Exception e) {
                    callbackContext.error("Error clearing cache: " + e.getMessage());
                }
            }
        });
    }

    private void clearCacheSelective(JSONObject options, CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    Context context = cordova.getActivity().getApplicationContext();
                    
                    if (options.optBoolean("webview", false)) {
                        clearWebViewCache();
                    }
                    
                    if (options.optBoolean("internal", false)) {
                        clearInternalCache(context);
                    }
                    
                    if (options.optBoolean("external", false)) {
                        clearExternalCache(context);
                    }
                    
                    if (options.optBoolean("databases", false)) {
                        clearDatabases(context);
                    }
                    
                    if (options.optBoolean("preferences", false)) {
                        clearSharedPreferences(context);
                    }
                    
                    callbackContext.success("Selected cache cleared successfully");
                    
                } catch (Exception e) {
                    callbackContext.error("Error clearing selective cache: " + e.getMessage());
                }
            }
        });
    }

    private void getCacheInfo(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    Context context = cordova.getActivity().getApplicationContext();
                    JSONObject info = new JSONObject();
                    
                    // Get internal cache size
                    File internalCacheDir = context.getCacheDir();
                    long internalCacheSize = getDirSize(internalCacheDir);
                    info.put("internalCacheSize", internalCacheSize);
                    
                    // Get external cache size
                    File externalCacheDir = context.getExternalCacheDir();
                    long externalCacheSize = externalCacheDir != null ? getDirSize(externalCacheDir) : 0;
                    info.put("externalCacheSize", externalCacheSize);
                    
                    // Get total cache size
                    info.put("totalCacheSize", internalCacheSize + externalCacheSize);
                    
                    callbackContext.success(info);
                    
                } catch (Exception e) {
                    callbackContext.error("Error getting cache info: " + e.getMessage());
                }
            }
        });
    }

    private void clearWebViewCache() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                WebView webView = (WebView) webView;
                if (webView != null) {
                    webView.clearCache(true);
                    webView.clearHistory();
                    webView.clearFormData();
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        WebStorage.getInstance().deleteAllData();
                    }
                }
            }
        });
    }

    private void clearApplicationCache(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            Method method = pm.getClass().getMethod("deleteApplicationCacheFiles", 
                String.class, PackageManager.IPackageDataObserver.class);
            method.invoke(pm, context.getPackageName(), null);
        } catch (Exception e) {
            // Fallback method for newer Android versions
            clearInternalCache(context);
        }
    }

    private void clearInternalCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            deleteDir(cacheDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearExternalCache(Context context) {
        try {
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir != null) {
                deleteDir(externalCacheDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearDatabases(Context context) {
        try {
            File databaseDir = new File(context.getApplicationInfo().dataDir + "/databases");
            if (databaseDir.exists()) {
                String[] children = databaseDir.list();
                if (children != null) {
                    for (String child : children) {
                        context.deleteDatabase(child);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearSharedPreferences(Context context) {
        try {
            File prefsDir = new File(context.getApplicationInfo().dataDir + "/shared_prefs");
            if (prefsDir.exists()) {
                deleteDir(prefsDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        }
        return false;
    }

    private long getDirSize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    File file = new File(dir, child);
                    if (file.isDirectory()) {
                        size += getDirSize(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        } else if (dir != null && dir.isFile()) {
            size = dir.length();
        }
        return size;
    }
}