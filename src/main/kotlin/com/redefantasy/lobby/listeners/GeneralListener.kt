package com.redefantasy.lobby.listeners

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.groups.Group
import com.redefantasy.core.shared.misc.preferences.FLY_IN_LOBBY
import com.redefantasy.core.shared.misc.preferences.PLAYER_VISIBILITY
import com.redefantasy.core.shared.misc.preferences.PreferenceState
import com.redefantasy.core.spigot.misc.utils.Title
import com.redefantasy.lobby.LobbyProvider
import com.redefantasy.lobby.misc.button.HotBarManager
import com.redefantasy.lobby.misc.preferences.post
import com.redefantasy.lobby.misc.scoreboard.ScoreboardManager
import com.redefantasy.lobby.user.data.LobbyUser
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.event.weather.WeatherChangeEvent

/**
 * @author Gutyerrez
 */
class GeneralListener : Listener {

    @EventHandler
    fun on(
        event: PlayerJoinEvent
    ) {
        val player = event.player

        Title.clear(player)

        player.maxHealth = 2.0

        player.spigot().collidesWithEntities = false

        val user = CoreProvider.Cache.Local.USERS.provide().fetchById(player.uniqueId)!!

        LobbyProvider.Cache.Local.LOBBY_USERS.provide().put(
            LobbyUser(user)
        )

        if (user.hasGroup(Group.VIP) && user.getPreferences()
                .find { it == FLY_IN_LOBBY }?.preferenceState === PreferenceState.ENABLED
        ) {
            player.allowFlight = true
            player.isFlying = true
        }

        ScoreboardManager.construct(player)
        HotBarManager.giveToPlayer(player)

        user.getPreferences().find { it == PLAYER_VISIBILITY }?.post(user)
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

        if (event.click === ClickType.NUMBER_KEY)
            event.isCancelled = true
    }

    @EventHandler
    fun on(
        event: EntityDamageEvent
    ) {
        if (event.entity !is Player) {
            println("Vixi!")
            return
        }

        event.isCancelled = true

        if (event.cause === EntityDamageEvent.DamageCause.VOID && event.entity is Player) {
            val player = event.entity

            val world = Bukkit.getWorld("world")

            player.teleport(world.spawnLocation)
        }

        println("opa")
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

        if (user === null || !user.hasGroup(Group.HELPER)) {
            return
        }

        Bukkit.getOnlinePlayers().forEach {
            it.sendMessage(
                ComponentBuilder()
                    .append(user.getHighestGroup().getColoredPrefix())
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
        val world = Bukkit.getWorld("world")

        event.spawnLocation = Location(
            world,
            0.5,
            78.0,
            -0.5,
            180F,
            0F
        )
    }

    @EventHandler
    fun on(
        event: PlayerInteractEvent
    ) {
        val player = event.player
        val item = player.itemInHand

        event.isCancelled = true

        if (item !== null) {
            val hotBarButton = HotBarManager.getHotBarButton(item)

            if (hotBarButton !== null) {
                HotBarManager.getEventBus(hotBarButton)?.post(event)
            }
        }
    }

    @EventHandler
    fun on(
        event: WeatherChangeEvent
    ) {
        if (event.toWeatherState())
            event.isCancelled = true
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
        event: PlayerInteractAtEntityEvent
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
        event: EntityDamageByEntityEvent
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