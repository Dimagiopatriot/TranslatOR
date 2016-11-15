package com.translator.troll.translator;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.translator.troll.translator.api.RestApiManager;
import com.translator.troll.translator.model.Languages;
import com.translator.troll.translator.model.TranslateRequest;
import com.translator.troll.translator.utils.Constants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Languages languages = new Languages();
    String sourceLanguage;
    String targetLanguage;
    String text;

    EditText sourceText;
    EditText targetText;

    Spinner chooseSourceLang;
    Spinner chooseTargetLang;

    Button translateButton;
    ImageButton getVoiceButton;
    ImageButton playTranslate;

    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sourceText = (EditText) findViewById(R.id.sourceText);
        targetText = (EditText) findViewById(R.id.targetText);

        chooseSourceLang = (Spinner) findViewById(R.id.chooseSourceLang);
        chooseTargetLang = (Spinner) findViewById(R.id.chooseTargetLang);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.languages,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        chooseSourceLang.setAdapter(adapter);
        chooseTargetLang.setAdapter(adapter);

        translateButton = (Button) findViewById(R.id.translateButton);
        getVoiceButton = (ImageButton) findViewById(R.id.getVoice);
        playTranslate = (ImageButton) findViewById(R.id.playTranslate);
        translateButton.setOnClickListener(this);
        playTranslate.setOnClickListener(this);

    }

    private void getTargetAndCompareLanguages() {
        String selectedSourceVal = getResources().getStringArray(R.array.languages)[chooseSourceLang.getSelectedItemPosition()];
        String selectedTargetVal = getResources().getStringArray(R.array.languages)[chooseTargetLang.getSelectedItemPosition()];
        HashMap<String, String> languagesHashMap = languages.getLanguagesHash();

        sourceLanguage = languagesHashMap.get(selectedSourceVal);
        targetLanguage = languagesHashMap.get(selectedTargetVal);

    }

    private void getResponse() {
        getTargetAndCompareLanguages();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("lang", sourceLanguage + "-" + targetLanguage);
        queryMap.put("text", sourceText.getText().toString());
        queryMap.put("key", Constants.API_KEY);
        Call<TranslateRequest> translateCall = new RestApiManager().getTranslateAPI().
                getTranslateRequestCall(queryMap);

        translateCall.enqueue(new Callback<TranslateRequest>() {
            @Override
            public void onResponse(Call<TranslateRequest> call, Response<TranslateRequest> response) {
                TranslateRequest translateRequest = response.body();
                targetText.setText(translateRequest.getText().get(0));
            }

            @Override
            public void onFailure(Call<TranslateRequest> call, Throwable t) {

            }
        });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.translateButton)
            getResponse();
        if (id == R.id.playTranslate)
            speakTargetLang();
    }

    private void speakTargetLang() {
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = -2;
                    if (targetLanguage.equals("de"))
                        result = tts.setLanguage(Locale.GERMANY);
                    if (targetLanguage.equals("en"))
                        result = tts.setLanguage(Locale.UK);
                    if (targetLanguage.equals("fr"))
                        result = tts.setLanguage(Locale.FRANCE);
                    if (targetLanguage.equals("it"))
                        result = tts.setLanguage(Locale.ITALY);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    } else {
                        ConvertTextToSpeech();
                    }
                } else
                    Log.e("error", "Initilization Failed!");
            }
        });
    }

    private void ConvertTextToSpeech() {
        // TODO Auto-generated method stub
        text = targetText.getText().toString();
        if (text == null || "".equals(text)) {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        } else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    protected void onPause() {
        // TODO Auto-generated method stub

        if(tts != null){

            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }
}
