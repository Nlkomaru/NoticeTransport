package com.noticemc.noticetransport.velocity.files

import com.noticemc.noticetransport.velocity.NoticeTransport
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.*

object Config {
    lateinit var config: Configuration

    @OptIn(ExperimentalSerializationApi::class)
    fun load() {
        val hocon = Hocon
        val renderOptions = ConfigRenderOptions.defaults().setOriginComments(false).setComments(false).setFormatted(true).setJson(false)
        val file = NoticeTransport.dataDirectory.toFile().resolve("config.hocon")
        val config = Configuration("1.0", hashMapOf(), 30, "順番が来ました")
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
            file.writeText(hocon.encodeToConfig(config).root().render(renderOptions))
        } else {
            val loadConfig = hocon.decodeFromConfig<Configuration>(ConfigFactory.parseFile(file))
            if (loadConfig.ver != "1.0") {
                file.writeText(hocon.encodeToConfig(config).root().render(renderOptions))
            }
        }
        Config.config = hocon.decodeFromConfig(ConfigFactory.parseFile(file))
    }
}

@Serializable
data class Configuration(val ver: String, val templateFileName: HashMap<String,String>, val timeOut: Int, val message: String)