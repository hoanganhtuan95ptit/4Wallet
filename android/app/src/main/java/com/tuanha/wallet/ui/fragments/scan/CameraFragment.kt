package com.tuanha.wallet.ui.fragments.scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Size
import android.util.TypedValue
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.fasterxml.jackson.databind.AnnotationIntrospector.ReferenceProperty.back
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyrd.krystal.R
import com.kyrd.krystal.databinding.LayoutInfoBinding
import com.kyrd.krystal.domain.ScanData
import com.kyrd.krystal.domain.ScanInputType
import com.kyrd.krystal.domain.ScanInputType.Companion.toInts
import com.kyrd.krystal.domain.ScanOutputType
import com.kyrd.krystal.domain.ScanOutputType.Companion.toInts
import com.kyrd.krystal.presentation.common.*
import com.kyrd.krystal.util.ChainDataUtils.isEVMChainId
import com.kyrd.krystal.util.ChainDataUtils.isSolanaId
import com.kyrd.krystal.util.SCAN_OUTPUT
import com.kyrd.krystal.util.ext.*
import com.tuanha.coreapp.ui.base.fragments.BaseViewBindingFragment
import com.tuanha.coreapp.utils.extentions.TextRes
import com.tuanha.coreapp.utils.extentions.TextStr
import com.tuanha.coreapp.utils.extentions.allPermissionsGranted
import com.tuanha.coreapp.utils.extentions.toPx
import com.tuanha.wallet.R
import com.tuanha.wallet.databinding.FragmentScanBinding
import kotlinx.coroutines.delay
import javax.inject.Inject

class CameraFragment : BaseViewBindingFragment<FragmentScanBinding, CameraViewModel>(R.layout.fragment_scan) {

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val action: String by lazy {
        arguments?.getString(ACTION) ?: ""
    }

    private val permissionResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

        val granted = permissions.entries.all {
            it.value == true
        }

