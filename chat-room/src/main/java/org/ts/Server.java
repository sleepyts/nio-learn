package org.ts;

import cn.hutool.core.util.RandomUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Server {
    private String host;
    private int port;
    private Selector selector;


    private final Set<User> onlineUsers=new HashSet<>();

    private final TerminalInputProcessor.CommandProcessor commandProcessor;
    private final TerminalInputProcessor.Commands commands;
    private Server(){
        commandProcessor=new TerminalInputProcessor.CommandProcessor();
        commands=new TerminalInputProcessor.Commands();
        commandProcessor.registerCommands(commands);
    }
    Server(String _host,int _port){
        this();
        host=_host;
        port=_port;
    }
    public  void start() throws Exception {
        selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(host, port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);


        while (true) {
            // Blocking until something from client happening
            selector.select();
            // Obtain the key of the client that needs to be handled
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            // Ergodic the set of keys
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();

                // 'If' to handle event

                // Connect
                if (key.isAcceptable()) {
                    handleRegister(selector, serverSocket);
                }

                // Input
                if (key.isReadable()) {
                    handleMes(key);
                }

                iter.remove();
            }
        }
    }

    private  void handleRegister(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        SelectionKey registerKey = client.register(selector, SelectionKey.OP_READ);

        User user = new User(RandomUtil.randomString(10), registerKey,this);
        registerKey.attach(user);
        onlineUsers.add(user);
        broadCast(String.format("User online: [%s]\n",user.getName()));
    }

    private  void handleMes( SelectionKey key) throws Exception {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int byteRead = client.read(buffer);
        if(byteRead==-1){
            broadCast("Client disconnected: " + client.getRemoteAddress()+"\n");
            key.cancel();
            client.close();
            return;
        }
        buffer.flip();
        User user= (User) key.attachment();
        String input = StandardCharsets.UTF_8.decode(buffer).toString();
        commandProcessor.handleInput(input,commands,user);
    }

    private void broadCast(String mes){
        ByteBuffer wrap = ByteBuffer.wrap(mes.getBytes(StandardCharsets.UTF_8));
        for (SelectionKey key : selector.keys()) {
            if (key.isValid()&&key.channel() instanceof SocketChannel) {
                SocketChannel clientChannel = (SocketChannel) key.channel();
                try {
                    clientChannel.write(wrap);
                    wrap.rewind(); // 重置 buffer 以便重用
                } catch (IOException e) {
                    System.out.println("Error broadcasting to client: " + e.getMessage());
                    try {
                        clientChannel.close();
                        key.cancel();
                    } catch (IOException ex) {
                        // 忽略关闭错误
                    }
                }
            }
        }
    }

    public List<String> getAllUserName(){
        return onlineUsers
                .stream()
                .map(User::getName)
                .collect(Collectors.toList());
    }
}
