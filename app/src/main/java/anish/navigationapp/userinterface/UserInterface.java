package anish.navigationapp.userinterface;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import anish.navigationapp.location.Coordinate;
import anish.navigationapp.location.Location;
import anish.navigationapp.location.LocationManager;
import anish.navigationapp.navigation.Map;

public class UserInterface implements SpeechCallback{

    enum UserState {
        OFF, // Not in building
        ENTERING, // Entering building / Map downloading
        INACTIVE, // Idle in building
        NAV_PROMPT, // Prompt user if they want to navigate
        DESTINATION_LIST, // Selecting destination number
        DESTINATION_CONFIRM, // Confirm Destination
        NAVIGATING // Navigation in progress
    }

    Context ctx;
    SpeechInterface s;
    UserState state = UserState.OFF;
    ArrayList<String> recognizeWhenDone = new ArrayList<>();
    Map m;
    ArrayList<Location> remainingLocations;
    ArrayList<Location> currentLocOpts;
    long lastInfo = 0;
    MapManager callback;
    Location dest;
    Coordinate start = null;
    public boolean pauseSound = false;

    public UserInterface(Activity ctx) {
        s = new SpeechInterface(ctx);
        s.setCallback(this);
        s.initTTS();
        this.ctx = ctx;
    }

    public void setCallback(MapManager m){
        this.callback = m;
    }

    // User Input
    @Override
    public void result(String r) {
        if(r.length() < 1){
            tryAgain();
            return;
        }

        if(r.contains("repeat")) repeat();
        if(r.contains("cancel")) state = UserState.INACTIVE;

        if(state == UserState.ENTERING){
            s.speak("Please wait for map to finish downloading");
        }
        else if(state == UserState.NAV_PROMPT){
            int val = getNumber(r, 4);
            switch(val){
                case 1:
                    state = UserState.INACTIVE;
                    break;
                case 2:
                    destinationList();
                    break;
                case 3:
                    exit();
                    break;
                case 4:
                    recognizeWhenDone.add(s.repeat());
                    break;
            }
        }
        else if(state == UserState.DESTINATION_LIST) {
            int val = getNumber(r, 6);
            if (val == 6) destinationList();
            if (val >= currentLocOpts.size() || val == 0) {
                recognizeWhenDone.add(s.speak("Please choose a valid option"));
            } else {
                Location l = currentLocOpts.get(val);
                LocationManager m = LocationManager.getInstance();
                if (m == null) {
                    s.speak("Location not available");
                } else {
                    if (m.getMap().getVal((int) l.getX(), (int) l.getY() + 1) > 0 &&
                            m.getMap().getVal((int) l.getX(), (int) l.getY() + 1) < 100000) {
                        if (callback != null)
                            navigate(new Location(l.getX(), l.getY() + 1, 1, l.getName()), true);

                    } else if (m.getMap().getVal((int) l.getX(), (int) l.getY() - 1) > 0 &&
                            m.getMap().getVal((int) l.getX(), (int) l.getY() - 1) < 100000) {
                        if (callback != null)
                            navigate(new Location(l.getX(), l.getY() - 1, 1, l.getName()), true);

                    } else if (m.getMap().getVal((int) l.getX() + 1, (int) l.getY()) > 0 &&
                            m.getMap().getVal((int) l.getX() + 1, (int) l.getY()) < 100000) {
                        if (callback != null)
                            navigate(new Location(l.getX() + 1, l.getY(), 1, l.getName()), true);

                    } else if (m.getMap().getVal((int) l.getX() - 1, (int) l.getY()) > 0 &&
                            m.getMap().getVal((int) l.getX() - 1, (int) l.getY()) < 100000) {
                        if (callback != null)
                            navigate(new Location(l.getX() - 1, l.getY(), 1, l.getName()), true);
                    }
                }
            }
        }
        else if(state == UserState.DESTINATION_CONFIRM) {
            boolean resp;
            try{
                resp = getYN(r);
            }catch(Exception e){
                navigate(dest, false);
                return;
            }
            if(resp){
                if(callback != null) callback.navigate(dest);
                state = UserState.NAVIGATING;
            } else {
                s.speak("Navigation has been cancelled. ");
                mainMenu();
            }
        }
        else if(state == UserState.NAVIGATING) {
            boolean resp;
            try{
                resp = getYN(r);
            }catch(Exception e){
                recognizeWhenDone.add(s.speak("Please choose a valid option"));
                return;
            }
            if(resp){
                if(callback != null) callback.cancelNav();
                state = UserState.INACTIVE;
                s.speak("Navigation has been cancelled. ");
                pauseSound = false;
                mainMenu();
            } else {
                s.speak("Continuing Navigation");
                pauseSound = false;
            }
        }
    }

