package anish.navigationapp.userinterface;

public interface SpeechCallback {
    void speechError(int type, int status); // 0 = init, 1 = not ready
    void doneSpeaking(String id);
    void result(String r);
    void recognitionError();
}

