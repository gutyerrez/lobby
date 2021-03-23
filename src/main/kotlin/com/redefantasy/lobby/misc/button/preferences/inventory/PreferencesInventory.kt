package com.redefantasy.lobby.misc.button.preferences.inventory

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.misc.preferences.PreferenceRegistry
import com.redefantasy.core.shared.misc.preferences.data.Preference
import com.redefantasy.core.spigot.inventory.CustomInventory
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

/**
 * @author Gutyerrez
 */
class PreferencesInventory(private val player: Player) : CustomInventory(
    "Preferências",
    6 * 9
) {

    private val PREFERENCES_ICON_SLOTS = arrayOf(
        1, 2, 3, 4, 5, 6, 7
    )

    private val PREFERENCES_BUTTON_SLOTS = arrayOf(
        10, 11, 12 ,13, 14, 15, 16
    )

    init {
        val user = CoreProvider.Cache.Local.USERS.provide().fetchById(this.player.uniqueId)!!
        val preferences = user.getPreferences()

        preferences.forEachIndexed { index, preference ->
            val slot = this.PREFERENCES_ICON_SLOTS[index]

            this.setPreferenceItem(
                slot,
                preference
            )
        }

        this.player.openInventory(this)
    }

    private fun setPreferenceItem(
        slot: Int,
        preference: Preference
    ) {
        this.setItem(
            slot,
            preference.getIcon() as ItemStack,
            Consumer<InventoryClickEvent> {
                val bus = PreferenceRegistry.BUS[preference.name]

                if (bus !== null) {
                    bus.post(it)
                }
            }
        )
    }

}