package com.redefantasy.lobby.misc.button.server.selector.inventory

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.applications.ApplicationType
import com.redefantasy.core.spigot.CoreSpigotProvider
import com.redefantasy.core.spigot.inventory.CustomInventory
import com.redefantasy.lobby.misc.utils.ServerConnectorUtils
import org.bukkit.entity.Player
import java.util.*
import java.util.stream.Collectors

/**
 * @author Gutyerrez
 */
class ServerSelectorInventory : CustomInventory(
    "Selecione o servidor",
    3 * 9
) {

    private val SLOTS = arrayOf(
        arrayOf(13),
        arrayOf(11, 15),
        arrayOf(10, 13, 16)
    )

    init {
        val servers = Arrays.stream(CoreProvider.Cache.Local.SERVERS.provide().fetchAll()).filter {
            CoreSpigotProvider.Cache.Local.SERVER_CONFIGURATION.provide().fetchByServer(it) !== null
        }.filter {
            CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServerAndApplicationType(
                it,
                ApplicationType.SERVER_SPAWN
            ) !== null
        }.collect(Collectors.toSet())

        val slots = this.SLOTS[
                if (servers.size >= this.SLOTS.size) {
                    this.SLOTS.lastIndex
                } else servers.size - 1
        ]

        servers.forEachIndexed { index, server ->
            val slot = slots[index]

            this.setItem(
                slot,
	            CoreSpigotProvider.Cache.Local.SERVER_CONFIGURATION.provide().fetchByServer(
		            server
	            )?.icon
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