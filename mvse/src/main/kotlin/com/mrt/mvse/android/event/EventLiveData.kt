package com.mrt.v12.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

/**
 * Created by jaehochoe on 2019-09-26.
 */
class EventLiveData(val subject: Int) : LiveData<InAppEvent>() {

    fun update(inAppEvent: InAppEvent) {
        postValue(inAppEvent)
    }

    override fun removeObservers(owner: LifecycleOwner) {
        super.removeObservers(owner)
        if (hasObservers().not())
            EventBus.unsubscribe(subject)
    }
}