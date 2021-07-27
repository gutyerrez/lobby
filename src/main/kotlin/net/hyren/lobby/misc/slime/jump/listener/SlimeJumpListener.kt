package net.hyren.lobby.misc.slime.jump.listener

import net.hyren.lobby.misc.slime.jump.SlimeJumpManager
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

        if (toLocation.x == fromLocation.x && toLocation.y == fromLocation.y && toLocation.z == fromLocation.z) {
            return
        }

        val slimeJump = SlimeJumpManager.fetchByLocation(toLocation)

        val subtract = if (player.isSprinting) {
            0.05
        } else {
            0.0
        }

        if (slimeJump != null) {
            val vector = slimeJump.toVector

            vector.x -= subtract
            vector.y -= subtract
            vector.z -= subtract

            player.velocity = vector

            player.playSound(player.location, Sound.FIREWORK_LAUNCH, 1f, 2f)
        }
    }

}