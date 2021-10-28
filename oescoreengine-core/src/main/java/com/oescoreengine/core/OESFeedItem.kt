package com.oescoreengine.core

interface OESFeedItem {

    var score: Float

    fun getUniqueId(): String

    fun getValue(key: String): String?

    fun setValue(key: String, value: String)

}