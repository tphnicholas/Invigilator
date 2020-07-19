package me.nicholas.invigilator.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.*

import me.nicholas.invigilator.services.LicenseService
import me.nicholas.invigilator.dataclasses.ProjectRepo
import me.nicholas.invigilator.util.nonEmbedded
import me.nicholas.invigilator.extensions.map

open class OpenSourceProjectRepoArg(override val name: String = "Project Repo URL") : VisibleProjectRepoArg() {
    companion object : OpenSourceProjectRepoArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<ProjectRepo> {
        val licenseService = event.discord.getInjectionObjects(LicenseService::class)

        return super.convert(arg, args, event).map {
            if (!licenseService.isOpenSourceLicense(it.license)) {
                return Error("${nonEmbedded(it.url)} does not have an open source license")
            }

            return Success(it, this.consumed)
        }
    }
}