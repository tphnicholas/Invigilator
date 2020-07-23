package me.nicholas.invigilator.extensions

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.json.*
import com.github.kittinunf.result.*

typealias JsonResponse = Result<FuelJson, FuelError>

fun Request.makeRequest(): JsonResponse = responseJson().third

fun JsonResponse.getJsonData() = when(this) {
    is Result.Failure -> null
    is Result.Success -> this.value
}

inline fun JsonResponse.readJson(crossinline block: (FuelJson) -> Unit) = this.map { block(it) }

inline fun JsonResponse.readJsonOrThrow(block: (FuelJson) -> Unit) = when(this) {
    is Result.Failure -> error(this.error.localizedMessage)
    is Result.Success -> block(this.value)
}