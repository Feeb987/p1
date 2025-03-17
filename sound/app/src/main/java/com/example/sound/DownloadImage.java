package com.example.sound;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import coil.ImageLoader;
import coil.request.CachePolicy;
import coil.request.ImageRequest;
import coil.target.Target;

public class DownloadImage {
    private final Context context;
    private final ImageLoader imageLoader;

    public DownloadImage(Context context) {
        this.context = context;
        this.imageLoader = new ImageLoader.Builder(context).build();
    }

    /**
     * 下載圖片並儲存到內部儲存
     * @param url 圖片 URL
     * @param fileName 檔案名稱
     */
    public void downloadImageToInternal(String url, String fileName) {
        ImageRequest request = new ImageRequest.Builder(context)
                .data(url)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .target(new Target() {
                    @SuppressLint("WrongThread")
                    @Override
                    public void onSuccess(@NonNull Drawable result) {
                        File directory = new File(context.getFilesDir(), "music/" + fileName);
                        File cache = new File(context.getCacheDir(), "music/" + fileName + ".jpg");
                        if(cache.exists()) cache.delete();
                        if (!directory.exists()) directory.mkdirs();
                        File file = new File(directory, fileName + ".jpg");


                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            Bitmap bitmap = ((BitmapDrawable) result).getBitmap();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        } catch (IOException e) {
                            System.out.println("儲存圖片失敗：" + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(@Nullable Drawable error) {
                        System.out.println("圖片下載失敗");
                    }
                })
                .build();
        imageLoader.enqueue(request);
    }
}