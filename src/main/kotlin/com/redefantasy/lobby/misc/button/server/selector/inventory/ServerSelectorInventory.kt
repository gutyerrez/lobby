package com.redefantasy.lobby.misc.button.server.selector.inventory

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.applications.ApplicationType
import com.redefantasy.core.spigot.inventory.CustomInventory
import com.redefantasy.lobby.LobbyProvider
import com.redefantasy.lobby.misc.utils.ServerConnectorUtils
import org.bukkit.entity.Player

/**
 * @author Gutyerrez
 */
class ServerSelectorInventory() : CustomInventory(
    "Selecione o servidor",
    3 * 9
) {

    private val SLOTS = arrayOf(
        arrayOf(13),
        arrayOf(11, 15),
        arrayOf(10, 13, 16)
    )

    init {
        val servers = CoreProvider.Cache.Local.SERVERS.provide().fetchAll()

        val slots = this.SLOTS[if (servers.size >= this.SLOTS.size) this.SLOTS.lastIndex else servers.size - 1]

        servers.forEachIndexed { index, server ->
            val serverConfiguration = LobbyProvider.Cache.Local.SERVER_CONFIGURATION.provide().fetchByServer(server) ?: return@forEachIndexed

            val slot = slots[index]

	        CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServerAndApplicationType(
                server,
                ApplicationType.SERVER_SPAWN
            ) ?: return@forEachIndexed

            this.setItem(
                slot,
                serverConfiguration.icon
            ) { it ->
                val player = it.whoClicked as Player

                ServerConnectorUtils.connect(
                    player,
                    server
                )
            }
        }
    }

}