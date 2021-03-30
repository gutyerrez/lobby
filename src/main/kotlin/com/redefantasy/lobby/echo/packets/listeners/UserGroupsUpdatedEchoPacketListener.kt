package com.redefantasy.lobby.echo.packets.listeners

import com.redefantasy.core.shared.echo.api.listener.EchoListener
import com.redefantasy.core.shared.echo.packets.UserGroupsUpdatedPacket
import com.redefantasy.lobby.LobbyProvider
import com.redefantasy.lobby.misc.scoreboard.ScoreboardManager
import org.greenrobot.eventbus.Subscribe

/**
 * @author Gutyerrez
 */
class UserGroupsUpdatedEchoPacketListener : EchoListener {

    @Subscribe
    fun on(
        packet: UserGroupsUpdatedPacket
    ) {
        val userId = packet.userId!!
        val lobbyUser = LobbyProvider.Cache.Local.LOBBY_USERS.provide().fetchById(userId)

        if (lobbyUser === null) return

        if (!lobbyUser.isOnline()) return

        val player = lobbyUser.player

        if (player === null) return

        ScoreboardManager.update(
            player,
            ScoreboardManager.Slot.TAB_LIST
        )
    }

}