package com.redefantasy.lobby.misc.queue

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.applications.ApplicationType
import com.redefantasy.core.shared.echo.packets.ConnectUserToApplicationPacket
import com.redefantasy.core.shared.groups.Group
import com.redefantasy.lobby.LobbyProvider

/**
 * @author Gutyerrez
 */
class QueueRunnable : Runnable {

    override fun run() {
        CoreProvider.Cache.Local.SERVERS.provide().fetchAll().forEach {
            val bukkitApplicationSpawn = CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServerAndApplicationType(
                it,
                ApplicationType.SERVER_SPAWN
            )

            if (bukkitApplicationSpawn === null) return@forEach

            val userId = LobbyProvider.Cache.Redis.QUEUE.provide().poll(
                bukkitApplicationSpawn
            )

            if (userId === null) return@forEach

            val user = CoreProvider.Cache.Local.USERS.provide().fetchById(userId)!!

            if (!user.isOnline()) {
                LobbyProvider.Cache.Redis.QUEUE.provide().remove(
                    bukkitApplicationSpawn,
                    user
                )
            } else {
                val maxPlayers = CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServer(
                    it
                ).stream().mapToInt { application -> application.slots ?: 0 }.findFirst().asInt

                val onlinePlayers = CoreProvider.Cache.Redis.USERS_STATUS.provide().fetchUsersByServer(it).size

                if (onlinePlayers >= maxPlayers && !user.hasGroup(Group.VIP)) return@forEach

                val packet = ConnectUserToApplicationPacket(
                    user.id,
                    bukkitApplicationSpawn
                )

                CoreProvider.Databases.Redis.ECHO.provide().publishToApplicationType(
                    packet,
                    ApplicationType.PROXY
                )
                
                LobbyProvider.Cache.Redis.QUEUE.provide().remove(
                    bukkitApplicationSpawn,
                    user
                )
            }
        }
    }

}