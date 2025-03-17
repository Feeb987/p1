package com.example.sound;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import coil.ImageLoader;
import coil.request.CachePolicy;
import coil.request.ImageRequest;
import coil.target.Target;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 下載音樂儲存格式
 */
class MusicItem {
    String image;
    String mus;
    String name;
}

/**
 * 下載(多個)音樂儲存格式
 */
class Music {
    Map<String, List<MusicItem>> musicMap;
}

public class Download {

    /**
     * 只獲取所有資料時的回調介面
     */
    public interface Data {
        /**
         * 資料成功獲取時回調此方法
         * @param nameList 所有歌曲名稱
         * @param imageList 所有歌曲圖片網址
         * @param platBtn 播放圖示
         * @param isDownload 所有歌曲是否下載
         */
        void dataCallBack(List<String> nameList, List<String> imageList, int platBtn, List<Integer> isDownload);
        /**
         * 資料獲取失敗時回調此方法
         * @param err 錯誤資訊
         */
        void failCallBack(String err);
    }

    /**
     *獲取特定資料時的回調介面
     */
    public interface DataDownload {
        /**
         * 因為資料直接儲存在程式內部，所以回調時不需回傳資料
         */
        void dataCallBack();
        void failCallBack(String err);
    }

    public Download(Context context) {
        this.context = context;
        this.mp3 = new DownloadMp3(context);
        this.image = new DownloadImage(context);
        this.directory = new File(context.getFilesDir(), "music/");
        this.imageLoader = new ImageLoader.Builder(context).okHttpClient(client).build();
    }

    private static final String url = "http://10.6.9.8:5001/music";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Context context;
    private final ImageLoader imageLoader;
    private final DownloadMp3 mp3;
    private final DownloadImage image;
    private final File directory;
    private final int platBtn = R.drawable.play;
    private Music music;
    private String key;

    /**
     * 獲取所有音樂資料(不包含音檔)
     * @param callback 回調介面
     */
    public void getData(Data callback) {
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.failCallBack("網路請求失敗：" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        callback.failCallBack("伺服器錯誤：" + response.code());
                        return;
                    }


                    String jsonData = response.body().string();
                    music = gson.fromJson(jsonData, Music.class);
                    Map<String, List<MusicItem>> musicMap = music.musicMap;


                    if (musicMap == null) {
                        callback.failCallBack("JSON 解析失敗");
                        return;
                    }


                    List<String> nameList = new ArrayList<>();
                    List<Integer> isDownloadList = new ArrayList<>();
                    List<String> imegeList = new ArrayList<>();
                    MusicItem item;


                    for (String key : musicMap.keySet()) {
                        item = musicMap.get(key).get(0);
                        loadImage(item.image, item.name);
                    }
                    for (String key : musicMap.keySet()) {
                        item = musicMap.get(key).get(0);
                        nameList.add(item.name);
                        imegeList.add(context.getCacheDir() + "/music/" + item.name + ".jpg");
                        isDownloadList.add((new File(directory + "/" + item.name + "/" + item.name + ".mp3").exists())? R.drawable.download : R.drawable.undownload);
                    }


                    callback.dataCallBack(nameList, imegeList, platBtn, isDownloadList);
                } //try
                catch (Exception e) {
                    callback.failCallBack("資料處理錯誤：" + e.getMessage());
                } //catch
            } //onResponse
        }); //client
    } //getData

    /**
     * 獲取特定音樂資料(包含音檔)
     * @param index 音樂編號
     * @param callback 回調介面
     */
    public void getMusic(int index, DataDownload callback) {
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println("網路請求失敗：" + e.getMessage()); // 🔥 傳遞錯誤
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        System.out.println("伺服器錯誤：" + response.code());
                        return;
                    }


                    String str = response.body().string();
                    music = gson.fromJson(str, Music.class);
                    Map<String, List<MusicItem>> musicMap = music.musicMap;


                    if (musicMap == null) {
                        System.out.println("JSON 解析失敗");
                        return;
                    }


                    if (index > 0) {
                        key = "mus" + index;
                        if (musicMap.containsKey(key)) {
                            MusicItem item = musicMap.get(key).get(0);
                            mp3.Mp3download(item.mus, item.name);
                            image.downloadImageToInternal(item.image, item.name);
                            callback.dataCallBack();
                        }
                    } //if
                } //try
                catch (Exception e) {
                    callback.failCallBack("資料處理錯誤：" + e.getMessage());
                } //catch
            } //onResponse
        }); //client
    } //getMusic

    /**
     * 下載圖檔到快取
     * @param url 圖檔網址
     * @param fileName 圖檔名稱
     */
    private void loadImage(String url, String fileName) {
        ImageRequest request = new ImageRequest.Builder(context)
                .data(url)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .target(new Target() {
                    @SuppressLint("WrongThread")
                    @Override
                    public void onSuccess(@Nullable Drawable result) {
                        File cache = new File(context.getCacheDir(), "music/");
                        if (!cache.exists()) cache.mkdirs();
                        File file = new File(cache, fileName + ".jpg");


                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            Bitmap bitmap = ((BitmapDrawable) result).getBitmap();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        } //try
                        catch (IOException e) {
                            System.out.println("儲存圖片錯誤：" + e.getMessage());
                        } //catch
                    } //onSuccess
                }) //target
                .build();
        imageLoader.enqueue(request);
    } //loadImage

    public void savedMusic(Data callback){
        List<String> nameList = new ArrayList<>();
        List<String> imageList = new ArrayList<>();
        List<Integer> isDownloadList = new ArrayList<>();
        if (directory.exists()){
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    File musicPath = new File(directory + "/" + file.getName(), file.getName());
                    if(!(new File(musicPath + ".mp3")).exists()) continue;
                    nameList.add(file.getName());
                    imageList.add(musicPath + ".jpg");
                    isDownloadList.add(R.drawable.download);
                }
                callback.dataCallBack(nameList, imageList, platBtn, isDownloadList);
            }
        }
    }

} //download