    private void navigate(Location dest, boolean valid){
        this.dest = dest;
        state = UserState.DESTINATION_CONFIRM;
        recognizeWhenDone.add(s.speak((valid ? "" : "Please choose a valid option. ")+
                "Are you sure you want to navigate to "+dest.getName()+". Say yes or no."));
    }

    private int getNumber(String r, int max){
        if(max > 6) return 0;
        int val = 0;
        if(r.contains("1") || r.contains("one")){
            val = 1;
        }
        if(r.contains("2") || r.contains("two") || r.contains("too") || r.contains("to")){
            val = 2;
        }
        if(r.contains("3") || r.contains("three") || r.contains("tree")){
            val = 3;
        }
        if(r.contains("4") || r.contains("four") || r.contains("for") || r.contains("far")){
            val = 4;
        }
        if(r.contains("5") || r.contains("five") || r.contains("fife")){
            val = 5;
        }
        if(r.contains("6") || r.contains("six")){
            val = 6;
        }
        if(val > max) return 0;
        return val;
    }

    private boolean getYN(String r) throws Exception{
        if(r.contains("yes") || r.contains("yas") || r.contains("yus") || r.contains("y")){
            return true;
        }
        if(r.contains("no") || r.contains("n") || r.contains("nope") || r.contains("not") || r.contains("cancel")){
            return false;
        }
        throw new Exception("No valid response");
    }

    public boolean isTTSReady(){
        return s.isTTSReady();
    }

    public String speak(String text){
        return s.speak(text);
    }

    @Override
    public void doneSpeaking(String id) {
        if(recognizeWhenDone.contains(id)){
            recognizeWhenDone.remove(id);
            s.recognize();
        }
    }

    void exit(){
        if(start == null){
            s.speak("Please wait until navigation is ready");
            mainMenu();
            return;
        }
        s.speak("Navigating to the exit where you entered from");
        Location exit = null;
        for(Location l : m.getLocations()){
            if(l.getType() == 0){
                if(exit == null || distance(start, l) < distance(start, exit)){
                    exit = l;
                }
            }
        }
        if(exit == null){
            s.speak("Internal Map error. Please try again.");
            return;
        }

        LocationManager m = LocationManager.getInstance();
        if (m.getMap().getVal((int) exit.getX(), (int) exit.getY() + 1) > 0 &&
                m.getMap().getVal((int) exit.getX(), (int) exit.getY() + 1) < 100000) {
            if (callback != null)
                navigate(new Location(exit.getX(), exit.getY() + 1, 1, exit.getName()), true);

        } else if (m.getMap().getVal((int) exit.getX(), (int) exit.getY() - 1) > 0 &&
                m.getMap().getVal((int) exit.getX(), (int) exit.getY() - 1) < 100000) {
            if (callback != null)
                navigate(new Location(exit.getX(), exit.getY() - 1, 1, exit.getName()), true);

        } else if (m.getMap().getVal((int) exit.getX() + 1, (int) exit.getY()) > 0 &&
                m.getMap().getVal((int) exit.getX() + 1, (int) exit.getY()) < 100000) {
            if (callback != null)
                navigate(new Location(exit.getX() + 1, exit.getY(), 1, exit.getName()), true);

        } else if (m.getMap().getVal((int) exit.getX() - 1, (int) exit.getY()) > 0 &&
                m.getMap().getVal((int) exit.getX() - 1, (int) exit.getY()) < 100000) {
            if (callback != null)
                navigate(new Location(exit.getX() - 1, exit.getY(), 1, exit.getName()), true);
        }
    }

