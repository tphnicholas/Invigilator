package me.nicholas.invigilator.util

import kotlin.math.*

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

fun String.chunkedRetainingFullLines(maxCharactersEach: Int): List<String> {
    val chunks = mutableListOf<String>()
    var stringRemaining = this

    while (stringRemaining.isNotEmpty()) {
        val wholeChunk = stringRemaining.take(maxCharactersEach)
        val firstChunk = if (wholeChunk.length < stringRemaining.length) {
            wholeChunk.substringBeforeLast('\n')
        } else {
            wholeChunk
        }

        chunks.add(firstChunk)
        stringRemaining = stringRemaining.drop(firstChunk.length).trim('\n')
    }

    return chunks
}