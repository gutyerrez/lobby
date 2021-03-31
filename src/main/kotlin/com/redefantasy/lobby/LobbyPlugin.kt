package com.redefantasy.lobby

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.applications.ApplicationType
import com.redefantasy.core.shared.applications.status.ApplicationStatus
import com.redefantasy.core.shared.applications.status.task.ApplicationStatusTask
import com.redefantasy.core.shared.echo.packets.listener.UserPreferencesUpdatedEchoPacketListener
import com.redefantasy.core.shared.misc.preferences.FLY_IN_LOBBY
import com.redefantasy.core.shared.misc.preferences.LOBBY_COMMAND_PROTECTION
import com.redefantasy.core.shared.misc.preferences.PreferenceRegistry
import com.redefantasy.core.shared.scheduler.AsyncScheduler
import com.redefantasy.core.shared.servers.data.Server
import com.redefantasy.core.shared.users.data.User
import com.redefantasy.core.spigot.command.CustomCommand
import com.redefantasy.core.spigot.command.registry.CommandRegistry
import com.redefantasy.core.spigot.misc.frame.data.Frame
import com.redefantasy.core.spigot.misc.hologram.Hologram
import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import com.redefantasy.core.spigot.misc.utils.ItemBuilder
import com.redefantasy.lobby.echo.packets.listeners.UserGroupsUpdatedEchoPacketListener
import com.redefantasy.lobby.listeners.GeneralListener
import com.redefantasy.lobby.misc.button.HotBarManager
import com.redefantasy.lobby.misc.button.lobby.selector.button.LobbySelectorHotBarButton
import com.redefantasy.lobby.misc.button.player.visibility.button.PlayerVisibilityOffHotBarButton
import com.redefantasy.lobby.misc.button.player.visibility.button.PlayerVisibilityOnHotBarButton
import com.redefantasy.lobby.misc.button.preferences.button.PreferencesHotBarButton
import com.redefantasy.lobby.misc.button.server.selector.button.ServerSelectorHotBarButton
import com.redefantasy.lobby.misc.queue.QueueRunnable
import net.minecraft.server.v1_8_R3.EntityGiantZombie
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Giant
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.net.URL
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

    private val HOLOGRAMS = mutableMapOf<Server, Hologram>()

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
            LobbySelectorHotBarButton(),
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
            it.isAutoSave = false

            it.isThundering = false
            it.weatherDuration = 0

            it.ambientSpawnLimit = 0
            it.animalSpawnLimit = 0
            it.monsterSpawnLimit = 0

            it.setTicksPerAnimalSpawns(99999)
            it.setTicksPerMonsterSpawns(99999)

            it.setStorm(false)

            it.setGameRuleValue("randomTickSpeed", "-999")
            it.setGameRuleValue("mobGriefing", "false")
            it.setGameRuleValue("doMobSpawning", "false")
            it.setGameRuleValue("doMobLoot", "false")
            it.setGameRuleValue("doFireTick", "false")
            it.setGameRuleValue("doDaylightCycle", "false")

            it.time = 1200
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

                    applicationStatus.onlinePlayers = Bukkit.getOnlinePlayers().size
                }
            },
            0,
            1,
            TimeUnit.SECONDS
        )

        /**
         * NPCs
         */

        CoreProvider.Cache.Local.SERVERS.provide().fetchAll().forEach {
            when (it.name.value) {
                "FACTIONS_PHOENIX" -> {
                    val npcLocation = Location(
                        Bukkit.getWorld("world"),
                        0.5,
                        91.5,
                        -73.5
                    )

                    val worldServer = (npcLocation.world as CraftWorld).handle

                    val customZombie = EntityGiantZombie(worldServer)

                    customZombie.setLocation(npcLocation.x, npcLocation.y, npcLocation.z, npcLocation.yaw, npcLocation.pitch)
                    customZombie.setPositionRotation(npcLocation.x, npcLocation.y, npcLocation.z, npcLocation.yaw, npcLocation.pitch)

                    worldServer.addEntity(customZombie, CreatureSpawnEvent.SpawnReason.CUSTOM)

                    val npc = customZombie.bukkitEntity as Giant

                    npc.addPotionEffect(
                        PotionEffect(
                            PotionEffectType.INVISIBILITY,
                            Int.MAX_VALUE,
                            1
                        ),
                        true
                    )
                    npc.removeWhenFarAway = false
                    npc.equipment.itemInHand = ItemBuilder(Material.BLAZE_POWDER)
                        .enchant(Enchantment.DURABILITY, 1)
                        .build()
                    npc.teleport(npcLocation.clone().add(1.9, -8.5, -3.5))

                    val hologram = Hologram(
                        listOf(
                            "§a${it.displayName}",
                            "?",
                            "§aClique para entrar!"
                        ),
                        Hologram.HologramPosition.DOWN
                    )
                    hologram.spawn(
                        npcLocation.clone().add(0.0, 3.5, 0.0)
                    )

                    HOLOGRAMS[it] = hologram
                }
            }
        }

        /**
         * Holograms
         */

        Bukkit.getScheduler().runTaskTimer(
            this,
            {
                HOLOGRAMS.forEach { (server, hologram) ->
                    val onlineUsersCount = CoreProvider.Cache.Redis.USERS_STATUS.provide().fetchUsersByServer(server).size

                    hologram.update(1, "§b$onlineUsersCount jogando!")

                    val bukkitSpawnApplication = CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServerAndApplicationType(
                        server,
                        ApplicationType.SERVER_SPAWN
                    )

                    if (bukkitSpawnApplication === null || CoreProvider.Cache.Redis.APPLICATIONS_STATUS.provide().fetchApplicationStatusByApplication(
                            bukkitSpawnApplication,
                            ApplicationStatus::class
                    ) === null) {
                        hologram.update(2, "§cOffline")
                    } else if (CoreProvider.Cache.Local.MAINTENANCE.provide().fetch(bukkitSpawnApplication) == true) {
                        hologram.update(2, "§cEm manutenção")
                    }
                }
            },
            20L,
            20L * 5
        )

        /**
         * Frames
         */
        val frame = Frame(URL("https://i.imgur.com/4r9csnG.png"))

        CommandRegistry.registerCommand(
            object : CustomCommand("frame") {

                override fun onCommand(
                    commandSender: CommandSender,
                    user: User?,
                    args: Array<out String>
                ): Boolean? {
                    frame.place(
                        Location(
                            Bukkit.getWorld("world"),
                            -3.0,
                            89.0,
                            -38.0
                        ),
                        BlockFace.SOUTH
                    )
                    return true
                }

            }
        )

        LobbyConstants.SERVERS_WORLD_CUBOIDS.values.forEach {
            it.getBlocks { block ->
                if (block.type === Material.AIR) {
                    block.type = Material.BARRIER
                }
            }
        }
    }

}