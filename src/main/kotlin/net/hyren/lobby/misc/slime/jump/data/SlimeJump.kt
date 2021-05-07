package net.hyren.lobby.misc.slime.jump.data

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.util.Vector

/**
 * @author Gutyerrez
 */
data class SlimeJump(
    val x: Double,
    val y: Double,
    val z: Double,
    val toVector: Vector
) {

    fun wentOver(location: Location) = this.wentOver(
        location.x,
        location.y,
        location.z
    )

    fun wentOver(
        x: Double,
        y: Double,
        z: Double
    ) = this.x.toInt() == x.toInt() && this.y.toInt() == y.toInt() && this.z.toInt() == z.toInt()

    fun getLocation() = Location(
        Bukkit.getWorld("world"),
        this.x,
        this.y,
        this.z
    )

}