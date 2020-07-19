package me.nicholas.invigilator.extensions

import me.jakejmattson.kutils.api.dsl.arguments.*

inline fun<T, U> ArgumentResult<T>.map(block: Success<T>.(T) -> ArgumentResult<U>) = when(this) {
    is Error -> Error(this.error)
    is Success -> this.block(this.result)
}