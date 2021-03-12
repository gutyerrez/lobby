package com.redefantasy.lobby

import com.redefantasy.core.spigot.listeners.GeneralListener
import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import org.bukkit.Bukkit

/**
 * @author Gutyerrez
 */
class LobbyPlugin : CustomPlugin(false) {

    override fun onEnable() {
        super.onEnable()

        val pluginManager = Bukkit.getServer().pluginManager

        pluginManager.registerEvents(GeneralListener(), this)
    }

}