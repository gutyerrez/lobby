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

    private val SLOTS = arrayOf(
        10, 11, 12, 13, 14, 15, 16
    )

    init {
        val serversCount = CoreProvider.Cache.Local.SERVERS.provide().fetchAll().size

        CoreProvider.Cache.Local.SERVERS.provide().fetchAll().forEach {
            val bukkitSpawnApplication = CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServerAndApplicationType(
                it,
                ApplicationType.SERVER_SPAWN
            )

            println(serversCount % this.SLOTS.size)
            println(serversCount / this.SLOTS.size)

            println("---")

            println(this.SLOTS.size % serversCount)
            println(this.SLOTS.size / serversCount)

            this.setItem(
                serversCount % this.SLOTS.size,
                ItemBuilder(Material.STONE)
                    .build()
            ) { event ->
                val player = event.whoClicked as Player
                val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)

                val packet = ConnectUserToApplicationPacket(
                    user?.id,
                    bukkitSpawnApplication
                )

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