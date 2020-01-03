package com.mrt.mvse.sample

import com.mrt.mvse.core.MvseEvent

/**
 * Created by jaehochoe on 2020-01-02.
 */
sealed class MainEvent : MvseEvent {
    object OnUpCount : MainEvent()
    object OnClick : MainEvent()
    object OnLongClick : MainEvent()
    object OnFinishedCleaning : MainEvent()
    object OnClickLayout : MainEvent()
}