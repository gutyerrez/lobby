package com.redefantasy.lobby.misc.button.server.selector.inventory

import com.redefantasy.core.shared.CoreConstants
import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.applications.ApplicationType
import com.redefantasy.core.shared.applications.status.ApplicationStatus
import com.redefantasy.core.shared.echo.packets.ConnectUserToApplicationPacket
import com.redefantasy.core.spigot.inventory.CustomInventory
import com.redefantasy.core.spigot.misc.utils.ItemBuilder
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

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
        this.construct()
    }

    private fun construct() {
        val servers = CoreProvider.Cache.Local.SERVERS.provide().fetchAll()

        val slots = this.SLOTS[if (servers.size > this.SLOTS.size) this.SLOTS.lastIndex else servers.size - 1]

        servers.forEachIndexed { index, server ->
            val slot = slots[index]

            val bukkitSpawnApplication = CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServerAndApplicationType(
                server,
                ApplicationType.SERVER_SPAWN
            )

            this.setItem(
                slot,
                ItemBuilder(Material.TNT)
                    .name("§b${server.displayName}")
                    .lore(
                        arrayOf(
                            "",
                            "§7  Convoque sua facção, construa sua base,",
                            "§7  defenda-se de invasões adversárias",
                            "§7  e realize suas próprias invasões.",
                            "",
                            "§aClique para jogar!"
                        )
                    )
                    .build()
            ) { it ->
                if (bukkitSpawnApplication !== null) {
                    val player = it.whoClicked as Player
                    val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)!!

                    if (CoreConstants.COOLDOWNS.inCooldown(user, "connect-to-server")) return@setItem

                    val bukkitSpawnApplicationStatus = CoreProvider.Cache.Redis.APPLICATIONS_STATUS.provide().fetchApplicationStatusByApplication(
                        bukkitSpawnApplication,
                        ApplicationStatus::class
                    )

                    if (bukkitSpawnApplicationStatus === null) {
                        player.sendMessage(
                            TextComponent("§cO servidor está offline.")
                        )
                        return@setItem
                    }

                    if (CoreProvider.Cache.Local.MAINTENANCE.provide().fetch(bukkitSpawnApplication) == true) {
                        player.sendMessage(
                            TextComponent("§cEste servidor encontra-se em manutenção.")
                        )
                        return@setItem
                    }

                    val packet = ConnectUserToApplicationPacket(
                        user.id,
                        bukkitSpawnApplication
                    )

                    CoreProvider.Databases.Redis.ECHO.provide().publishToApplications(
                        packet,
                        CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByApplicationType(
                            ApplicationType.PROXY
                        )
                    )

                    CoreConstants.COOLDOWNS.start(
                        user,
                        "connect-to-server",
                        TimeUnit.SECONDS.toMillis(5)
                    )
                }
            }
        }
    }

}