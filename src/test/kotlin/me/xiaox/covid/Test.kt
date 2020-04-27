package me.xiaox.covid

import com.google.gson.Gson
import me.xiaox.covid.utils.HttpUtils
import java.text.SimpleDateFormat

fun main() {
    val data = getCovidData("中国") ?: return

}
val json = Gson()
private fun getCovidData(name: String): CovidData? {
    val result = HttpUtils.get(
        "https://lab.isaaclin.cn//nCoV/api/area",
        mapOf("province" to name)
    )
    if (result["code"]?.toInt() != 200) {
        return null
    }
    return json.fromJson(result["result"], CovidData::class.java)
}

data class CovidData(
    val results: List<CovidResults>
)

data class CovidResults(
    val currentConfirmedCount: Int,
    val confirmedCount: Int,
    val curedCount: Int,
    val deadCount: Int,
    val cities: List<CovidCities>,
    val updateTime: Long
) {
    fun getUpdateTime(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return simpleDateFormat.format(updateTime)
    }
}

data class CovidCities(
    val cityName: String,
    val currentConfirmedCount: Int,
    val confirmedCount: Int,
    val curedCount: Int,
    val deadCount: Int
)