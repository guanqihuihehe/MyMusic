package com.example.music;

import android.app.Application;
import com.example.music.SongSheetActivity.MyHandler;

public class HandlerAPP extends Application {
    private SongSheetActivity.MyHandler handler = null;

    // set方法
    public void setHandler(MyHandler handler) {
        this.handler = handler;
    }

    // get方法
    public MyHandler getHandler() {
        return handler;
    }

}
