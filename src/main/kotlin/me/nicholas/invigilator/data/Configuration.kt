package me.nicholas.invigilator.data

import me.jakejmattson.kutils.api.dsl.data.Data

class Configuration : Data("config/config.json", killIfGenerated = true) {
    val prefix: String = ""
    val projectListingsChannelId: String = ""
    val tutoringChannelId: String = ""
    val hireMeChannelId: String = ""
    val openSourceContributionsChannelId: String = ""
    val logChannelId: String = ""
}