package com.samsung.hackathon.drawtogether.communication;

import com.samsung.hackathon.drawtogether.ui.model.ArtworkItem;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

/**
 * Created by Hi.JiGOO on 16. 9. 14..
 */
public interface ServerAPI {

    @GET("filelist")
    Call<List<ArtworkItem>> getFileList();

//    @Multipart
//    @POST("upload/{filename}")
//    Call<ResponseBody> uploadFile(@Path("filename") String filename, @Part MultipartBody.Part strokeData);

    @Multipart
    @POST("upload/{filename}")
    Call<ResponseBody> uploadFile(@Path("filename") String fileName, @Part MultipartBody.Part file);

    @Multipart
    @POST("upload/{filename}")
    Call<ResponseBody> uploadFile(@Path("filename") String fileName,
                                  @Part MultipartBody.Part file1, @Part MultipartBody.Part file2);

    @Streaming
    @GET("download/{filename}")
    Call<ResponseBody> downloadFile(@Path("filename") String filename);

}
