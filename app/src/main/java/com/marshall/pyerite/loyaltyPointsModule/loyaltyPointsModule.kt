package com.marshall.pyerite.loyaltyPointsModule

import com.marshall.pyerite.loyaltyPointsModule.data.LoyaltyPointsLoader
import com.marshall.pyerite.loyaltyPointsModule.viewModel.LoyaltyCorpDetailViewModel
import com.marshall.pyerite.loyaltyPointsModule.viewModel.LoyaltyCorpOffersViewModel
import com.marshall.pyerite.loyaltyPointsModule.viewModel.LoyaltyCorpStationsViewModel
import com.marshall.pyerite.loyaltyPointsModule.viewModel.LoyaltyFactionCorpsViewModel
import com.marshall.pyerite.loyaltyPointsModule.viewModel.LoyaltyPointsHubViewModel
import com.marshall.pyerite.loyaltyPointsModule.viewModel.LoyaltyPointsRepository
import com.marshall.pyerite.loyaltyPointsModule.viewModel.LoyaltyStoreViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val loyaltyPointsModule = module {
    singleOf(::LoyaltyPointsLoader)
    singleOf(::LoyaltyPointsRepository)
    viewModelOf(::LoyaltyPointsHubViewModel)
    viewModelOf(::LoyaltyStoreViewModel)
    viewModelOf(::LoyaltyFactionCorpsViewModel)
    viewModelOf(::LoyaltyCorpDetailViewModel)
    viewModelOf(::LoyaltyCorpStationsViewModel)
    viewModelOf(::LoyaltyCorpOffersViewModel)
}
