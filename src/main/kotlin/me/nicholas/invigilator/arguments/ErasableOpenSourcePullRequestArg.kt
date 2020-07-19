package me.nicholas.invigilator.arguments

import me.jakejmattson.kutils.api.dsl.arguments.ArgumentResult
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.nicholas.invigilator.dataclasses.ProjectRepo

open class ErasableOpenSourcePullRequestArg : OpenSourcePullRequestArg() {
    companion object : ErasableOpenSourcePullRequestArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<ProjectRepo> {
        return super.convert(arg.trim('<', '~', '>'), args, event)
    }
}