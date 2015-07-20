package com.weicheng.fingerkiss.NetUtil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.weicheng.fingerkiss.MainActivity;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

/**
 * Created by weicheng on 2015/7/19.
 */
public class MySocket extends Thread{
    private Selector mSelector;
    private static Logger log = Logger.getLogger(MySocket.class.getName());
    private InetSocketAddress inetSocketAddress;
    private SocketChannel socketChannel;
    private static MySocket gMySocket;
    private Handler handler;
    private MySocket(){
        inetSocketAddress = new InetSocketAddress("58.196.155.190", 8989);
    }
    public static synchronized MySocket instance(){
            return gMySocket;
    }
    public static synchronized MySocket instance(Handler handler){
        if(gMySocket == null) {
            try {
                gMySocket = new MySocket();
                gMySocket.handler = handler;
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        return gMySocket;
    }

    private class SendMsgThread implements Runnable{
        private String msg;
        public SendMsgThread(String msg){this.msg = msg;}
        @Override
        public void run() {
            try {
                socketChannel.write(ByteBuffer.wrap(msg.getBytes()));
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    public void sendMsg(String msg){
        new Thread(new SendMsgThread(msg)).start();
    }
    @Override
    public void run() {
        System.out.println("Client: Start");
        try {
            mSelector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.socket().setSoTimeout(10000);
            boolean isconnect = socketChannel.connect(inetSocketAddress);
            // 将客户端设定为异步
            socketChannel.configureBlocking(false);
            // 在轮讯对象中注册此客户端的读取事件(就是当服务器向此客户端发送数据的时候)
            socketChannel.register(mSelector, SelectionKey.OP_READ);
            long waittimes = 0;
            if(!isconnect) {
                while (!socketChannel.finishConnect()) {
                    log.info("等待非阻塞连接建立....");
                    Thread.sleep(50);
                    if(waittimes < 100)
                        waittimes++;
                    else
                        break;
                }
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            while (true) {
                byteBuffer.clear();
                int readBytes = socketChannel.read(byteBuffer);
                if (readBytes > 0) {
                    byteBuffer.flip();
                    String data = new String(byteBuffer.array(), 0, readBytes);
                    log.info("From Server: data = " + data);
                    String[] array = data.split("<");
                    for(int i=1;i<array.length;++i){
                        try {
                            String[] itemArray = array[i].split(" ");
                            if(itemArray.length!=3)break;
                            int op = Integer.parseInt(itemArray[0]);
                            int x = (int)Float.parseFloat(itemArray[1]);
                            int y = (int)Float.parseFloat(itemArray[2]);
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putInt("x",x);
                            bundle.putInt("y",y);
                            msg.what = op;
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                            if(op==1)
                                break;
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            try {
                socketChannel.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
