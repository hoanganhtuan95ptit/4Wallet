package com.tuanha.wallet.ui.fragments.base.screenscope

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.map
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.tuanha.coreapp.ui.viewmodels.BaseViewModel
import com.tuanha.coreapp.utils.extentions.combineSources
import com.tuanha.coreapp.utils.extentions.findParentFirstOrNull
import com.tuanha.coreapp.utils.extentions.get
import com.tuanha.coreapp.utils.extentions.postDifferentValue
import javax.inject.Inject

interface PageChildView {

    val index: Int

    val viewModel: BaseViewModel?

    val pageParentViewModel: PageViewModel?
        get() = null

    fun observePageParentData() {

        val pageParentViewModel = pageParentViewModel ?: ((this as? Fragment)?.findParentFirstOrNull<PageParentView>() as? PageParentView)?.pageViewModel ?: return

        if (viewModel == pageParentViewModel) {

            error("viewModel must be different from pageParentViewModel")
        }

        val viewLifecycleOwner: LifecycleOwner = when (this@PageChildView) {
            is Fragment -> {
                this@PageChildView.viewLifecycleOwner
            }
            is Activity -> {
                this@PageChildView as LifecycleOwner
            }
            else -> {
                throw RuntimeException("")
            }
        }

        pageParentViewModel.currentPageIndex.map { it }.observe(viewLifecycleOwner) {

            viewModel?.updateUiReady(it == this@PageChildView.index)
        }
    }
}

interface PageParentView {


    val viewModel: BaseViewModel?

    val pageViewModel: PageViewModel


    val viewPager1: ViewPager?
        get() = null

    val viewPager2: ViewPager2?
        get() = null


    fun setupPage() {

        if (viewPager2 == null && viewPager1 == null) {

            error("${this.javaClass.simpleName} is PageParentView so need override field viewPager1 or field viewPager2")
        }

        val onPageSelected: (Int) -> Unit = {

            pageViewModel.updateCurrentPageIndex(it)
        }

        viewPager1?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageSelected(position: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                if (positionOffsetPixels == 0) onPageSelected.invoke(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })

        viewPager2?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                if (positionOffsetPixels == 0) onPageSelected.invoke(position)
            }

            override fun onPageSelected(position: Int) {
            }
        })
    }

    fun observeUiData() {

        val viewModel = viewModel ?: return

        if (viewModel == pageViewModel) {

            error("viewModel phải khác pageParentViewModel")
        }

        val viewLifecycleOwner: LifecycleOwner = when (this@PageParentView) {
            is Fragment -> {
                this@PageParentView.viewLifecycleOwner
            }
            is Activity -> {
                this@PageParentView as LifecycleOwner
            }
            else -> {
                throw RuntimeException("")
            }
        }

        viewModel.uiReady.map { it }.observe(viewLifecycleOwner) {

            pageViewModel.updateUiReady(it)
        }
    }

    fun getCurrentItem(): Int {
        return viewPager2?.currentItem ?: viewPager1?.currentItem ?: -1
    }
}


open class PageViewModel @Inject constructor() : BaseViewModel() {

    val currentIndex: LiveData<Int> = MediatorLiveData()

    open val currentPageIndex: LiveData<Int> = combineSources(currentIndex, uiReady) {

        if (!uiReady.get()) {
            postDifferentValue(-1)
        } else {
            postDifferentValue(currentIndex.get())
        }
    }

    open fun updateCurrentPageIndex(currentPageIndex: Int) {
        this.currentIndex.postDifferentValue(currentPageIndex)
    }
}