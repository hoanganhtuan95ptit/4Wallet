package com.tuanha.wallet.utils.ext

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.tuanha.coreapp.utils.extentions.findParentFirstOrThis
import org.koin.androidx.viewmodel.koin.getViewModel
import org.koin.java.KoinJavaComponent.getKoin

inline fun <reified T : ViewModel, reified E> Fragment.findViewModel(): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {

    getKoin().getViewModel(findParentFirstOrThis<E>(), T::class)
}