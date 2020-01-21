package com.mrt.v12.event

import android.util.SparseArray
import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.mrt.box.android.event.InAppEvent
import com.mrt.box.core.Box

/**
 * Created by jaehochoe on 2019-09-26.
 */
object EventBus {

    private val subjects = SparseArray<EventLiveData>()

    private fun liveData(subjectCode: Int): EventLiveData {
        var liveData = subjects.get(subjectCode)
        if (liveData == null) {
            liveData = EventLiveData(subjectCode)
            subjects.put(subjectCode, liveData)
        }
        return liveData
    }

    fun subscribe(subject: Int, @NonNull lifecycle: LifecycleOwner, @NonNull action: Observer<InAppEvent>) {
        liveData(subject).observe(lifecycle, Observer { inAppEvent ->
            Box.log("lifecycles $lifecycle")
            inAppEvent?.let {
                action.onChanged(it)
                completedSubscription(subject)
            }
        })
    }

    fun unsubscribe(subject: Int) {
        subjects.remove(subject)
    }

    fun publish(subject: Int, message: InAppEvent) {
        liveData(subject).update(message)
    }

    fun completedSubscription(subject: Int) {
        liveData(subject).update(null)
    }
}