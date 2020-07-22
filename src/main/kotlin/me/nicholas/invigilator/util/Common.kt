package me.nicholas.invigilator.util

import com.github.kittinunf.fuel.httpPost
import kotlin.math.*

import me.nicholas.invigilator.extensions.makeRequest

fun bold(obj: Any) = "**$obj**"
fun nonEmbedded(obj: Any) = "<$obj>"

fun <T, U> T.zipWithAll(c2: Iterable<U>) = c2.map { this to it }

fun levenshteinPercentage(lhs : CharSequence, rhs : CharSequence): Double {
    val lhsLength = lhs.length
    val rhsLength = rhs.length

    var cost = Array(lhsLength) { it }
    var newCost = Array(lhsLength) { 0 }

    for (i in 1 until rhsLength) {
        newCost[0] = i

        for (j in 1 until lhsLength) {
            val match = if(lhs[j - 1] == rhs[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = min(min(costInsert, costDelete), costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return (1 - cost[lhsLength - 1] / max(lhsLength, rhsLength).toDouble())
}

fun uploadToHastebin(message: String): String {
    val baseUrl = "https://hasteb.in"

    val hastebinJsonResponse = "$baseUrl/documents"
            .httpPost()
            .header("Content-Type", "text/plain")
            .body(message)
            .makeRequest()!!

    val key = hastebinJsonResponse.obj().getString("key")

    return nonEmbedded("$baseUrl/$key")
}