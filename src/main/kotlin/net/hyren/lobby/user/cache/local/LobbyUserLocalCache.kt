package net.hyren.lobby.user.cache.local

import com.github.benmanes.caffeine.cache.Caffeine
import net.hyren.core.shared.cache.local.LocalCache
import net.hyren.core.shared.users.storage.table.UsersTable
import net.hyren.lobby.user.data.LobbyUser
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

/**
 * @author Gutyerrez
 */
class LobbyUserLocalCache : LocalCache {

    private val CACHE_BY_ID = Caffeine.newBuilder()
        .build<EntityID<UUID>, LobbyUser>()

    fun fetchById(userId: EntityID<UUID>) = this.CACHE_BY_ID.getIfPresent(userId)

    fun fetchById(userId: UUID) = this.CACHE_BY_ID.getIfPresent(
        EntityID(
            userId,
            UsersTable
        )
    )

    fun put(lobbyUser: LobbyUser) {
        this.CACHE_BY_ID.put(lobbyUser.id, lobbyUser)
    }

    fun remove(userId: EntityID<UUID>) = this.CACHE_BY_ID.invalidate(userId)

    fun remove(userId: UUID) = this.CACHE_BY_ID.invalidate(
        EntityID(
            userId,
            UsersTable
        )
    )

}