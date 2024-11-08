package org.ts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    private String name;
    private SelectionKey key;
    private Server belongServer;

    public void sendMesToOther(User user,String mes) {
        mes = String.format("[%s] has send a private mes to you:%s", name, mes);
        user.receiveMes(mes);
    }

    public void receiveMes(String mes) {
        mes += Objects.equals(mes, ">") ? "" : mes.endsWith("\n") ? "" : '\n';
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            channel.write(ByteBuffer.wrap(mes.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException ignored) {

        }
    }
    public void receiveMes(String format,String ...args) {
        receiveMes(String.format(format,args));
    }

    public void exit() {
        try {
            key.channel().close();
        } catch (IOException ignored) {

        }
        belongServer.removeUser(name);
        key.cancel();
    }

    public void broadcast(String mes) {
        belongServer.getOnlineUsers().values().forEach(u -> {
            u.receiveMes(String.format("[%s]: %s", name, mes));
        });

    }

    private static final long serialVersionUID = 1L;
}
