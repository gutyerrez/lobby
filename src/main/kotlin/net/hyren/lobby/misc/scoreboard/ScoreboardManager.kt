package net.hyren.lobby.misc.scoreboard

import net.hyren.core.shared.CoreConstants
import net.hyren.core.shared.CoreProvider
import net.hyren.core.shared.applications.ApplicationType
import net.hyren.core.shared.applications.status.ApplicationStatus
import net.hyren.core.shared.users.storage.table.UsersTable
import net.hyren.core.spigot.misc.scoreboard.bukkit.GroupScoreboard
import net.hyren.lobby.LobbyProvider
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

/**
 * @author Gutyerrez
 */
object ScoreboardManager {

    private val WITH_SCORE_BOARD = mutableMapOf<UUID, Long>()

    fun construct(player: Player) {
        val user = LobbyProvider.Cache.Local.LOBBY_USERS.provide().fetchById(player.uniqueId)!!

        val fancyGroupName = user.getHighestGroup().getFancyDisplayName()
        val scoreboard = LobbyScoreboard()

        scoreboard.registerTeams()

        user.scoreboard = scoreboard

        scoreboard.setTitle(
            CoreConstants.Info.COLORED_SERVER_NAME
        )
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
        scoreboard.set(0, "§e${CoreConstants.Info.SHOP_URL}")

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

                    CoreProvider.Cache.Local.SERVERS.provide().fetchAll().filter { !it.isAlphaServer() }.forEach { server ->
                        val bukkitSpawnApplication = CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServerAndApplicationType(
                            server,
                            ApplicationType.SERVER_SPAWN
                        )

                        val onlineUsers = CoreProvider.Cache.Redis.USERS_STATUS.provide().fetchUsersByServer(server)

                        scoreboard.set(
                            i, "§f ${server.getFancyDisplayName()}: ${
                                when {
                                    bukkitSpawnApplication === null || CoreProvider.Cache.Redis.APPLICATIONS_STATUS.provide().fetchApplicationStatusByApplication(
                                        bukkitSpawnApplication,
                                        ApplicationStatus::class
                                    ) == null -> "§cOff."
                                    CoreProvider.Cache.Local.MAINTENANCE.provide().fetch(bukkitSpawnApplication) == true -> {
                                        "§cMan."
                                    }
                                    else -> "§a${onlineUsers.size}"
                                }
                            }"
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

                        scoreboard as GroupScoreboard

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
