package me.xiaox.covid

import com.google.gson.Gson
import me.xiaox.covid.utils.HttpUtils
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.subscribeGroupMessages
import java.lang.StringBuilder
import java.text.SimpleDateFormat

class Covid : PluginBase() {

    private val json = Gson()

    override fun onEnable() {
        subscribeGroupMessages {
            startsWith("#", removePrefix = true) {
                val command = it.split(" ")
                val root = command[0].toLowerCase()
                val args = command.drop(1)
                when (root) {
                    "疫情" -> {
                        if (args.isEmpty()) {
                            reply("[疫情查询] 请输入一个国家、省份或直辖市")
                            return@startsWith
                        }
                        val data = getCovidData(args[0]) ?: kotlin.run {
                            reply("[疫情查询] 获取数据失败")
                            return@startsWith
                        }
                        if (data.results.isEmpty()) {
                            reply("[疫情查询] 未查到目标地区的数据")
                            return@startsWith
                        }
                        val result = data.results[0]
                        val text = StringBuilder(
                            "${result.provinceName}疫情 ->\n" +
                                    "现存确诊>> ${result.currentConfirmedCount}\n" +
                                    "累计确诊>> ${result.confirmedCount}\n" +
                                    "累计治愈>> ${result.curedCount}\n" +
                                    "累计死亡>> ${result.deadCount}"
                        )
                        val cities = result.cities
                        if (!cities.isNullOrEmpty()) {
                            for (city in cities) {
                                text.append("\n${city.cityName}疫情 ->\n" +
                                        "现存确诊>> ${city.currentConfirmedCount}\n" +
                                        "累计确诊>> ${city.confirmedCount}\n" +
                                        "累计治愈>> ${city.curedCount}\n" +
                                        "累计死亡>> ${city.deadCount}")
                            }
                        }
                        text.append("\n更新时间>> ${result.getUpdateTime()}")
                        reply(text.toString())
                    }
                }
            }
        }
    }

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
        val provinceName: String,
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
}