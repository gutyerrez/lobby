package com.redefantasy.lobby.misc.scoreboard

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.users.data.User
import com.redefantasy.core.shared.users.storage.table.UsersTable
import com.redefantasy.core.spigot.misc.scoreboard.bukkit.BaseScoreboard
import com.redefantasy.core.spigot.misc.scoreboard.bukkit.GroupScoreboard
import org.apache.commons.lang3.StringUtils
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.id.EntityID

/**
 * @author Gutyerrez
 */
object ScoreboardManager {

    fun construct(player: Player) {
        val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)
        val fancyGroupName = user?.getHighestGroup()?.getFancyDisplayName() ?: "§7Membro"

        val scoreboard = BaseScoreboard()

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

            i--
        }

        val bukkitApplicationName = CoreProvider.application.displayName.split(" ")[1]

        scoreboard.set(3, "§2")
        scoreboard.set(2, "§f Saguão: §7#$bukkitApplicationName")
        scoreboard.set(1, "§3")
        scoreboard.set(0, "§e  loja.redefantasy.com")

        scoreboard.send(arrayOf(player))

        val groupScoreboard = GroupScoreboard()

        groupScoreboard.registerUser(user ?: User(
            EntityID(player.uniqueId, UsersTable),
            player.name
        ))
    }

}