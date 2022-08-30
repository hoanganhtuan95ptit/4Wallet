package com.tuanha.wallet.ui.fragments.scan

import android.graphics.PointF
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.tuanha.coreapp.ui.viewmodels.BaseViewModel
import com.tuanha.coreapp.utils.extentions.getOrEmpty
import com.tuanha.coreapp.utils.extentions.postDifferentValue
import javax.inject.Inject

class CameraViewModel @Inject constructor(
    private val scanUseCase: ScanUseCase
) : BaseViewModel() {

    val scanInputTypeList: LiveData<List<ScanInputType>> = MediatorLiveData()

    val scanOutputTypeList: LiveData<List<ScanOutputType>> = MediatorLiveData()


    val scanInputType: LiveData<ScanInputType> = combineSourcesBackground(scanInputTypeList) {

        postDifferentValue(scanInputTypeList.getOrEmpty().firstOrNull() ?: ScanInputType.Qrcode)
    }


    val scanDataList: LiveData<List<ScanData>> = MediatorLiveData()

    val outputData: LiveData<ScanData> = combineSourcesBackground(scanDataList, scanOutputTypeList) {

        scanDataList.getOrEmpty().filter {

            it.outputType in scanOutputTypeList.getOrEmpty()
        }.minByOrNull {

            it.outputType.value
        }?.let {

            postDifferentValue(it)
        }
    }


    fun process(imageProxy: ImageProxy, size: Size, points: List<PointF>) {

        scanUseCase.dispose()

        scanUseCase.execute({

            if (it.isNotEmpty()) scanDataList.postDifferentValue(it)
            imageProxy.close()
        }, {

            imageProxy.close()
        }, ScanUseCase.Param(imageProxy, size, points, scanInputType.value ?: ScanInputType.Qrcode))
    }

    fun updateInputType(inputTypes: ArrayList<Int>) {

        scanInputTypeList.postDifferentValue(inputTypes.toScanInputTypes())
    }

    fun updateOutputType(outputTypes: ArrayList<Int>) {

        scanOutputTypeList.postDifferentValue(outputTypes.toScanOutputTypes())
    }

    fun updateIndexScanInputType(index: Int) {

        scanInputType.postDifferentValue(scanInputTypeList.getOrEmpty().getOrNull(index) ?: ScanInputType.Qrcode)
    }

}