package com.redefantasy.lobby.misc.queue.command

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.commands.argument.Argument
import com.redefantasy.core.shared.commands.restriction.CommandRestriction
import com.redefantasy.core.shared.users.data.User
import com.redefantasy.core.spigot.command.CustomCommand
import com.redefantasy.lobby.LobbyProvider
import org.bukkit.command.CommandSender

/**
 * @author Gutyerrez
 */
class QueueCommand : CustomCommand("queue_3#@5") {

    override fun getCommandRestriction() = CommandRestriction.GAME

    override fun getArguments() = listOf(
        Argument("leave"),
        Argument("application")
    )

    override fun onCommand(
        commandSender: CommandSender,
        user: User?,
        args: Array<out String>
    ): Boolean {
        val bukkitApplicationSpawn = CoreProvider.Cache.Local.APPLICATIONS.provide().fetchByName(
            args[1]
        )

        if (bukkitApplicationSpawn === null) return false

        LobbyProvider.Cache.Redis.QUEUE.provide().remove(
            bukkitApplicationSpawn,
            user!!
        )
        return true
    }

}