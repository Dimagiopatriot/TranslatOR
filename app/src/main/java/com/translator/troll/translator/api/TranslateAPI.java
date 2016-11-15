package com.translator.troll.translator.api;

import com.translator.troll.translator.model.TranslateRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by troll on 11/12/2016.
 */
public interface TranslateAPI {

    @POST("translate")
    Call<TranslateRequest> getTranslateRequestCall(@QueryMap Map<String, String> options);
}
