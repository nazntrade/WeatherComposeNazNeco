package com.becker.weathercomposeneconaz

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.becker.weathercomposeneconaz.data.WeatherModel
import com.becker.weathercomposeneconaz.screens.DialogSearch
import com.becker.weathercomposeneconaz.screens.MainCard
import com.becker.weathercomposeneconaz.screens.TabLayout
import com.becker.weathercomposeneconaz.ui.theme.WeatherComposeNecoNazTheme
import org.json.JSONObject

const val WEATHER_KEY = "c3fe5d3d2f614afea0381111241005"  //demo. worked 1 week
// https://www.weatherapi.com/

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherComposeNecoNazTheme {

                val dialogState = remember {
                    mutableStateOf(false)
                }
                val daysList = remember {
                    mutableStateOf(listOf<WeatherModel>())
                }
                val currentDay = remember {
                    mutableStateOf(
                        WeatherModel(
                            "",
                            "",
                            "0.0",
                            "",
                            "",
                            "0.0",
                            "0.0",
                            ""
                        )
                    )
                }
                if (dialogState.value) {
                    DialogSearch(
                        dialogState,
                        onSubmit = {
                            getData(it, this, daysList, currentDay)
                        }
                    )
                }
                getData("Vancouver", this, daysList, currentDay)
                Image(
                    painter = painterResource(id = R.drawable.img),
                    contentDescription = "background image",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.5f),
                    contentScale = ContentScale.FillBounds
                )
                Column {
                    MainCard(
                        currentDay,
                        onClickSync = {
                            getData("Vancouver", this@MainActivity, daysList, currentDay)
                        },
                        onClickSearch = {
                            dialogState.value = true
                        }
                    )
                    TabLayout(daysList, currentDay)
                }
            }
        }
    }
}

// by using volley lib
private fun getData(
    city: String,
    context: Context,
    daysList: MutableState<List<WeatherModel>>,
    currentDay: MutableState<WeatherModel>
) {
    val url = "https://api.weatherapi.com/v1/forecast.json?key=$WEATHER_KEY" +
            "&q=$city" +
            "&days=" +
            "30" +
            "&aqi=no&alerts=no"
    val queue = Volley.newRequestQueue(context)
    val sRequest = StringRequest(
        Request.Method.GET,
        url,
        { response ->
            val list = getWeatherByDays(response)
            currentDay.value = list[0]
            daysList.value = list
        },
        { error ->
            Log.d("MyLog", "VolleyError: $error")
        }
    )
    queue.add(sRequest)
}

private fun getWeatherByDays(response: String): List<WeatherModel> {
    if (response.isEmpty()) return listOf()
    val list = ArrayList<WeatherModel>()
    val mainObject = JSONObject(response)
    val city = mainObject.getJSONObject("location").getString("name")
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

    for (i in 0 until days.length()) {
        val item = days[i] as JSONObject
        list.add(
            WeatherModel(
                city,
                item.getString("date"),
                "",
                item.getJSONObject("day").getJSONObject("condition").getString("text"),
                item.getJSONObject("day").getJSONObject("condition").getString("icon"),
                item.getJSONObject("day").getString("maxtemp_c"),
                item.getJSONObject("day").getString("mintemp_c"),
                item.getJSONArray("hour").toString()
            )
        )
    }
    list[0] = list[0].copy(
        time = mainObject.getJSONObject("current").getString("last_updated"),
        currentTemp = mainObject.getJSONObject("current").getString("temp_c")
    )
    return list
}
