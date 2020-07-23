package me.nicholas.invigilator.listeners

import com.google.common.eventbus.Subscribe
import kotlinx.coroutines.*
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import me.jakejmattson.kutils.api.Discord

import me.nicholas.invigilator.services.LicenseFormatService

class MessageUpdateListener(private val discord: Discord) {
    @Subscribe
    fun onMessageUpdate(event: MessageUpdateEvent) {
        val channel = event.channel
        val message = event.message

        val licenseFormatService = discord.getInjectionObjects(LicenseFormatService::class)

        licenseFormatService.getTemplateFor(channel) ?: return

        GlobalScope.launch {
            licenseFormatService.validateLayout(message, userAction = "Message Edit")
        }
    }
}