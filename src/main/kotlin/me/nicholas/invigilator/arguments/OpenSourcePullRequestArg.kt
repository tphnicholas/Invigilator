package me.nicholas.invigilator.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.*

import me.nicholas.invigilator.dataclasses.ProjectRepo
import me.nicholas.invigilator.util.nonEmbedded
import me.nicholas.invigilator.extensions.map

open class OpenSourcePullRequestArg(override val name: String = "Project Pull Request URL") : OpenSourceProjectRepoArg() {
    companion object : OpenSourcePullRequestArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<ProjectRepo> {
        return super.convert(arg, args, event).map {
            if (!it.isPullRequestUrl(it.url)) {
                return Error("${nonEmbedded(it.url)} is either not a valid pull request or not publicly accessible")
            }

            return Success(it, this.consumed)
        }
    }
}