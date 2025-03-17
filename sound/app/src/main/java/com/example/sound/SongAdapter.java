package com.example.sound;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

class Song {
    private final String name;         // 歌曲名稱
    private final String leftImagePath; // 左側圖片路徑
    private final int rightImageRes;   // 右側圖片資源 ID
    private final int isDownload;      // 下載狀態圖標資源 ID

    public Song(String name, String leftImagePath, int rightImageRes, int isDownload) {
        this.name = name;
        this.leftImagePath = leftImagePath;
        this.rightImageRes = rightImageRes;
        this.isDownload = isDownload;
    }

    public String getName() { return name; }
    public String getLeftImagePath() { return leftImagePath; }
    public int getRightImageRes() { return rightImageRes; }
    public int getIsDownload() { return isDownload; }
}

public class SongAdapter extends BaseAdapter {
    private final Context context;
    private final List<Song> songList;
    private final LayoutInflater inflater;
    private final coil.ImageLoader imageLoader;

    public SongAdapter(Context context, List<Song> songList) {
        this.context = context;
        this.songList = songList;
        this.inflater = LayoutInflater.from(context);
        this.imageLoader = new coil.ImageLoader.Builder(context).build();
    }

    @Override
    public int getCount() { return songList.size(); }
    @Override
    public Object getItem(int position) { return songList.get(position); }
    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.imageLeft = convertView.findViewById(R.id.image_left);
            holder.songName = convertView.findViewById(R.id.song_name);
            holder.imageRight = convertView.findViewById(R.id.image_right);
            holder.isDownload = convertView.findViewById(R.id.isDownload);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Song song = songList.get(position);
        holder.songName.setText(song.getName());
        holder.isDownload.setImageResource(song.getIsDownload());

        // 載入左側圖片
        File imgFile = new File(song.getLeftImagePath());
        if (imgFile.exists()) {
            coil.request.ImageRequest request = new coil.request.ImageRequest.Builder(context)
                    .data(imgFile)
                    .target(holder.imageLeft)
                    .build();
            imageLoader.enqueue(request);
        } else {
            holder.imageLeft.setImageResource(R.drawable.nofile);
        }

        return convertView;
    }

    public void clear(){
        songList.clear();
        notifyDataSetChanged();
    }

    // ViewHolder 模式提升性能
    static class ViewHolder {
        ImageView imageLeft;   // 左側圖片
        TextView songName;     // 歌曲名稱
        ImageView imageRight;  // 右側圖片
        ImageView isDownload;  // 下載狀態圖標
    }
}