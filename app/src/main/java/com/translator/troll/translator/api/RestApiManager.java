package com.translator.troll.translator.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.translator.troll.translator.utils.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by troll on 11/12/2016.
 */
public class RestApiManager {

    private TranslateAPI translateAPI ;

    public TranslateAPI getTranslateAPI(){
        if (translateAPI==null){
            Gson gson = new GsonBuilder().create();
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .baseUrl(Constants.BASE_URL)
                    .build();
            translateAPI = retrofit.create(TranslateAPI.class);
        }
        return translateAPI;
    }

}
