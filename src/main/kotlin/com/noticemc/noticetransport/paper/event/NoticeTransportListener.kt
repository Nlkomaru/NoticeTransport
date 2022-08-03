package com.noticemc.noticetransport.paper.event

import com.noticemc.noticetransport.common.ChannelKey.key
import com.noticemc.noticetransport.common.PlayerLocation
import com.noticemc.noticetransport.paper.event.PlayerJoinEvent.Companion.list
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener

class NoticeTransportListener : PluginMessageListener {

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel != key) return

        val string = String(message, Charsets.UTF_8)
        val playerLocation = Json.decodeFromString<PlayerLocation>(string)

        val offlinePlayer = Bukkit.getOfflinePlayer(playerLocation.player)

        if (offlinePlayer.player != null) {
            offlinePlayer.player?.teleport(Location(Bukkit.getWorld(playerLocation.location.world), playerLocation.location.x, playerLocation.location.y, playerLocation.location.z))
        } else {
            list.add(playerLocation)
        }

    }
}