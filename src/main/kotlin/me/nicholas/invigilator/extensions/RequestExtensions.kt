package me.nicholas.invigilator.extensions

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.json.*

fun Request.makeRequest(): FuelJson? {
    val (_, response, result) = responseJson()

    if (!response.isSuccessful) { return null }

    return result.component1()
}