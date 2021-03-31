package com.redefantasy.lobby.misc.slime.jump

import com.redefantasy.lobby.misc.slime.jump.data.SlimeJump
import org.bukkit.Location

/**
 * @author Gutyerrez
 */
object SlimeJumpManager {

    private val SLIME_JUMPS = mutableListOf<SlimeJump>()

    fun register(vararg slimeJumps: SlimeJump) {
        slimeJumps.forEach { SLIME_JUMPS.add(it) }
    }

    fun fetchByLocation(location: Location) = SLIME_JUMPS.stream()
        .filter { it.wentOver(location) }
        .findFirst()
        .orElse(null)

}