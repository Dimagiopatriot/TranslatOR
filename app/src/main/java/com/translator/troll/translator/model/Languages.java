package com.translator.troll.translator.model;

import java.util.HashMap;

/**
 * Created by troll on 11/12/2016.
 */
public class Languages {
    HashMap<String, String> languagesHash = new HashMap<>();

    public Languages(){
        putHash();
    }

    private void putHash(){
        languagesHash.put("Английский","en");
        languagesHash.put("Украинский", "uk");
        languagesHash.put("Русский", "ru");
        languagesHash.put("Итальянский", "it");
        languagesHash.put("Французский", "fr");
        languagesHash.put("Немецкий", "de");
    }

    public HashMap<String, String> getLanguagesHash() {
        return languagesHash;
    }
}
