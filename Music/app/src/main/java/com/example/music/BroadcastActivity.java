package com.example.music;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.example.music.Constents.cut_song_name;
import static com.example.music.Constents.list;
import static com.example.music.Constents.playmode;
import static com.example.music.Constents.song_index;
import static com.example.music.Constents.listsize;
import static com.example.music.Constents.jump_index;
import com.example.music.SongSheetActivity.MyHandler;

import java.util.Random;

public class BroadcastActivity extends Activity {

    public ImageView music_symbol,next_song,pre_song,stopmusic,style,share,back,more;
    public TextView songname,songsinger,total_time,current_time;

    public MediaPlayer mplayer;
    public boolean ischanging = false;
    public SeekBar BroadcastseekBar;
    Thread broadcast_thread;

    private HandlerAPP handlerAPP = null;

    private MyHandler mHandler = null;

    public  FreshHandler freshHandler=new FreshHandler();

    String cur_totaltime,cur_time;


    public void	onCreate(Bundle savedInstanceState)	{
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        int res=intent.getIntExtra("jump",-1);
        if(res!=-1)
        {
            System.out.println("okk");
            jump_index=res;
        }
        setContentView(R.layout.broadcast);
        initview();
        setMediaPlayerListener();
    }

    public void initview()
    {
        music_symbol =  this.findViewById(R.id.imageview_face);
        next_song =  this.findViewById(R.id.imageview_next);
        pre_song =  this.findViewById(R.id.imageview_front);
        stopmusic =  this.findViewById(R.id.imageview_play);
        songname =  this.findViewById(R.id.song_name);
        songsinger =  this.findViewById(R.id.singer);
        style = this.findViewById(R.id.style);
        share = this.findViewById(R.id.share);
        total_time =  this.findViewById(R.id.total_time);
        current_time =  this.findViewById(R.id.current_time);
        back=this.findViewById(R.id.back);
        more=this.findViewById(R.id.more);



        mplayer=Constents.mplayer;

        BroadcastseekBar=this.findViewById(R.id.broadcast_seekbar);
        if(song_index!=jump_index)
        {
            song_index=jump_index;
            musicplay(song_index);
        }
        songname.setText(cut_song_name(list.get(song_index).getSong()).trim());
        songsinger.setText(list.get(song_index).getSinger().trim());
//        text_main.setText(cut_song_name(list.get(song_index).getSong()));
        BroadcastseekBar.setMax(list.get(song_index).getDuration());
        broadcast_thread = new Thread(new BroadcastSeekBarThread());
        broadcast_thread.start();
        setclick();
        if(mplayer.isPlaying())
        {
            stopmusic.setImageResource(R.mipmap.broadcast);
        }
        else
        {
            stopmusic.setImageResource(R.mipmap.stop);
        }
        if(playmode==1)
        {
            style.setImageResource(R.mipmap.list_order3);
        }
        else
        {
            style.setImageResource(R.mipmap.random3);
        }

        refreshtime();

    }
    public class FreshHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 100) {
                refreshtime();
            }
        }
    }

    public void refreshtime()
    {
        int cur_duration = list.get(song_index).getDuration();
        cur_totaltime = MusicUtils.formatTime(cur_duration);
        total_time.setText(cur_totaltime);
        String cur_curtime=MusicUtils.formatTime(mplayer.getCurrentPosition());
        current_time.setText(cur_curtime);
    }


    public void setclick()
    {
        stopmusic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
//
                change_play_image(R.mipmap.broadcast, R.mipmap.stop);
//

                if (mplayer.isPlaying()) {
                    mplayer.pause();
//
                } else {
                    mplayer.start();
                    broadcast_thread = new Thread(new BroadcastSeekBarThread());
                    broadcast_thread.start();
//
                }
                handlerAPP = (HandlerAPP) getApplication();
                // 获得该共享变量实例
                mHandler = handlerAPP.getHandler();
                mHandler.sendEmptyMessage(1);
            }
        });

        style.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
