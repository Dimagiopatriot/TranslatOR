package com.translator.troll.translator.views;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.translator.troll.translator.R;
import com.translator.troll.translator.api.RestApiManager;
import com.translator.troll.translator.model.Languages;
import com.translator.troll.translator.model.TranslateRequest;
import com.translator.troll.translator.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Languages languages;
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
    ImageButton langChange;

    TextToSpeech tts;
    SharedPreferences sPref;
    SharedPreferences.Editor ed;
    int tap_pressed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sPref = getPreferences(MODE_PRIVATE);
        ed = sPref.edit();
        tap_pressed = sPref.getInt(Constants.BACK_PRESSED_TAP, 0);

        languages = new Languages(getResources().getStringArray(R.array.languages));

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
        langChange = (ImageButton) findViewById(R.id.changeLang);

        translateButton.setOnClickListener(this);
        getVoiceButton.setOnClickListener(this);
        playTranslate.setOnClickListener(this);
        langChange.setOnClickListener(this);

    }

    /**
     * This method is initialize source and target languages as checkboxes lang choosen
     */
    private void getTargetAndCompareLanguages() {
        String selectedSourceVal = getResources().getStringArray(R.array.languages)[chooseSourceLang.getSelectedItemPosition()];
        String selectedTargetVal = getResources().getStringArray(R.array.languages)[chooseTargetLang.getSelectedItemPosition()];
        HashMap<String, String> languagesHashMap = languages.getLanguagesHash();

        sourceLanguage = languagesHashMap.get(selectedSourceVal);
        targetLanguage = languagesHashMap.get(selectedTargetVal);

    }

    /**
     * Getting response from server and output translate if Internet connection is good
     */
    private void getResponse() {
        getTargetAndCompareLanguages();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("lang", sourceLanguage + "-" + targetLanguage);
        queryMap.put("text", sourceText.getText().toString());
        queryMap.put("key", Constants.API_KEY);
        Call<TranslateRequest> translateCall = new RestApiManager().getTranslateAPI().
                getTranslateRequestCall(queryMap);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.progress_message));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        translateCall.enqueue(new Callback<TranslateRequest>() {
            @Override
            public void onResponse(Call<TranslateRequest> call, Response<TranslateRequest> response) {
                TranslateRequest translateRequest = response.body();
                if (translateRequest.getText() == null)
                    targetText.setText("");
                targetText.setText(translateRequest.getText().get(0));
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<TranslateRequest> call, Throwable t) {
                Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });
    }


    /**
     * @param v - view, which have been pressed
     *          Handle elements clicking
     */

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.translateButton)
            getResponse();
        if (id == R.id.playTranslate)
            speakTargetLang();
        if (id == R.id.changeLang) {
            int positionForSourceLang = chooseTargetLang.getSelectedItemPosition();
            int positionForTargetLang = chooseSourceLang.getSelectedItemPosition();
            chooseSourceLang.setSelection(positionForSourceLang);
            chooseTargetLang.setSelection(positionForTargetLang);

            getResponse();
        }
        if (id == R.id.getVoice) {
            convertSpeechToText();
        }
    }

    /**
     * Audio output
     */
    private void speakTargetLang() {
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS && targetLanguage != null) {
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
                        convertTextToSpeech();
                    }
                } else
                    Log.e("error", "Initilization Failed!");
            }
        });
    }

    /**
     * Speech converting
     */
    private void convertTextToSpeech() {
        text = targetText.getText().toString();
        if (text == null || "".equals(text)) {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        } else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    /**
     * Text speech
     */
    private void convertSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        getTargetAndCompareLanguages();
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLanguage);
        try {
            startActivityForResult(intent, Constants.SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {

        if (tts != null) {

            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> resultString = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            sourceText.setText(resultString.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (tap_pressed < 3) {
            AlertDialog.Builder productMark = new AlertDialog.Builder(this);
            productMark.setMessage(getResources().getString(R.string.mark_question))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.positive_answer), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=com.translator.troll.translator"));
                            tap_pressed++;
                            ed.putInt(Constants.BACK_PRESSED_TAP, tap_pressed);
                            ed.commit();
                            startActivity(intent);
                            MainActivity.this.finish();

                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.negative_answer), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            tap_pressed++;
                            ed.putInt(Constants.BACK_PRESSED_TAP, tap_pressed);
                            ed.commit();
                            MainActivity.this.finish();
                        }
                    });
            AlertDialog alert = productMark.create();
            alert.show();
        } else
            super.onBackPressed();
    }
}
