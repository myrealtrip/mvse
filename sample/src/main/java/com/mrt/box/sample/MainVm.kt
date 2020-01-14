package com.mrt.box.sample

import com.mrt.box.android.BoxVm
import com.mrt.box.core.BoxBlueprint
import com.mrt.box.core.bluePrint
import kotlinx.coroutines.async

/**
 * Created by jaehochoe on 2020-01-02.
 */
class MainVm : BoxVm<MainState, MainEvent, MainSideEffect>() {

    override val bluePrint: BoxBlueprint<MainState, MainEvent, MainSideEffect>
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
                event<MainEvent.OnClickFinish> {
                    toBeNothing(MainSideEffect.Finish {
                        it.activity.finish()
                    })
                }
            }

            state<MainState.Clean> {
                event<MainEvent.OnFinishedCleaning> {
                    toBe(MainState.Count(0))
                }
            }

            sideEffectOnBackground<MainSideEffect.AutoCountUp> {
                return@sideEffectOnBackground autoCountUpAsync((it.sideEffect as MainSideEffect.AutoCountUp).count)
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