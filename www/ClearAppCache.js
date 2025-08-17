var exec = require('cordova/exec');

var ClearAppCache = {
    /**
     * Clear the entire application cache
     * @param {Function} successCallback - Called when cache is cleared successfully
     * @param {Function} errorCallback - Called when an error occurs
     */
    clearCache: function(successCallback, errorCallback) {
        successCallback = successCallback || function() {};
        errorCallback = errorCallback || function() {};
        
        exec(successCallback, errorCallback, 'ClearAppCache', 'clearCache', []);
    },

    /**
     * Clear specific cache types
     * @param {Object} options - Options object with cache types to clear
     * @param {Function} successCallback - Called when cache is cleared successfully
     * @param {Function} errorCallback - Called when an error occurs
     */
    clearCacheSelective: function(options, successCallback, errorCallback) {
        options = options || {};
        successCallback = successCallback || function() {};
        errorCallback = errorCallback || function() {};
        
        exec(successCallback, errorCallback, 'ClearAppCache', 'clearCacheSelective', [options]);
    },

    /**
     * Get cache size information
     * @param {Function} successCallback - Called with cache size info
     * @param {Function} errorCallback - Called when an error occurs
     */
    getCacheInfo: function(successCallback, errorCallback) {
        successCallback = successCallback || function() {};
        errorCallback = errorCallback || function() {};
        
        exec(successCallback, errorCallback, 'ClearAppCache', 'getCacheInfo', []);
    }
};

module.exports = ClearAppCache;