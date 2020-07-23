package me.nicholas.invigilator.services

import java.awt.Color
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

    fun logError(message: Message, action: String, logMessage: LogMessage, echoToAuthor: Boolean = false) {
        val channel = message.channel
        val author = message.author
        val messageContent = message.contentRaw

        val (messageToLog, additionalInfo) = logMessage

        val errorEmbed = embed {
            simpleTitle = action
            description = "${author.asMention} ${author.asTag}"

            field {
                name = "Reason"
                value = messageToLog
            }

            val chunks = messageContent
                    .chunkedRetainingFullLines(maxCharactersEach = Constants.embedFieldCharacterLimit)

            field {
                name = "Message in ${channel.name}"
                value = chunks.first()
            }

            chunks.drop(1).forEach {
                field {
                    value = it
                }
            }

            color = Color.RED
        }

        logChannel.sendMessage(errorEmbed).queue()

        if (echoToAuthor) {
            "$messageToLog\n\nMessage Received:\n$messageContent\n\nAdditional Info:\n$additionalInfo"
                    .chunkedRetainingFullLines(maxCharactersEach = Constants.guildMessageCharacterLimit)
                    .forEach { author.sendPrivateMessage(it) }
        }
    }
}

fun generateUnintelligibleListingReply(examples: String)
        = LogMessage("Could not understand which template you were trying to fill", "Examples of all templates available in the channel you posted:\n\n$examples")

fun generateHeaderErrorReply(header: Header, listingExample: String)
        = LogMessage("[First mistake] Missing, misspelled or misplaced header: $header", "Make sure headers are line-separated with no empty lines in-between\n\nExample listing:\n$listingExample")

fun generateValueConversionErrorReply(header: Header, conversionError: String)
        = LogMessage("[First mistake] On header $header encountered error $conversionError", "If you think this might be a mistake please contact a staff member")

fun generateUnexpectedTextReply(header: Header, textConsumed: String, textToBeRemoved: String)
        = LogMessage("[First mistake] On header $header received more text than expected", "Correct text: $textConsumed\nText to be removed: $textToBeRemoved")