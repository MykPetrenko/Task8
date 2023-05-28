package com.example.task7

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Repository(
  private val localDataSource: LocalDataSource,
  private val remoteDataSource: RemoteDataSource
) {

  suspend fun getCatResponse(
    breedList: String?,
    pictNum: Int?,
    onSuccess: (List<CatResponseItem>) -> Unit,
    onError: (String) -> Unit
  ) {
    try {
      remoteDataSource.getCatResponse(
        breedList = breedList,
        pictNum = pictNum,
        onResponse = { catResponse ->
          onSuccess(catResponse)
          CoroutineScope(Dispatchers.IO).launch {
            updateLocalDatabase(catResponse)
          }
        },
        onFailure = { error ->
          onError(error.message ?: "Unknown error")
        }
      )
    } catch (e: Exception) {
      onError(e.message ?: "Unknown error")
    }
  }

  suspend fun updateLocalDatabase(catResponse: List<CatResponseItem>) {
    withContext(Dispatchers.IO) {

        val images = mutableListOf<String>()
        val info = catResponse[0].breeds[0]
        for (item in catResponse!!) {
          images.add(item.url)
        }
        val catDataEntity = CatDataEntity(
          breed_name = info.name,
          origin = info.origin,
          lifespan = info.life_span,
          description = info.description,
          temperament = info.temperament,
          affection_level = info.affection_level.toFloat(),
          adaptability = info.adaptability.toFloat(),
          child_friendly = info.child_friendly.toFloat(),
          intelligence = info.intelligence.toFloat(),
          grooming = info.grooming.toFloat(),
          health_issues = info.health_issues.toFloat(),
          urls = images
        )
        println("BREED NAME:")
        println(info.name)
        if (localDataSource.doesBreedExist(info.name)) {
          val existingCatData = localDataSource.retrieveInfoByBreedName(info.name)
          val updatedUrls = (existingCatData.urls + images).distinct()
          existingCatData.urls = updatedUrls
          localDataSource.clearBreedsRecord(info.name)
          localDataSource.insertCatData(existingCatData)
        } else {
          localDataSource.insertCatData(catDataEntity)
        }

    }
  }

  suspend fun retrieveCatData(): List<CatDataEntity> {
    return localDataSource.retrieveCatData()
  }
}



