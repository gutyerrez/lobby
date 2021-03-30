package com.redefantasy.lobby.misc.button.server.selector.inventory

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.applications.ApplicationType
import com.redefantasy.core.shared.servers.ServerType
import com.redefantasy.core.spigot.inventory.CustomInventory
import com.redefantasy.core.spigot.misc.utils.ItemBuilder
import com.redefantasy.lobby.misc.utils.ServerConnectorUtils
import org.bukkit.Material
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
            val slot = slots[index]

            val bukkitSpawnApplication =
                CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServerAndApplicationType(
                    server,
                    ApplicationType.SERVER_SPAWN
                )

            this.setItem(
                slot,
                ItemBuilder(Material.TNT)
                    .name("§b${server.displayName}")
                    .lore(
                        when (server.serverType) {
                            ServerType.FACTIONS -> arrayOf(
                                "",
                                "§7  Convoque sua facção, construa sua base,  ",
                                "§7  defenda-se de invasões adversárias",
                                "§7  e realize suas próprias invasões.",
                                "",
                                "§aClique para jogar!"
                            )
                            ServerType.RANK_UP -> arrayOf(
                                "",
                                "§7  Convoque seu clã, minere, evolua,  ",
                                "§7  e domine o universo Rank UP.",
                                "",
                                "§aClique para jogar!"
                            )
                        }
                    )
                    .build()
            ) { it ->
                if (bukkitSpawnApplication !== null) {
                    val player = it.whoClicked as Player

                    ServerConnectorUtils.connect(
                        player,
                        server
                    )
                }
            }
        }
    }

}