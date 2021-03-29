package com.redefantasy.lobby

import com.redefantasy.core.shared.providers.cache.local.LocalCacheProvider
import com.redefantasy.core.shared.providers.cache.redis.RedisCacheProvider
import com.redefantasy.lobby.misc.queue.cache.redis.QueueRedisCache
import com.redefantasy.lobby.user.cache.local.LobbyUserLocalCache

/**
 * @author Gutyerrez
 */
object LobbyProvider {

    fun prepare() {
        Cache.Local.LOBBY_USERS.prepare()

        Cache.Redis.QUEUE.prepare()
    }

    object Cache {

        object Local {

            val LOBBY_USERS = LocalCacheProvider(
                LobbyUserLocalCache()
            )

        }

        object Redis {

            val QUEUE = RedisCacheProvider(
                QueueRedisCache()
            )

        }

    }

}