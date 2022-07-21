package com.noticemc.noticetransport.paper.event

import com.noticemc.noticetransport.common.PlayerLocation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerJoinEvent : Listener {
    @EventHandler
    fun onPlayerJoin(event: org.bukkit.event.player.PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val playerLocation = list.find { it.player == uuid } ?: return
        player.teleport(Location(Bukkit.getWorld(playerLocation.world), playerLocation.x, playerLocation.y, playerLocation.z))
        list.remove(playerLocation)
    }

    companion object {
        val list = ArrayList<PlayerLocation>()
    }
}