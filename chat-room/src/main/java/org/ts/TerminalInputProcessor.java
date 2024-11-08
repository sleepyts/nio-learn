package org.ts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;

public class TerminalInputProcessor {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface CommandHandler {
        String value();

        String man() default "";

    }

    static class Commands {
        @CommandHandler(value = "exit", man = "Exit this server.")
        public void exit(User user) {
            user.receiveMes("Good bye! :)");
            user.exit();
        }

        @CommandHandler(value = "help", man = "Display all commands, their help, and their descriptions.")
        public void help(User user) {
            for (String cmd : Commands.commandHelp)
                user.receiveMes(cmd);
        }

        @CommandHandler(value = "echo", man = "Show args. Eg: echo [s1] [s2] [s3] ...")
        public void echo(User user, String[] args) {
            user.receiveMes(Arrays.toString(args));
        }

        @CommandHandler(value = "bd", man = "Broadcast a message to all online users. Eg:bd [message].")
        public void broadcast(User user, String[] args) {
            StringBuilder mes = new StringBuilder();
            for (String arg : args) {
                mes.append(arg).append(" ");
            }
            user.broadcast(mes.toString());
        }

        @CommandHandler(value = "rename", man = "Rename you username. Eg: rename [newname]")
        public void rename(User user, String[] args) {
            String newUserName=args[0];
            if (user.getBelongServer().getOnlineUsers().containsKey(newUserName)){
                user.receiveMes("The new username [%s] is already occupied!",newUserName);
                ou(user);
                return;
            }
            user.getBelongServer().getOnlineUsers().remove(user.getName());
            user.setName(newUserName);
            user.getBelongServer().getOnlineUsers().put(user.getName(),user);
        }

        @CommandHandler(value = "send", man = "Send a private message to another user. Eg: send [userName] [message].")
        public void send(User user, String[] args) {
            String sendToUser = args[0];
            StringBuilder sendMes = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                sendMes.append(args[i]).append(" ");
            }
            Map<String, User> onlineUsers = user.getBelongServer().getOnlineUsers();
            Optional<User> optional = Optional.ofNullable(onlineUsers.get(sendToUser));
            if (optional.isPresent()){
                user.sendMesToOther(optional.get(), sendMes.toString());
            }else {
                user.receiveMes("Can't find user [%s]",sendToUser);
                ou(user);
            }

        }

        @CommandHandler(value = "ou", man = "Show all online users.")
        public void ou(User user) {
            List<String> allUserName = user.getBelongServer().getAllUserName();
            user.receiveMes("Here is all online users:");
            for (String userName : allUserName) {
                user.receiveMes(userName.equals(user.getName()) ? userName + " <--- it's you" : userName);
            }
        }


        @CommandHandler(value = "whoami", man = "Show your username.")
        public void whoami(User user) {
            user.receiveMes(user.getName());
        }


        public static List<String> genCmdHelp() {
            Method[] declaredMethods = Commands.class.getDeclaredMethods();
            List<String> res = new ArrayList<>();
            for (Method method : declaredMethods) {
                CommandHandler annotation = method.getAnnotation(CommandHandler.class);
                if (annotation != null)
                    res.add(annotation.value() + " :" + annotation.man());

            }
            return res;
        }

        public static List<String> commandHelp=genCmdHelp();
    }

    static class CommandProcessor {
        private final Map<String, Method> commandMethods = new HashMap<>();

        public void registerCommands(Object commands) {
            for (Method method : commands.getClass().getDeclaredMethods()) {
                CommandHandler annotation = method.getAnnotation(CommandHandler.class);
                if (annotation != null) {
                    commandMethods.put(annotation.value().toLowerCase(), method);
                }
            }
        }

        public void handleInput(String input, Object commandInstance, User user) throws Exception {
            String[] parts = input.split("\\s+", 2);
            String commandName = parts[0].toLowerCase();
            String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[0];
            if (commandName.equals("\n")) {
                user.receiveMes(">");
                return;
            }
            Method method = commandMethods.get(commandName);
            if (method != null) {
                if (method.getParameterCount() == 1) {
                    method.invoke(commandInstance, user);
                } else {
                    method.invoke(commandInstance, user, args);
                }
            } else {
                user.receiveMes("Unknown command. Type 'help' for available commands.");
            }
            user.receiveMes(">");
        }
    }

}