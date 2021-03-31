package com.redefantasy.lobby.misc.slime.jump

import com.redefantasy.core.spigot.CoreSpigotPlugin
import com.redefantasy.lobby.misc.slime.jump.data.SlimeJump
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Material


/**
 * @author Gutyerrez
 */
object SlimeJumpManager {

    private val SLIME_JUMPS = mutableListOf<SlimeJump>()

    init {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            CoreSpigotPlugin.instance,
            {
                SLIME_JUMPS.forEach {
                    it.getLocation()
                        .world
                        .spigot()
                        .playEffect(
                            it.getLocation().clone().add(0.5, 0.0, -0.0),
                            Effect.HAPPY_VILLAGER,
                            1,
                            0,
                            0.5f,
                            0.1f,
                            0.5f,
                            1f,
                            10,
                            7
                        )
                }
            }, 0L, 10L
        )
    }

    fun register(vararg slimeJumps: SlimeJump) {
        slimeJumps.forEach { SLIME_JUMPS.add(it) }
    }

    fun fetchByLocation(location: Location) = SLIME_JUMPS.stream()
        .filter { it.wentOver(location) }
        .findFirst()
        .orElse(null)

    fun setup() {
        SLIME_JUMPS.forEach {
            val location = it.getLocation().clone().subtract(0.0, 1.0, 1.0)
            val block = location.block

            println("Tipo: ${block.type}")

            block.type = Material.SLIME_BLOCK
        }
    }

}