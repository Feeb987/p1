package com.example.sound;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView play, next, previous, refresh, image;
    private final List<Song> songList = new ArrayList<>();
    private boolean isServiceBound = false;
    private final Context context = this;
    private TextView textView, isConnect;
    private MusicService musicService;
    private File musicDir, imagecache;
    private SongAdapter adapter;
    private ListView listView;
    private int currentIndex = 0;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent serviceIntent = new Intent(this, MusicService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);


        musicDir = new File(getFilesDir(), "music");
        if (!musicDir.exists()) musicDir.mkdirs();
        imagecache = new File(getCacheDir(), "music");
        if (!imagecache.exists()) imagecache.mkdirs();
        musicService = new MusicService(this);


        textView = findViewById(R.id.name);
        listView = findViewById(R.id.list);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        refresh = findViewById(R.id.refresh);
        image = findViewById(R.id.image);
        isConnect = findViewById(R.id.isConnect);


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicService.isPlaying()) play.setImageResource(R.drawable.play);
                else play.setImageResource(R.drawable.pause);
                musicService.playOrPause();
            }
        });

        next.setOnClickListener(v -> next());
        previous.setOnClickListener(v -> previous());
        refresh.setOnClickListener(v -> upDateListView());


        Download download = new Download(this);
        adapter = new SongAdapter(this, songList);
        listView.setAdapter(adapter);


        upDateListView();


        listView.setOnItemClickListener((parent, view, position, id) -> {
            currentIndex = position;
            String songName = songList.get(position).getName();
            File musicFile = new File(this.musicDir + "/" + songName, songName + ".mp3");
            File title = new File(this.imagecache + "/" + songName + ".jpg");

            if (!musicFile.exists() || !imagecache.exists()) {
                Toast.makeText(this, "正在下載：" + songName, Toast.LENGTH_SHORT).show();
                download.getMusic(position + 1, new Download.DataDownload() {
                    @Override
                    public void dataCallBack() {
                        runOnUiThread(() -> upDateListView());
                    }

                    @Override
                    public void failCallBack(String err) {
                        System.out.println(err);
                    }
                }); //download
            } //if
            else {
                Bitmap bitmap = BitmapFactory.decodeFile(title.getAbsolutePath());
                play.setImageResource(R.drawable.pause);
                image.setImageBitmap(bitmap);
                textView.setText(songName);
                musicService.playMusic(songName);
            } //else
        }); //listView.setOnItemClickListener

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                delFile(songList.get(position).getName());
                upDateListView();
                return true;
            }
        });

    } //onCreate


    private void next() {
        currentIndex = (currentIndex + 1) % songList.size();
        File title = new File(imagecache , songList.get(currentIndex).getName() + ".jpg");
        Bitmap bm = BitmapFactory.decodeFile(title.getAbsolutePath());
        image.setImageBitmap(bm);
        play.setImageResource(R.drawable.pause);
        textView.setText(songList.get(currentIndex).getName());
        musicService.playMusic(songList.get(currentIndex).getName());
    }


    private void previous() {
        currentIndex = (currentIndex - 1 + songList.size()) % songList.size();
        File title = new File(imagecache , songList.get(currentIndex).getName() + ".jpg");
        Bitmap bm = BitmapFactory.decodeFile(title.getAbsolutePath());
        image.setImageBitmap(bm);
        play.setImageResource(R.drawable.pause);
        textView.setText(songList.get(currentIndex).getName());
        musicService.playMusic(songList.get(currentIndex).getName());
    }





    private void upDateListView(){
        if(isNetworkAvailable()){
            isConnect.setText("連接到網路");
            Download download = new Download(this);
            adapter.clear();
            download.getData(new Download.Data() {
                @Override
                public void dataCallBack(List<String> nameList, List<String> imageList, int platBtn, List<Integer> isDownload) {
                        runOnUiThread(() -> {
                            for (int i = 0; i < nameList.size(); i++) songList.add(new Song(nameList.get(i), imageList.get(i), platBtn, isDownload.get(i)));
                            adapter.notifyDataSetChanged();
                        });
                }

                @Override
                public void failCallBack(String err) {
                    runOnUiThread(() -> {
                        Toast.makeText(context, "未連接上伺服器", Toast.LENGTH_SHORT).show();
                        notConnectWifi();
                    });

                }
            });
        }
        else {
            notConnectWifi();
        }
    }


    private void notConnectWifi(){
        isConnect.setText("未連上網路");
        Toast.makeText(this, "網路連線異常", Toast.LENGTH_SHORT).show();
        adapter.clear();
        Download download = new Download(this);
        download.savedMusic(new Download.Data() {
            @Override
            public void dataCallBack(List<String> nameList, List<String> imageList, int platBtn, List<Integer> isDownload) {
                runOnUiThread(() -> {
                    for (int i = 0; i < nameList.size(); i++) songList.add(new Song(nameList.get(i), imageList.get(i), platBtn, isDownload.get(i)));
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void failCallBack(String err) {

            }
        });
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void delFile(String name){
        File[] musicFile = (new File(musicDir, name)).listFiles();
        for (File file : musicFile) {
            file.delete();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

}