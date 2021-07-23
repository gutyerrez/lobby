package net.hyren.lobby.user.data

import net.hyren.core.shared.users.data.User
import net.hyren.core.spigot.misc.scoreboard.bukkit.BaseScoreboard
import org.bukkit.Bukkit

/**
 * @author Gutyerrez
 */
data class LobbyUser(val user: User) : User(
    user.id,
    user.name,
    user.email,
    user.discordId,
    user.twoFactorAuthenticationEnabled,
    user.twoFactorAuthenticationCode,
    user.twitterAccessToken,
    user.twitterTokenSecret,
    user.lastAddress,
    user.lastLobbyName,
    user.lastLoginAt,
    user.createdAt,
    user.updatedAt
) {

    lateinit var scoreboard: BaseScoreboard

    val player = Bukkit.getPlayer(this.id.value)

    fun isScoreboardInitialized() = this::scoreboard.isInitialized

}