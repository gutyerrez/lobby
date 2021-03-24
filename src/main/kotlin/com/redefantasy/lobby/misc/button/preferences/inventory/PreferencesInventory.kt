package com.redefantasy.lobby.misc.button.preferences.inventory

import com.redefantasy.core.shared.CoreConstants
import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.misc.kotlin.copyFrom
import com.redefantasy.core.shared.misc.preferences.Preference
import com.redefantasy.core.shared.misc.preferences.PreferenceRegistry
import com.redefantasy.core.shared.misc.preferences.PreferenceState
import com.redefantasy.core.shared.users.preferences.storage.dto.UpdateUserPreferencesDTO
import com.redefantasy.core.spigot.inventory.CustomInventory
import com.redefantasy.core.spigot.inventory.ICustomInventory
import com.redefantasy.core.spigot.misc.preferences.toItemStack
import com.redefantasy.core.spigot.misc.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import java.util.concurrent.TimeUnit

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
        10, 11, 12, 13, 14, 15, 16
    )

    init {
        val user = CoreProvider.Cache.Local.USERS.provide().fetchById(this.player.uniqueId)!!
        val preferences = user.getPreferences()

        preferences.forEachIndexed { index, preference ->
            val iconSlot = this.PREFERENCES_ICON_SLOTS[index]
            val buttonSlot = this.PREFERENCES_BUTTON_SLOTS[index]

            this.setPreferenceIcon(
                iconSlot,
                preference
            )
            this.setPreferenceButton(
                buttonSlot,
                preference
            )
        }

        this.player.openInventory(this)
    }

    private fun setPreferenceIcon(
        slot: Int,
        preference: Preference
    ) {
        this.setItem(
            slot,
            preference.getIcon().toItemStack(),
            this.preferenceClickConsumer(preference)
        )
    }

    private fun setPreferenceButton(
        slot: Int,
        preference: Preference
    ) {
        this.setItem(
            slot,
            ItemBuilder(Material.STAINED_GLASS_PANE)
                .name(preference.getIcon().displayName)
                .data(if (preference.preferenceState === PreferenceState.ENABLED) 5 else 14)
                .lore(
                    arrayOf(
                        "Estado: ${preference.preferenceState.getColor()}${
                            if (preference.preferenceState === PreferenceState.ENABLED)
                                "Ligado"
                            else "Desligado"
                        }"
                    )
                )
                .build(),
            this.preferenceClickConsumer(preference)
        )
    }

    private fun preferenceClickConsumer(
        preference: Preference
    ) = object : ICustomInventory.ConsumerClickListener {
        override fun accept(
            event: InventoryClickEvent
        ) {
            val player = event.whoClicked as Player
            val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)!!

            val preferences = user.getPreferences()

            if (preferences.size != PreferenceRegistry.fetchAll().size)
                preferences.copyFrom(PreferenceRegistry.fetchAll())

            if (CoreConstants.COOLDOWNS.inCooldown(user, preference.name)) return

            val switchPreferenceState = when (preference.preferenceState) {
                PreferenceState.ENABLED -> PreferenceState.DISABLED
                PreferenceState.DISABLED -> PreferenceState.ENABLED
            }

            preference.preferenceState = switchPreferenceState

            CoreProvider.Repositories.Postgres.USERS_PREFERENCES_REPOSITORY.provide().update(
                UpdateUserPreferencesDTO(
                    user.id,
                    preferences
                )
            )

            CoreConstants.COOLDOWNS.start(user, preference.name, TimeUnit.SECONDS.toMillis(3))

            val slot = event.slot

            if (this@PreferencesInventory.PREFERENCES_ICON_SLOTS.contains(slot)) {
                this@PreferencesInventory.setPreferenceIcon(slot, preference)
            } else if (this@PreferencesInventory.PREFERENCES_BUTTON_SLOTS.contains(slot)) {
                this@PreferencesInventory.setPreferenceButton(slot, preference)
            }
        }
    }

}