package com.redefantasy.lobby

import com.redefantasy.core.shared.providers.cache.local.LocalCacheProvider
import com.redefantasy.lobby.user.cache.local.LobbyUserLocalCache

/**
 * @author Gutyerrez
 */
object LobbyProvider {

    fun prepare() {
        Cache.Local.LOBBY_USERS.prepare()
    }

    object Cache {

        object Local {

            val LOBBY_USERS = LocalCacheProvider(
                LobbyUserLocalCache()
            )

        }

    }

}