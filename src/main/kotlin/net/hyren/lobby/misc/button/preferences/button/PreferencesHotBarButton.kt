package net.hyren.lobby.misc.button.preferences.button

import net.hyren.core.spigot.misc.utils.ItemBuilder
import net.hyren.lobby.misc.button.HotBarButton
import net.hyren.lobby.misc.button.preferences.inventory.PreferencesInventory
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.greenrobot.eventbus.Subscribe

/**
 * @author Gutyerrez
 */
class PreferencesHotBarButton : HotBarButton(
    ItemBuilder(Material.REDSTONE_COMPARATOR)
        .name("§aPreferências")
        .lore(
            arrayOf(
                "§7Controle diversas preferências",
                "§7pessoais em nossa rede."
            )
        )
        .build(),
    1
) {

    @Subscribe
    fun on(
        event: PlayerInteractEvent
    ) {
        val player = event.player

        PreferencesInventory(player)
    }

}