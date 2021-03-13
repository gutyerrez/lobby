package com.redefantasy.lobby

import com.redefantasy.core.spigot.CoreSpigotConstants
import com.redefantasy.core.spigot.CoreSpigotProvider
import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import com.redefantasy.lobby.listeners.GeneralListener
import com.redefantasy.lobby.misc.button.HotBarManager
import com.redefantasy.lobby.misc.button.server.selector.button.ServerSelectorHotBarButton
import org.bukkit.Bukkit

/**
 * @author Gutyerrez
 */
class LobbyPlugin : CustomPlugin(false) {

    override fun onEnable() {
        super.onEnable()

        LobbyProvider.prepare()

        val pluginManager = Bukkit.getServer().pluginManager

        pluginManager.registerEvents(GeneralListener(), this)

        val spawnSerializedLocation = CoreSpigotProvider.Repositories.Postgres.SPAWN_REPOSITORY.provide().fetch()

        /**
         * Hot Bar Buttons
         */

        HotBarManager.registerHotBarButton(
            ServerSelectorHotBarButton()
        )

        /**
         * World settings
         */
        Bukkit.getServer().worlds.forEach {

            it.setStorm(false)

            it.isThundering = false
            it.weatherDuration = 0

            it.ambientSpawnLimit = 0
            it.animalSpawnLimit = 0
            it.monsterSpawnLimit = 0

            it.setTicksPerAnimalSpawns(99999)
            it.setTicksPerMonsterSpawns(99999)

            it.setGameRuleValue("randomTickSpeed", "-999")
            it.setGameRuleValue("mobGriefing", "false")
            it.setGameRuleValue("doMobSpawning", "false")
            it.setGameRuleValue("doMobLoot", "false")
            it.setGameRuleValue("doFireTick", "false")
            it.setGameRuleValue("doDaylightCycle", "false")

            it.time = 1200

            if (spawnSerializedLocation !== null) {
                val spawnLocation = CoreSpigotConstants.BUKKIT_LOCATION_PARSER.apply(spawnSerializedLocation)

                it.setSpawnLocation(
                    spawnLocation.blockX,
                    spawnLocation.blockY,
                    spawnLocation.blockZ
                )
            }
        }
    }

}