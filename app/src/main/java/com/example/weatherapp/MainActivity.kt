package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.media.tv.TvContract.Programs
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.net.URL
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    val CITY:String="dhaka,bd"
    val API:String="302220847445bae73334f6a568daca12"
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
     var Latitude :String=""
    var Longitude:String=""
    lateinit var geocoder: Geocoder

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var lon :String=""
        var lat :String=""
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this,Locale.getDefault())

        val task = fusedLocationProviderClient.lastLocation
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),101)
            return
        }
        task.addOnSuccessListener {
            if (it!=null)
            {

                lon= it.longitude.toString()
                lat = it.latitude.toString()
                weatherTask(it.latitude.toString(),it.longitude.toString()).execute()
                val address =geocoder.getFromLocation(it.latitude,it.longitude,1)
                findViewById<TextView>(R.id.address).text = address[0].locality+","+address[0].countryName
                Toast.makeText(this, address[0].locality+address[0].countryName, Toast.LENGTH_LONG).show()




            }
            else
            {
                Toast.makeText(this, "some error", Toast.LENGTH_SHORT).show()
            }
        }


    }


    /*fun getCurrentLocation()
    {
        if (checkPermissions())
        {
            if (isLocationEnabled())
            {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                )!=PackageManager.PERMISSION_GRANTED)
                {
                    requestPermission()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task->
                    val location:Location? = task.result
                    if (location == null)
                    {
                        Toast.makeText(this,"null Recieved",Toast.LENGTH_SHORT).show()

                    }else{
                        Toast.makeText(this,"get Success ",Toast.LENGTH_SHORT).show()
                        Latitude=location.latitude.toString()
                        Longitude = location.longitude.toString()
                        fetchCurrentLocationWeather(location.latitude.toString(),location.longitude.toString())
                    }

                }

            }

        }
    }

    private fun fetchCurrentLocationWeather(latitude: String, longitude: String) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }*/


    inner class weatherTask(private var lat:String, private var lon:String) : AsyncTask<String,Void,String>()
    {


        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility=View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility=View.GONE
            findViewById<TextView>(R.id.errortext).visibility=View.GONE
        }

        override fun doInBackground(vararg p0: String?): String? {
            var response:String?


            response = try {

                URL("https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=302220847445bae73334f6a568daca12")
                    .readText(Charsets.UTF_8)
            } catch (e:Exception) {
                ""
            }

            return response
        }

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                val updatedAt:Long=jsonObj.getLong("dt")
                val updatedAtText = "Updated at:  "+SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(
                    Date(updatedAt*1000)
                )
                var temp = main.getString("temp").toDouble()
                temp -= 273.15
                var tempMin = main.getString("temp_min").toDouble()
                tempMin-=273.15
                var tempMax = main.getString("temp_max").toDouble()
                tempMax -=273.15
                val pressure =main.getString("pressure")
                val humidity = main.getString("humidity")
                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription= weather.getString("description")


                val address = jsonObj.getString("name")+", "+sys.getString("country")

                findViewById<TextView>(R.id.update_at).text = updatedAtText
                findViewById<TextView>(R.id.status).text = weatherDescription
                findViewById<TextView>(R.id.temp).text = "${temp.toInt()} °C"
                findViewById<TextView>(R.id.temp_min).text ="Min Temp: "+"${tempMin.toInt()}"+"°C"
                findViewById<TextView>(R.id.temp_max).text = "Max Temp: "+"${tempMax.toInt()}"+"°C"
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a",Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a",Locale.ENGLISH).format(Date(sunset*1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.cloudy).text = pressure
                findViewById<TextView>(R.id.rain).text = humidity
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

            }
            catch (e:Exception)
            {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errortext).visibility =  View.VISIBLE

            }
        }
    }
}