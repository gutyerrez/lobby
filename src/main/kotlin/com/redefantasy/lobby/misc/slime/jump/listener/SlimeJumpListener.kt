package com.redefantasy.lobby.misc.slime.jump.listener

import com.redefantasy.lobby.misc.slime.jump.SlimeJumpManager
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

/**
 * @author Gutyerrez
 */
class SlimeJumpListener : Listener {

    @EventHandler
    fun on(
        event: PlayerMoveEvent
    ) {
        val player = event.player
        val fromLocation = event.from
        val toLocation = event.to

        if (toLocation.x == fromLocation.x && toLocation.y == fromLocation.y && toLocation.z == fromLocation.z) return

        val slimeJump = SlimeJumpManager.fetchByLocation(toLocation)

        if (slimeJump !== null) {
            val vector = slimeJump.toVector

            player.velocity = vector

            player.playSound(player.location, Sound.FIREWORK_LAUNCH, 1f, 2f)
        }
    }

}