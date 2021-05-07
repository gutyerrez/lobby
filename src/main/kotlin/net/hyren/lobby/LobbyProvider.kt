package net.hyren.lobby

import net.hyren.core.shared.providers.cache.local.LocalCacheProvider
import net.hyren.core.shared.providers.cache.redis.RedisCacheProvider
import net.hyren.lobby.misc.queue.cache.redis.QueueRedisCache
import net.hyren.lobby.user.cache.local.LobbyUserLocalCache

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