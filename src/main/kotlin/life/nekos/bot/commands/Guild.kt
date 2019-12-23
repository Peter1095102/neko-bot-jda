package life.nekos.bot.commands

import life.nekos.bot.framework.annotations.DonorOnly
import life.nekos.bot.utils.Colors
import life.nekos.bot.utils.Formats
import me.devoxin.flight.annotations.Command
import me.devoxin.flight.api.Context
import me.devoxin.flight.models.Cog
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.util.concurrent.CompletableFuture
import kotlin.streams.toList

class Guild : Cog {

    @Command(aliases = ["nsfw", "toggle"], description = "Toggles the current channel's NSFW setting", guildOnly = true,
        botPermissions = [Permission.MANAGE_CHANNEL], userPermissions = [Permission.MANAGE_CHANNEL])
    fun nsfwtoggle(ctx: Context) {
        val tc = ctx.textChannel!!
        val newSetting = !tc.isNSFW
        tc.manager.setNSFW(newSetting).queue {
            val str = if (newSetting) "enabled" else "disabled"
            ctx.send("Nya, I have $str NSFW on this channel! ${Formats.randomCat()}")
        }
    }

    @DonorOnly
    @Command(aliases = ["rc"], description = "Changes all hoisted roles to a random color", guildOnly = true,
        botPermissions = [Permission.MANAGE_ROLES], userPermissions = [Permission.MANAGE_ROLES])
    suspend fun recolor(ctx: Context) {
        ctx.sendAsync("This command will change the color of all hoisted roles to something random! Do you want to continue? (yes/no)")
        ctx.waitFor(
            GuildMessageReceivedEvent::class.java,
            { it.author.idLong == ctx.author.idLong && it.message.contentRaw.toLowerCase() == "yes" }, 60000
        ) ?: return

        val modifiableRoles = ctx.guild!!.roleCache.stream()
            .filter(Role::isHoisted)
            .filter { ctx.guild!!.selfMember.canInteract(it) }
            .toList()

        val msg = ctx.sendAsync("This may take a while, nya~")
        val tasks = modifiableRoles.map { it.manager.setColor(Colors.getRandomColor()).submit() }.toTypedArray()

        CompletableFuture.allOf(*tasks).handle { _, _ ->
            val updated = tasks.filter { !it.isCompletedExceptionally }.size
            msg.editMessage("Done nya! I have set the color of $updated/${tasks.size} roles \\o/").queue()
        }
    }

}
