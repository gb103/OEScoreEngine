package com.oescoreengine

import com.oescoreengine.core.OESFeedItem

data class ModelClass(var uniqueueId: String,
                      var name: String,
                      override var score: Float = 0.0f,
                      var map: HashMap<String, String>): OESFeedItem {
    override fun getUniqueId(): String {
        return uniqueueId
    }

    override fun getValue(key: String): String? {
        return map[key]
    }

    override fun setValue(key: String, value: String) {
        map[key] = value
    }
}
