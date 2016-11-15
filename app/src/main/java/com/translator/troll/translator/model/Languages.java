package com.translator.troll.translator.model;

import java.util.HashMap;

/**
 * Created by troll on 11/12/2016.
 */
public class Languages {
    String[] langArray;
    HashMap<String, String> languagesHash = new HashMap<>();

    public Languages(String[] langArray) {
        this.langArray = langArray;
        putHash();
    }

    private void putHash() {
        languagesHash.put(langArray[0], "en");
        languagesHash.put(langArray[1], "uk");
        languagesHash.put(langArray[2], "ru");
        languagesHash.put(langArray[3], "it");
        languagesHash.put(langArray[4], "fr");
        languagesHash.put(langArray[5], "de");
    }

    public HashMap<String, String> getLanguagesHash() {
        return languagesHash;
    }
}
