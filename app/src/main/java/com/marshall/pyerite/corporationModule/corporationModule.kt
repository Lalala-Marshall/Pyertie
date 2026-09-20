package com.marshall.pyerite.corporationModule

import com.marshall.pyerite.corporationModule.wallet.data.CorporationWalletLoader
import com.marshall.pyerite.corporationModule.wallet.viewModel.CorporationWalletDivisionViewModel
import com.marshall.pyerite.corporationModule.wallet.viewModel.CorporationWalletJournalDayViewModel
import com.marshall.pyerite.corporationModule.wallet.viewModel.CorporationWalletRepository
import com.marshall.pyerite.corporationModule.wallet.viewModel.CorporationWalletTransactionDayViewModel
import com.marshall.pyerite.corporationModule.wallet.viewModel.CorporationWalletsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val corporationModule = module {
    singleOf(::CorporationWalletLoader)
    singleOf(::CorporationWalletRepository)
    viewModelOf(::CorporationWalletsViewModel)
    viewModelOf(::CorporationWalletDivisionViewModel)
    viewModelOf(::CorporationWalletJournalDayViewModel)
    viewModelOf(::CorporationWalletTransactionDayViewModel)
}
