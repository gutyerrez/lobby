package com.redefantasy.lobby.misc.npc.entity

import net.minecraft.server.v1_8_R3.*

/**
 * @author Gutyerrez
 */
class CustomZombie(
    world: World
) : EntityGiantZombie(world) {

    override fun damageEntity(damagesource: DamageSource?, f: Float): Boolean {
        return false
    }

    override fun collide(entity: Entity?) {}

    override fun a(d0: Double, flag: Boolean, block: Block?, blockposition: BlockPosition?) {}

    override fun a(blockposition: BlockPosition?, block: Block?) {}

    override fun onLightningStrike(entitylightning: EntityLightning?) {}

    override fun move(x: Double, y: Double, z: Double) {}

    override fun d(damagesource: DamageSource?, f: Float): Boolean {
        return false
    }

    override fun burn(i: Float) {}

    override fun burnFromLava() {}

    override fun makeSound(s: String?, f: Float, f1: Float) {}

}