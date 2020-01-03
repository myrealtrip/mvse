package com.mrt.mvse.sample

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProviders
import com.mrt.mvse.android.MvseActivity
import com.mrt.mvse.android.MvseRenderer
import com.mrt.mvse.android.MvseViewInitializer
import com.mrt.mvse.core.MvseEventHandler
import com.mrt.mvse.core.be
import com.mrt.mvse.sample.databinding.ActivityMainBinding

class MainActivity : MvseActivity<MainState, MainEvent, MainSideEffect>() {

    override val renderer: MvseRenderer? = MainRenderer()
    override val viewInitializer = MainInitView()
    override val layout: Int
        get() = R.layout.activity_main


    override val vm: MainVm by lazy {
        ViewModelProviders.of(this).get(MainVm::class.java)
    }

    override fun <B : ViewDataBinding, VM : MvseEventHandler> bindingVm(b: B?, vm: VM) {
        b?.be<ActivityMainBinding>()?.vm = vm
    }
}
