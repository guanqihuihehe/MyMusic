package com.example.music;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Random;

//import static com.example.music.Constents.currentposition;
import static com.example.music.Constents.ConstentsHandler;
import static com.example.music.Constents.cut_song_name;
import static com.example.music.Constents.list;
import static com.example.music.Constents.maincontext;
import static com.example.music.Constents.pageindex;
import static com.example.music.Constents.song_index;
import static com.example.music.Constents.playmode;
import static com.example.music.Constents.listsize;
import static com.example.music.Constents.jump_index;
import static com.example.music.Constents.maxpagenum;

public	class SongSheetActivity extends AppCompatActivity implements View.OnTouchListener{
    public ListView listview;
    //    public ArrayList<Song> list=new ArrayList<Song>();
    public MediaPlayer mplayer = new MediaPlayer();


    public MyAdapter adapter;

    public SeekBar seekBar;
    public TextView textView1, textView2,text_main,broadcasting;
    public ImageView imageView_play, imageView_next, imageView_front,
            imageview, imageview_playstyle, imageview_location,
            page_pre,page_next;
    public int screen_width;

    public int play_style = 0;
    // 判断seekbar是否正在滑动
    public boolean ischanging = false;
    public Thread thread;
    // 当前音乐播放位置,从0开始
//    public int currentposition;
    // 屏幕显示的最大listview条数

//    public int pageindex;

//    public Context maincontext;

    private HandlerAPP handlerAPP = null;
    private MyHandler handler = null;

    GestureDetector mGestureDetector;
    @Override
    public void onCreate(Bundle savedInstanceState)	{
        super.onCreate(savedInstanceState);

        maincontext=SongSheetActivity.this;

        HandlerAPP handlerAPP = null;
        MyHandler myHandler = null;
        handlerAPP = (HandlerAPP) getApplication();
        // 获得该共享变量实例
        myHandler = handlerAPP.getHandler();
        ConstentsHandler=myHandler;

        setContentView(R.layout.main);

        mGestureDetector = new GestureDetector(this, myGestureListener);
        RelativeLayout mRelativeLayout = (RelativeLayout)findViewById(R.id.main_box);//布局的主容器
        mRelativeLayout.setOnTouchListener(this);//将主容器的监听交给本activity，本activity再交给mGestureDetector
        mRelativeLayout.setLongClickable(true);   //必需设置这为true 否则也监听不到手势
//        动态获取权限
        if	(ContextCompat.checkSelfPermission (SongSheetActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) !=PackageManager.PERMISSION_GRANTED   )	{
            ActivityCompat.requestPermissions(SongSheetActivity.this,	new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE	},	1);
        }
        else if(ContextCompat.checkSelfPermission (SongSheetActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE) !=PackageManager.PERMISSION_GRANTED   )	{
            ActivityCompat.requestPermissions(SongSheetActivity.this,	new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE	},	1);
        }
        else	{
            Constents.mplayer=mplayer;
            makelist();
            handlerAPP = (HandlerAPP) getApplication();
            handler = new MyHandler();
            handlerAPP.setHandler(handler);

        }


        setClick();
        setMediaPlayerListener();

    }


    private void makelist(){

//        setContentView(R.layout.activity_main);
        listview = (ListView) this.findViewById(R.id.listview);
        list = (ArrayList<Song>) MusicUtils.getMusicData(SongSheetActivity.this);
        listsize=list.size();
        System.out.println("size:"+listsize);
        maxpagenum=(listsize-1)/8;
        text_main = (TextView) this.findViewById(R.id.text_main);
        broadcasting=this.findViewById(R.id.broadcasting_song);
        broadcasting.setSelected(true);
        page_pre=this.findViewById(R.id.pre_page);
        //change 10.20
        pageindex=0;
        ArrayList<Song> list2=new ArrayList<Song>();
        int endindex;
        if(listsize<(pageindex+1)*8)
        {
            endindex=listsize;
        }
        else
        {
            endindex=(pageindex+1)*8;
        }
        for(int i=pageindex*8;i<endindex;i++)
        {
            list2.add(list.get(i));
        }
        adapter = new MyAdapter(this, list2);
        maincontext=this;
        listview.setAdapter((ListAdapter)adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                song_index = position;
                System.out.println("position:"+position);
                musicplay(song_index);
            }
        });

