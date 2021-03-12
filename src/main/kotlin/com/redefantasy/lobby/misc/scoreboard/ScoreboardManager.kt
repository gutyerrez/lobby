package com.redefantasy.lobby.misc.scoreboard

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.spigot.misc.scoreboard.bukkit.BaseScoreboard
import org.bukkit.entity.Player

/**
 * @author Gutyerrez
 */
object ScoreboardManager {

    fun construct(player: Player) {
        val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)
        val groupPrefix = user?.getHighestGroup()?.getFancyDisplayName() ?: "§7Membro"

        val scoreboard = BaseScoreboard(player)

        val onlineUsers = CoreProvider.Cache.Redis.USERS_STATUS.provide().fetchUsers()

        scoreboard.setTitle("§6§lREDE FANTASY")
        scoreboard.set(15, "§0")
        scoreboard.set(14, "§f Online: §a${onlineUsers.size}")
        scoreboard.set(13, "§1")

        var i = 12

        CoreProvider.Cache.Local.SERVERS.provide().fetchAll().forEach {
            val onlineUsers = CoreProvider.Cache.Redis.USERS_STATUS.provide().fetchUsersByServer(it)

            scoreboard.set(i, "§f ${it.displayName}: §e${onlineUsers.size}")

            i++
        }

        scoreboard.set(3, "§2")
        scoreboard.set(2, "§f Grupo: $groupPrefix")
        scoreboard.set(1, "§3")
        scoreboard.set(0, "§e  redefantasy.com")

        player.scoreboard = scoreboard.scoreboard
    }

}