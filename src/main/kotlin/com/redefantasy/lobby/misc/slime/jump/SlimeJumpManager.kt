package com.redefantasy.lobby.misc.slime.jump

import com.redefantasy.core.spigot.CoreSpigotPlugin
import com.redefantasy.lobby.misc.slime.jump.data.SlimeJump
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld


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
                            it.getLocation(),
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
            val location = it.getLocation()
            val world = location.world as CraftWorld

            val block = world.getBlockAt(
                it.x.toInt(),
                it.y.toInt(),
                it.z.toInt()
            )

            block.type = Material.SLIME_BLOCK
        }
    }

}