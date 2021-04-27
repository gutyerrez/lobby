package com.redefantasy.lobby.misc.server.utils

import com.redefantasy.core.shared.servers.data.Server
import com.redefantasy.core.spigot.misc.hologram.Hologram
import com.redefantasy.lobby.misc.server.npc.createWall
import com.redefantasy.lobby.misc.server.npc.getNPCLocation
import com.redefantasy.lobby.misc.server.npc.spawnNPC
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

		val hologram = Hologram(
			listOf(
				"§e${server.displayName}",
				"?",
				"§eClique para entrar!"
			),
			Hologram.HologramPosition.DOWN
		)

		hologram.spawn(
			server.getNPCLocation().clone().add(0.0, 3.5, 0.0)
		)

		hologramsMap[server] = hologram
	}

}