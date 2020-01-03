package com.mrt.mvse.sample

import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.mrt.mvse.android.MvseRenderer
import com.mrt.mvse.core.*
import com.mrt.mvse.sample.databinding.ActivityMainBinding

/**
 * Created by jaehochoe on 2020-01-03.
 */
class MainRenderer : MvseRenderer {
    override fun <S : MvseState, E : MvseEvent> render(
        view: MvseView<S, E>,
        state: MvseState,
        vm: MvseEventHandler?
    ) {
        when (state) {
            is MainState.Count -> view.binding.get<ActivityMainBinding>().count = state.count
            is MainState.Clean -> {
                val a = AnimationUtils.loadAnimation(view.activity(), android.R.anim.fade_out)
                a.duration = 1000
                a.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(p0: Animation?) {
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        vm?.intends(MainEvent.OnFinishedCleaning)
                    }

                    override fun onAnimationStart(p0: Animation?) {
                    }
                })
                view.binding.get<ActivityMainBinding>().label.startAnimation(a)
            }
        }
    }
}