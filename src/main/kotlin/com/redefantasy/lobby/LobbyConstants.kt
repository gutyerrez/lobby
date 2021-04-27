package com.redefantasy.lobby

import com.redefantasy.core.spigot.world.WorldCuboid
import org.bukkit.event.player.PlayerInteractEvent
import java.util.function.Consumer

/**
 * @author Gutyerrez
 */
object LobbyConstants {

	val SERVERS_CUBOIDS = mutableMapOf<WorldCuboid, Consumer<PlayerInteractEvent>>()

}