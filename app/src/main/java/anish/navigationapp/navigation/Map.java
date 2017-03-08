package anish.navigationapp.navigation;

import android.util.Base64;
import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Arrays;

import anish.navigationapp.location.Coordinate;
import anish.navigationapp.location.Location;

public class Map {
    private int[][] dataArray;
    private Location[] locations;
    private int direction = 0;

    public static Map fromString(String s){
        if(!s.contains(";")) return null;
        String k = s.split(";")[1];
        Log.w("NUMBER", k+", "+Arrays.toString(k.getBytes()));
        s = new String(Base64.decode(s.split(";")[0], Base64.DEFAULT));
        if(!s.contains(";:;")) return null;
        String[] data = s.split(";:;");
        Map m = new Map();
        m.direction = Integer.parseInt(k);

        String[] rows = data[0].replace("[[", "").replace("]]", "").split("],\\[");

        int row = rows.length;
        int col = rows[0].split(",").length;

        m.dataArray = new int[row][col];

        for(int i = 0; i < rows.length; i++){
            String[] cols = rows[i].split(",");
            for(int j = 0; j < cols.length; j++){
                int x = Integer.parseInt(cols[j]);
                m.dataArray[i][j] = x;
            }
        }


        JSONParser parser = new JSONParser();
        String[] loc = data[1].replace("[", "").replace("]", "").split(",(?![^\\{]*\\})");
        m.locations = new Location[loc.length];

        for(int i = 0; i < loc.length; i++){
            String locationStr = loc[i];
            System.out.println(locationStr);
            JSONObject location;
            try{
                location = (JSONObject) parser.parse(locationStr);
            }catch(ParseException e){
                e.printStackTrace();
                Log.e("Error", "Parse Error");
                return null;
            }
            Log.d("loc", location.toJSONString());
            Location l = new Location((Long)location.get("x"), (Long)location.get("y"), (Long)location.get("type"), (String)location.get("name"));
            m.locations[i] = l;
        }

        return m;
    }

    public int getVal(int x, int y) {
        return dataArray[x][y];
    }

    public Location[] getLocations() {
        return locations;
    }

    public Location getLocation(int n) {
        return locations[n];
    }

    public int getRows(){
        return dataArray.length;
    }

    public int getCols(){
        return dataArray[0].length;
    }

    public int getDirectionOffset() {
        return direction;
    }

    public Coordinate[] getNeighbors(Coordinate c){
        ArrayList<Coordinate> n = new ArrayList<>();
        int x = (int) c.getX();
        int y = (int) c.getY();

        if(getVal(x+1, y) > 0 && getVal(x+1, y) < 100000) n.add(new Coordinate(x+1, y));
        if(getVal(x, y+1) > 0 && getVal(x, y+1) < 100000) n.add(new Coordinate(x, y+1));
        if(getVal(x, y-1) > 0 && getVal(x, y-1) < 100000) n.add(new Coordinate(x, y-1));
        if(getVal(x-1, y) > 0 && getVal(x-1, y) < 100000) n.add(new Coordinate(x-1, y));

        Coordinate[] neighbors = new Coordinate[n.size()];
        n.toArray(neighbors);
        return neighbors;
    }
}

