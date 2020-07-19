package me.nicholas.invigilator.arguments

import me.jakejmattson.kutils.api.arguments.UrlArg
import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.*

import me.nicholas.invigilator.dataclasses.ProjectRepo
import me.nicholas.invigilator.util.nonEmbedded
import me.nicholas.invigilator.extensions.map

open class VisibleProjectRepoArg(override val name: String = "Project Repo URL") : ArgumentType<ProjectRepo>() {
    companion object : VisibleProjectRepoArg()

    override fun generateExamples(event: CommandEvent<*>) = listOf(nonEmbedded("https://github.com/torvalds/linux"), nonEmbedded("https://gitlab.com/Aberrantfox/hotbot"))

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<ProjectRepo> {
        return UrlArg.convert(arg.trim('<', '>'), args, event).map {
            val repo = ProjectRepo.parseFromUrl(it)
                    ?: return Error("${nonEmbedded(it)} is not a project in a supported platform, currently supported platforms: github, gitlab")

            if (!repo.isValidAndVisible) {
                return Error("${nonEmbedded(it)} is either not a valid project or not publicly accessible")
            }

            return Success(repo, this.consumed)
        }
    }
}