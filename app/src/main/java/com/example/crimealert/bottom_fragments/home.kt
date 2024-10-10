package com.example.crimealert.bottom_fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crimealert.R
import com.example.crimealert.adapter.NewsAdapter
import com.example.crimealert.model.NewsItem
import com.example.crimealert.activity.NewsDetailActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

class home : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationEditText: EditText
    private lateinit var newsRecyclerView: RecyclerView
    private val apiKey = "1c3b55c591c89c1bce9d2f4acfeb97fc"
    private val sharedPreferences by lazy { requireActivity().getSharedPreferences("app_prefs", 0) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        locationEditText = view.findViewById(R.id.locationEditText)
        newsRecyclerView = view.findViewById(R.id.newsRecyclerView)
        newsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

        locationEditText.setOnEditorActionListener { _, _, _ ->
            val city = locationEditText.text.toString().trim()
            if (city.isNotEmpty()) {
                fetchNewsForCity(city)
                // Save searched city to SharedPreferences
                sharedPreferences.edit().putString("search_city", city).apply()
            }
            true
        }

        // Set the locationEditText to the stored city or current location on startup
        locationEditText.setText(sharedPreferences.getString("search_city", ""))

        // Setup onClickListeners for emergency numbers
        setupEmergencyNumbers(view)

        return view
    }

    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val (city, fullAddress) = getLocationDetails(location.latitude, location.longitude)
                    locationEditText.setText(fullAddress)
                    fetchNewsForCity(city)
                } else {
                    locationEditText.setText("Location not available")
                }
            }
    }

    private fun getLocationDetails(lat: Double, lon: Double): Pair<String, String> {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lon, 1)
        return if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            val city = address.locality ?: "City not found"
            val fullAddress = address.getAddressLine(0) ?: "Address not found"
            Pair(city, fullAddress)
        } else {
            Pair("City not found", "Address not found")
        }
    }

    private fun fetchNewsForCity(city: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://gnews.io/api/v4/search?q=$city&country=in&token=$apiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NewsAPI", "Failed to fetch news", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string()
                if (!jsonString.isNullOrEmpty()) {
                    val newsItems = parseNews(jsonString)
                    requireActivity().runOnUiThread {
                        // Update RecyclerView with click listener
                        newsRecyclerView.adapter = NewsAdapter(newsItems) { newsItem ->
                            val intent = Intent(requireContext(), NewsDetailActivity::class.java).apply {
                                putExtra("title", newsItem.title)
                                putExtra("description", newsItem.description)
                                putExtra("imageUrl", newsItem.imageUrl)
                                putExtra("url", newsItem.url) // Pass the URL to the NewsDetailActivity
                            }
                            startActivity(intent)
                        }
                    }
                }
            }
        })
    }

    private fun parseNews(jsonString: String): List<NewsItem> {
        val newsList = mutableListOf<NewsItem>()
        val jsonObject = JSONObject(jsonString)
        val articles = jsonObject.getJSONArray("articles")

        for (i in 0 until articles.length()) {
            val article = articles.getJSONObject(i)
            val title = article.getString("title")
            val description = article.getString("description")
            val imageUrl = article.optString("image", "")
            val url = article.getString("url") // Get the URL for the news article
            newsList.add(NewsItem(title, description, imageUrl, url))
        }

        return newsList
    }

    private fun setupEmergencyNumbers(view: View) {
        val ambulanceLayout = view.findViewById<LinearLayout>(R.id.ambulanceLayout)
        val fireStationLayout = view.findViewById<LinearLayout>(R.id.fireStationLayout)
        val roadAccidentLayout = view.findViewById<LinearLayout>(R.id.roadAccidentLayout)
        val wildlifeSupportLayout = view.findViewById<LinearLayout>(R.id.wildlifeSupportLayout)
        val womenHelplineLayout = view.findViewById<LinearLayout>(R.id.womenHelplineLayout)
        val childHelplineLayout = view.findViewById<LinearLayout>(R.id.childHelplineLayout)

        ambulanceLayout.setOnClickListener { makeCall("102") }
        fireStationLayout.setOnClickListener { makeCall("101") }
        roadAccidentLayout.setOnClickListener { makeCall("108") }
        wildlifeSupportLayout.setOnClickListener { makeCall("1800-11-4566") }
        womenHelplineLayout.setOnClickListener { makeCall("181") }
        childHelplineLayout.setOnClickListener { makeCall("1098") }
    }

    private fun makeCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), CALL_PHONE_PERMISSION_REQUEST_CODE)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val CALL_PHONE_PERMISSION_REQUEST_CODE = 2
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
        } else if (requestCode == CALL_PHONE_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Handle permission granted for CALL_PHONE
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