//        page_next=findViewById(R.id.next_page);
//        page_pre=this.findViewById(R.id.pre_page);
//
//        page_next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//
//        page_pre.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//            }
//        });
    }

    public void nextpage()
    {
        if(pageindex<maxpagenum)
        {
            pageindex++;
            ArrayList<Song> list2=new ArrayList<Song>();
            int endindex;
            if(listsize<(pageindex+1)*8)
            {
                endindex=listsize;
            }
            else
            {
                endindex=(pageindex+1)*8;
            }
            for(int i=pageindex*8;i<endindex;i++)
            {
                list2.add(list.get(i));
            }
            adapter = new MyAdapter(maincontext, list2);
            listview.setAdapter((ListAdapter)adapter);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    song_index = 8*pageindex+position;
                    musicplay(song_index);
                }
            });
        }
        else
        {
            Toast toast=Toast.makeText(getApplicationContext(), "已经是最后一页了", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void prepage()
    {
        if(pageindex>0)
        {
            pageindex--;
            ArrayList<Song> list2=new ArrayList<Song>();
            int endindex;
            if(listsize<(pageindex+1)*8)
            {
                endindex=listsize;
            }
            else
            {
                endindex=(pageindex+1)*8;
            }
            for(int i=pageindex*8;i<endindex;i++)
            {
                list2.add(list.get(i));
            }
            adapter = new MyAdapter(maincontext, list2);
            listview.setAdapter((ListAdapter)adapter);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    song_index = 8*pageindex+position;
                    musicplay(song_index);
                }
            });
        }
        else
        {
            Toast toast=Toast.makeText(getApplicationContext(), "已经是第一页了", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public	void	onRequestPermissionsResult(int	requestCode, String[]	permissions, int[]	grantResults)	{
        switch	(requestCode)	{
            case	1:
                if	(grantResults.length	>	0	&&	grantResults[0]	==	PackageManager.PERMISSION_GRANTED)	{
                    makelist();
                }
                else	{
                    Toast.makeText(this,	"拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public void musicplay(int position) {
        System.out.println("target:"+list.get(position).getPath());
        String broadcasting_song=list.get(position).getSinger().trim()+" - "+cut_song_name(list.get(position).getSong()).trim();
        broadcasting.setText(broadcasting_song);
//        textView1.setText(cut_song_name(list.get(position).getSong()).trim());
//        textView2.setText(list.get(position).getSinger().trim());
//        text_main.setText(cut_song_name(list.get(song_index).getSong()));
//        seekBar.setMax(list.get(position).getDuration());
        imageView_play.setImageResource(R.mipmap.broadcast);
        try {
            mplayer.reset();

            mplayer.setDataSource(list.get(position).getPath());

            mplayer.prepare();
            mplayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        thread = new Thread(new SeekBarThread());
//        thread.start();
    }

    // 上一曲
    private void frontMusic() {
        if(playmode==1)
        {
            song_index--;
            if (song_index < 0) {
                song_index = list.size() - 1;
            }
            refreshpage();
            musicplay(song_index);
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
            refreshpage();
            musicplay(song_index);
        }
//
    }

    // 下一曲
    private void nextMusic() {
        if(playmode==1)
        {
            song_index++;
            if (song_index > list.size() - 1) {
                song_index = 0;
            }
            refreshpage();
            musicplay(song_index);
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
            refreshpage();
            musicplay(song_index);
        }
    }



    private void setClick() {

        View tips_bar=findViewById(R.id.tips_bar);
        View layout_playbar = (View) findViewById(R.id.main_playbar);
//        layout_playbar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                jump_index=song_index;
//                Intent intent=new Intent(MainActivity.this,BroadcastActivity.class);
//                startActivity(intent);
//            }
//        });
        tips_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jump_index=song_index;
                Intent intent=new Intent(SongSheetActivity.this,BroadcastActivity.class);
                startActivity(intent);
            }
        });
        tips_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jump_index=song_index;
                Intent intent=new Intent(SongSheetActivity.this,BroadcastActivity.class);
                startActivity(intent);
            }
        });
//        imageview = (ImageView) layout_playbar.findViewById(R.id.imageview);
        imageView_play = (ImageView) layout_playbar.findViewById(R.id.imageview_play);
        imageView_next = (ImageView) layout_playbar.findViewById(R.id.imageview_next);
        imageView_front = (ImageView) layout_playbar.findViewById(R.id.imageview_front);
//        textView1 = (TextView) layout_playbar.findViewById(R.id.name);
//        textView2 = (TextView) layout_playbar.findViewById(R.id.singer);
//        seekBar = (SeekBar) layout_playbar.findViewById(R.id.seekbar);
        imageView_play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                change_play_image(R.mipmap.broadcast, R.mipmap.stop);

                if (mplayer.isPlaying()) {
                    mplayer.pause();
//
                } else {
                    mplayer.start();
//                     thread = new Thread(new SeekBarThread());
//                     thread.start();
//
                }
            }
        });

        imageView_next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
