package com.translator.troll.translator.model;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by troll on 11/12/2016.
 */
public class TranslateRequest {
    @Expose
    List<String> text;

    public List<String> getText() {
        return text;
    }
}
