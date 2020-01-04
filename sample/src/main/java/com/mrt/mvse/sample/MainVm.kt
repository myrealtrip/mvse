package com.mrt.mvse.sample

import com.mrt.mvse.android.MvseVm
import com.mrt.mvse.core.MvcoBluePrint
import com.mrt.mvse.core.bluePrint
import kotlinx.coroutines.async

/**
 * Created by jaehochoe on 2020-01-02.
 */
class MainVm : MvseVm<MainState, MainEvent, MainSideEffect>() {

    override val bluePrint: MvcoBluePrint<MainState, MainEvent, MainSideEffect>
        get() = bluePrint {
            initialState(MainState.Count(2))

            state<MainState.Count> {
                event<MainEvent.OnUpCount> {
                    toBe(copy(count = count + 1))
                }
                event<MainEvent.OnClick> {
                    toBe(copy(count = count + 1))
                }
                event<MainEvent.OnLongClick> {
                    toBe(MainState.Clean)
                }
                event<MainEvent.OnClickLayout> {
                    toBe(copy(), MainSideEffect.AutoCountUp(3))
                }
            }

            state<MainState.Clean> {
                event<MainEvent.OnFinishedCleaning> {
                    toBe(MainState.Count(0))
                }
            }

            effect<MainSideEffect.AutoCountUp> {
                if (it is MvcoBluePrint.Transition.Valid) {
                    when (it.sideEffect) {
                        is MainSideEffect.AutoCountUp -> {
                            return@effect autoCountUpAsync((it.sideEffect as MainSideEffect.AutoCountUp).count)
                        }
                        else -> Unit
                    }
                } else
                    Unit
            }
        }

    private suspend fun autoCountUpAsync(count: Int) = async {
        for (i in 0..count) {
            mainThread { intent(MainEvent.OnUpCount) }
            kotlinx.coroutines.delay(1000)
        }
        return@async MainState.Clean
    }
}