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

    public void sendMesToOther(String mes,User user) throws IOException {
        mes=String.format("[%s] has send a private mes to you :%s",name,mes);
        ByteBuffer byteBuffer = ByteBuffer.wrap(mes.getBytes(StandardCharsets.UTF_8));
        SelectionKey userKey = user.getKey();

        SocketChannel channel = (SocketChannel) userKey.channel();
        channel.write(byteBuffer);
    }

    public void receiveMesFromServer(String mes)  {
        mes+= Objects.equals(mes, ">") ?"":mes.endsWith("\n")?"":'\n';
        try{
            SocketChannel channel = (SocketChannel) key.channel();
            channel.write(ByteBuffer.wrap(mes.getBytes(StandardCharsets.UTF_8)));
        }catch (IOException e){

        }

    }
    public void exit(){
        try{
            key.channel().close();
        }catch (IOException e) {

        }
        belongServer.removeUser(name);
        key.cancel();
    }

    public void broadcast(String mes){
        belongServer.getOnlineUsers().forEach(u->{
            if (!Objects.equals(u.getName(), name))
                u.receiveMesFromServer(String.format("broadcast mes from user [%s]: %s",name,mes));
        });

    }
    private static final long serialVersionUID = 1L;
}
