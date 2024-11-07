package org.ts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerminalInputProcessor {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface CommandHandler {
        String value();
    }

    static class Commands {
        @CommandHandler("exit")
        public void exit(User user)  {
            user.receiveMesFromServer("Good bye! :)");
            user.exit();
        }

        @CommandHandler("help")
        public void help(User user) {

        }

        @CommandHandler("echo")
        public void echo(User user,String[] args) {
            user.receiveMesFromServer(Arrays.toString(args));
        }

        @CommandHandler("send")
        public void send(User user,String[] args){

        }

        @CommandHandler("ou")
        public void ou(User user){
            List<String> allUserName = user.getBelongServer().getAllUserName();
            for (String userName:allUserName){
                user.receiveMesFromServer(userName.equals(user.getName())?userName+" <--- it's you":userName);
            }
        }

        @CommandHandler("whoami")
        public void whoami(User user){
            user.receiveMesFromServer(user.getName());
        }
        @CommandHandler("clear")
        public void clear(User user){
            user.clear();
        }
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

        public void handleInput(String input, Object commandInstance,User user) throws Exception {
            String[] parts = input.split("\\s+", 2);
            String commandName = parts[0].toLowerCase();
            String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[0];

            Method method = commandMethods.get(commandName);
            if (method != null) {
                if (method.getParameterCount() == 1) {
                    method.invoke(commandInstance,user);
                } else {
                    method.invoke(commandInstance, user, args);
                }
            } else {
                user.receiveMesFromServer("Unknown command. Type 'help' for available commands.");
            }
            user.receiveMesFromServer(">");
        }
    }

}