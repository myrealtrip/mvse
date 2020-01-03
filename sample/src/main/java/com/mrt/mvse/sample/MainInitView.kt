package com.mrt.mvse.sample

import com.mrt.mvse.android.MvseViewInitializer
import com.mrt.mvse.core.MvseView
import com.mrt.mvse.sample.databinding.ActivityMainBinding


/**
 * Created by jaehochoe on 2020-01-03.
 */
class MainInitView : MvseViewInitializer<MainState, MainEvent> {
    override fun initializeView(view: MvseView<MainState, MainEvent>) {
        view.binding<ActivityMainBinding>().label.setOnLongClickListener {
            view.intends(MainEvent.OnLongClick)
            true
        }
    }
}