package com.example.sound;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;
import com.example.sound.MainActivity;

import androidx.annotation.Nullable;

import java.io.File;

public class MusicService extends Service {

    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private String currentSong;
    private File directory;
    private Context context;

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public MusicService(Context context){
        this.mediaPlayer = new MediaPlayer();
        this.directory = new File(context.getFilesDir(), "music");
        this.mediaPlayer.setOnCompletionListener(mp -> stopSelf());
        this.context = context;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    public void playMusic(String name) {
        try {
            File playFile = new File(directory, name + "/" + name + ".mp3");
            if (isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(playFile.toString());
            mediaPlayer.prepare();
            mediaPlayer.start();
            System.out.println("成功");
        }
        catch (Exception e) {
            System.out.println("失敗:" + e.getMessage());
        }
    } //playMusic


    public void playOrPause() {
        if (isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    private String getCurrentSong() {
        return currentSong;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }


}