package com.example.task7

import RatingAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity2 : AppCompatActivity() {
  private lateinit var viewPager: ViewPager2
  private lateinit var carouselAdapter: CarouselAdapter
  private lateinit var recyclerView: RecyclerView
  private lateinit var ratingAdapter: RatingAdapter
  private lateinit var catDataDao: CatDataDao
  private lateinit var catDataDatabase: CatDataDatabase
  private lateinit var repository: Repository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_layout)

    catDataDatabase = Room.databaseBuilder(
      applicationContext,
      CatDataDatabase::class.java,
      "cat_data_database"
    ).build()

    catDataDao = catDataDatabase.catDataDao()

    repository = Repository(
      LocalDataSource(catDataDao),
      RemoteDataSource()
    )

    val breed = intent.getStringExtra("breed_ids")
    val limit = intent.getIntExtra("limit", 1)

    CoroutineScope(Dispatchers.Main).launch {
      try {
        repository.getCatResponse(
          breedList = breed,
          pictNum = limit,
          onSuccess = { catResponse ->
            CoroutineScope(Dispatchers.Main).launch {
              handleCatResponse(catResponse)
            }
          },
          onError = { error ->
            handleError(error)
          }
        )
      } catch (error: Exception) {
        handleError(error.message ?: "Unknown error")
      }
    }


    val goBack = findViewById<Button>(R.id.btnGoBack)

    goBack.setOnClickListener {
      val intent = Intent(this, MainActivity::class.java)
      startActivity(intent)
    }
  }

  private suspend fun handleCatResponse(catResponse: List<CatResponseItem>) {
    val images = mutableListOf<String>()
    for (item in catResponse) {
      images.add(item.url)
    }

    viewPager = findViewById(R.id.viewPager)
    carouselAdapter = CarouselAdapter(images)
    viewPager.adapter = carouselAdapter

    val info = catResponse[0].breeds[0]

    val origin = findViewById<TextView>(R.id.origin)
    val lifespan = findViewById<TextView>(R.id.lifespan)
    val name = findViewById<TextView>(R.id.name)
    val description = findViewById<TextView>(R.id.description)
    val temperament = findViewById<TextView>(R.id.temperament)

    origin.text = info.origin
    lifespan.text = info.life_span
    name.text = info.name
    description.text = info.description
    temperament.text = info.temperament

    val ratingValues = mapOf(
      "Affection level" to info.affection_level.toFloat(),
      "Adaptability" to info.adaptability.toFloat(),
      "Child friendly" to info.child_friendly.toFloat(),
      "Dog friendly" to info.dog_friendly.toFloat(),
      "Intelligence" to info.intelligence.toFloat(),
      "Grooming" to info.grooming.toFloat(),
      "Health issues" to info.health_issues.toFloat()
    )

    recyclerView = findViewById(R.id.recyclerView)
    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity2)

    ratingAdapter = RatingAdapter(ratingValues)
    recyclerView.adapter = ratingAdapter


    withContext(Dispatchers.IO) {
      repository.updateLocalDatabase(catResponse)
    }
  }

  private fun handleError(error: String) {
    Log.e("MainActivity2", "Error: $error")
  }
}
