package com.redefantasy.lobby.misc.scoreboard

import com.redefantasy.core.spigot.misc.scoreboard.bukkit.GroupScoreboard
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team

/**
 * @author Gutyerrez
 */
class LobbyScoreboard : GroupScoreboard {

    constructor(): super()

    constructor(player: Player) : super(player)

    val ghostTeam: Team = run {
        var team = scoreboard.getTeam("team-ghost")

        if (team === null) {
            team = scoreboard.registerNewTeam("team-ghost")
            team.setCanSeeFriendlyInvisibles(false)
        }

        team
    }

}