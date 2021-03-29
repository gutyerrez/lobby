package com.redefantasy.lobby.misc.scoreboard

import com.google.common.collect.Queues
import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.applications.ApplicationType
import com.redefantasy.core.shared.applications.status.ApplicationStatus
import com.redefantasy.core.shared.users.storage.table.UsersTable
import com.redefantasy.core.spigot.misc.scoreboard.bukkit.GroupScoreboard
import com.redefantasy.lobby.LobbyPlugin
import com.redefantasy.lobby.LobbyProvider
import com.redefantasy.lobby.user.data.LobbyUser
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * @author Gutyerrez
 */
object ScoreboardManager {

    private val WITH_SCORE_BOARD = mutableMapOf<UUID, Long>()

    val UPDATE_SCOREBOARD = Consumer<LobbyUser> {
        this.update(
            it.player,
            Slot.ONLINE_PLAYERS,
            Slot.SERVER_LIST
        )
    }

    init {
        val queue = Queues.newConcurrentLinkedQueue<LobbyUser>()

        Bukkit.getScheduler().runTaskTimer(
            LobbyPlugin.instance,
            {
                if (queue.isEmpty() && Bukkit.getOnlinePlayers().isEmpty()) return@runTaskTimer

                if (queue.isEmpty() && Bukkit.getOnlinePlayers().isNotEmpty()) {
                    queue.addAll(
                        Bukkit.getOnlinePlayers().stream()
                            .filter { this.WITH_SCORE_BOARD.containsKey(it.uniqueId) }
                            .map {
                                LobbyProvider.Cache.Local.LOBBY_USERS.provide().fetchById(
                                    it.uniqueId
                                )
                            }
                            .collect(Collectors.toSet())
                    )
                }

                val lobbyUser = queue.poll()
                val player = Bukkit.getPlayer(lobbyUser.getUniqueId())

                if (lobbyUser !== null && player !== null) this.UPDATE_SCOREBOARD.accept(lobbyUser)
            },
            0,
            20
        )
    }

    fun construct(player: Player) {
        val user = LobbyProvider.Cache.Local.LOBBY_USERS.provide().fetchById(player.uniqueId)!!

        val fancyGroupName = user.getHighestGroup().getFancyDisplayName()
        val scoreboard = LobbyScoreboard()

        scoreboard.registerTeams()

        user.scoreboard = scoreboard

        scoreboard.setTitle("§6§lREDE FANTASY")
        scoreboard.set(15, "§0")
        scoreboard.set(13, "§f Grupo: $fancyGroupName")
        scoreboard.set(12, "§1")

        this.update(
            player,
            Slot.ONLINE_PLAYERS,
            Slot.SERVER_LIST,
            Slot.TAB_LIST
        )

        val bukkitApplicationName = CoreProvider.application.displayName.split(" ")[1]

        scoreboard.set(3, "§2")
        scoreboard.set(2, "§f Saguão: §7#$bukkitApplicationName")
        scoreboard.set(1, "§3")
        scoreboard.set(0, "§e  loja.redefantasy.com")

        this.WITH_SCORE_BOARD[player.uniqueId] = System.currentTimeMillis()

        scoreboard.send(arrayOf(player))
    }

    fun update(player: Player, vararg slots: Slot) {
        val user = LobbyProvider.Cache.Local.LOBBY_USERS.provide().fetchById(player.uniqueId)!!

        val scoreboard = user.scoreboard

        slots.forEach {
            when (it) {
                Slot.ONLINE_PLAYERS -> {
                    val onlineUsers = CoreProvider.Cache.Redis.USERS_STATUS.provide().fetchUsers()

                    scoreboard.set(14, "§f Online: §7${onlineUsers.size}")
                }
                Slot.SERVER_LIST -> {
                    var i = 11

                    CoreProvider.Cache.Local.SERVERS.provide().fetchAll().forEach { server ->
                        val bukkitSpawnApplication = CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServerAndApplicationType(
                            server,
                            ApplicationType.SERVER_SPAWN
                        )

                        val onlineUsers = CoreProvider.Cache.Redis.USERS_STATUS.provide().fetchUsersByServer(server)

                        val statusString: String = when {
                            bukkitSpawnApplication === null || CoreProvider.Cache.Redis.APPLICATIONS_STATUS.provide().fetchApplicationStatusByApplication(
                                bukkitSpawnApplication,
                                ApplicationStatus::class
                            ) === null -> "§cOff."
                            CoreProvider.Cache.Local.MAINTENANCE.provide().fetch(bukkitSpawnApplication) == true -> {
                                "§cMan."
                            }
                            else -> "§a${onlineUsers.size}"
                        }

                        scoreboard.set(
                            i, "§f ${server.getFancyDisplayName()}: $statusString"
                        )

                        if (i >= 4) i--
                    }
                }
                Slot.TAB_LIST -> {
                    Bukkit.getOnlinePlayers().forEach { player ->
                        val targetUser = LobbyProvider.Cache.Local.LOBBY_USERS.provide().fetchById(
                            EntityID(
                                player.uniqueId,
                                UsersTable
                            )
                        )!!

                        val scoreboard = scoreboard as GroupScoreboard
                        val groupBoard = targetUser.scoreboard as GroupScoreboard

                        groupBoard.registerUser(user)
                        scoreboard.registerUser(targetUser)
                    }
                }
            }
        }
    }

    enum class Slot {

        ONLINE_PLAYERS, SERVER_LIST, TAB_LIST;

    }

}