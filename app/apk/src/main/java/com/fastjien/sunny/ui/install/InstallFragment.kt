package com.fastjien.sunny.ui.install

import com.fastjien.sunny.R
import com.fastjien.sunny.arch.BaseFragment
import com.fastjien.sunny.arch.viewModel
import com.fastjien.sunny.databinding.FragmentInstallMd2Binding
import com.fastjien.sunny.core.R as CoreR

class InstallFragment : BaseFragment<FragmentInstallMd2Binding>() {

    override val layoutRes = R.layout.fragment_install_md2
    override val viewModel by viewModel<InstallViewModel>()

    override fun onStart() {
        super.onStart()
        requireActivity().setTitle(CoreR.string.install)
    }
}