//
                change_style_image(R.mipmap.list_order3, R.mipmap.random3);
                playmode=playmode*-1;

            }
        });

        next_song.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                nextMusic();

//                auto_change_listview();
//                }
            }
        });

        pre_song.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

//
                frontMusic();
//                auto_change_listview();
//                }
            }
        });

        BroadcastseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                ischanging = false;
                mplayer.seekTo(seekBar.getProgress());
                broadcast_thread = new Thread(new BroadcastSeekBarThread());
                broadcast_thread.start();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                ischanging = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                // 可以用来写拖动的时候实时显示时间
            }


        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                goback();
                finish();
            }
        });
    }



    private void change_play_image(int resID_play, int resID_pause) {
        if (stopmusic
                .getDrawable()
                .getCurrent()
                .getConstantState()
                .equals(getResources().getDrawable(resID_play)
                        .getConstantState())) {
            stopmusic.setImageResource(resID_pause);
        } else {
            stopmusic.setImageResource(resID_play);
        }
    }

    private void change_style_image(int resID_ordered, int resID_unordered) {
        if (style
                .getDrawable()
                .getCurrent()
                .getConstantState()
                .equals(getResources().getDrawable(resID_ordered)
                        .getConstantState())) {
            style.setImageResource(resID_unordered);
        } else {
            style.setImageResource(resID_ordered);
        }
    }

    class BroadcastSeekBarThread implements Runnable {

        @Override
        public void run() {
            while (!ischanging && mplayer.isPlaying()) {
                // 将SeekBar位置设置到当前播放位置
                Message message1=new Message();
                message1.what=100;
                freshHandler.sendMessage(message1);
                BroadcastseekBar.setProgress(mplayer.getCurrentPosition());

                try {
                    // 每500毫秒更新一次位置
                    Thread.sleep(500);
                    // 播放进度

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setMediaPlayerListener() {
        // 监听mediaplayer播放完毕时调用
        mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
//
                // 这里会引发初次进入时直接点击播放按钮时，播放的是下一首音乐的问题
                nextMusic();
//                auto_change_listview();
//
//
            }
        });

    }

    private void musicplay(int position) {
        songname.setText(cut_song_name(list.get(position).getSong()).trim());
        songsinger.setText(list.get(position).getSinger().trim());
//        text_main.setText(cut_song_name(list.get(song_index).getSong()));
        BroadcastseekBar.setMax(list.get(position).getDuration());
        stopmusic.setImageResource(R.mipmap.broadcast);
        handlerAPP = (HandlerAPP) getApplication();
        // 获得该共享变量实例
        mHandler = handlerAPP.getHandler();
        mHandler.sendEmptyMessage(1);
        try {
            mplayer.reset();
            mplayer.setDataSource(list.get(position).getPath());
            mplayer.prepare();
            mplayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        broadcast_thread = new Thread(new BroadcastSeekBarThread());
        broadcast_thread.start();


        total_time.setText(cur_totaltime);
    }

    private void frontMusic() {
        if(playmode==1)
        {
            song_index--;
            if (song_index < 0) {
                song_index = list.size() - 1;
            }
            System.out.println("songindex:"+song_index);
        }
        else {
            Random random=new Random();
            int r=song_index;
            while (r==song_index)
            {
                r=random.nextInt(listsize);
            }
            System.out.println("rrrr:"+r);
            song_index=r;
        }
        musicplay(song_index);
//
    }

    private void nextMusic() {
        if(playmode==1)
        {
            song_index++;
            if (song_index > list.size() - 1) {
                song_index = 0;
            }
            System.out.println("songindex:"+song_index);
        }
        else
        {
            Random random=new Random();
            int r=song_index;
            while (r==song_index)
            {
                r=random.nextInt(listsize);
            }
            System.out.println("rrrr:"+r);
            song_index=r;
        }
        musicplay(song_index);
    }

}
