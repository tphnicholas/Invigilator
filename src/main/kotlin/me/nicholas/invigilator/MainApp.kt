package me.nicholas.invigilator

import me.jakejmattson.kutils.api.dsl.bot

import me.nicholas.invigilator.data.Configuration

fun main(args: Array<String>) {
    val token = args.firstOrNull()
            ?: error("Expected bot token as a program argument")

    bot(token) {
        configure {
            val configuration = it.getInjectionObjects(Configuration::class)

            prefix { configuration.prefix }
            requiresGuild = true
            commandReaction = null
        }
    }
}