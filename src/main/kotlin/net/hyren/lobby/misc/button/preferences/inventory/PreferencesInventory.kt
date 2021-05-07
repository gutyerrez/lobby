package net.hyren.lobby.misc.button.preferences.inventory

import net.hyren.core.shared.CoreConstants
import net.hyren.core.shared.CoreProvider
import net.hyren.core.shared.echo.packets.UserPreferencesUpdatedPacket
import net.hyren.core.shared.groups.Group
import net.hyren.core.shared.misc.preferences.PreferenceState
import net.hyren.core.shared.misc.preferences.data.Preference
import net.hyren.core.shared.users.data.User
import net.hyren.core.shared.users.preferences.storage.dto.UpdateUserPreferencesDTO
import net.hyren.core.spigot.inventory.CustomInventory
import net.hyren.core.spigot.inventory.ICustomInventory
import net.hyren.core.spigot.misc.utils.ItemBuilder
import net.hyren.lobby.misc.preferences.post
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
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
            preference.icon,
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
                .name(preference.icon?.itemMeta?.displayName ?: "§7Desconhecido")
                .durability(if (preference.preferenceState === PreferenceState.ENABLED) 5 else 14)
                .lore(
                    arrayOf(
                        "§7Estado: ${preference.preferenceState.getColor()}${
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

            if (!preference.canSwitch(user)) return

            if (CoreConstants.COOLDOWNS.inCooldown(user, preference.name)) return

            val switchPreferenceState = when (preference.preferenceState) {
                PreferenceState.ENABLED -> PreferenceState.DISABLED
                PreferenceState.DISABLED -> PreferenceState.ENABLED
            }

            preference.preferenceState = switchPreferenceState

            CoreProvider.Repositories.Postgres.USERS_PREFERENCES_REPOSITORY.provide().update(
                UpdateUserPreferencesDTO(
                    user.id,
                    user.getPreferences()
                )
            )

            when (preference.name) {
                "player-visibility-preference" -> preference.post(user)
                "fly-in-lobby-preference" -> {
                    player.allowFlight = switchPreferenceState === PreferenceState.ENABLED
                    player.isFlying = switchPreferenceState === PreferenceState.ENABLED
                }
            }

            val packet = UserPreferencesUpdatedPacket(
                user.id,
                user.getPreferences()
            )

            CoreProvider.Databases.Redis.ECHO.provide().publishToAll(packet)

            CoreConstants.COOLDOWNS.start(user, preference.name, TimeUnit.SECONDS.toMillis(3))

            val slot = event.slot

            if (this@PreferencesInventory.PREFERENCES_ICON_SLOTS.contains(slot)) {
                val index = this@PreferencesInventory.PREFERENCES_ICON_SLOTS.indexOf(slot)

                this@PreferencesInventory.setPreferenceIcon(
                    this@PreferencesInventory.PREFERENCES_ICON_SLOTS[index],
                    preference
                )
                this@PreferencesInventory.setPreferenceButton(
                    this@PreferencesInventory.PREFERENCES_BUTTON_SLOTS[index],
                    preference
                )
            } else if (this@PreferencesInventory.PREFERENCES_BUTTON_SLOTS.contains(slot)) {
                val index = this@PreferencesInventory.PREFERENCES_BUTTON_SLOTS.indexOf(slot)

                this@PreferencesInventory.setPreferenceIcon(
                    this@PreferencesInventory.PREFERENCES_ICON_SLOTS[index],
                    preference
                )
                this@PreferencesInventory.setPreferenceButton(
                    this@PreferencesInventory.PREFERENCES_BUTTON_SLOTS[index],
                    preference
                )
            }
        }
    }

    private val Preference.icon: ItemStack?
        get() = when (this.name) {
            "user-private-messages-preference" -> {
                ItemBuilder(
                    Material.EMPTY_MAP
                ).name(
                    "${this.getStateColor()}Mensagens privadas",
                ).lore(
                    arrayOf("§7Receber mensagens privadas.")
                ).build()
            }
            "player-visibility-preference" -> {
                ItemBuilder(
                    Material.WATCH
                ).name(
                    "${this.getStateColor()}Visibilidade"
                ).lore(
                    arrayOf("§7Ver outros usuários nos saguões.")
                ).build()
            }
            "fly-in-lobby-preference" -> {
                ItemBuilder(
                    Material.FEATHER
                ).name(
                    "${this.getStateColor()}Voar no saguão"
                ).lore(
                    arrayOf("§7Habilitar automaticamente o voo nos saguões.")
                ).build()
            }
            "lobby-command-protection-preference" -> {
                ItemBuilder(
                    Material.NETHER_STAR
                ).name(
                    "${this.getStateColor()}Proteção no /lobby"
                ).lore(
                    arrayOf(
                        "§7Requisitar o comando",
                        "§7/lobby 2 vezes."
                    )
                ).build()
            }
            "premium-account-preference" -> {
                ItemBuilder(
                    Material.DIAMOND
                ).name(
                    "${this.getStateColor()}Autênticação automática"
                ).lore(
                    arrayOf(
                        "§7Definir sua conta como original."
                    )
                ).build()
            }
            else -> null
        }

    private fun Preference.canSwitch(user: User) = when (this.name) {
        "fly-in-lobby-preference" -> user.hasGroup(Group.VIP)
        else -> true
    }

}