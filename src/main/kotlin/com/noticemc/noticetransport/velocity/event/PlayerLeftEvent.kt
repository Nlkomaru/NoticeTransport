package com.noticemc.noticetransport.velocity.event

import com.noticemc.noticetransport.common.TemplateLocation
import com.noticemc.noticetransport.velocity.commands.TransportCommand.Companion.locationFiles
import com.noticemc.noticetransport.velocity.commands.TransportCommand.Companion.mm
import com.noticemc.noticetransport.velocity.files.Config
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.proxy.Player
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import java.nio.file.Files

class PlayerLeftEvent {

    private val locationFile = Config.config.templateFileName.values.map { locationFiles.resolve("${it}.json") }
    private val serverName = locationFile.map { Json.decodeFromString<TemplateLocation>(Files.readString(it.toPath())).server }

    @Subscribe
    suspend fun playerLeft(event: ServerConnectedEvent) {
        val currentServer = event.previousServer.orElse(null) ?: return
        val currentServerName = currentServer.serverInfo.name
        if (!serverName.contains(currentServerName)) {
            return
        }
        if (nowPlaying[currentServerName]?.isNotEmpty() == true) {
            return
        }

        nextPlayer(currentServerName)
    }

    @Subscribe
    suspend fun disconnect(event: DisconnectEvent) {
        val currentServer = event.player.currentServer.get()
        val currentServerName = currentServer.serverInfo.name
        if (!serverName.contains(currentServerName)) {
            return
        }
        if (nowPlaying[currentServerName]?.isNotEmpty() == true) {
            return
        }

        nextPlayer(currentServerName)
    }

    companion object {
        suspend fun nextPlayer(serverName: String) {
            if (list.isEmpty()) {
                return
            }
            val player = list.first()
            list.remove(player)

            if (!player.isActive) {
                nextPlayer(serverName)
                return
            }


            waiting[serverName]?.add(player)

            player.sendMessage(mm.deserialize("<click:run_command:'/nt tp wait accept $serverName'><color:green><hover:show_text:'クリックで承認'>${
                Config.config.message
            }</hover></click>"))

            player.playSound(Sound.sound(Key.key("entity.firework_rocket.twinkle"), Sound.Source.AMBIENT, 1.0f, 1.0f))

            delay(Config.config.timeOut.toLong() * 1000)

            if (waiting[serverName]?.contains(player) == true) {
                waiting[serverName]?.remove(player)
                player.sendMessage(mm.deserialize("<color:red>一定時間操作がなかったため、キャンセルされました"))
                nextPlayer(serverName)
            }
        }

        val nowPlaying: HashMap<String, ArrayList<Player>> = HashMap()
        val list: LinkedHashSet<Player> = linkedSetOf()
        val waiting: HashMap<String, ArrayList<Player>> = HashMap()
    }

}