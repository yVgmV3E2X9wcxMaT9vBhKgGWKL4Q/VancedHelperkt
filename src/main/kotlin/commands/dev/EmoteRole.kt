package commands.dev

import commandhandler.CommandContext
import commands.BaseCommand
import commands.CommandType.Dev
import database.collections.EmoteRole
import database.emoteRolesCollection
import database.getEmoteRoles
import database.updateEmoteRoles
import ext.required
import ext.useArguments
import ext.useCommandProperly

class EmoteRole : BaseCommand(
    commandName = "emoterole",
    commandDescription = "Set a reaction detection to a message and add roles to members automatically",
    commandType = Dev,
    commandArguments = mapOf("message ID".required(), "emote".required(), "role ID".required())
) {

    override fun execute(ctx: CommandContext) {
        super.execute(ctx)
        val args = ctx.args
        if (args.isNotEmpty() && args.size == 3) {
            val messageId = contentIDRegex.find(args[0])?.value
            val emote = emoteRegex.find(args[1])?.value
            val emoteId = contentIDRegex.find(args[1])?.value
            val roleId = contentIDRegex.find(args[2])?.value
            if (messageId != null && emote != null && roleId != null) {
                if (guildId.getEmoteRoles(messageId, emote) == null) {
                    emoteRolesCollection.insertOne(
                        EmoteRole(
                            guildId = guildId,
                            messageId = messageId,
                            emote = emote,
                            roleId = roleId
                        )
                    )
                    ctx.guild.getTextChannelById(channel.id)?.retrieveMessageById(messageId)?.queue {
                        if (emoteId != null) {
                            val guildEmote = ctx.guild.getEmoteById(emoteId)
                            if (guildEmote != null) {
                                it.addReaction(guildEmote).queue()
                            }
                        }
                    }
                    sendMessage("Successfully configured emote role!")
                } else {
                    guildId.updateEmoteRoles(messageId, emote, roleId)
                    sendMessage("Successfully reconfigured emote role!")
                }
            } else {
                useCommandProperly()
            }
        } else {
            useArguments(3)
        }
    }

}