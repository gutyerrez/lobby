package com.redefantasy.lobby.misc.button.implementations

import com.redefantasy.core.spigot.misc.utils.ItemBuilder
import com.redefantasy.lobby.misc.button.HotBarButton
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.greenrobot.eventbus.Subscribe

/**
 * @author Gutyerrez
 */
class LobbySelectorHotBarButton : HotBarButton(
    ItemBuilder(Material.COMPASS)
        .name("§bSelecionar Servidor")
        .lore(
            arrayOf(
                "§7Clique com direito para escolher",
                "§7o servidor que deseja jogar."
            )
        )
        .build(),
    4
) {

    @Subscribe
    fun on(
        event: PlayerInteractEvent
    ) {
        val player = event.player

        player.sendMessage(TextComponent("Yeah"))
    }

}