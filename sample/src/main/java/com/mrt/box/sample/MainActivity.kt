package com.mrt.box.sample

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProviders
import com.mrt.box.android.BoxActivity
import com.mrt.box.android.BoxRenderer
import com.mrt.box.core.Vm
import com.mrt.box.be
import com.mrt.box.sample.databinding.ActivityMainBinding

class MainActivity : BoxActivity<MainState, MainEvent, MainSideEffect>() {

    override val renderer: BoxRenderer? = MainRenderer()
    override val viewInitializer = MainInitView()
    override val layout: Int
        get() = R.layout.activity_main


    override val vm: MainVm by lazy {
        ViewModelProviders.of(this).get(MainVm::class.java)
    }

    override fun <B : ViewDataBinding, VM : Vm> bindingVm(b: B?, vm: VM) {
        b?.be<ActivityMainBinding>()?.vm = vm
    }
}
