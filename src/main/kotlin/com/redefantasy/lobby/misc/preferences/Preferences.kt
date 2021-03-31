package com.redefantasy.lobby.misc.preferences

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.groups.Group
import com.redefantasy.core.shared.misc.preferences.PLAYER_VISIBILITY
import com.redefantasy.core.shared.misc.preferences.PreferenceState
import com.redefantasy.core.shared.misc.preferences.data.Preference
import com.redefantasy.core.shared.users.data.User
import com.redefantasy.lobby.misc.button.HotBarManager
import com.redefantasy.lobby.misc.button.player.visibility.button.PlayerVisibilityOffHotBarButton
import com.redefantasy.lobby.misc.button.player.visibility.button.PlayerVisibilityOnHotBarButton
import org.bukkit.Bukkit

/**
 * @author Gutyerrez
 */
fun Preference.post(user: User) = when (this.name) {
    "player-visibility-preference" -> {
        val player = Bukkit.getPlayer(user.getUniqueId())

        Bukkit.getOnlinePlayers().forEach { _player ->
            val _user = CoreProvider.Cache.Local.USERS.provide().fetchById(_player.uniqueId)!!

            if (!user.hasGroup(Group.HELPER) && _user.getPreferences().find { it == PLAYER_VISIBILITY }?.preferenceState === PreferenceState.DISABLED) {
                _player.hidePlayer(player)
            } else if (_user.getPreferences().find { it == PLAYER_VISIBILITY }?.preferenceState === PreferenceState.ENABLED) {
                _player.showPlayer(player)
            }

            if (
                user.getPreferences().find { it == PLAYER_VISIBILITY }?.preferenceState === PreferenceState.DISABLED && !_user.hasGroup(
                    Group.HELPER
                )
            ) {
                player.hidePlayer(_player)
            } else if (user.getPreferences().find { it == PLAYER_VISIBILITY }?.preferenceState === PreferenceState.ENABLED) {
                player.showPlayer(_player)
            }
        }

        HotBarManager.giveHotBarButtonToPlayer(
            if (this.preferenceState === PreferenceState.ENABLED) {
                PlayerVisibilityOnHotBarButton()
            } else {
                PlayerVisibilityOffHotBarButton()
            },
            player
        )
    }
    else -> TODO("not yet implemented")
}