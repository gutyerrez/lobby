package net.hyren.lobby.echo.packets.listeners

import net.hyren.core.shared.echo.api.listener.EchoListener
import net.hyren.core.shared.echo.packets.UserGroupsUpdatedPacket
import net.hyren.lobby.LobbyProvider
import net.hyren.lobby.misc.scoreboard.ScoreboardManager
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