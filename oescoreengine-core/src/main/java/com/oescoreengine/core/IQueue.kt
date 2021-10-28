package com.oescoreengine.core

interface IQueue<T> {

    fun getValueBySpecificKeyForItem(uniqueueId: String, key: String): String?

    fun addItem(item: T)

    fun removeLeastScoreItem(): T

    fun prePopulateQueue(itemList: ArrayList<T>)

    fun getSortedList(@Constants.SortOrder sortOrder: Int): ArrayList<T>

    fun isItemExists(uniqueId: String): Boolean

    fun getLeastScoreItem(): T

    fun getHighestScoreItem(): T

    fun clearQueue()

    fun evictItem(uniqueId: String): T?//it's impl should be private

    fun calculateScore(item: T): Float//should be private

}