package com.oescoreengine.core

import android.text.TextUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class OESFeedQueue<T: OESFeedItem>(var inputKeyList: MutableList<InputKey> = DEFAULT_INPUTKEY_LIST) : IQueue<T> {

    companion object {
        var DEFAULT_INPUTKEY_LIST = ArrayList<InputKey>()
    }


    /**
     * sortedScoreTreeSet  : At any given point of time sortedScoreTreeSet will provide least score
     * trackScoreShuffler : From the score, we will get the Map of uniqueue Id and their timestamp. We will
     * get the uniqueue Id which is oldest entry
     * trackScoreMap : Then from the uniqueueId, any action update/evict can be taken and complete OESFeedItem can be returned, Also it
     * is used to know if a certain OESFeedItem with uniqueId exists or not
     */
    private var sortedScoreTreeSet //<score> sortedSet (to know at any given point the lowest or highest score)
            : TreeSet<Float>
    private var trackScoreShuffler // <score, LinkedHashMap<uniqueId, timestamp>
            : ConcurrentHashMap<Float, LinkedHashMap<String, Long>>
    private var trackScoreMap //<uniqueId, OESFeedItem>
            : ConcurrentHashMap<String, T>


    private var baseTimeStamp: Long = 0L

    init {
        baseTimeStamp = System.currentTimeMillis()
        sortedScoreTreeSet = TreeSet()
        trackScoreShuffler = ConcurrentHashMap()
        trackScoreMap = ConcurrentHashMap()

        DEFAULT_INPUTKEY_LIST.add(InputKey(Constants.RECENCY_KEY, InputKey.CURRENT_TIMESTAMP, Constants.DEFAULT_RECENCY_WEIGHTAGE))
        DEFAULT_INPUTKEY_LIST.add(InputKey(Constants.FREQUENCY_KEY, InputKey.FREQUENCY, Constants.DEFAULT_FREQUENCY_WEIGHTAGE))
        DEFAULT_INPUTKEY_LIST.add(InputKey(Constants.ACCESS_DURATION, InputKey.CUSTOM, Constants.DEFAULT_ACCESS_WEIGHTAGE))

    }

    override fun getValueBySpecificKeyForItem(uniqueueId: String, key: String): String? {
        if(trackScoreMap[uniqueueId] != null) {
            return trackScoreMap[uniqueueId]!!.getValue(key)
        }
        return null
    }

    override fun addItem(item: T) {
        // to add or update track with latest score external method
        if (TextUtils.isEmpty(item.getUniqueId())) return
        if (trackScoreMap[item.getUniqueId()] != null) {
            evictItem(item.getUniqueId())
        }

        val score = calculateScore(item)
        sortedScoreTreeSet.add(score)
        if (trackScoreShuffler.containsKey(score)) {
            val trackListOnSameScore = trackScoreShuffler[score]
            trackListOnSameScore!![item.getUniqueId()] = System.currentTimeMillis()
            item.score = score
            trackScoreMap!![item.getUniqueId()] = item
            //trackListOnSameScore.removeEldestEntry//addLast(trackCacheTable.trackId);
        } else {
            val trackListOnSameScore = LinkedHashMap<String, Long>()
            trackListOnSameScore[item.getUniqueId()] = System.currentTimeMillis()
            trackScoreShuffler!![score] = trackListOnSameScore
            item.score = score
            trackScoreMap!![item.getUniqueId()] = item
        }
    }

    override fun removeLeastScoreItem(): T {
        var item = getLeastScoreItem()
        evictItem(item.getUniqueId())
        return item
    }

    override fun prePopulateQueue(itemList: ArrayList<T>) {
        for (item in itemList) {
            addItem(item)
        }
    }

    override fun getSortedList(sortOrder: Int): ArrayList<T> {
        var list = ArrayList<T>()
        val iterator = sortedScoreTreeSet.iterator()
        while (iterator.hasNext()) {
            val score = iterator.next()
            val ite = trackScoreShuffler[score]!!.iterator()
            while(ite.hasNext()) {
                var id = ite.next().key
                list.add(trackScoreMap[id]!!)
            }
        }
        if(sortOrder == Constants.Ascending) {
            return list
        } else {
            Collections.reverse(list)
            return list
        }
    }

    override fun isItemExists(uniqueId: String): Boolean {
        return trackScoreMap.containsKey(uniqueId)
    }

    /**
     * get Least Score Item
     */
    override fun getLeastScoreItem(): T {
        //get least scored track to know to remove track
        val lowestScore = sortedScoreTreeSet.first()
        val id = trackScoreShuffler[lowestScore]!!.keys.iterator().next()
        return trackScoreMap[id]!!
    }

    /**
     * get Highest Score Item
     */
    override fun getHighestScoreItem(): T {
        //get least scored track to know to remove track
        val highestScore = sortedScoreTreeSet.last()
        var lastId = ""
        for(id in trackScoreShuffler[highestScore]!!.keys) {
            lastId = id
        }
        //val id = trackScoreShuffler[lowestScore]!!.keys.iterator().next()
        return trackScoreMap[lastId]!!
    }

    /**
     * clear the queue
     */
    override fun clearQueue() {
        trackScoreMap.clear()
        trackScoreShuffler.clear()
        sortedScoreTreeSet.clear()
    }

    /**
     * remove entry of any item based on uniqueId
     */
    override fun evictItem(uniqueId: String): T? {
        trackScoreMap[uniqueId]?.let {
            val scoreInSearch: Float = trackScoreMap[uniqueId]!!.score
            trackScoreShuffler[scoreInSearch]?.remove(uniqueId)
            val oesFeedItem: T = trackScoreMap[uniqueId]!!
            trackScoreMap.remove(uniqueId)
            if (trackScoreShuffler[scoreInSearch] == null || trackScoreShuffler[scoreInSearch]!!.size <= 0) {
                sortedScoreTreeSet.remove(scoreInSearch)
                trackScoreShuffler.remove(scoreInSearch)
            }
            return oesFeedItem
        }
        return null
    }

    override fun calculateScore(item: T): Float {
        var score = 0f
        for(inputKey in inputKeyList!!) {
            var itemValue = item.getValue(inputKey.name)
            itemValue?.let {
                when(inputKey.type) {
                    InputKey.CURRENT_TIMESTAMP -> {
                        var timestamp = itemValue.toLong()
                        var recencyScore = ((timestamp - baseTimeStamp)/1000) * (inputKey.weightage) as Float
                        score += recencyScore
                    }
                    InputKey.FREQUENCY -> {
                        var freqValue = itemValue.toFloat()
                        var freqScore = freqValue * inputKey.weightage
                        score += freqScore
                    }
                    InputKey.CUSTOM -> {
                        var customValue = itemValue.toFloat()
                        var customScore = customValue * inputKey.weightage
                        score += customScore
                    }
                }
            }

        }

        /*val E: Int = isExpired(expiryTime)
        score = if (E == 1) {
            (R + F + M) * E
        } else {
            Int.MIN_VALUE.toFloat()
        }*/
        val bd: BigDecimal = BigDecimal(score.toDouble()).setScale(2, RoundingMode.HALF_UP)
        return bd.toFloat()
    }

    inner class Builder<T: OESFeedItem> {

        var inputKeyList: MutableList<InputKey>? = null

        fun setInputKeyList(inputKeyList: ArrayList<InputKey>) {
            this.inputKeyList = inputKeyList
        }

        fun build(): OESFeedQueue<T> {
            return OESFeedQueue(inputKeyList!!)
        }
    }

}