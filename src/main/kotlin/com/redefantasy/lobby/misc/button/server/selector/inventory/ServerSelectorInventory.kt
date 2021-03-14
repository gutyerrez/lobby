package com.redefantasy.lobby.misc.button.server.selector.inventory

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.applications.ApplicationType
import com.redefantasy.core.shared.applications.status.ApplicationStatus
import com.redefantasy.core.shared.echo.packets.ConnectUserToApplicationPacket
import com.redefantasy.core.spigot.inventory.CustomInventory
import com.redefantasy.core.spigot.inventory.ICustomInventory
import com.redefantasy.core.spigot.misc.utils.ItemBuilder
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * @author Gutyerrez
 */
class ServerSelectorInventory : CustomInventory(
    "Selecione o servidor",
    3 * 9
) {

    init {
        this.construct()
    }

    private fun construct() {
        val factionsOmegaBukkitSpawnApplication =
            CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServerAndApplicationType(
                CoreProvider.Cache.Local.SERVERS.provide().fetchByName("FACTIONS_OMEGA")!!,
                ApplicationType.SERVER_SPAWN
            )

        this.setItem(
            13,
            ItemBuilder(Material.TNT)
                .name("§bFactions Ômega")
                .lore(
                    arrayOf(
                        "§7Bah meu lança uma lore legal ai."
                    )
                )
                .build(),
            object : ICustomInventory.ConsumerClickListener {
                override fun accept(
                    event: InventoryClickEvent
                ) {
                    if (factionsOmegaBukkitSpawnApplication !== null) {
                        val factionsOmegaBukkitSpawnApplicationStatus =
                            CoreProvider.Cache.Redis.APPLICATIONS_STATUS.provide().fetchApplicationStatusByApplication(
                                factionsOmegaBukkitSpawnApplication,
                                ApplicationStatus::class
                            )

                        println(factionsOmegaBukkitSpawnApplication)

                        val player = event.whoClicked as Player
                        val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)

                        if (factionsOmegaBukkitSpawnApplicationStatus === null) {
                            println("nullo")

                            player.sendMessage(TextComponent("§cEste servidor está offline."))
                            return
                        }

                        println(factionsOmegaBukkitSpawnApplicationStatus.toString())

                        val packet = ConnectUserToApplicationPacket(
                            user?.id,
                            factionsOmegaBukkitSpawnApplication
                        )

                        println("Enviar o packet")

                        CoreProvider.Databases.Redis.ECHO.provide().publishToApplications(
                            packet,
                            CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByApplicationType(
                                ApplicationType.PROXY
                            )
                        )
                    }
                }
            }
        )
    }

}