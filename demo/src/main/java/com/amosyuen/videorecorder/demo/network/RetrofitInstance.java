package com.amosyuen.videorecorder.demo.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {

    //private static final String BASE_URL = "http://192.168.1.26:4700/talentcruz/api/";
    public static final String BASE_URL = "http://203.76.249.210:4700/talentcruz-api/";
    //public static final String BASE_URL="http://192.168.1.11:3000/talentcruz-api/";

    //public static final String IMAGE_URL="http://192.168.1.11:3000/";

    public static final String IMAGE_URL="http://203.76.249.210:4700/";


   // http://localhost:3000/talentcruz-api/public/

    private Retrofit retrofit = null;
    /**
     * This method creates a new instance of the API interface.
     *
     * @return The API interface
     */


    public ApiService getAPI() {

        if (retrofit == null) {
            retrofit = new Retrofit
                    .Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(ApiService.class);
    }


    public static Retrofit getImageRetrofit() {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(50, TimeUnit.SECONDS)
                .connectTimeout(50, TimeUnit.SECONDS)
                .build();
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

}