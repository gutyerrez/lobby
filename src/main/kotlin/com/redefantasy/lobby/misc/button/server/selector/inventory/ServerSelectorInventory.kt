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

    init {
        this.construct()
    }

    private fun construct() {
        val factionsOmegaBukkitSpawnApplication = CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServerAndApplicationType(
            CoreProvider.Cache.Local.SERVERS.provide().fetchByName("FACTIONS_OMEGA")!!,
            ApplicationType.SERVER_SPAWN
        )

        this.setItem(
            13,
            ItemBuilder(Material.TNT)
                .name("§bFactions Ômega")
                .lore(
                    arrayOf(
                        "",
                        "§7  Convoque sua facção, consrtua sua base,",
                        "§7  defenda-se de invasões adversárias",
                        "§7  e realize suas próprias invasões.",
                        "",
                        "§aClique para jogar!"
                    )
                )
                .build()
        ) { it ->
            if (factionsOmegaBukkitSpawnApplication !== null) {
                val player = it.whoClicked as Player
                val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)!!

                if (CoreConstants.COOLDOWNS.inCooldown(user, "connect-to-server")) return@setItem

                val factionsOmegaBukkitSpawnApplicationStatus = CoreProvider.Cache.Redis.APPLICATIONS_STATUS.provide().fetchApplicationStatusByApplication(
                    factionsOmegaBukkitSpawnApplication,
                    ApplicationStatus::class
                )

                if (factionsOmegaBukkitSpawnApplicationStatus === null) {
                    player.sendMessage(
                        TextComponent("§cO servidor está offline.")
                    )
                    return@setItem
                }

                if (CoreProvider.Cache.Local.MAINTENANCE.provide().fetch(factionsOmegaBukkitSpawnApplication) == true) {
                    player.sendMessage(
                        TextComponent("§cEste servidor encontra-se em manutenção.")
                    )
                    return@setItem
                }

                val packet = ConnectUserToApplicationPacket(
                    user.id,
                    factionsOmegaBukkitSpawnApplication
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