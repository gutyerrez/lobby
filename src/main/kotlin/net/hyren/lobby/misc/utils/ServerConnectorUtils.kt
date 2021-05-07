package net.hyren.lobby.misc.utils

import net.hyren.core.shared.CoreConstants
import net.hyren.core.shared.CoreProvider
import net.hyren.core.shared.applications.ApplicationType
import net.hyren.core.shared.applications.status.ApplicationStatus
import net.hyren.core.shared.echo.packets.ConnectUserToApplicationPacket
import net.hyren.core.shared.groups.Group
import net.hyren.core.shared.servers.data.Server
import net.hyren.lobby.LobbyProvider
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

/**
 * @author Gutyerrez
 */
object ServerConnectorUtils {

    fun connect(player: Player, server: Server) {
        val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)!!

        if (CoreConstants.COOLDOWNS.inCooldown(user, "connect-to-server")) return

        val bukkitSpawnApplication = CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServerAndApplicationType(
            server,
            ApplicationType.SERVER_SPAWN
        )

        if (bukkitSpawnApplication === null) {
            player.sendMessage(
                TextComponent("§cNão foi possível localizar o spawn desse servidor.")
            )
            return
        }

        val bukkitSpawnApplicationStatus = CoreProvider.Cache.Redis.APPLICATIONS_STATUS.provide().fetchApplicationStatusByApplication(
            bukkitSpawnApplication,
            ApplicationStatus::class
        )

        if (bukkitSpawnApplicationStatus === null) {
            player.sendMessage(
                TextComponent("§cO servidor está offline.")
            )
            return
        }

        if (CoreProvider.Cache.Local.MAINTENANCE.provide().fetch(bukkitSpawnApplication) == true && !user.hasGroup(Group.MANAGER)) {
            player.sendMessage(
                TextComponent("§cEste servidor encontra-se em manutenção.")
            )
            return
        }

        val currentPosition = LobbyProvider.Cache.Redis.QUEUE.provide().fetchByUserId(
            user, bukkitSpawnApplication
        )

        if (user.hasGroup(Group.VIP)) {
            val packet = ConnectUserToApplicationPacket(
                user.id,
                bukkitSpawnApplication
            )

            CoreProvider.Databases.Redis.ECHO.provide().publishToApplicationType(
                packet,
                ApplicationType.PROXY
            )
        } else {
            if (currentPosition === null) {
                val position = LobbyProvider.Cache.Redis.QUEUE.provide().create(user, bukkitSpawnApplication)

                player.sendMessage(
                    ComponentBuilder()
                        .append("\n")
                        .append("§b * §fVocê entrou na posição §b#${position} §fda fila do ${server.displayName}.")
                        .append("\n")
                        .append("§b * §fCaso queira sair clique ")
                        .append("§c§lAQUI")
                        .event(
                            ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/queue_3#@5 leave ${bukkitSpawnApplication.name}"
                            )
                        )
                        .append("§f.")
                        .append("\n")
                        .create()
                )
            } else {
                player.sendMessage(
                    ComponentBuilder()
                        .append("\n")
                        .append("§b * §fVocê está na posição §b#${currentPosition + 1} §fda fila do ${server.displayName}.")
                        .append("\n")
                        .append("§b * §fCaso queira sair clique ")
                        .append("§c§lAQUI")
                        .event(
                            ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/queue_3#@5 leave ${server.name.value}"
                            )
                        )
                        .append("§f.")
                        .append("\n")
                        .create()
                )
            }
        }

        CoreConstants.COOLDOWNS.start(
            user,
            "connect-to-server",
            TimeUnit.SECONDS.toMillis(5)
        )
    }

}