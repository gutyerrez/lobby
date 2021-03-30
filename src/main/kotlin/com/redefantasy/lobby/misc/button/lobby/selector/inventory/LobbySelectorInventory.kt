package com.redefantasy.lobby.misc.button.lobby.selector.inventory

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.applications.ApplicationType
import com.redefantasy.core.shared.applications.status.ApplicationStatus
import com.redefantasy.core.shared.echo.packets.ConnectUserToApplicationPacket
import com.redefantasy.core.shared.users.data.User
import com.redefantasy.core.spigot.inventory.CustomInventory
import com.redefantasy.core.spigot.misc.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import java.util.function.Consumer

/**
 * @author Gutyerrez
 */
class LobbySelectorInventory(user: User) : CustomInventory(
    "Selecionar saguão",
    3 * 9
) {

    private val SLOTS = arrayOf(
        10, 12, 14, 16
    )

    init {
        CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByApplicationType(
            ApplicationType.LOBBY
        ).forEachIndexed { index, application ->
            val applicationStatus =
                CoreProvider.Cache.Redis.APPLICATIONS_STATUS.provide().fetchApplicationStatusByApplication(
                    application,
                    ApplicationStatus::class
                )

            val itemBuilder = ItemBuilder(Material.INK_SACK)
                .name("§e${application.displayName}")
                .durability(
                    if (applicationStatus === null) {
                        8
                    } else 10
                ).lore(
                    when {
                        applicationStatus === null -> {
                            arrayOf(
                                "§cSaguão offline."
                            )
                        }
                        user.getConnectedBukkitApplication() == application -> {
                            arrayOf(
                                "§eVocê já está aqui"
                            )
                        }
                        else -> arrayOf(
                            "§7Jogadores: ${applicationStatus.onlinePlayers}/${application.slots}",
                            "§aeClique para entrar!"
                        )
                    }
                )

            if (user.getConnectedBukkitApplication() == application)
                itemBuilder.enchant(Enchantment.DURABILITY, 1)

            this.setItem(
                SLOTS[index],
                itemBuilder.build(),
                Consumer {
                    if (applicationStatus === null || user.getConnectedBukkitApplication() === application) return@Consumer

                    val packet = ConnectUserToApplicationPacket(
                        user.id,
                        application
                    )

                    CoreProvider.Databases.Redis.ECHO.provide().publishToApplicationType(
                        packet,
                        ApplicationType.PROXY
                    )
                }
            )
        }
    }

}