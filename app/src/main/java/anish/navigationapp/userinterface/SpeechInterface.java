package anish.navigationapp.userinterface;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import anish.navigationapp.MainActivity;

public class SpeechInterface extends UtteranceProgressListener implements TextToSpeech.OnInitListener, RecognitionListener {
    private Context context;
    private boolean speechReady = false;
    private TextToSpeech tts;
    private SpeechCallback callback;
    private int n = 0;
    private SpeechRecognizer r;
    public boolean speaking = false;

    public static final int REQ_ID = 349827;

    private String last = "";

    public SpeechInterface(Context context){
        this.context = context;
        r = SpeechRecognizer.createSpeechRecognizer(context);
        r.setRecognitionListener(this);
    }

    public void setCallback(SpeechCallback callback) {
        this.callback = callback;
    }

    // Voice recognition methods
    public void recognize() {
        final Intent i = new Intent();
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        MainActivity.ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                r.startListening(i);
            }
        });
    }


    // Text to speech related methods
    public void initTTS(){
        tts = new TextToSpeech(context, this);
        tts.setSpeechRate(0.85f);
    }

    public void endTTS(){
        tts.stop();
        tts.shutdown();
        tts = null;
        speechReady = false;
    }

    public boolean isTTSReady(){
        return speechReady;
    }

    public String speak(String s){
        speaking = true;
        last = s;
        if(!isTTSReady()){
            if(callback != null) callback.speechError(1, 0);
            return null;
        }
        n++;
        tts.speak(s, TextToSpeech.QUEUE_ADD, null, s+n);
        tts.setOnUtteranceProgressListener(this);
        return s+n;
    }

    public String repeat(){
        speaking = true;
        if(!isTTSReady()){
            if(callback != null) callback.speechError(1, 0);
            return null;
        }
        n++;
        tts.speak(last, TextToSpeech.QUEUE_ADD, null, last+n);
        tts.setOnUtteranceProgressListener(this);
        return last+n;
    }

    @Override
    public void onError(int error) {
        Log.e("Rec error", ""+error);
        if(error == 7) return;
        if(callback != null) callback.recognitionError();
    }

    @Override
    public void onResults(Bundle results) {
        Log.e("result", results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));
        if(callback != null) callback.result(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0));
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            speechReady = true;
        }else{
            if(callback != null) callback.speechError(0, status);
            tts = null;
        }
    }

    @Override
    public void onDone(String utteranceId) {
        speaking = false;
        callback.doneSpeaking(utteranceId);
    }

    @Override
    public void onStart(String utteranceId) {

    }

    @Override
    public void onError(String utteranceId) {
        callback.speechError(2, 0);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}
