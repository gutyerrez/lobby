package com.redefantasy.lobby

import com.redefantasy.core.spigot.world.WorldCuboid
import org.bukkit.event.player.PlayerInteractEvent
import java.util.function.Consumer

/**
 * @author Gutyerrez
 */
object LobbyConstants {

	const val NPC_METADATA = "hyren-npc"

	val SERVERS_CUBOIDS = mutableMapOf<WorldCuboid, Consumer<PlayerInteractEvent>>()

}