package net.hyren.lobby.listeners

import net.hyren.core.shared.CoreProvider
import net.hyren.core.shared.groups.Group
import net.hyren.core.shared.misc.preferences.FLY_IN_LOBBY
import net.hyren.core.shared.misc.preferences.PreferenceState
import net.hyren.core.spigot.misc.frame.FrameManager
import net.hyren.core.spigot.misc.utils.Title
import net.hyren.lobby.LobbyConstants
import net.hyren.lobby.LobbyPlugin
import net.hyren.lobby.LobbyProvider
import net.hyren.lobby.misc.button.HotBarManager
import net.hyren.lobby.misc.captcha.inventory.CaptchaInventory
import net.hyren.lobby.misc.scoreboard.ScoreboardManager
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Giant
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInitialSpawnEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.weather.WeatherChangeEvent

/**
 * @author Gutyerrez
 */
class GenericListener : Listener {

    @EventHandler
    fun on(
        event: PlayerJoinEvent
    ) {
        val player = event.player

        Title.clear(player)

        player.maxHealth = 2.0
        player.teleport(
            Location(LobbyPlugin.instance.getDefaultWorld(), 0.5, 75.0, 0.5)
        )

        player.spigot().collidesWithEntities = true

        ScoreboardManager.construct(player)

        CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId).also {
            /**
             * Fly preference
             */

            if (it != null && it.hasGroup(Group.VIP) && it.getPreferences().any { preference ->
                preference == FLY_IN_LOBBY && preference.preferenceState == PreferenceState.ENABLED
            }) {
                player.allowFlight = true
                player.isFlying = true
            }

            /**
             * Captcha
             */

            if (it == null || !it.hasGroup(Group.VIP)) {
                Bukkit.getScheduler().runTaskLater(
                    LobbyPlugin.instance,
                    {
                        player.openInventory(CaptchaInventory())
                    },
                    5L
                )
            }
        }
    }

    @EventHandler
    fun on(
        event: PlayerQuitEvent
    ) {
        val player = event.player

        LobbyProvider.Cache.Local.LOBBY_USERS.provide().remove(player.uniqueId)
    }

    @EventHandler
    fun on(
        event: InventoryClickEvent
    ) {
        if (event.whoClicked !is Player) return

        event.isCancelled = true

        if (event.click === ClickType.NUMBER_KEY) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun on(
        event: EntityDeathEvent
    ) {
        val entity = event.entity

        println("${entity.type} morreu em: ${entity.location.blockX} | ${entity.location.blockY} | ${entity.location.blockZ}")
    }

    @EventHandler
    fun on(
        event: EntityDamageEvent
    ) {
        when (val entity = event.entity) {
            is ItemFrame -> event.isCancelled = true
            is Giant -> event.isCancelled = entity.hasMetadata(LobbyConstants.NPC_METADATA)
            is Player -> {
                event.isCancelled = true

                if (event.cause === EntityDamageEvent.DamageCause.VOID) {
                    entity.teleport(
                        Location(LobbyPlugin.instance.getDefaultWorld(), 0.5, 75.0, 0.5)
                    )
                }
            }
        }
    }

    @EventHandler
    fun on(
        event: EntityDamageByEntityEvent
    ) {
        when (val entity = event.entity) {
            is ItemFrame -> event.isCancelled = true
            is Giant -> event.isCancelled = entity.hasMetadata(LobbyConstants.NPC_METADATA)
        }
    }

    @EventHandler
    fun on(
        event: EntityChangeBlockEvent
    ) {
        val entity = event.entity
        val block = event.block

        if (entity.type == EntityType.FALLING_BLOCK) {
            val blockState = block.state

            blockState.update()

            entity.remove()

            blockState.update()

            event.isCancelled = true

            blockState.update()
        }
    }

    @EventHandler
    fun on(
        event: AsyncPlayerChatEvent
    ) {
        val player = event.player
        val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)

        event.isCancelled = true

        if (user == null || !user.hasGroup(Group.HELPER)) {
            return
        }

        Bukkit.getOnlinePlayers().forEach {
            it.sendMessage(
                ComponentBuilder()
                    .append(user.getHighestGroup().prefix)
                    .append(user.name)
                    .append("§7: ${event.message}")
                    .create()
            )
        }
    }

    @EventHandler
    fun on(
        event: PlayerInitialSpawnEvent
    ) {
        event.spawnLocation = Location(LobbyPlugin.instance.getDefaultWorld(), 0.5, 75.0, 0.5)
    }

    @EventHandler(ignoreCancelled = false)
    fun on(
        event: PlayerInteractEvent
    ) {
        val player = event.player
        val item = player.itemInHand

        val clickedBlock = event.clickedBlock

        event.isCancelled = true

        if (clickedBlock !== null && clickedBlock.type === Material.BARRIER) {
            return LobbyConstants.SERVERS_CUBOIDS.entries.stream().filter {
                it.key.contains(clickedBlock.location, true)
            }.findFirst().orElse(null)?.value?.accept(event) ?: Unit
        }

        if (item !== null && item.type !== Material.AIR) {
            val hotBarButton = HotBarManager.getHotBarButton(item)

            if (hotBarButton !== null) {
                HotBarManager.getEventBus(hotBarButton)?.post(event)
            }
        }
    }

    @EventHandler
    fun on(
        event: FoodLevelChangeEvent
    ) {
        event.foodLevel = 20
    }

    @EventHandler
    fun on(
        event: WeatherChangeEvent
    ) {
        if (event.toWeatherState()) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun on(
        event: PlayerInteractEntityEvent
    ) {
        val player = event.player
        val entity = event.rightClicked

        event.isCancelled = true

        if (entity is ItemFrame) {
            val frame = FrameManager.INTERACTABLE_FRAMES[entity.uniqueId]

            frame?.interactConsumer?.accept(player)
        }
    }

    @EventHandler
    fun on(
        event: PlayerInteractAtEntityEvent
    ) {
        val entity = event.rightClicked

        event.isCancelled = true

        if (entity is ArmorStand && entity.hasMetadata(LobbyConstants.NPC_SERVER_METADATA)) {
            try {
                val callback = entity.getMetadata(
                    LobbyConstants.NPC_SERVER_METADATA
                )[0].value() as (PlayerInteractAtEntityEvent) -> Unit

                callback(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @EventHandler
    fun on(
        event: BlockBreakEvent
    ) {
        event.isCancelled = true
    }

    @EventHandler
    fun on(
        event: BlockPlaceEvent
    ) {
        event.isCancelled = true
    }

    @EventHandler
    fun on(
        event: BlockPhysicsEvent
    ) {
        event.isCancelled = true
    }

    @EventHandler
    fun on(
        event: EntityCombustEvent
    ) {
        event.isCancelled = true
    }

    @EventHandler
    fun on(
        event: BlockIgniteEvent
    ) {
        event.isCancelled = true
    }

    @EventHandler
    fun on(
        event: BlockFromToEvent
    ) {
        event.isCancelled = true
    }

    @EventHandler
    fun on(
        event: BlockFadeEvent
    ) {
        event.isCancelled = true
    }

    @EventHandler
    fun on(
        event: PlayerDropItemEvent
    ) {
        event.isCancelled = true
    }

}