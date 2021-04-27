package com.redefantasy.lobby.misc.server.npc

import com.redefantasy.core.shared.servers.data.Server
import com.redefantasy.core.spigot.CoreSpigotProvider
import com.redefantasy.core.spigot.world.WorldCuboid
import com.redefantasy.lobby.LobbyConstants
import com.redefantasy.lobby.LobbyPlugin
import com.redefantasy.lobby.misc.utils.ServerConnectorUtils
import net.minecraft.server.v1_8_R3.EntityGiantZombie
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.entity.Giant
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.function.Consumer

/**
 * @author Gutyerrez
 */
fun Server.getNPCLocation(): Location {
	val serverConfiguration = CoreSpigotProvider.Cache.Local.SERVER_CONFIGURATION.provide().fetchByServer(
		this
	) ?: throw NullPointerException("server configuration cannot be null")

	return Location(
		Bukkit.getWorld(
			serverConfiguration.settings.npcLocation.worldName
		),
		serverConfiguration.settings.npcLocation.x,
		serverConfiguration.settings.npcLocation.y,
		serverConfiguration.settings.npcLocation.z,
		serverConfiguration.settings.npcLocation.yaw,
		serverConfiguration.settings.npcLocation.pitch
	)
}

fun Server.spawnNPC(): Giant {
	val worldServer = (this.getNPCLocation().world as CraftWorld).handle

	val customZombie = EntityGiantZombie(worldServer)

	customZombie.setLocation(
		this.getNPCLocation().x,
		this.getNPCLocation().y,
		this.getNPCLocation().z,
		this.getNPCLocation().yaw,
		this.getNPCLocation().pitch
	)
	customZombie.setPositionRotation(
		this.getNPCLocation().x,
		this.getNPCLocation().y,
		this.getNPCLocation().z,
		this.getNPCLocation().yaw,
		this.getNPCLocation().pitch
	)

	worldServer.addEntity(customZombie, CreatureSpawnEvent.SpawnReason.CUSTOM)

	val npc = customZombie.bukkitEntity as Giant

	npc.maxHealth = 2048.0
	npc.health = 2048.0

	npc.setMetadata(
		LobbyConstants.NPC_METADATA,
		FixedMetadataValue(
			LobbyPlugin.instance,
			true
		)
	)

	npc.addPotionEffect(
		PotionEffect(
			PotionEffectType.INVISIBILITY,
			Int.MAX_VALUE,
			1
		),
		true
	)
	npc.removeWhenFarAway = false
	npc.equipment.itemInHand = CoreSpigotProvider.Cache.Local.SERVER_CONFIGURATION.provide().fetchByServer(
		this
	)?.icon

	npc.teleport(this.getNPCLocation().clone().add(1.9, -8.5, -3.5))

	return npc
}

fun Server.createWall() {
	val serverConfiguration = CoreSpigotProvider.Cache.Local.SERVER_CONFIGURATION.provide().fetchByServer(
		this
	) ?: throw NullPointerException("npc location cannot be null")

	val worldCuboid = WorldCuboid(
		serverConfiguration.settings.npcLocation.x.toInt() - 3,
		serverConfiguration.settings.npcLocation.y.toInt() + 0,
		serverConfiguration.settings.npcLocation.z.toInt() - 3,
		serverConfiguration.settings.npcLocation.x.toInt() + 2,
		serverConfiguration.settings.npcLocation.y.toInt() + 3,
		serverConfiguration.settings.npcLocation.z.toInt() + 1
	)

	LobbyConstants.SERVERS_CUBOIDS[worldCuboid] = Consumer {
		val player = it.player

		ServerConnectorUtils.connect(
			player,
			this
		)
	}

	worldCuboid.getBlocks {
		it.type = Material.BARRIER
	}
}

fun Giant.update(
	server: Server
) {
	this.health = 2048.0

	println(this.isDead)

	this.equipment.itemInHand = CoreSpigotProvider.Cache.Local.SERVER_CONFIGURATION.provide().fetchByServer(
		server
	)?.icon

	this.teleport(server.getNPCLocation().clone().add(1.9, -8.5, -3.5))
}