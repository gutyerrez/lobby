package com.redefantasy.lobby.misc.scoreboard

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.users.storage.table.UsersTable
import com.redefantasy.core.spigot.misc.scoreboard.bukkit.GroupScoreboard
import com.redefantasy.lobby.LobbyProvider
import org.apache.commons.lang3.StringUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.id.EntityID

/**
 * @author Gutyerrez
 */
object ScoreboardManager {

    fun construct(player: Player) {
        val user = LobbyProvider.Cache.Local.LOBBY_USERS.provide().fetchById(player.uniqueId)!!

        val fancyGroupName = user.getHighestGroup().getFancyDisplayName()
        val scoreboard = LobbyScoreboard(player)
        val onlineUsers = CoreProvider.Cache.Redis.USERS_STATUS.provide().fetchUsers()

        scoreboard.setTitle("§6§lREDE FANTASY")
        scoreboard.set(15, "§0")
        scoreboard.set(14, "§f Online: §7${onlineUsers.size}")
        scoreboard.set(13, "§f Grupo: $fancyGroupName")
        scoreboard.set(12, "§1")

        var i = 11

        CoreProvider.Cache.Local.SERVERS.provide().fetchAll().forEach {
            val onlineUsers = CoreProvider.Cache.Redis.USERS_STATUS.provide().fetchUsersByServer(it)

            scoreboard.set(
                i, "§f ${
                    StringUtils.replaceEach(
                        it.displayName,
                        arrayOf(
                            "Rankup",
                            "Factions"
                        ),
                        arrayOf(
                            "R.",
                            "F."
                        )
                    )
                }: §a${onlineUsers.size}"
            )

            if (i >= 4) i--
        }

        val bukkitApplicationName = CoreProvider.application.displayName.split(" ")[1]

        scoreboard.set(3, "§2")
        scoreboard.set(2, "§f Saguão: §7#$bukkitApplicationName")
        scoreboard.set(1, "§3")
        scoreboard.set(0, "§e  loja.redefantasy.com")

        user.scoreboard = scoreboard

        Bukkit.getOnlinePlayers().forEach {
            val targetUser = LobbyProvider.Cache.Local.LOBBY_USERS.provide().fetchById(
                EntityID(
                    it.uniqueId,
                    UsersTable
                )
            )!!

            val groupBoard = targetUser.scoreboard as GroupScoreboard

            groupBoard.registerUser(user)
            scoreboard.registerUser(targetUser)
        }

        scoreboard.send(
            arrayOf(
                player
            )
        )
    }

}