package me.nicholas.invigilator.services

import net.dv8tion.jda.api.entities.*
import me.jakejmattson.kutils.api.annotations.Service
import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.api.dsl.embed.embed
import me.jakejmattson.kutils.api.extensions.jda.sendPrivateMessage

import me.nicholas.invigilator.data.Configuration
import me.nicholas.invigilator.util.*

data class LogMessage(val message: String = "", val additionalInfo: String = "")

@Service
class LoggingService(
        private val configuration: Configuration,
        private val discord: Discord
) {
    val logChannel = discord.jda.getTextChannelById(configuration.logChannelId)
            ?: error("Could not retrieve logging channel")

    fun logError(message: Message, userAction: String, logMessage: LogMessage, echoToAuthor: Boolean = false) {
        val channel = message.channel
        val author = message.author
        val messageContent = message.contentRaw

        val (messageToLog, additionalInfo) = logMessage

        val errorEmbed = embed {
            simpleTitle = userAction
            description = "${author.asMention} ${author.asTag}"

            field {
                name = "Reason"
                value = messageToLog
            }
            field {
                name = "Message in ${channel.name}"
                value = uploadToHastebin(messageContent)
            }

            color = failureColor
        }

        logChannel.sendMessage(errorEmbed).queue()

        if (echoToAuthor) {
            "$messageToLog\n\n$additionalInfo\n\nMessage Received:\n$messageContent"
                    .chunked(2000)
                    .forEach { author.sendPrivateMessage(it) }
        }
    }
}

fun generateUnintelligibleListingReply(examples: String)
        = LogMessage("Could not understand which template you were trying to fill", "Examples of all templates available in the channel you posted:\n\n$examples")

fun generateHeaderErrorReply(header: Header, listingExample: String)
        = LogMessage("${bold("First mistake:")} Missing or misplaced header: $header", "Make sure headers are line-separated with no empty lines in-between\n\nExample listing:\n$listingExample")

fun generateValueConversionErrorReply(header: Header, conversionError: String)
        = LogMessage("${bold("First mistake:")} On header $header encountered error $conversionError", "If you think this might be a mistake please contact a staff member")

fun generateUnexpectedTextReply(header: Header, textConsumed: String, textToBeRemoved: String)
        = LogMessage("${bold("First mistake:")} On header $header received more text than expected", "Correct text: $textConsumed\nText to be removed: $textToBeRemoved")