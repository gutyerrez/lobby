package net.hyren.lobby

import net.hyren.core.shared.CoreConstants
import net.hyren.core.shared.CoreProvider
import net.hyren.core.shared.applications.ApplicationType
import net.hyren.core.shared.applications.status.ApplicationStatus
import net.hyren.core.shared.applications.status.task.ApplicationStatusTask
import net.hyren.core.shared.echo.packets.listener.UserPreferencesUpdatedEchoPacketListener
import net.hyren.core.shared.misc.preferences.FLY_IN_LOBBY
import net.hyren.core.shared.misc.preferences.LOBBY_COMMAND_PROTECTION
import net.hyren.core.shared.misc.preferences.PreferenceRegistry
import net.hyren.core.shared.scheduler.AsyncScheduler
import net.hyren.core.shared.servers.data.Server
import net.hyren.core.spigot.command.registry.CommandRegistry
import net.hyren.core.spigot.misc.frame.data.Frame
import net.hyren.core.spigot.misc.hologram.Hologram
import net.hyren.core.spigot.misc.plugin.CustomPlugin
import net.hyren.core.spigot.misc.skin.command.SkinCommand
import net.hyren.core.spigot.misc.theme.data.Theme
import net.hyren.core.spigot.world.generator.VoidChunkGenerator
import net.hyren.lobby.echo.packets.listeners.UserGroupsUpdatedEchoPacketListener
import net.hyren.lobby.listeners.GenericListener
import net.hyren.lobby.misc.button.HotBarManager
import net.hyren.lobby.misc.button.lobby.selector.button.LobbySelectorHotBarButton
import net.hyren.lobby.misc.button.player.visibility.button.PlayerVisibilityOffHotBarButton
import net.hyren.lobby.misc.button.player.visibility.button.PlayerVisibilityOnHotBarButton
import net.hyren.lobby.misc.button.preferences.button.PreferencesHotBarButton
import net.hyren.lobby.misc.button.server.selector.button.ServerSelectorHotBarButton
import net.hyren.lobby.misc.queue.QueueRunnable
import net.hyren.lobby.misc.queue.command.QueueCommand
import net.hyren.lobby.misc.scoreboard.ScoreboardManager
import net.hyren.lobby.misc.server.utils.ServerConfigurationUtils
import net.hyren.lobby.misc.slime.jump.SlimeJumpManager
import net.hyren.lobby.misc.slime.jump.data.SlimeJump
import net.hyren.lobby.misc.slime.jump.listener.SlimeJumpListener
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Giant
import org.bukkit.util.Vector
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * @author Gutyerrez
 */
class LobbyPlugin : CustomPlugin() {

    companion object {

        lateinit var instance: CustomPlugin

    }

    init {

        instance = this

    }

    private val NPCS = mutableMapOf<Server, Giant>()
    private val HOLOGRAMS = mutableMapOf<Server, Hologram>()

    private var onlineSince = 0L

