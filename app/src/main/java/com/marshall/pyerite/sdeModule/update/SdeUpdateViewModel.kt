package com.marshall.pyerite.sdeModule.update

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class SdeUpdateViewModel(
    private val controller: SdeUpdateController,
) : ViewModel() {

    val uiState: StateFlow<SdeUpdateUiState> = controller.uiState

    fun repairStaleCheckingState() = controller.repairStaleCheckingState()
    fun openUpdateSheet() = controller.openUpdateSheet()
    fun dismissSheet() = controller.dismissSheet()
    fun startDownload() = controller.startDownload()
    fun retryDownload() = controller.retryDownload()
    fun retryCheck() = controller.retryCheck()
}
