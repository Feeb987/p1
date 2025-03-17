package com.example.sound;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DownloadMp3 {
    private final Context context;
    private final OkHttpClient client;

    public DownloadMp3(Context context) {
        this.context = context;
        this.client = UnsafeOkHttpClient.getUnsafeOkHttpClient();
    }

    /**
     * 下載 MP3 檔案
     * @param url MP3 的 URL
     * @param name 檔案名稱
     */
    public void Mp3download(String url, String name) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("下載失敗：" + response.code());
                return;
            }
            ResponseBody body = response.body();
            if (body != null) {
                saveFile(body, name);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("下載錯誤：" + e.getMessage());
        }
    }

    /**
     * 將 MP3 儲存到內部儲存
     * @param body 回應內容
     * @param fileName 檔案名稱
     */
    private void saveFile(ResponseBody body, String fileName) {
        File directory = new File(context.getFilesDir(), "music/" + fileName);
        if (!directory.exists()) directory.mkdirs();

        File file = new File(directory, fileName + ".mp3");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(body.bytes());
            fos.flush();
            System.out.println("下載成功：" + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("儲存失敗：" + e.getMessage());
        }
    }
}

    /**
     *下載未經安全檢測資料
     */
class UnsafeOkHttpClient {
    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
                    }
            };
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}