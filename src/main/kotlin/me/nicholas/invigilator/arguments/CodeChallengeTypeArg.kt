package me.nicholas.invigilator.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.*

import me.nicholas.invigilator.util.bold

open class CodeChallengeTypeArg(override val name: String = "Code Challenge Type") : ArgumentType<String>() {
    companion object : CodeChallengeTypeArg()

    private val challengeTypes = arrayOf("code golf", "code bowling")

    override fun generateExamples(event: CommandEvent<*>): List<String> = challengeTypes.toList()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
        val argsToString = args.joinToString(" ")

        val challengeType = challengeTypes.find { argsToString.startsWith(it, ignoreCase = true) }
                ?: return Error("${bold(argsToString)} does not name a challenge type\nAvailable challenge types: ${challengeTypes.joinToString(", ") { bold(it) }}")

        return Success(challengeType, challengeType.split(' ').size)
    }
}