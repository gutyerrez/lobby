package net.hyren.lobby.misc.server.utils

import net.hyren.core.shared.servers.data.Server
import net.hyren.core.spigot.misc.hologram.Hologram
import net.hyren.lobby.misc.server.npc.createWall
import net.hyren.lobby.misc.server.npc.spawnHologram
import net.hyren.lobby.misc.server.npc.spawnNPC
import org.bukkit.entity.Giant

/**
 * @author Gutyerrez
 */
object ServerConfigurationUtils {

	fun initServer(
		server: Server,
		npcsMap: MutableMap<Server, Giant>,
		hologramsMap: MutableMap<Server, Hologram>
	) {
		server.createWall()

		npcsMap[server] = server.spawnNPC()

		hologramsMap[server] = server.spawnHologram()
	}

}