//
                nextMusic();
                auto_change_listview();
//                }
            }
        });
//
        imageView_front.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

//
                frontMusic();
                auto_change_listview();
//                }
            }
        });
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                // TODO Auto-generated method stub
//                ischanging = false;
//                mplayer.seekTo(seekBar.getProgress());
//                thread = new Thread(new SeekBarThread());
//                thread.start();
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                // TODO Auto-generated method stub
//                ischanging = true;
//            }
//
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress,
//                                          boolean fromUser) {
//                // TODO Auto-generated method stub
//                // 可以用来写拖动的时候实时显示时间
//            }
//
//
//        });
    }

    class SeekBarThread implements Runnable {

        @Override
        public void run() {
            while (!ischanging && mplayer.isPlaying()) {
                // 将SeekBar位置设置到当前播放位置
//                seekBar.setProgress(mplayer.getCurrentPosition());

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




    public void change_play_image(int resID_play, int resID_pause) {
        if (imageView_play
                .getDrawable()
                .getCurrent()
                .getConstantState()
                .equals(getResources().getDrawable(resID_play)
                        .getConstantState())) {
            imageView_play.setImageResource(resID_pause);
        } else {
            imageView_play.setImageResource(resID_play);
        }
    }

    public void auto_change_listview() {
        if (song_index <= listview.getFirstVisiblePosition()) {
            listview.setSelection(song_index);
        }
        if (song_index >= listview.getLastVisiblePosition()) {
            listview.smoothScrollToPosition(song_index);
//            listview.setSelection(currentposition );
        }
    }

    private void setMediaPlayerListener() {
        // 监听mediaplayer播放完毕时调用
        mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
//
                // 这里会引发初次进入时直接点击播放按钮时，播放的是下一首音乐的问题
                nextMusic();
                auto_change_listview();
//
//
            }
        });

    }

    final class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1) { // 更新UI
//                textView1.setText(cut_song_name(list.get(song_index).getSong()).trim());
//                textView2.setText(list.get(song_index).getSinger().trim());
//                text_main.setText(cut_song_name(list.get(song_index).getSong()));
//                seekBar.setMax(list.get(song_index).getDuration());
                String broadcasting_song=list.get(song_index).getSinger().trim()+" - "+cut_song_name(list.get(song_index).getSong()).trim();
                broadcasting.setText(broadcasting_song);
                if(mplayer.isPlaying())
                {
                    imageView_play.setImageResource(R.mipmap.broadcast);
                }
                else
                {
                    imageView_play.setImageResource(R.mipmap.stop);
                }
//                thread = new Thread(new SeekBarThread());
//                thread.start();
                refreshpage();
            }

            if(msg.what==2)
            {
                int ii=(int)msg.obj;
                song_index = ii;
                musicplay(song_index);
                Intent intent=new Intent(SongSheetActivity.this,BroadcastActivity.class);
                startActivity(intent);
            }
        }
    }



    public void refreshpage()
    {
        pageindex=song_index/8;
        ArrayList<Song> list2=new ArrayList<Song>();
        if((pageindex+1)*8>=listsize)
        {
            for(int i=pageindex*8;i<listsize;i++)
            {
                list2.add(list.get(i));
            }
        }
        else
        {
            for(int i=pageindex*8;i<(pageindex+1)*8;i++)
            {
                list2.add(list.get(i));
            }
        }
        adapter = new MyAdapter(maincontext, list2);
        listview.setAdapter((ListAdapter)adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                song_index = 8*pageindex+position;
                musicplay(song_index);
            }
        });
    }

    private static final int FLING_MIN_DISTANCE = 50;
    private static final int FLING_MIN_VELOCITY = 0;


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        return mGestureDetector.onTouchEvent(event);
    }

    GestureDetector.SimpleOnGestureListener myGestureListener = new GestureDetector.SimpleOnGestureListener(){
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            System.out.println("开始滑动");
            float x = e1.getX()-e2.getX();
            float x2 = e2.getX()-e1.getX();
            if(x > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY){
//                Log.i(TAG,"向左手势");
                System.out.println("向左");
                nextpage();
            }
            else if(x2 > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY){
                System.out.println("向右");
                prepage();
            }

            return false;
        };
    };


}
