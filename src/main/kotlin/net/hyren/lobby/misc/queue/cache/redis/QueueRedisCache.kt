package net.hyren.lobby.misc.queue.cache.redis

import net.hyren.core.shared.CoreConstants
import net.hyren.core.shared.CoreProvider
import net.hyren.core.shared.applications.data.Application
import net.hyren.core.shared.cache.redis.RedisCache
import net.hyren.core.shared.users.data.User
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
            val key = this.key.apply(targetApplication)

            return@use it.zrange(
                key,
                0,
                1
            ).stream().map { stringified -> UUID.fromString(stringified) }.findFirst().orElse(null)
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
    ): Int? {
        return CoreProvider.Databases.Redis.REDIS_MAIN.provide().resource.use {
            val key = this.key.apply(targetApplication)

            if (!it.hexists(key, user.getUniqueId().toString())) return@use null

            return@use it.zrank(
                key,
                user.getUniqueId().toString()
            ).toInt()
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