    long distance(Coordinate c, Location l){
        return Math.abs(c.getX() - l.getX()) + Math.abs(c.getY() - l.getY());
    }

    public void startLocation(Coordinate c){
        this.start = c;
    }

    void destinationList(){
        state = UserState.DESTINATION_LIST;
        if(remainingLocations == null){
            Location[] destinations = m.getLocations();
            remainingLocations = new ArrayList<>(Arrays.asList(destinations));
            for(int i = 0; i < remainingLocations.size(); i++){
                if(remainingLocations.get(i).getType() == 0) remainingLocations.remove(i);
            }
        }

        if(remainingLocations.size() > 5){
            currentLocOpts = new ArrayList<>();
            currentLocOpts.add(null); // 0 index is blank
            for(int i = 1; i <= 5; i++){
                Location l = remainingLocations.get(i);
                remainingLocations.remove(i);
                s.speak(String.format("Say %d to navigate to %s", i, l.getName()));
                currentLocOpts.add(l);
            }
            for(int i = 0; i < 5; i++) remainingLocations.remove(i);
            s.speak("Say 6 for more options");
        }else{
            int i = 0;
            currentLocOpts = new ArrayList<>();
            currentLocOpts.add(null); // 0 index is blank
            for(Location l : remainingLocations){
                i++;
                s.speak(String.format("Say %d to navigate to %s", i, l.getName()));
                currentLocOpts.add(l);
            }
            remainingLocations = null;
        }
        recognizeWhenDone.add(s.speak("Please choose an option"));
    }

    void mainMenu(){
        state = UserState.NAV_PROMPT;
        recognizeWhenDone.add(s.speak("Please choose an option. Say 1 if you would not like to navigate. Say 2 if you would like to hear a list of destinations. Say 3 if you would like to find an exit. Say 4 if you would like to hear this prompt again."));
    }

    public void arrived(){
        s.speak("You have arrived at your destination. Press the button if you would like to navigate again");
        state = UserState.INACTIVE;
    }

    void tryAgain(){
        s.speak("Your command was not understood. Press the button and try again");
    }

    public void enterBuilding(){
        state = UserState.ENTERING;
        if(System.nanoTime() - lastInfo >= 60000000000L) s.speak("You have entered a building with navigation features. The map will now begin downloading. Please do not move further into the building.");
        remainingLocations = null;
    }

    public void downloadComplete(Map m){
        state = UserState.NAV_PROMPT;
        s.speak("Your map has downloaded.");
        mainMenu();
        this.m = m;
    }

    private void repeat(){
        s.repeat();
    }

    public void userInterrupt(){
        if(s.speaking) return;
        if(state == UserState.INACTIVE){
            mainMenu();
            return;
        } else if (state == UserState.NAVIGATING){
            pauseSound = true;
            recognizeWhenDone.add(s.speak("Would you like to cancel navigation? Say yes or no."));
            return;
        }
        if(state != UserState.OFF) s.recognize();
        else s.speak("Please enter a supported building to use this app");
    }

    // Util
    @Override
    public void recognitionError() {
        s.speak("Speech Recognition error. Please try again");
    }

    public void btn1(){
        userInterrupt();
    }

    @Override
    public void speechError(int type, int status) {
        String error = "";
        switch (type){
            case 0:
                error = "Init error: "+status;
                break;
            case 1:
                error = "Not ready";
                break;
            case 2:
                error = "Speech error";
                break;
        }
        Log.e("Speech error", error);
    }
}
