package com.noticemc.noticetransport.paper.event

import com.noticemc.noticetransport.common.ChannelKey.key
import com.noticemc.noticetransport.common.PlayerLocation
import com.noticemc.noticetransport.paper.NoticeTransport
import com.noticemc.noticetransport.paper.event.PlayerJoinEvent.Companion.list
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import org.jetbrains.annotations.Nullable

class NoticeTransportListener : PluginMessageListener {

    override fun onPluginMessageReceived(channel: String,@Nullable player: Player, message: ByteArray) {
        NoticeTransport.plugin.logger.info("plugin message is received")
        if (channel != key) return

        val string = String(message, Charsets.UTF_8)
        val playerLocation = Json.decodeFromString<PlayerLocation>(string)

        val offlinePlayer = Bukkit.getOfflinePlayer(playerLocation.player)

        if (offlinePlayer.isOnline) {

            offlinePlayer.player!!.teleport(Location(Bukkit.getWorld(playerLocation.location.world),
                playerLocation.location.x,
                playerLocation.location.y,
                playerLocation.location.z))
        } else {
            NoticeTransport.plugin.logger.info("plugin message is received but player is offline")
            list.add(playerLocation)
        }

    }
}