package com.noticemc.noticetransport.paper

import com.noticemc.noticetransport.common.Location
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object InitLocation {
    var initLocation : Location? = null

    fun loadConfig(){
        val file = NoticeTransport.plugin.dataFolder.resolve("init.json")
        if(!file.exists()){
            return
        }
        initLocation = Json.decodeFromString(file.readText())
    }
}