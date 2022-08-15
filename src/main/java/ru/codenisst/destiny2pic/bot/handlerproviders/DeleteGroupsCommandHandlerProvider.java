package ru.codenisst.destiny2pic.bot.handlerproviders;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.codenisst.destiny2pic.bot.Configurator;
import ru.codenisst.destiny2pic.bot.handlerproviders.handlers.DeleteGroups;
import ru.codenisst.destiny2pic.bot.listeners.Command;
import ru.codenisst.destiny2pic.bot.listeners.CommandListener;
import ru.codenisst.destiny2pic.vk.VkDispatcher;

import java.util.List;

@Component
public class DeleteGroupsCommandHandlerProvider implements CommandHandlerProvider{

    private final Configurator configurator;
    private final VkDispatcher dispatcher;
    private CommandListener commandListener;
    private final Command command = Command.DELETE_GROUPS;

    @Autowired
    public DeleteGroupsCommandHandlerProvider(Configurator configurator,
                                              VkDispatcher dispatcher) {
        this.configurator = configurator;
        this.dispatcher = dispatcher;
    }

    @Override
    public void setCommandListener(CommandListener commandListener) {
        this.commandListener = commandListener;
    }

    @Override
    public Thread getHandler(TextChannel channel) {
        return new DeleteGroups(channel, configurator, dispatcher, commandListener);
    }

    @Override
    public Command getCommand() {
        return command;
    }

    @Override
    public boolean isCommandAllowed(MessageCreateEvent event) {
        List<Role> eventUserRoles = event.getMessage()
                .getAuthor()
                .asUser()
                .get()
                .getRoles(event
                        .getServer()
                        .get());
        for (Role role : eventUserRoles) {
            if (role.getName().equalsIgnoreCase(configurator.getMainRole())) {
                return true;
            }
        }
        return false;
    }
}
