package com.mrt.box.sample

import com.mrt.box.android.BoxViewInitializer
import com.mrt.box.android.BoxAndroidView
import com.mrt.box.sample.databinding.ActivityMainBinding


/**
 * Created by jaehochoe on 2020-01-03.
 */
class MainInitView : BoxViewInitializer<MainState, MainEvent> {
    override fun initializeView(view: BoxAndroidView<MainState, MainEvent>) {
        view.binding<ActivityMainBinding>().label.setOnLongClickListener {
            view.intent(MainEvent.OnLongClick)
            true
        }
    }
}