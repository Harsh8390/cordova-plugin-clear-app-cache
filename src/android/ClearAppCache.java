package com.example.clearcache;  // ✅ match this with plugin.xml

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
                try {
                    // Access the WebView through CordovaInterface
                    if (webView != null && webView.getView() instanceof WebView) {
                        WebView webViewInstance = (WebView) webView.getView();
                        webViewInstance.clearCache(true);
                        webViewInstance.clearHistory();
                        webViewInstance.clearFormData();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            WebStorage.getInstance().deleteAllData();
                        }
                    } else {
                        // Fallback: Clear WebStorage without WebView instance
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            WebStorage.getInstance().deleteAllData();
                        }
                    }
                } catch (Exception e) {
                    // If we can't access the WebView, try alternative methods
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            WebStorage.getInstance().deleteAllData();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void clearApplicationCache(Context context) {
        try {
            // Try using reflection for older Android versions
            PackageManager pm = context.getPackageManager();

            // This method is deprecated and may not work on newer Android versions
            // We'll use reflection to avoid compilation issues
            Class<?> pmClass = pm.getClass();
            Class<?> observerClass = null;

            try {
                // Try to find the IPackageDataObserver class
                observerClass = Class.forName("android.content.pm.PackageManager$IPackageDataObserver");
                Method method = pmClass.getMethod("deleteApplicationCacheFiles", String.class, observerClass);
                method.invoke(pm, context.getPackageName(), null);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                // IPackageDataObserver not found or method not available
                // Use alternative approach
                clearInternalCacheAlternative(context);
            }

        } catch (Exception e) {
            // Fallback method for newer Android versions
            clearInternalCacheAlternative(context);
        }
    }

    private void clearInternalCacheAlternative(Context context) {
        try {
            // Clear internal cache directory
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                deleteDir(cacheDir);
                // Recreate the cache directory
                cacheDir.mkdirs();
            }

            // Clear code cache (available from API 21+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                File codeCacheDir = context.getCodeCacheDir();
                if (codeCacheDir != null && codeCacheDir.exists()) {
                    deleteDir(codeCacheDir);
                    codeCacheDir.mkdirs();
                }
            }

            // Clear other cache directories
            clearOtherCacheDirectories(context);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearOtherCacheDirectories(Context context) {
        try {
            // Clear files directory cache subdirectories
            File filesDir = context.getFilesDir();
            if (filesDir != null) {
                File[] files = filesDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().contains("cache") || file.getName().contains("temp")) {
                            deleteDir(file);
                        }
                    }
                }
            }

            // Clear no backup files directory
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                File noBackupDir = context.getNoBackupFilesDir();
                if (noBackupDir != null && noBackupDir.exists()) {
                    File[] noBackupFiles = noBackupDir.listFiles();
                    if (noBackupFiles != null) {
                        for (File file : noBackupFiles) {
                            if (file.getName().contains("cache") || file.getName().contains("temp")) {
                                deleteDir(file);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearInternalCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                deleteDir(cacheDir);
                // Recreate the cache directory to avoid issues
                cacheDir.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearExternalCache(Context context) {
        try {
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir != null && externalCacheDir.exists()) {
                deleteDir(externalCacheDir);
                // Recreate the cache directory
                externalCacheDir.mkdirs();
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