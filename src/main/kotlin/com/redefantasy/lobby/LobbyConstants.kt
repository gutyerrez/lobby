package com.redefantasy.lobby

import com.redefantasy.core.spigot.world.WorldCuboid
import org.bukkit.event.player.PlayerInteractEvent
import java.util.function.Function

/**
 * @author Gutyerrez
 */
object LobbyConstants {

    val SERVERS_NPC_CUBOIDS = mapOf(
        Pair(
            "FACTIONS_PHOENIX", WorldCuboid(
                -2,
                91,
                -71,
                2,
                95,
                -75
            )
        )
    )

    val SERVER_CUBOID = Function<PlayerInteractEvent, Unit> {
        try {
            val player = it.player
            val clickedBlock = it.clickedBlock

            println(clickedBlock)

            val cuboid = this.SERVERS_NPC_CUBOIDS.values.stream().filter { worldCuboid ->
                worldCuboid.contains(
                    clickedBlock.x,
                    clickedBlock.y,
                    clickedBlock.z
                )
            }.findFirst().orElse(null)

            if (cuboid !== null) {
                player.sendMessage("Opa!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}