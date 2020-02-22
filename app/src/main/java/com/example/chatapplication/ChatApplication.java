package com.example.chatapplication;
import android.app.Application;
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;
public class ChatApplication extends Application {
    IO.Options options = new IO.Options();
//    private SocketIo
    private Socket mSocket;
    {
        try {
            options.reconnectionAttempts = 2;
            options.reconnectionDelay = 1000;//失败重连的时间间隔
            options.timeout = 500;//连接超时时间(ms)
//           Constants.CHAT_SERVER_URL----->Constants类下面有一个公有静态字符串CHAT_SERVER_URL
            mSocket = IO.socket(Constants.CHAT_SERVER_URL,options);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    //返回创建的的Socket
    public Socket getSocket() {
        return mSocket;
    }

}
