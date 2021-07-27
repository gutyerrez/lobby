package net.hyren.lobby.misc.server.info

import net.hyren.core.shared.servers.data.Server
import net.hyren.core.spigot.CoreSpigotConstants
import net.hyren.core.spigot.CoreSpigotProvider
import net.hyren.core.spigot.misc.hologram.Hologram
import net.hyren.lobby.LobbyConstants
import net.hyren.lobby.LobbyPlugin
import net.hyren.lobby.misc.utils.ServerConnectorUtils
import net.minecraft.server.v1_8_R3.EntityArmorStand
import net.minecraft.server.v1_8_R3.EntityGiantZombie
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Giant
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

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
	val worldServer = (getNPCLocation().world as CraftWorld).handle

	val customZombie = EntityGiantZombie(worldServer)

	customZombie.setLocation(
		getNPCLocation().x,
		getNPCLocation().y,
		getNPCLocation().z,
		getNPCLocation().yaw,
		getNPCLocation().pitch
	)
	customZombie.setPositionRotation(
		getNPCLocation().x,
		getNPCLocation().y,
		getNPCLocation().z,
		getNPCLocation().yaw,
		getNPCLocation().pitch
	)

	worldServer.addEntity(customZombie, CreatureSpawnEvent.SpawnReason.CUSTOM)

	val giant = CraftEntity.getEntity(worldServer.server, customZombie) as Giant

	giant.setMetadata(LobbyConstants.NPC_METADATA, FixedMetadataValue(
		LobbyPlugin.instance,
		true
	))

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

	entityArmorStand.customNameVisible = false
	entityArmorStand.isInvisible = true

	worldServer.addEntity(entityArmorStand, CreatureSpawnEvent.SpawnReason.CUSTOM)

	val armorStand = entityArmorStand.bukkitEntity

	armorStand.setMetadata(LobbyConstants.NPC_SERVER_METADATA, FixedMetadataValue(
		LobbyPlugin.instance,
		{ event: PlayerInteractAtEntityEvent ->
			val player = event.player

			ServerConnectorUtils.connect(player, this)
		}
	))

	giant.setMetadata("base", FixedMetadataValue(
		LobbyPlugin.instance,
		armorStand
	))

	println("Spawnei, está vivo ainda? ${!giant.isDead}")

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

fun Giant.update(
	server: Server
) {
	println("Está morto? ${!isDead}")

	equipment.itemInHand = CoreSpigotProvider.Cache.Local.SERVER_CONFIGURATION.provide().fetchByServer(
		server
	)?.icon

	teleport(server.getNPCLocation().clone().add(1.9, -8.5, -3.5))

	val armorStand = getMetadata("base")[0].value() as ArmorStand

	armorStand.teleport(server.getNPCLocation())

	Bukkit.getOnlinePlayers().forEach { it.teleport(location) }
}