package com.redefantasy.lobby.misc.queue

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.applications.ApplicationType
import com.redefantasy.core.shared.echo.packets.ConnectUserToApplicationPacket
import com.redefantasy.lobby.LobbyProvider

/**
 * @author Gutyerrez
 */
class QueueRunnable : Runnable {

    override fun run() {
        println("Executar runnable")

        CoreProvider.Cache.Local.SERVERS.provide().fetchAll().forEach {
            try {
                println(it)

                val bukkitApplicationSpawn =
                    CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServerAndApplicationType(
                        it,
                        ApplicationType.SERVER_SPAWN
                    )

                if (bukkitApplicationSpawn === null) return@forEach

                println(bukkitApplicationSpawn)

                val userId = LobbyProvider.Cache.Redis.QUEUE.provide().poll(
                    bukkitApplicationSpawn
                )

                if (userId === null) return@forEach

                println(userId)

                val user = CoreProvider.Cache.Local.USERS.provide().fetchById(userId)!!

                if (!user.isOnline()) {
                    println("Offline")

                    LobbyProvider.Cache.Redis.QUEUE.provide().remove(
                        bukkitApplicationSpawn,
                        user
                    )
                } else {
                    println("Online")

                    val maxPlayers = CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByServer(
                        it
                    ).stream().mapToInt { application -> application.slots ?: 0 }.findFirst().asInt

                    val onlinePlayers = CoreProvider.Cache.Redis.USERS_STATUS.provide().fetchUsersByServer(it).size

                    println(maxPlayers)
                    println(onlinePlayers)

                    if (maxPlayers >= onlinePlayers) return@forEach

                    println("Não tá lotado")

                    val packet = ConnectUserToApplicationPacket(
                        user.id,
                        bukkitApplicationSpawn
                    )

                    println("Manda o packet!")

                    CoreProvider.Databases.Redis.ECHO.provide().publishToApplicationType(
                        packet,
                        ApplicationType.PROXY
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}