        if (granted) {
            requestPermissionCamera()
        } else {
            showConfirmPermission()
        }
    }

    private val settingResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { permissions ->

        requestPermissionCamera()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDrag()
        setupInfo()
        setupBack()
        setupOverlay()
        setupStatusBar()
        setupTabLayout()
        setupPrivateKey()

        binding?.root?.post {
            requestPermissionCamera()
        }

        observeData()
    }


    private fun setupDrag() {

        val binding = binding ?: return

        binding.frameInfoDrag.setup(imageRes = R.drawable.ic_scan_drag_primary_24dp, textSize = 14f, imageSize = 20.toPx())
    }

    private fun setupInfo() {

        val binding = binding ?: return

        val infoRes = when (action) {

            ACTION_SCAN_IMPORT_PRIVATE_KEY -> {
                R.string.scan_info_import_private_key
            }
            ACTION_SCAN_WALLET_CONNECT -> {
                R.string.scan_info_wallet_connect
            }
            ACTION_SCAN_ADDRESS -> {
                R.string.scan_info_address
            }
            else -> {
                if (arguments?.getLong(CHAIN_ID)?.isSolanaId() != true) R.string.scan_info else R.string.scan_info_solana
            }
        }

        binding.frameInfo.setup(R.drawable.ic_info, R.drawable.rounded_corner_menu_item_text_active, TextRes(infoRes))
    }

    private fun setupBack() {

        val binding = binding ?: return

        binding.ivBack.setDebouncedClickListener {
            activity?.finish()
        }
    }

    private fun setupOverlay() {

        val binding = binding ?: return

        binding.spaceDrag.viewTreeObserver.addOnGlobalLayoutListener {

            binding.overlayView.updatePadding(top = binding.spaceInfo.bottom, bottom = binding.root.height - binding.spaceDrag.top)
        }
    }

    private fun setupStatusBar() = requireActivity().window.decorView.setOnApplyWindowInsetsListener { _, insets ->

        val binding = binding ?: return@setOnApplyWindowInsetsListener insets

        val statusHeight = insets.getStatusBar()
        val navigationHeight = insets.getNavigationBar()

        if (statusHeight > 0) binding.statusBar.resize(height = statusHeight)

        insets
    }

    private fun setupTabLayout() {

        val binding = binding ?: return

        val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.updateIndexScanInputType(binding.tabLayout.selectedTabPosition)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        }

        binding.tabLayout.addOnTabSelectedListener(onTabSelectedListener)
    }

    private fun setupPrivateKey() {

        val binding = binding ?: return

        val infoPrivateKeyText = when (action) {

            ACTION_SCAN_WALLET_CONNECT, ACTION_SCAN_ADDRESS -> {
                TextStr("")
            }
            else -> {
                TextRes(R.string.scan_info_sub_normal)
            }
        }

        binding.frameInfoPrivateKey.setup(imageRes = R.drawable.ic_scan_private_key_primary_24dp, info = infoPrivateKeyText, textSize = 14f, imageSize = 20.toPx())
    }

    private fun observeData() = with(viewModel) {

        outputData.observe(viewLifecycleOwner) {

            if (action == ACTION_SCAN) {

                finish(it)
                return@observe
            }

            if (it.outputType == ScanOutputType.SolAddress && arguments?.getLong(CHAIN_ID)?.isSolanaId() != true) {
                return@observe
            }

            if (it.outputType == ScanOutputType.EVMAddress && arguments?.getLong(CHAIN_ID)?.isEVMChainId() != true) {
                return@observe
            }

            if (it.outputType == ScanOutputType.SolPrivateKey && arguments?.getLong(CHAIN_ID)?.isSolanaId() != true) {
                return@observe
            }

            if (it.outputType == ScanOutputType.EVMPrivateKey && arguments?.getLong(CHAIN_ID)?.isEVMChainId() != true) {
                return@observe
            }

            if (action == ACTION_SCAN_IMPORT_PRIVATE_KEY && it.outputType !in listOf(ScanOutputType.PrivateKey, ScanOutputType.EVMPrivateKey, ScanOutputType.SolPrivateKey)) {
                return@observe
            }

            if (action.isBlank() && it.outputType == ScanOutputType.None) {
                return@observe
            }

            finish(it)
        }

        scanDataList.observe(viewLifecycleOwner) {

        }

        scanInputType.observe(viewLifecycleOwner) {

            val binding = binding ?: return@observe

            val tabLayout = binding.tabLayout

            val overlayView = binding.overlayView

            binding.frameInfoDrag.setInfo(
                if (it == ScanInputType.Qrcode) {
                    TextStr("")
                } else {
                    TextRes(R.string.scan_info_drag)
                }
            )

            binding.tvTitle.setText(
                if (it == ScanInputType.Qrcode) {
                    R.string.scan_title_qr
                } else {
                    R.string.scan_title_text
                }
            )

            if (scanInputTypeList.getOrEmpty().size >= 2) viewLifecycleOwner.lifecycleScope.launchWhenResumed {

                while (tabLayout.tabCount != scanInputTypeList.getOrEmpty().size) delay(50)

                tabLayout.getTabAt(scanInputTypeList.getOrEmpty().indexOf(it))?.select()
            }

            overlayView.setQrcode(it == ScanInputType.Qrcode)
        }

        scanInputTypeList.observe(viewLifecycleOwner) { list ->

            val tabLayout = binding?.tabLayout ?: return@observe

            tabLayout.setVisible(list.size >= 2)

            if (list.size < 2) {

                return@observe
            }

            for (i in (tabLayout.tabCount - 1) downTo 0) {

                tabLayout.removeTabAt(i)
            }

            list.map {

                if (it == ScanInputType.Qrcode) R.string.scan_qr
                else R.string.scan_text
            }.forEach {

                tabLayout.addTab(tabLayout.newTab().setText(it), false)
            }
        }

        updateOutputType(arguments?.getIntegerArrayList(OUTPUT_TYPE) ?: arrayListOf())
    }

    private fun showConfirmPermission() = navigator.showConfirm(this, false,
        title = getString(R.string.scan_title_permission), message = getString(R.string.scan_message_permission),
        negative = getString(R.string.back), positive = getString(R.string.scan_action_go_setting), onNegativeClick = {

            activity?.finish()
        }, onPositiveClick = {

            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", requireActivity().packageName, null)
            settingResult.launch(intent)
        })

    private fun requestPermissionCamera() {

        if (requireContext().allPermissionsGranted(REQUIRED_PERMISSIONS.toList())) {
            startCamera()
        } else {
            permissionResult.launch(REQUIRED_PERMISSIONS)
        }
    }

    @Suppress("SimpleRedundantLet")
    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({

            val binding = binding ?: return@addListener

            val cameraProvider = cameraProviderFuture.get()


            val preview = Preview.Builder()
                .setTargetResolution(Size(binding.overlayView.width, binding.overlayView.height))
                .build()

            preview.setSurfaceProvider(binding.preview.surfaceProvider)


            val analysisUseCase = ImageAnalysis.Builder()
                .setTargetResolution(Size(binding.overlayView.width, binding.overlayView.height))
                .build()

            analysisUseCase.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { image ->

                viewModel.updateInputType(arguments?.getIntegerArrayList(INPUT_TYPE) ?: arrayListOf())

                val size = Size(binding.overlayView.width, binding.overlayView.height)
                val points = binding.overlayView.getPoint()

                viewModel.process(image, size, points)
            }

            cameraProvider.unbindAll()

            kotlin.runCatching {
                cameraProvider.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysisUseCase)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun finish(scanData: ScanData) {

        when (scanData.outputType) {
            in listOf(ScanOutputType.PrivateKey, ScanOutputType.EVMPrivateKey, ScanOutputType.SolPrivateKey) -> {
                "private_key"
            }
            in listOf(ScanOutputType.EVMAddress, ScanOutputType.SolAddress) -> {
                "public_key"
            }
            ScanOutputType.WalletConnect -> {
                "wallet_connect"
            }
            else -> {
                null
            }
        }?.let {

            analytics.logEvent(SCAN_OUTPUT, Bundle().createEvent(it))
        }

        activity?.setResult(Activity.RESULT_OK, Intent().putExtra(DATA, scanData))
        activity?.finish()
    }

    fun LayoutInfoBinding.setup(imageRes: Int, backgroundRes: Int? = null, info: Text<*> = TextStr(""), hideWhenInfoEmpty: Boolean = true, textSize: Float = 0f, imageSize: Int = 0) {

        if (imageRes > 0) ivInfo.setImageResource(imageRes)
        if (imageSize > 0) ivInfo.resize(width = imageSize)

        if (backgroundRes != null) root.setBackgroundResource(backgroundRes)

        if (textSize > 0) tvInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)

        setInfo(info, hideWhenInfoEmpty)
    }

    fun LayoutInfoBinding.setInfo(info: Text<*> = TextStr(""), hideWhenInfoEmpty: Boolean = true) {

        tvInfo.setText(info)

        if (hideWhenInfoEmpty) root.setVisible(tvInfo.text.isNotBlank())
    }

    companion object {


        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        fun newInstance(

            action: String = "",
            currentChainId: Long = 0,

            inputType: List<Int> = ScanInputType.values().toList().toInts(),
            outputType: List<Int> = ScanOutputType.values().toList().toInts()
        ) = CameraFragment().apply {

            arguments = bundleOf(ACTION to action, CHAIN_ID to currentChainId, INPUT_TYPE to inputType, OUTPUT_TYPE to outputType)
        }
    }
}