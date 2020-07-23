package me.nicholas.invigilator.services

import org.json.JSONObject
import com.github.kittinunf.fuel.httpGet
import me.jakejmattson.kutils.api.annotations.Service

import me.nicholas.invigilator.extensions.*

@Service
class LicenseService {
    private var openSourceLicenses: List<String> = emptyList()

    init {
        "https://api.opensource.org/licenses"
                .httpGet()
                .header("Content-Type", "application/json")
                .makeRequest()
                .readJsonOrThrow {
                    openSourceLicenses = it.array().map { (it as JSONObject).getString("id") }
                }
    }

    fun isOpenSourceLicense(arg: String?) = openSourceLicenses.any { it.equals(arg, ignoreCase = true) }
}