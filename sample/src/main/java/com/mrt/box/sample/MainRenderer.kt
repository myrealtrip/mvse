package com.mrt.box.sample

import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.mrt.box.android.BoxRenderer
import com.mrt.box.core.*
import com.mrt.box.sample.databinding.ActivityMainBinding

/**
 * Created by jaehochoe on 2020-01-03.
 */
class MainRenderer : BoxRenderer {
    override fun <S : BoxState, E : BoxEvent> render(
            view: BoxView<S, E>,
            state: BoxState,
            vm: Vm?
    ) {
        when (state) {
            is MainState.Count -> view.binding<ActivityMainBinding>().count = state.count
            is MainState.Clean -> {
                val a = AnimationUtils.loadAnimation(view.activity(), android.R.anim.fade_out)
                a.duration = 1000
                a.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(p0: Animation?) {
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        vm?.intent(MainEvent.OnFinishedCleaning)
                    }

                    override fun onAnimationStart(p0: Animation?) {
                    }
                })
                view.binding<ActivityMainBinding>().label.startAnimation(a)
            }
        }
    }
}