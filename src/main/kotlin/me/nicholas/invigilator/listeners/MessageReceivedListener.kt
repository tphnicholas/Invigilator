package me.nicholas.invigilator.listeners

import com.google.common.eventbus.Subscribe
import kotlinx.coroutines.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import me.jakejmattson.kutils.api.Discord

import me.nicholas.invigilator.services.LicenseFormatService

class MessageReceivedListener(private val discord: Discord) {
    @Subscribe
    fun onMessageReceived(event: MessageReceivedEvent) {
        val channel = event.channel
        val message = event.message

        val licenseFormatService = discord.getInjectionObjects(LicenseFormatService::class)

        licenseFormatService.getTemplateFor(channel) ?: return

        GlobalScope.launch {
            if (!licenseFormatService.validateLayout(message, userAction = "Message Create")) {
                try {
                    message.delete().queue()
                } catch (e: InsufficientPermissionException) {
                    println(e.localizedMessage)
                }
            }
        }
    }
}