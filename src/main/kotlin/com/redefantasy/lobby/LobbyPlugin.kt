package com.redefantasy.lobby

import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import com.redefantasy.lobby.listeners.GeneralListener
import org.bukkit.Bukkit

/**
 * @author Gutyerrez
 */
class LobbyPlugin : CustomPlugin(false) {

    override fun onEnable() {
        super.onEnable()

        val pluginManager = Bukkit.getServer().pluginManager

        pluginManager.registerEvents(GeneralListener(), this)

        /**
         * World settings
         */
        Bukkit.getServer().worlds.forEach {
            if (it.hasStorm()) it.setStorm(false)

            it.weatherDuration = 0

            it.setGameRuleValue("randomTickSpeed", "-999")
            it.setGameRuleValue("doFireTick", "false")
            it.setGameRuleValue("doDaylightCycle", "false")

            it.time = 1200
        }
    }

}