package com.redefantasy.lobby.misc.queue.cache.redis

import com.redefantasy.core.shared.CoreConstants
import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.applications.data.Application
import com.redefantasy.core.shared.cache.redis.RedisCache
import com.redefantasy.core.shared.users.data.User
import org.joda.time.DateTime
import java.util.*
import java.util.function.Function

/**
 * @author Gutyerrez
 */
class QueueRedisCache : RedisCache {

    private val key = Function<Application, String> { "queue:${it.name}" }

    fun poll(
        targetApplication: Application
    ): UUID? {
        return CoreProvider.Databases.Redis.REDIS_MAIN.provide().resource.use {
            val pipeline = it.pipelined()
            val key = this.key.apply(targetApplication)

            val uuid = pipeline.zrange(
                key,
                0,
                1
            ).get().stream().map { stringified -> UUID.fromString(stringified) }.findFirst().orElse(null)
            pipeline.sync()

            return@use uuid
        }
    }

    fun create(
        user: User,
        targetApplication: Application
    ): Int {
        return CoreProvider.Databases.Redis.REDIS_MAIN.provide().resource.use {
            val pipeline = it.pipelined()
            val key = this.key.apply(targetApplication)

            val position = pipeline.zadd(
                key,
                DateTime.now(
                    CoreConstants.DATE_TIME_ZONE
                ).millis.toDouble(),
                user.getUniqueId().toString()
            )
            pipeline.sync()

            return@use position.get().toInt()
        }
    }

    fun fetchByUserId(
        user: User,
        targetApplication: Application
    ): Int {
        return CoreProvider.Databases.Redis.REDIS_MAIN.provide().resource.use {
            val pipeline = it.pipelined()
            val key = this.key.apply(targetApplication)

            val position = pipeline.zrank(
                key,
                user.getUniqueId().toString()
            ).get().toInt()
            pipeline.sync()

            return@use position
        }
    }

    fun remove(
        targetApplication: Application,
        user: User
    ) {
        CoreProvider.Databases.Redis.REDIS_MAIN.provide().resource.use {
            val pipeline = it.pipelined()
            val key = this.key.apply(targetApplication)

            pipeline.zrem(key, user.getUniqueId().toString())
            pipeline.sync()
        }
    }

}