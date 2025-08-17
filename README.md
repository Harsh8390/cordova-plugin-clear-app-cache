# Cordova Plugin Clear App Cache

A Cordova plugin for Android that clears the entire application cache, not just the WebView cache.

## Installation

```bash
cordova plugin add https://github.com/Harsh8390/cordova-plugin-clear-app-cache

Usage
Clear All Cache (Preserves Databases and Preferences)

cordova.plugins.ClearAppCache.clearCache(
    function(success) {
        console.log("Cache cleared successfully (databases and preferences preserved)");
    },
    function(error) {
        console.log("Error clearing cache: " + error);
    }
);

Clear Selective Cache

var options = {
    webview: true,      // Clear WebView cache
    internal: true,     // Clear internal cache
    external: true,     // Clear external cache
    databases: false,   // Don't clear databases (default: false)
    preferences: false  // Don't clear shared preferences (default: false)
};

cordova.plugins.ClearAppCache.clearCacheSelective(
    options,
    function(success) {
        console.log("Selected cache cleared successfully");
    },
    function(error) {
        console.log("Error clearing cache: " + error);
    }
);

Get Cache Information
cordova.plugins.ClearAppCache.getCacheInfo(
    function(info) {
        console.log("Internal cache size: " + info.internalCacheSize + " bytes");
        console.log("External cache size: " + info.externalCacheSize + " bytes");
        console.log("Total cache size: " + info.totalCacheSize + " bytes");
    },
    function(error) {
        console.log("Error getting cache info: " + error);
    }
);

Features

Clears WebView cache and history
Clears application internal cache
Clears external cache
Preserves databases and shared preferences by default
Optional database and shared preferences clearing (via selective method)
Provides cache size information
Selective cache clearing options

Supported Platforms

Android

License
Apache 2.0