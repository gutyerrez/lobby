package net.hyren.lobby.misc.server.info

import net.hyren.core.shared.servers.data.Server
import net.hyren.core.spigot.*
import net.hyren.core.spigot.misc.hologram.Hologram
import net.hyren.lobby.*
import net.hyren.lobby.misc.utils.ServerConnectorUtils
import net.minecraft.server.v1_8_R3.*
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.entity.Giant
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.*

/**
 * @author Gutyerrez
 */
fun Server.getNPCLocation(): Location {
	val serverConfiguration = CoreSpigotProvider.Cache.Local.SERVER_CONFIGURATION.provide().fetchByServer(
		this
	) ?: throw NullPointerException("server configuration cannot be null")

	return CoreSpigotConstants.BUKKIT_LOCATION_PARSER.apply(serverConfiguration.settings.npcLocation)
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

	val giant = customZombie.bukkitEntity as Giant

	giant.setMetadata(LobbyConstants.NPC_METADATA, FixedMetadataValue(
		LobbyPlugin.instance,
		true
	))

	giant.maxHealth = 2048.0
	giant.health = 2048.0

	giant.setMetadata(
		LobbyConstants.NPC_METADATA,
		FixedMetadataValue(
			LobbyPlugin.instance,
			true
		)
	)

	giant.addPotionEffect(
		PotionEffect(
			PotionEffectType.INVISIBILITY,
			Int.MAX_VALUE,
			1
		),
		true
	)
	giant.removeWhenFarAway = false
	giant.equipment.itemInHand = CoreSpigotProvider.Cache.Local.SERVER_CONFIGURATION.provide().fetchByServer(
		this
	)?.icon

	giant.teleport(getNPCLocation().clone().add(1.9, -8.5, -3.5))

	val entityArmorStand = EntityArmorStand(worldServer, getNPCLocation().x, getNPCLocation().y, getNPCLocation().z)

	entityArmorStand.isSmall = false
	entityArmorStand.customNameVisible = true
	entityArmorStand.isInvisible = true
	entityArmorStand.noclip = true

	entityArmorStand.setArms(false)
	entityArmorStand.setGravity(false)
	entityArmorStand.setBasePlate(false)

	entityArmorStand.n(true)

	worldServer.addEntity(entityArmorStand, CreatureSpawnEvent.SpawnReason.CUSTOM)

	val armorStand = entityArmorStand.bukkitEntity

	armorStand.setMetadata(LobbyConstants.NPC_SERVER_METADATA, FixedMetadataValue(
		LobbyPlugin.instance,
		{ event: PlayerInteractAtEntityEvent ->
			val player = event.player

			ServerConnectorUtils.connect(player, this)
		}
	))

	return giant
}

fun Server.spawnHologram(): Hologram {
	val hologram = Hologram(
		listOf(
			"§e${displayName}",
			"?",
			"§eClique para entrar!"
		),
		Hologram.HologramPosition.DOWN
	)

	val cloned = getNPCLocation().clone().add(0.0, 3.5, 0.0)

	hologram.spawn(cloned)

	return hologram
}

/*fun Server.createWall() {
	val serverConfiguration = CoreSpigotProvider.Cache.Local.SERVER_CONFIGURATION.provide().fetchByServer(
		this
	) ?: throw NullPointerException("npc location for server $displayName cannot be null")

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
}*/

fun Giant.update(
	server: Server
) {
	this.health = 2048.0

	this.equipment.itemInHand = CoreSpigotProvider.Cache.Local.SERVER_CONFIGURATION.provide().fetchByServer(
		server
	)?.icon

	this.teleport(server.getNPCLocation().clone().add(1.9, -8.5, -3.5))
}