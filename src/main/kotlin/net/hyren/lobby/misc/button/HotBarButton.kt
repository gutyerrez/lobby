package net.hyren.lobby.misc.button

import org.bukkit.inventory.ItemStack

/**
 * @author Gutyerrez
 */
abstract class HotBarButton(
    val icon: ItemStack,
    val slot: Int
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is HotBarButton) return false

        if (icon != other.icon) return false

        if (slot != other.slot) return false

        return true
    }

    override fun hashCode(): Int {
        var result = icon.hashCode()

        result = 31 * result + slot

        return result
    }

}