package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MainFragment extends AppCompatActivity {
    //获取EditText的输入内容
    private EditText mInputMessageView;
    //创建一个Socket
    private Socket mSocket;

    //用来存储当前用户Id
    private String userId="20170304010137";

    private String staffId=null;

    private TextView textView;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //各种监听事件
        ChatApplication app = (ChatApplication) getApplication();
        mSocket = app.getSocket();
        //连接监控注册，若报错，请根据错误依次排查问题
        mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.on(Socket.EVENT_ERROR,onEvent_Error);
        mSocket.on(Socket.EVENT_CONNECT_ERROR,onEvent_Connect_Error);
        //获取客服的Id
        mSocket.on("getStaffId"+userId,getStaffId);

        //开启连接
        mSocket.connect();

        textView=(TextView)findViewById(R.id.textview);
        handler=new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 1:
                      textView.setText("收到客服发来的消息:"+msg.obj);
                      break;

                }
                super.handleMessage(msg);
            }

        };


    }
    //信息接收监控   广播监控，监控服务器端发来的信息  这条是客服发给服务器，服务器转发至用户，所以是客服发给用户的信息
//    private Emitter.Listener onNewMessage = new Emitter.Listener() {
//        @Override
//        public void call(final Object... args) {
//            System.out.println("event：“borcast:a10001”  收到客服发来的消息:" + args[0].toString());
//            //收到Server发来的客服ID消息后，创建一个接收客服消息监听
//            mSocket.on("getstaffmessage:"+staffId,getStaffmessage);
//        }
//    };

    //在Server发送过来客服的Id后，开启本监控，接收客服发来的消息
    private Emitter.Listener getStaffMessage=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
//            System.out.println("event：“getStaffMessage”  收到客服发来的消息:" + args[0].toString());
            Log.e("Tag","event：“borcast:"+staffId+"”  收到客服发来的消息:"+args[0].toString());
//            textView=(TextView)findViewById(R.id.textview);
//            textView.setText(args[0].toString());
            Message message=new Message();
//            message.what=args[0].toString();
            message.what=1;
            message.obj=args[0];

            handler.sendMessage(message);
        }
    };

    //Server 用send函数发来客服Staff的Id，在这里创建接收客服信息监听
    private Emitter.Listener getStaffId=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //用来存储Server发来的客服ID
            staffId=args[0].toString();
            Log.e("Tag","获取到的客服ID为："+staffId);
            //客服ID一旦获取到，立马打开监听
            mSocket.on("borcast:"+staffId,getStaffMessage);
        }
    };

    //发送本机信息    加入寻求客服回音的队列
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //TODO  发送本机ID    1代表是用户
            mSocket.send("2:"+userId);
//            Log.e("Tag","连接成功，收到服务器的反馈信息："+args[0].toString());
//            mSocket.send("1:"+userId);
        }
    };

    //网络错误代码监控
    private Emitter.Listener onEvent_Error=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("error:" + args[0].toString());
        }
    };

    //网络错误监控
    private Emitter.Listener onEvent_Connect_Error=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("connect error:" + args[0].toString()+"--------若有错误提示出错，请检查ip、端口是否打开");
        }
    };

    public void btn_click(View view) {
        mInputMessageView = (EditText)findViewById(R.id.message_input);
        mSocket.send("hello 我要开始发送信息咯");
        if (!mSocket.connected()) {
            Toast.makeText(MainFragment.this,"发送失败，请检查您的网络是否通畅",Toast.LENGTH_SHORT).show();
            Log.e("Tag","fail connected");
            return;
        }
        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(MainFragment.this,"请输入您要发送的信息",Toast.LENGTH_SHORT).show();
            mInputMessageView.requestFocus();
            return;
        }
        mInputMessageView.setText("");

        // 执行发送消息尝试。
        mSocket.emit("submitMessageToStaff", userId+":"+message);

    }


    //发送本机信息，请求连接     加入寻求客服回应的队列
    public void btn_getStaff(View view) {
        mSocket.send("1:"+userId);
    }

    //离开客服
    public void btn_getAwayStaff(View view) {

    }




    //活动结束，关闭监听
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭连接
        mSocket.disconnect();
        //分别关闭各种监听
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR,onEvent_Connect_Error);
        mSocket.off(Socket.EVENT_ERROR,onEvent_Error);
//        mSocket.off("borcast", onNewMessage);
    }



}
