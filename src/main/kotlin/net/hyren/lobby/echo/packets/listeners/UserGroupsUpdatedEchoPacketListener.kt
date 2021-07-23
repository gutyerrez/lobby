package net.hyren.lobby.echo.packets.listeners

import net.hyren.core.shared.echo.api.listener.EchoPacketListener
import net.hyren.core.shared.echo.packets.UserGroupsUpdatedPacket
import net.hyren.lobby.LobbyProvider
import net.hyren.lobby.misc.scoreboard.ScoreboardManager
import org.greenrobot.eventbus.Subscribe

/**
 * @author Gutyerrez
 */
class UserGroupsUpdatedEchoPacketListener : EchoPacketListener {

    @Subscribe
    fun on(
        packet: UserGroupsUpdatedPacket
    ) {
        val userId = packet.userId!!
        val lobbyUser = LobbyProvider.Cache.Local.LOBBY_USERS.provide().fetchById(userId)

        if (lobbyUser === null || !lobbyUser.isOnline()) {
            return
        }

        ScoreboardManager.update(
            lobbyUser,
            ScoreboardManager.Slot.TAB_LIST
        )
    }

}