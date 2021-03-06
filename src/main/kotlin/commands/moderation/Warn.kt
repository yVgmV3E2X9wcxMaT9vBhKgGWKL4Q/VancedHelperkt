package commands.moderation

import commandhandler.CommandContext
import commands.BaseCommand
import commands.CommandType.Moderation
import ext.*
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse

class Warn : BaseCommand(
    commandName = "warn",
    commandDescription = "Warn a user",
    commandType = Moderation,
    commandArguments = mapOf("User ID | User Mention".required(), "Reason".optional())
) {

    override fun execute(ctx: CommandContext) {
        super.execute(ctx)
        val args = ctx.args
        if (args.isNotEmpty()) {
            val user = args[0]
            val reason = if (args.size > 1) args.apply { remove(user) }.joinToString(" ") else "no reason provided"
            val id = user.filter { it.isDigit() }
            if (id.isEmpty()) {
                useCommandProperly()
                return
            }
            ctx.guild.retrieveMemberById(id).queue({ member ->
                if (!ctx.authorAsMember?.canInteract(member)!!) {
                    sendMessage("You can't warn this member!")
                    return@queue
                }
                member.warn(guildId, reason, channel, embedBuilder)
                sendMessage("Successfully warned ${member.user.asMention}")
            }, ErrorHandler().handle(ErrorResponse.UNKNOWN_USER) {
                sendMessage("Provided user does not exist!")
            })

        } else {
            useArguments(1)
        }
    }

}