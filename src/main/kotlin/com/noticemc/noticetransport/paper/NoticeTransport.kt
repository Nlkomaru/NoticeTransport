package com.noticemc.noticetransport.paper

import com.noticemc.noticetransport.common.ChannelKey.key
import com.noticemc.noticetransport.paper.InitLocation.loadConfig
import com.noticemc.noticetransport.paper.event.NoticeTransportListener
import com.noticemc.noticetransport.paper.event.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class NoticeTransport : JavaPlugin() {
    override fun onEnable() {
        plugin = this
        loadConfig()
        registerEvents()
    }

    override fun onDisable() {
    }

    private fun registerEvents() {
        server.messenger.registerIncomingPluginChannel(this, key, NoticeTransportListener())
        server.pluginManager.registerEvents(PlayerJoinEvent(), this)
    }

    companion object {
        lateinit var plugin: NoticeTransport
    }
}