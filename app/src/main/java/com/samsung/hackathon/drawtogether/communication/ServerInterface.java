package com.samsung.hackathon.drawtogether.communication;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.samsung.hackathon.drawtogether.App;
import com.samsung.hackathon.drawtogether.ui.model.ArtworkItem;
import com.samsung.hackathon.drawtogether.util.FileHelper;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Hi.JiGOO on 16. 9. 14..
 */
public class ServerInterface {

    private static ServerInterface mInstance = new ServerInterface();

    public static final String BASE_URL = "http://192.168.43.225:3100/";
    public static final String DOWNLOAD_URL = "http://192.168.43.225:3100/download/";
//    public static final String BASE_URL = "http://draw-together-server-minegi.c9users.io/";
//    public static final String DOWNLOAD_URL =
//            "http://draw-together-server-minegi.c9users.io/download/";

    private Retrofit mRetrofit;
    private ServerAPI mServerAPI;

    public interface ServerApiEventListener<T> {
        void onResponse(Response<T> response);
        void onFailure(Throwable t);
    }

    static public ServerInterface getInstance() {
        return mInstance;
    }

    private ServerInterface() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mServerAPI = mRetrofit.create(ServerAPI.class);
    }

    public void getArtworkList(final ServerApiEventListener<List<ArtworkItem>> mServerApiEventListener) {
        Call<List<ArtworkItem>> list =  mServerAPI.getFileList();
        list.enqueue(new Callback<List<ArtworkItem>>() {
            @Override
            public void onResponse(Call<List<ArtworkItem>> call, Response<List<ArtworkItem>> response) {
                mServerApiEventListener.onResponse(response);
            }

            @Override
            public void onFailure(Call<List<ArtworkItem>> call, Throwable t) {
                mServerApiEventListener.onFailure(t);
            }
        });
    }

    public void uploadFile(Context context, final String fileName, Uri fileUri,
                           final ServerApiEventListener<ResponseBody> mServerApiEventListener) {
        File file = new File(FileHelper.getInstance().getRealPathFromUri(context, fileUri));
        uploadFile(fileName, file, mServerApiEventListener);
    }

    public void uploadFile(final String fileName, File file,
                           final ServerApiEventListener<ResponseBody> mServerApiEventListener) {

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploadFile", file.getName(), requestFile);

        Call<ResponseBody> call = mServerAPI.uploadFile(fileName, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                mServerApiEventListener.onResponse(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mServerApiEventListener.onFailure(t);
            }
        });
    }

    private void uploadFile(final String fileName, File file1, File file2,
                            final ServerApiEventListener<ResponseBody> mServerApiEventListener) {
        RequestBody requestFile1 = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
        MultipartBody.Part body1 = MultipartBody.Part.createFormData("uploadFile1", file1.getName(), requestFile1);

        RequestBody requestFile2 = RequestBody.create(MediaType.parse("multipart/form-data"), file2);
        MultipartBody.Part body2 = MultipartBody.Part.createFormData("uploadFile2", file2.getName(), requestFile2);

        Call<ResponseBody> call = mServerAPI.uploadFile(fileName, body1, body2);
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                mServerApiEventListener.onResponse(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mServerApiEventListener.onFailure(t);
            }
        });

    }

    public void uploadFile(final File thumbnailFile, final String thumbnailFileName,
                           final byte[] strokeData, final String strokeDataFileName,
                           final String fileName,
                           final ServerApiEventListener<ResponseBody> fileUploadEventListener) {
        final RequestBody requestFile1 =
                RequestBody.create(MediaType.parse("multipart/form-data"), thumbnailFile);
        final MultipartBody.Part body1 =
                MultipartBody.Part.createFormData("uploadFile1", thumbnailFileName, requestFile1);
        final RequestBody requestFile2 =
                RequestBody.create(MediaType.parse("multipart/form-data"), strokeData);
        final MultipartBody.Part body2 =
                MultipartBody.Part.createFormData("uploadFile2", strokeDataFileName, requestFile2);

        Call<ResponseBody> call = mServerAPI.uploadFile(fileName, body1, body2);
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                fileUploadEventListener.onResponse(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                fileUploadEventListener.onFailure(t);
            }
        });
    }

    public void downloadFile(final String fileName, final String fileDir, final ServerApiEventListener<ResponseBody> mServerApiEventListener) {

        final Handler handler = new Handler();

        Call<ResponseBody> call = mServerAPI.downloadFile(fileName);
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    App.L.d("server contacted and has strokeData");
                    String header = response.headers().get("Content-Disposition");
                    String fileName = header.replace("attachment; filename=", "").replace("\"", "");
                    final String filePath = fileDir + File.separator + fileName;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final boolean result = FileHelper.getInstance().writeResponseBodyToDisk(response.body(), filePath);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (result == true) {
                                        mServerApiEventListener.onResponse(null);
                                    } else {
                                        mServerApiEventListener.onFailure(new Throwable("fail to write response body to disk"));
                                    }
                                }
                            });
                        }

                    }).start();

                } else {
                    App.L.d("server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, final Throwable t) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mServerApiEventListener.onFailure(t);
                    }
                });
            }
        });

//        Call<ResponseBody> call = mServerAPI.downloadFile(fileName);
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                mServerApiEventListener.onResponse(response);
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                mServerApiEventListener.onFailure(t);
//            }
//        });
    }



}
