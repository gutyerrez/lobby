package net.hyren.lobby.misc.button.server.selector.button

import net.hyren.core.spigot.misc.utils.ItemBuilder
import net.hyren.lobby.misc.button.HotBarButton
import net.hyren.lobby.misc.button.server.selector.inventory.ServerSelectorInventory
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.greenrobot.eventbus.Subscribe

/**
 * @author Gutyerrez
 */
class ServerSelectorHotBarButton : HotBarButton(
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

        player.openInventory(
            ServerSelectorInventory()
        )
    }

}