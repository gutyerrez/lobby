package net.hyren.lobby.misc.queue.command

import net.hyren.core.shared.CoreProvider
import net.hyren.core.shared.commands.argument.Argument
import net.hyren.core.shared.commands.restriction.CommandRestriction
import net.hyren.core.shared.users.data.User
import net.hyren.core.spigot.command.CustomCommand
import net.hyren.lobby.LobbyProvider
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