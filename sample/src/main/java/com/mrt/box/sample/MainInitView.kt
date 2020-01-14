package com.mrt.box.sample

import com.mrt.box.android.BoxViewInitializer
import com.mrt.box.core.BoxView
import com.mrt.box.sample.databinding.ActivityMainBinding


/**
 * Created by jaehochoe on 2020-01-03.
 */
class MainInitView : BoxViewInitializer<MainState, MainEvent> {
    override fun initializeView(view: BoxView<MainState, MainEvent>) {
        view.binding<ActivityMainBinding>().label.setOnLongClickListener {
            view.intent(MainEvent.OnLongClick)
            true
        }
    }
}