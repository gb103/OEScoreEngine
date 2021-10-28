package com.oescoreengine.core

import androidx.annotation.IntDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

data class InputKey(var name: String,
                    @KeyType var type: Int,
                    var weightage: Float = 0.0f) {

    companion object {
        @IntDef(CUSTOM, CURRENT_TIMESTAMP, FREQUENCY)
        @Retention(RetentionPolicy.SOURCE)
        annotation class KeyType
        const val CUSTOM = 0
        const val CURRENT_TIMESTAMP = 1
        const val FREQUENCY = 2
    }


}