    override fun onEnable() {
        super.onEnable()

        LobbyProvider.prepare()

        this.onlineSince = System.currentTimeMillis()

        val pluginManager = Bukkit.getServer().pluginManager

        /**
         * Spigot listeners
         */

        pluginManager.registerEvents(GenericListener(), this)
        pluginManager.registerEvents(SlimeJumpListener(), this)

        /**
         * Commands
         */

        CommandRegistry.registerCommand(QueueCommand())
        CommandRegistry.registerCommand(SkinCommand())

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

        Bukkit.getWorlds().forEach {
            it.isAutoSave = false

            it.isThundering = false
            it.weatherDuration = 0

            it.ambientSpawnLimit = 0
            it.animalSpawnLimit = 0
            it.monsterSpawnLimit = 0

            it.setTicksPerAnimalSpawns(99999)
            it.setTicksPerMonsterSpawns(99999)

            it.setStorm(false)

            it.setGameRuleValue("randomTickSpeed", "-1")
            it.setGameRuleValue("mobGriefing", "false")
            it.setGameRuleValue("doMobSpawning", "false")
            it.setGameRuleValue("doMobLoot", "false")
            it.setGameRuleValue("doFireTick", "false")
            it.setGameRuleValue("doDaylightCycle", "false")

            it.time = 6000
        }

        val theme = Theme()

        theme.load()
        theme.paste(
            getDefaultWorld().name, 0, 75, 0
        )

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

        CoreProvider.Cache.Local.SERVERS.provide().fetchAll().filter { !it.isAlphaServer() }.forEach {
            ServerConfigurationUtils.initServer(it, NPCS, HOLOGRAMS)
        }

        /**
         * Holograms
         */

        Bukkit.getScheduler().runTaskTimer(
            this,
            {
                /**
                 * Revalidating NPCS and HOLOGRAMS
                 */

                CoreProvider.Cache.Local.SERVERS.provide().fetchAll().filter { !it.isAlphaServer() }.forEach {
                    if (!NPCS.containsKey(it) || !HOLOGRAMS.containsKey(it)) {
                        ServerConfigurationUtils.initServer(it, NPCS, HOLOGRAMS)
                    }
                }

                HOLOGRAMS.forEach { (server, hologram) ->
                    hologram.update(1, "§e${CoreProvider.Cache.Redis.USERS_STATUS.provide().fetchUsersByServer(server).size} jogando!")

                    val bukkitSpawnApplication = CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServerAndApplicationType(
                        server,
                        ApplicationType.SERVER_SPAWN
                    )

                    if (bukkitSpawnApplication === null || CoreProvider.Cache.Redis.APPLICATIONS_STATUS.provide().fetchApplicationStatusByApplication(
                        bukkitSpawnApplication,
                        ApplicationStatus::class
                    ) == null) {
                        hologram.update(2, "§cOffline")
                    } else if (CoreProvider.Cache.Local.MAINTENANCE.provide().fetch(bukkitSpawnApplication) == true) {
                        hologram.update(2, "§cEm manutenção")
                    } else {
                        hologram.remove(2)
                    }
                }
            },
            20L,
            20L * 5
        )

        /**
         * Scoreboard task
         */


        Bukkit.getScheduler().runTaskTimer(
            this,
            {
                Thread {
                    run {
                        LobbyProvider.Cache.Local.LOBBY_USERS.provide().fetchAll().forEach {
                            try {
                                if (it.player != null && ! it.player.isDead) {
                                    ScoreboardManager.update(
                                        it,
                                        ScoreboardManager.Slot.ONLINE_PLAYERS,
                                        ScoreboardManager.Slot.SERVER_LIST
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }.start()
            }, 0, 20
        )

        /**
         * Frames
         */

        Frame(URL("https://i.imgur.com/G053pKt.png")).also {
            it.interactConsumer = Consumer { player ->
                val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId) ?: return@Consumer

                if (CoreConstants.COOLDOWNS.inCooldown(user, "frame-interact")) {
                    return@Consumer
                }

                player.sendMessage(
                    ComponentBuilder()
                        .append("\n")
                        .append("§e Clique ")
                        .append("§e§lAQUI")
                        .event(
                            ClickEvent(
                                ClickEvent.Action.OPEN_URL,
                                CoreConstants.Info.SHOP_URL
                            )
                        )
                        .append("§e para adquirir seu plano §6VIP§e!")
                        .append("\n")
                        .create()
                )

                CoreConstants.COOLDOWNS.start(
                    user,
                    "frame-interact",
                    TimeUnit.SECONDS.toMillis(3)
                )
            }

            it.place(
                Location(
                    getDefaultWorld(),
                    3.0,
                    78.0,
                    72.0
                ),
                BlockFace.NORTH
            )
        }

        /**
         * Slime Jumps
         */

        val leftJumperVector = Vector(-0.5, 1.0, 5.0)
        val rightJumperVector = Vector(0.5, 1.0, 5.0)

        SlimeJumpManager.register(
            /**
             * left jumpers
             */

            SlimeJump(9.5, 76.0, 13.5, leftJumperVector),
            SlimeJump(9.5, 76.0, 12.5, leftJumperVector),
            SlimeJump(9.5, 76.0, 11.5, leftJumperVector),
            SlimeJump(9.5, 76.0, 10.5, leftJumperVector),
            SlimeJump(8.5, 76.0, 13.5, leftJumperVector),
            SlimeJump(8.5, 76.0, 12.5, leftJumperVector),
            SlimeJump(8.5, 76.0, 11.5, leftJumperVector),
            SlimeJump(8.5, 76.0, 10.5, leftJumperVector),
            SlimeJump(7.5, 76.0, 12.5, leftJumperVector),
            SlimeJump(7.5, 76.0, 11.5, leftJumperVector),

            /**
             * right jumpers
             */

            SlimeJump(-8.5, 76.0, 13.5, rightJumperVector),
            SlimeJump(-8.5, 76.0, 12.5, rightJumperVector),
            SlimeJump(-8.5, 76.0, 11.5, rightJumperVector),
            SlimeJump(-8.5, 76.0, 10.5, rightJumperVector),
            SlimeJump(-7.5, 76.0, 13.5, rightJumperVector),
            SlimeJump(-7.5, 76.0, 12.5, rightJumperVector),
            SlimeJump(-7.5, 76.0, 11.5, rightJumperVector),
            SlimeJump(-7.5, 76.0, 10.5, rightJumperVector),
            SlimeJump(-6.5, 76.0, 12.5, rightJumperVector),
            SlimeJump(-6.5, 76.0, 11.5, rightJumperVector),
        )

        SlimeJumpManager.setup()
    }

    override fun getDefaultWorldGenerator(
        worldName: String,
        id: String
    ) = VoidChunkGenerator()

}
