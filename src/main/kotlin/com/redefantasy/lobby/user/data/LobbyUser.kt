package com.redefantasy.lobby.user.data

import com.redefantasy.core.shared.users.data.User
import com.redefantasy.core.spigot.misc.scoreboard.bukkit.BaseScoreboard

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

}