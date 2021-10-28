package com.oescoreengine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import com.oescoreengine.core.Constants
import com.oescoreengine.core.InputKey
import com.oescoreengine.core.OESFeedQueue
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    lateinit var oesFeedQueue: OESFeedQueue<ModelClass>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //1. setup OESFeedQueue with Keys and load it with the DB data
        setupOesFeedQueue()
        //2. To test this let's make some operation, here we open facebook (increased its freq)
        performFewOps()
        //3. Show the results and verify if it working right :)
        showAndCheckTheData()
    }

    fun setupOesFeedQueue() {
        //1. init the Queue Properties, such as which keys need to consider in score/priority calculation
        var inputKeyList = ArrayList<InputKey>()
        inputKeyList.add(InputKey(Constants.RECENCY_KEY, InputKey.CURRENT_TIMESTAMP, Constants.DEFAULT_RECENCY_WEIGHTAGE))
        inputKeyList.add(InputKey(Constants.FREQUENCY_KEY, InputKey.FREQUENCY, Constants.DEFAULT_FREQUENCY_WEIGHTAGE))
        inputKeyList.add(InputKey(Constants.ACCESS_DURATION, InputKey.CUSTOM, Constants.DEFAULT_ACCESS_WEIGHTAGE))
        inputKeyList.add(InputKey("custom_key", InputKey.CUSTOM, 0.1f))
        oesFeedQueue = OESFeedQueue(inputKeyList)

        //2. initiate it when app starts or relatable functionality starts, should be done into thread
        oesFeedQueue.prePopulateQueue(getStoredItemList())
    }

    /**
     * This should ideally be populated from persistant storage such as database
     */
    fun getStoredItemList(): ArrayList<ModelClass> {
        var list = ArrayList<ModelClass>()
        var baseTime = System.currentTimeMillis()
        Log.v("order", baseTime.toString())

        //Consider the case when user accessed Twitter, then Whatsapp,then Linkedin and then facebook
        //and all are open 5 times and 5 sec is the duration for which they accessed it

        //1
        var valueMap1 = HashMap<String, String>()
        valueMap1.put(Constants.RECENCY_KEY, baseTime.toString())//time should come from stored DB
        valueMap1.put(Constants.FREQUENCY_KEY, "5")
        valueMap1.put(Constants.ACCESS_DURATION, "5")
        list.add(ModelClass("1", "Linkedin", Constants.SCORE_NOT_SET, map=valueMap1))

        //2
        var valueMap2 = HashMap<String, String>()
        valueMap2.put(Constants.RECENCY_KEY, (baseTime + 500).toString())
        valueMap2.put(Constants.FREQUENCY_KEY, "5")
        valueMap2.put(Constants.ACCESS_DURATION, "5")
        list.add(ModelClass("2", "Facebook", Constants.SCORE_NOT_SET, map=valueMap2))


        //3
        var valueMap3 = HashMap<String, String>()
        valueMap3.put(Constants.RECENCY_KEY, (baseTime - 1000).toString())
        valueMap3.put(Constants.FREQUENCY_KEY, "5")
        valueMap3.put(Constants.ACCESS_DURATION, "5")
        list.add(ModelClass("3", "Whatsapp", Constants.SCORE_NOT_SET, map=valueMap3))


        //4
        var valueMap4 = HashMap<String, String>()
        valueMap4.put(Constants.RECENCY_KEY, (baseTime - 2000).toString())
        valueMap4.put(Constants.FREQUENCY_KEY, "5")
        valueMap4.put(Constants.ACCESS_DURATION, "5")
        list.add(ModelClass("4", "Twitter", Constants.SCORE_NOT_SET, map=valueMap4))

        return list

    }

    fun performFewOps() {
        var valueMap1 = HashMap<String, String>()
        valueMap1.put(Constants.RECENCY_KEY, System.currentTimeMillis().toString())//when adding entry then timestammp would be current time only
        var freq = oesFeedQueue.getValueBySpecificKeyForItem("2", Constants.FREQUENCY_KEY)
        if(TextUtils.isEmpty(freq)) {
            valueMap1.put(Constants.FREQUENCY_KEY, "1")
        } else {
            val f = freq!!.toInt()
            valueMap1.put(Constants.FREQUENCY_KEY, (f+1).toString())
        }
        valueMap1.put(Constants.ACCESS_DURATION, "20")
        var modelClass = ModelClass("2", "Facebook", Constants.SCORE_NOT_SET, map=valueMap1)
        oesFeedQueue.addItem(modelClass)
    }

    fun showAndCheckTheData() {
        var list = oesFeedQueue.getSortedList(Constants.Descending)
        var resultString =  StringBuilder()
        for(item in list) {
            Log.v("order", "name : " + item.name + " | score : " + item.score)
            resultString.append("name : " + item.name + " | score : " + item.score + " \n")
        }
        findViewById<TextView>(R.id.result).setText(resultString.toString())
    }
}