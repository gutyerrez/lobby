package com.redefantasy.lobby.misc.button.player.visibility.button

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.misc.preferences.PLAYER_VISIBILITY
import com.redefantasy.core.spigot.misc.utils.ItemBuilder
import com.redefantasy.lobby.misc.button.HotBarButton
import com.redefantasy.lobby.misc.preferences.post
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.greenrobot.eventbus.Subscribe

/**
 * @author Gutyerrez
 */
class PlayerVisibilityOnHotBarButton : HotBarButton(
    ItemBuilder(Material.INK_SACK)
        .name(
            "§fJogadores: §aON"
        ).durability(
            10
        ).lore(
            arrayOf(
                "§7Clique para que os usuários",
                "§7desapareçam!"
            )
        ).build(),
    7
) {

    @Subscribe
    fun on(
        event: PlayerInteractEvent
    ) {
        val player = event.player

        val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)!!

        println("pei")

        user.getPreferences().find { it == PLAYER_VISIBILITY }?.post(user)
    }

}

class PlayerVisibilityOffHotBarButton : HotBarButton(
    ItemBuilder(Material.INK_SACK)
        .name(
            "§fJogadores: §cOFF"
        ).durability(
            8
        ).lore(
            arrayOf(
                "§7Clique para que os usuários",
                "§7apareçam!"
            )
        ).build(),
    7
) {

    @Subscribe
    fun on(
        event: PlayerInteractEvent
    ) {
        val player = event.player

        val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)!!

        println("pei")

        user.getPreferences().find { it == PLAYER_VISIBILITY }?.post(user)
    }

}