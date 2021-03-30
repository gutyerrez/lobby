package com.redefantasy.lobby

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.applications.status.ApplicationStatus
import com.redefantasy.core.shared.applications.status.task.ApplicationStatusTask
import com.redefantasy.core.shared.echo.packets.listener.UserPreferencesUpdatedEchoPacketListener
import com.redefantasy.core.shared.misc.preferences.FLY_IN_LOBBY
import com.redefantasy.core.shared.misc.preferences.LOBBY_COMMAND_PROTECTION
import com.redefantasy.core.shared.misc.preferences.PreferenceRegistry
import com.redefantasy.core.shared.scheduler.AsyncScheduler
import com.redefantasy.core.shared.users.data.User
import com.redefantasy.core.spigot.command.CustomCommand
import com.redefantasy.core.spigot.command.registry.CommandRegistry
import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import com.redefantasy.lobby.echo.packets.listeners.UserGroupsUpdatedEchoPacketListener
import com.redefantasy.lobby.listeners.GeneralListener
import com.redefantasy.lobby.misc.button.HotBarManager
import com.redefantasy.lobby.misc.button.player.visibility.button.PlayerVisibilityOffHotBarButton
import com.redefantasy.lobby.misc.button.player.visibility.button.PlayerVisibilityOnHotBarButton
import com.redefantasy.lobby.misc.button.preferences.button.PreferencesHotBarButton
import com.redefantasy.lobby.misc.button.server.selector.button.ServerSelectorHotBarButton
import com.redefantasy.lobby.misc.npc.entity.CustomZombie
import com.redefantasy.lobby.misc.queue.QueueRunnable
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.entity.Giant
import org.bukkit.entity.Item
import org.bukkit.event.entity.CreatureSpawnEvent
import java.util.concurrent.TimeUnit

/**
 * @author Gutyerrez
 */
class LobbyPlugin : CustomPlugin(false) {

    companion object {

        lateinit var instance: CustomPlugin

    }

    init {

        instance = this

    }

    private var onlineSince = 0L

    override fun onEnable() {
        super.onEnable()

        LobbyProvider.prepare()

        this.onlineSince = System.currentTimeMillis()

        val pluginManager = Bukkit.getServer().pluginManager

        pluginManager.registerEvents(GeneralListener(), this)

        /**
         * Preferences
         */

        PreferenceRegistry.register(
            FLY_IN_LOBBY,
            LOBBY_COMMAND_PROTECTION
        )

        /**
         * Hot Bar Buttons
         */

        HotBarManager.registerHotBarButton(
            PreferencesHotBarButton(),
            ServerSelectorHotBarButton(),
            PlayerVisibilityOnHotBarButton(),
            PlayerVisibilityOffHotBarButton()
        )

        /**
         * ECHO
         */

        CoreProvider.Databases.Redis.ECHO.provide().registerListener(UserPreferencesUpdatedEchoPacketListener())
        CoreProvider.Databases.Redis.ECHO.provide().registerListener(UserGroupsUpdatedEchoPacketListener())

        /**
         * Queue
         */

        AsyncScheduler.scheduleAsyncRepeatingTask(
            QueueRunnable(),
            0,
            1,
            TimeUnit.SECONDS
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

            it.livingEntities.forEach { livingEntity ->
                if (livingEntity is Item) {
                    livingEntity.remove()
                }
            }
        }

        /**
         * Application status
         */

        AsyncScheduler.scheduleAsyncRepeatingTask(
            object : ApplicationStatusTask(
                ApplicationStatus(
                    CoreProvider.application.name,
                    CoreProvider.application.applicationType,
                    CoreProvider.application.server,
                    CoreProvider.application.address,
                    this.onlineSince
                )
            ) {
                override fun buildApplicationStatus(
                    applicationStatus: ApplicationStatus
                ) {
                    val runtime = Runtime.getRuntime()

                    applicationStatus.heapSize = runtime.totalMemory()
                    applicationStatus.heapMaxSize = runtime.maxMemory()
                    applicationStatus.heapFreeSize = runtime.freeMemory()
                }
            },
            0,
            1,
            TimeUnit.SECONDS
        )

        /**
         * Temporary
         */

        CommandRegistry.registerCommand(
            object : CustomCommand("spawn") {
                override fun onCommand(
                    commandSender: CommandSender,
                    user: User?,
                    args: Array<out String>
                ): Boolean {
                    val npcLocation = Location(
                        Bukkit.getWorlds()[0],
                        0.5,
                        94.5,
                        73.5
                    )

                    val worldServer = (npcLocation.world as CraftWorld).handle

                    val customZombie = CustomZombie(worldServer)

                    customZombie.setLocation(npcLocation.x, npcLocation.y, npcLocation.z, npcLocation.yaw, npcLocation.pitch)
                    customZombie.setPositionRotation(npcLocation.x, npcLocation.y, npcLocation.z, npcLocation.yaw, npcLocation.pitch)

                    if (!worldServer.addEntity(customZombie, CreatureSpawnEvent.SpawnReason.CUSTOM)) {
                        println("Não consegui adicionar a entidade")
                    }

                    val npc = customZombie.bukkitEntity as Giant

                    npc.removeWhenFarAway = false
                    npc.teleport(npcLocation.clone().add(1.9, -8.5, -3.5))

                    commandSender.sendMessage("Spawnou!")
                    return true
                }
            }
        )
    }

}