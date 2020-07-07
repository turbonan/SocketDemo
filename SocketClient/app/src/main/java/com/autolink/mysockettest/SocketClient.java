package com.autolink.mysockettest;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.socks.library.KLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Created by Administrator on 2016/12/5.
 */

public class SocketClient {
    private Socket client;
    private Context context;
    private int port;           //IP
    private String site;            //端口
    private Thread thread;
    public static Handler mHandler;
    private boolean isClient = false;
    private PrintWriter out;
    private InputStream in;
    private String str;

    /**
     * @effect 开启线程建立连接开启客户端
     */
    public void openClientThread() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /**
                     *  connect()步骤
                     * */
                    client = new Socket(site, port);
                    Log.d("tag", site + ' ' + port);
                    //client.setSoTimeout ( 5000 );//设置超时时间
                    KLog.e(client);
                    if (client != null) {
                        isClient = true;
                        forOut();
                        forIn();
                        Toast.makeText(context.getApplicationContext(), "连结成功",Toast.LENGTH_SHORT).show();
                    } else {
                        isClient = false;
                       KLog.e("连接失败"+"site=" + site + " ,port=" + port);
                    }
                } catch (UnknownHostException e) {
                    Toast.makeText(context.getApplicationContext(), "服务器可能未开启", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    KLog.e(e);
                } catch (IOException e) {
                    e.printStackTrace();
                    KLog.e(e);
                }

            }
        });
        thread.start();
    }

    /**
     * 调用时向类里传值
     */
    public void clintValue(Context context, String site, int port) {
        this.context = context;
        this.site = site;
        this.port = port;
    }

    /**
     * @effect 得到输出字符串
     */
    public void forOut() {
        try {
            out = new PrintWriter(client.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            KLog.e(e);
        }
    }

    /**
     * @steps read();
     * @effect 得到输入字符串
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void forIn() {
        while (isClient) {
            try {
                in = client.getInputStream();

                /**得到的是16进制数，需要进行解析*/
                byte[] bt = new byte[50];
                in.read(bt);
                str = new String(bt, StandardCharsets.UTF_8);
            } catch (IOException e) {
            }
            if (str != null) {
                Message msg = new Message();
                msg.obj = str;
                mHandler.sendMessage(msg);
            }

        }
    }

    /**
     * @steps write();
     * @effect 发送消息
     */
    public void sendMsg(final String str) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (client != null) {
                    out.print(str);
                    out.flush();
                    KLog.e(out + "");
                } else {
                    isClient = false;
                    KLog.e("连接失败");
                }
            }
        }).start();

    }

}
