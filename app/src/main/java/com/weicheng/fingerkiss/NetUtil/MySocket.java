package com.weicheng.fingerkiss.NetUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

/**
 * Created by weicheng on 2015/7/19.
 */
public class MySocket implements Runnable{
    private Socket mySocket;
    private static Logger log = Logger.getLogger(MySocket.class.getName());
    private InetSocketAddress inetSocketAddress;
    private SocketChannel socketChannel;
    public MySocket(){
        inetSocketAddress = new InetSocketAddress("58.196.155.190", 8989);
        System.out.println("Client: Start");
    }
    @Override
    public void run() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(inetSocketAddress);
            while ( ! socketChannel.finishConnect( )){
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(512);
            socketChannel.write(ByteBuffer.wrap("Actions speak louder than words!".getBytes()));
            while (true) {
                byteBuffer.clear();
                int readBytes = socketChannel.read(byteBuffer);
                if (readBytes > 0) {
                    byteBuffer.flip();
                    log.info("Client: readBytes = " + readBytes);
                    log.info("Client: data = " + new String(byteBuffer.array(), 0, readBytes));
                    socketChannel.close();
                    break;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
