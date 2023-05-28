package com.example.task7

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
const val baseUrl = "https://api.thecatapi.com/"
class RemoteDataSource {

  private val catDataService: GetCatData

  init {
    val retrofit = Retrofit.Builder()
      .baseUrl(baseUrl)
      .addConverterFactory(GsonConverterFactory.create())
      .build()

    catDataService = retrofit.create(GetCatData::class.java)
  }

  fun getCatResponse(
    breedList: String?,
    pictNum: Int?,
    onResponse: (List<CatResponseItem>) -> Unit,
    onFailure: (Throwable) -> Unit
  ) {
    val call = catDataService.getData(breedList = breedList, pictNum = pictNum)

    call.enqueue(object : Callback<CatResponse> {
      override fun onResponse(call: Call<CatResponse>, response: Response<CatResponse>) {
        if (response.isSuccessful) {
          val catResponse = response.body()
          catResponse?.let {
            onResponse(it)
          }
        } else {
          onFailure(Throwable("Response code: ${response.code()}"))
        }
      }

      override fun onFailure(call: Call<CatResponse>, t: Throwable) {
        onFailure(t)
      }
    })
  }
}
