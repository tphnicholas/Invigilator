package me.nicholas.invigilator.services

import net.dv8tion.jda.api.entities.*
import me.jakejmattson.kutils.api.annotations.Service
import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.api.arguments.*
import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.*

import me.nicholas.invigilator.arguments.*
import me.nicholas.invigilator.data.Configuration
import me.nicholas.invigilator.util.*

typealias Header = String
typealias TemplateField = Pair<Header, ArgumentType<*>>
typealias ListingTemplate = Array<TemplateField>
typealias ChannelId = String

@Service
class LicenseFormatService(
        private val configuration: Configuration,
        private val loggingService: LoggingService,
        private val discord: Discord
) {
    private val acceptableMatchingScore: Double = 0.5

    val listingTemplates = mapOf<ChannelId, Array<ListingTemplate>>(
            configuration.projectListingsChannelId to arrayOf<ListingTemplate>(
                    arrayOf(
                            bold("Project name:") to EveryArg,
                            bold("Main language(s):") to EveryArg,
                            bold("Any additional libraries or overhead:") to EveryArg,
                            bold("Single line description of project:") to EveryArg,
                            bold("Progress to completion:") to EveryArg,
                            bold("Detailed description:") to EveryArg,
                            bold("Repo link:") to OpenSourceProjectRepoArg
                     )
            ),
            configuration.tutoringChannelId to arrayOf<ListingTemplate>(
                    arrayOf(
                            bold("Name:") to EveryArg,
                            bold("Description of service offered:") to EveryArg,
                            bold("Do you tutor for free for any reason?:") to EveryArg,
                            bold("What programming languages are you comfortable tutoring in?:") to EveryArg,
                            bold("What natural languages (e.g. English, German, French, etc.) are you comfortable teaching in?:") to EveryArg,
                            bold("Do you offer a trial lesson? How long is it?:") to EveryArg,
                            bold("When are you available? (Feel free to include a screenshot of a calendar here):") to EveryArg,
                            bold("Price for charged lessons:") to EveryArg,
                            bold("What is your relevant experience?:") to EveryArg,
                            bold("How long have you been programming?:") to EveryArg,
                            bold("Have you tutored before? How much?:") to EveryArg
                    )
            ),
            configuration.hireMeChannelId to arrayOf<ListingTemplate>(
                    arrayOf(
                            bold("Who are you?:") to EveryArg,
                            bold("What is a brief description of the service you intend to provide?:") to EveryArg,
                            bold("Describe in detail the service that you provide:") to EveryArg,
                            bold("How much relevant experience do you have?:") to EveryArg,
                            bold("What methods of contact do you have?:") to EveryArg,
                            bold("Are you okay with divulging personal information to people who use this service?:") to EveryArg,
                            bold("What is your pricing scheme?:") to EveryArg,
                            bold("Who are you representing?:") to EveryArg,
                            bold("Do you have a portfolio?:") to EveryArg,
                            bold("How free are you to work?:") to EveryArg
                    )
            ),
            configuration.openSourceContributionsChannelId to arrayOf<ListingTemplate>(
                    arrayOf(
                            bold("Issue:") to ErasableOpenSourceIssueArg,
                            bold("Language(s)/Framework(s):") to EveryArg,
                            bold("Description:") to EveryArg,
                            bold("License:") to EveryArg
                    ),
                    arrayOf(
                            bold("Pull Request:") to ErasableOpenSourcePullRequestArg,
                            bold("Language/Framework:") to EveryArg,
                            bold("Description:") to EveryArg,
                            bold("License:") to EveryArg
                    )
            ),
            configuration.codeChallengeChannelId to arrayOf<ListingTemplate>(
                    arrayOf(
                            bold("Challenge:") to EveryArg,
                            bold("Type:") to CodeChallengeTypeArg,
                            bold("Language:") to EveryArg,
                            bold("Length of solution:") to IntegerArg,
                            bold("Link to solution:") to UrlArg
                    )
            )
    )

    fun getTemplateFor(messageChannel: MessageChannel) = listingTemplates[messageChannel.id]

    fun generateListingExample(template: ListingTemplate, dummyEvent: CommandEvent<*>)
            = template
            .joinToString("\n") { (header, valueType) ->
                "$header ${valueType.generateExamples(dummyEvent).random()}"
            }

    fun generateListingExamples(availableTemplates: Array<ListingTemplate>, dummyEvent: CommandEvent<*>)
            = availableTemplates
            .joinToString("\n\n") { generateListingExample(it, dummyEvent) }

    fun chooseApproximateListingTemplate(input: Collection<Pair<List<String>, ListingTemplate>>): ListingTemplate? {
        return input.mapNotNull { (lines, template) ->
            val headers = template.map { (header, _) -> header }.toMutableList()

            lines.mapNotNull { line ->
                headers.mapIndexed { pos, header ->
                    pos to levenshteinPercentage(header, line.take(header.length))
                }.maxBy { (_, levenshteinScore) -> levenshteinScore }?.let { (pos, score) ->
                    if (score > acceptableMatchingScore) {
                        headers.removeAt(pos)
                    }
                    score
                }
            }.let { return@mapNotNull if (it.isEmpty()) null else (template to it.average()) }
        }.let{
            it.maxBy { (_, score) -> score }?.let { (template, highestScore) ->
                return if (
                        (highestScore > acceptableMatchingScore) &&
                        (it.count { (_, score) -> score == highestScore } == 1)
                ) template else null
            }
        }
    }

    fun validateLayout(message: Message, userAction: String): Boolean {
        val dummyEvent = CommandEvent<GenericContainer>(RawInputs("", "", emptyList(), 1), CommandsContainer(), DiscordContext(discord, message))

        val channel = message.channel
        val messageContent = message.contentRaw

        val availableTemplates = listingTemplates[channel.id]!!

        val messageLines = messageContent.lines()

        val chosenTemplate = if (availableTemplates.size == 1) {
            availableTemplates.first()
        } else {
            chooseApproximateListingTemplate(
                    messageLines.zipWithAll(availableTemplates.asIterable())
            ) ?: return false.also {
                loggingService.logError(
                        message,
                        userAction,
                        generateUnintelligibleListingReply(generateListingExamples(availableTemplates, dummyEvent)),
                        echoToAuthor = true
                )
            }
        }

        messageLines.zip(chosenTemplate).forEach { (line, template) ->
            val (header, valueType) = template

            if (!line.startsWith(header)) {
                val listingExample = generateListingExample(chosenTemplate, dummyEvent)
                return false.also {
                    loggingService.logError(
                            message,
                            userAction,
                            generateHeaderErrorReply(header, listingExample),
                            echoToAuthor = true
                    )
                }
            }

            val args = line.removePrefix(header).trim().split(' ')

            when (val conversion = valueType.convert(args.first(), args, dummyEvent)) {
                is Error -> return false.also {
                    loggingService.logError(
                            message,
                            userAction,
                            generateValueConversionErrorReply(header, conversion.error),
                            echoToAuthor = true
                    )
                }
                is Success -> {
                    if (conversion.consumed < args.size) {
                        val textConsumed = args.take(conversion.consumed).joinToString(" ")
                        val textToBeRemoved = args.drop(conversion.consumed).joinToString(" ")
                        return false.also {
                            loggingService.logError(
                                    message,
                                    userAction,
                                    generateUnexpectedTextReply(header, textConsumed, textToBeRemoved),
                                    echoToAuthor = true
                            )
                        }
                    }
                }
            }
        }

        return true
    }
}