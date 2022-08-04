package com.noticemc.noticetransport.paper.event

import com.noticemc.noticetransport.common.PlayerLocation
import com.noticemc.noticetransport.paper.InitLocation.initLocation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerJoinEvent : Listener {
    @EventHandler
    fun onPlayerJoin(event: org.bukkit.event.player.PlayerJoinEvent) {
        val initLocation = initLocation
        val player = event.player
        val uuid = player.uniqueId
        val playerLocation = list.find { it.player == uuid }
        if (playerLocation != null) {
            player.teleport(Location(Bukkit.getWorld(playerLocation.location.world),
                playerLocation.location.x,
                playerLocation.location.y,
                playerLocation.location.z))
            list.remove(playerLocation)
        } else {
            if (initLocation != null && !player.hasPermission("noticetransport.init.pass")) {
                player.teleport(Location(Bukkit.getWorld(initLocation.world), initLocation.x, initLocation.y, initLocation.z))
            }
        }

    }

    companion object {
        val list = ArrayList<PlayerLocation>()
    }
}