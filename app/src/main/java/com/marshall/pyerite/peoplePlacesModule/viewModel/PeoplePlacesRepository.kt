package com.marshall.pyerite.peoplePlacesModule.viewModel

import com.marshall.pyerite.peoplePlacesModule.data.PeoplePlacesLoader
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesSearchOutcome
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesSearchRequest
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesViewer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class PeoplePlacesRepository(
    private val loader: PeoplePlacesLoader,
) {
    suspend fun loadViewer(characterId: Long): PeoplePlacesViewer =
        withContext(Dispatchers.IO) { loader.loadViewer(characterId) }

    suspend fun search(request: PeoplePlacesSearchRequest): PeoplePlacesSearchOutcome =
        withContext(Dispatchers.IO) { loader.search(request) }
}
