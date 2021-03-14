package com.redefantasy.lobby.misc.button.server.selector.inventory

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.applications.ApplicationType
import com.redefantasy.core.shared.echo.packets.ConnectUserToApplicationPacket
import com.redefantasy.core.spigot.inventory.CustomInventory
import com.redefantasy.core.spigot.misc.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player

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
                .build()
        ) { it ->
            if (factionsOmegaBukkitSpawnApplication !== null) {
                val player = it.whoClicked as Player
                val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)

                println(factionsOmegaBukkitSpawnApplication.toString())

                val packet = ConnectUserToApplicationPacket(
                    user?.id,
                    factionsOmegaBukkitSpawnApplication
                )

                println("asd")

                CoreProvider.Databases.Redis.ECHO.provide().publishToApplications(
                    packet,
                    CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByApplicationType(
                        ApplicationType.PROXY
                    )
                )
            }
        }
    }

}