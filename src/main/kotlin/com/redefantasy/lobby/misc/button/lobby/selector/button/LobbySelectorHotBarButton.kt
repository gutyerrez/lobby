package com.redefantasy.lobby.misc.button.lobby.selector.button

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.spigot.misc.utils.ItemBuilder
import com.redefantasy.lobby.misc.button.HotBarButton
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.greenrobot.eventbus.Subscribe

/**
 * @author Gutyerrez
 */
class LobbySelectorHotBarButton : HotBarButton(
    ItemBuilder(Material.NETHER_STAR)
        .name("§aSelecionar saguão")
        .lore(
            arrayOf(
                "§7Clique para escolher",
                "§7um saguão."
            )
        )
        .build(),
    6
) {

    @Subscribe
    fun on(
        event: PlayerInteractEvent
    ) {
        val player = event.player
        val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)!!


    }

}