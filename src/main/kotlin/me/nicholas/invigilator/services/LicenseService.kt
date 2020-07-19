package me.nicholas.invigilator.services

import org.json.JSONObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import me.jakejmattson.kutils.api.annotations.Service

@Service
class LicenseService {
    private val openSourceLicenses: List<String>

    init {
        val (_, response, result) = "https://api.opensource.org/licenses"
                .httpGet()
                .header("Content-Type", "application/json")
                .responseJson()

        openSourceLicenses = result.component1()!!.array().map { (it as JSONObject).getString("id") }
    }

    fun isOpenSourceLicense(arg: String?) = openSourceLicenses.any { it.equals(arg, ignoreCase = true) }
}