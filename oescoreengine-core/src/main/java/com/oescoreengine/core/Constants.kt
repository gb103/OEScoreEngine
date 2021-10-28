package com.oescoreengine.core

import androidx.annotation.IntDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

object Constants {

    const val DEFAULT_RECENCY_WEIGHTAGE = 0.04f
    const val DEFAULT_FREQUENCY_WEIGHTAGE = 40f
    const val DEFAULT_ACCESS_WEIGHTAGE = 2f

    const val RECENCY_KEY = "RECENCY_KEY"
    const val FREQUENCY_KEY = "FREQUENCY_KEY"
    const val ACCESS_DURATION = "ACCESS_DURATION"

    const val SCORE_NOT_SET = 12.13f

    @IntDef(Ascending, Descending)
    @Retention(RetentionPolicy.SOURCE)
    annotation class SortOrder
    const val Ascending = 0
    const val Descending = 1


}