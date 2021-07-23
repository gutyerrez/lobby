package net.hyren.lobby.misc.captcha.inventory

import net.hyren.core.shared.CoreProvider
import net.hyren.core.shared.groups.Group
import net.hyren.core.shared.misc.preferences.FLY_IN_LOBBY
import net.hyren.core.shared.misc.preferences.PLAYER_VISIBILITY
import net.hyren.core.shared.misc.preferences.PreferenceState
import net.hyren.core.shared.users.storage.dto.CreateUserDTO
import net.hyren.core.spigot.inventory.CustomInventory
import net.hyren.core.spigot.misc.utils.ItemBuilder
import net.hyren.lobby.LobbyPlugin
import net.hyren.lobby.LobbyProvider
import net.hyren.lobby.misc.button.HotBarManager
import net.hyren.lobby.misc.preferences.post
import net.hyren.lobby.user.data.LobbyUser
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import java.util.function.Consumer

/**
 * @author Gutyerrez
 */
class CaptchaInventory : CustomInventory(
    "Validação",
    3 * 9
) {

    private val ITEMS_SLOTS = arrayOf(
        10, 11, 12, 13, 14, 15, 16
    )

    private val FILL_ITEM = ItemBuilder(Material.CARROT_ITEM)
        .build()

    private val MATERIALS = arrayOf(
        Material.PUMPKIN_PIE,
        Material.APPLE,
        Material.RAW_FISH,
        Material.CAKE,
        Material.BREAD,
        Material.GOLDEN_APPLE,
    )

    private var remainingItems = 3

    init {
        ITEMS_SLOTS.shuffle()

        val filledSlots = mutableListOf<Int>()

        for (index in 0 until remainingItems) {
            val slot = ITEMS_SLOTS[index]
            val material = MATERIALS[index]

            setItem(
                slot,
                ItemBuilder(material)
                    .name("§eClique aqui")
                    .build(),
                Consumer {
                    val player = it.whoClicked as Player

                    this@CaptchaInventory.remainingItems--

                    this@CaptchaInventory.setItem(
                        slot,
                        ItemBuilder(Material.BARRIER).build()
                    )

                    if (this@CaptchaInventory.remainingItems <= 0) {
                        var user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)

                        player.closeInventory()

                        if (user == null) {
                            user = CoreProvider.Repositories.PostgreSQL.USERS_REPOSITORY.provide().create(
                                CreateUserDTO(
                                    player.uniqueId,
                                    player.name,
                                    (player as CraftPlayer).address.address.hostAddress
                                )
                            )
                        }

                        LobbyProvider.Cache.Local.LOBBY_USERS.provide().put(
                            LobbyUser(user)
                        )

                        if (user.hasGroup(Group.VIP) && user.getPreferences()
                                .find { preference -> preference == FLY_IN_LOBBY }?.preferenceState === PreferenceState.ENABLED
                        ) {
                            player.allowFlight = true
                            player.isFlying = true
                        }

                        HotBarManager.giveToPlayer(player)

                        user.getPreferences().find { preference -> preference == PLAYER_VISIBILITY }?.post(user)
                    }
                }
            )

            filledSlots.add(slot)
        }

        ITEMS_SLOTS.filter { !filledSlots.contains(it) }.forEach { slot ->
            setItem(
                slot,
                FILL_ITEM,
                Consumer {
                    val player = it.whoClicked as Player

                    player.kick(
                        TextComponent("§cValidação informada incorreta!")
                    )
                }
            )
        }
    }

    override fun on(
        event: InventoryCloseEvent
    ) {
        val player = event.player

        if (remainingItems <= 0) {
            return
        }

        Bukkit.getScheduler().runTaskLater(
            LobbyPlugin.instance,
            {
                player.openInventory(this@CaptchaInventory)
            },
            1L
        )
    }

}