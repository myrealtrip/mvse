package com.mrt.box.android.event.event

import com.mrt.box.android.event.InAppEvent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

/**
 * Created by jaehochoe on 2019-09-26.
 */

/**
 * You can use like this.
 * val channel = EventBus().asChannel<ItemChangeAction>()
 * launch (UI){
 *   for(action in channel){
 *     // You can use item
 *     action.item
 *   }
 * }
 */
object BoxInAppEvent {
    val bus: BroadcastChannel<InAppEvent> = ConflatedBroadcastChannel()

    fun send(o: InAppEvent) {
        MainScope().launch {
            bus.send(o)
        }
    }

    inline fun <reified T : InAppEvent> asChannel(): ReceiveChannel<T> {
        return bus.openSubscription() as ReceiveChannel<T>
    }
}