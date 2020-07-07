package com.autolink.mysockettest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity /*todo implements View.OnClickListener*/{
    Button A_button, B_button, C_button, D_button;
    Button conn_button;
    TextView text;
    private SocketClient client;
    //todo private Button plastart, pause, playstop, rebroadcast;
    private SurfaceView surfaceView;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private int currentPosition = 0;
    private boolean isPlaying;
    private TextView startTime,sumTime;

    // 获取视频文件地址
    private String path = "/storage/emulated/0/Download/video.mp4";

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_layout);
        initView();
        // 为SurfaceHolder添加回调
        surfaceView.getHolder().addCallback(callback);
        // 设置Surface不维护自己的缓冲区，而是等待屏幕的渲染引擎将内容推送到界面
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 为进度条添加进度更改事件
        seekBar.setOnSeekBarChangeListener(change);


        A_button = (Button) findViewById(R.id.A_button);
        B_button = (Button) findViewById(R.id.B_button);
        C_button = (Button) findViewById(R.id.C_button);
        D_button = (Button) findViewById(R.id.D_button);
        conn_button = (Button) findViewById(R.id.conn_button);
        text= (TextView) findViewById(R.id.text);

        conn_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                client=new SocketClient ();
                //服务端的IP地址和端口号
                client.clintValue (getApplicationContext(),"10.0.2.2" ,8080);
                //开启客户端接收消息线程
                client.openClientThread ();
            }
        });
        A_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                client.sendMsg ("Play_A_Media_Server");
            }
        });
        B_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                client.sendMsg ("Play_B_Media_Server");
            }
        });
        C_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                client.sendMsg ("Play_C_Media_Server");
            }
        });
        D_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                client.sendMsg ("Play_D_Media_Server");
            }
        });

        SocketClient.mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                stop();
                String messeges = msg.obj.toString ().substring(0, 19);
                text.setText(messeges);

                Log.d("tag", messeges);
                if(messeges.equals("Play_A_Media_Client"))
                    path = "/storage/emulated/0/Download/vidio1.mp4";
                if(messeges.equals("Play_B_Media_Client"))
                    path = "/storage/emulated/0/Download/vidio2.mp4";
                if(messeges.equals("Play_C_Media_Client"))
                    path = "/storage/emulated/0/Download/vidio3.mp4";
                if(messeges.equals("Play_D_Media_Client"))
                    path = "/storage/emulated/0/Download/vidio4.mp4";
                play(0, path);
                //todo plastart.setEnabled(false);
            }
        };
    }
    private void initView() {
        requestMyPermissions();
        /*todo plastart = (Button) findViewById(R.id.playstart);
        plastart.setOnClickListener(this);

        pause = (Button) findViewById(R.id.pause);
        pause.setOnClickListener(this);

        playstop = (Button) findViewById(R.id.playstop);
        playstop.setOnClickListener(this);

        rebroadcast = (Button) findViewById(R.id.rebroadcast);
        rebroadcast.setOnClickListener(this);*/

        sumTime= (TextView) findViewById(R.id.sumTime);
        startTime= (TextView) findViewById(R.id.startime);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
    }

    /*todo
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.playstart://开始
                play(0, path);
                plastart.setEnabled(false);
                break;
            case R.id.playstop://停止
                stop();
                break;
            case R.id.rebroadcast://重播
                replay();
                break;
            case R.id.pause://暂停
                pause();
                break;
        }

    }*/

    // 添加一个Callback对象监听SurfaceView的变化
    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        // SurfaceHolder被修改的时候回调
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i("info", "SurfaceHolder 被销毁");
            // 销毁SurfaceHolder的时候记录当前的播放位置并停止播放
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                currentPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.stop();
            }
        }

        //SurfaceView创建时触发
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i("info", "SurfaceHolder 被创建");
            if (currentPosition > 0) {
                // 创建SurfaceHolder的时候，如果存在上次播放的位置，则按照上次播放位置进行播放
                play(currentPosition, path);
                currentPosition = 0;
            }
        }

        //SurfaceView改变时触发
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            Log.i("info", "SurfaceHolder 大小被改变");
        }
    };

    private SeekBar.OnSeekBarChangeListener change = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // 当进度条停止修改的时候触发
            // 取得当前进度条的刻度
            int progress = seekBar.getProgress();
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                // 设置当前播放的位置
                mediaPlayer.seekTo(progress);

            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            Log.i("info","onStopTrackingTouch--"+progress);
            shiftTime(startTime,progress);
        }
    };


    /**
     * 开始播放
     *
     * @param msec 播放初始位置
     */
    protected void play(final int msec, String New_path) {
        // 获取视频文件地址
        path = New_path;
        Log.d("tag", path);
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(this, "视频文件路径错误", Toast.LENGTH_LONG).show();

            return;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // 设置播放的视频源
            Log.i("info", "1111111" + file.getAbsolutePath());
            mediaPlayer.setDataSource(file.getAbsolutePath());
            // 设置显示视频的SurfaceHolder,指定视频画面输出到SurfaceView之上
            mediaPlayer.setDisplay(surfaceView.getHolder());
            Log.i("info", "开始装载");
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i("info", "装载完成");
                    mediaPlayer.start();
                    // 按照初始位置播放
                    mediaPlayer.seekTo(msec);
                    // 设置进度条的最大进度为视频流的最大播放时长
                    seekBar.setMax(mediaPlayer.getDuration());
                    Log.i("info","2222222222-----"+mediaPlayer.getDuration());
                    //将刻度转换成时间mm：ss
                    shiftTime(sumTime,mediaPlayer.getDuration());
                    // 开始线程，更新进度条的刻度
                    new Thread() {

                        @Override
                        public void run() {
                            try {
                                isPlaying = true;
                                while (isPlaying) {
                                    int current = mediaPlayer
                                            .getCurrentPosition();
                                    seekBar.setProgress(current);
                                    sleep(1000);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    //将停止按钮设置不可点击
                    //todo playstop.setEnabled(false);
                }
            });
            /*todo
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // 在播放完毕被回调
                    playstop.setEnabled(true);
                }
            });*/

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // 发生错误重新播放
                    Log.i("info","onError---"+what);
                    play(0, path);
                    isPlaying = false;
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 重新开始播放
     */
    protected void replay() {
        if (mediaPlayer != null&& mediaPlayer.isPlaying()){
            mediaPlayer.seekTo(0);
            Toast.makeText(this,"重新播放",Toast.LENGTH_LONG).show();
            //todo pause.setText("暂停");
            return;
        }
        play(0, path);
        isPlaying = false;


    }

    /**
     * 暂停或继续
     */
    /*todo
    protected void pause() {
        if (pause.getText().toString().trim().equals("继续")) {
            pause.setText("暂停");
            mediaPlayer.start();
            Toast.makeText(this, "继续播放", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            pause.setText("继续");
            Toast.makeText(this,"暂停播放",Toast.LENGTH_SHORT).show();
        }

    }*/

    /**
     * 停止播放
     */
    protected void stop() {

        if (mediaPlayer != null /*todo && mediaPlayer.isPlaying()*/) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            //todo playstop.setEnabled(true);
            isPlaying = false;
        }
    }

    //将刻度转换为时间

    private void shiftTime(TextView view,int time){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        date.setTime(time);
        String str = sdf.format(date);
        Log.i("info","33333333-----"+str);
        view.setText(str);
    }
    private void requestMyPermissions() {

        String TAG = "tag";
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //没有授权，编写申请权限代码
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            Log.d(TAG, "requestMyPermissions: 有写SD权限");
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //没有授权，编写申请权限代码
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else {
            Log.d(TAG, "requestMyPermissions: 有读SD权限");
        }
    }
}
