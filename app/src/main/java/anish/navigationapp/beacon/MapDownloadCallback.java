package anish.navigationapp.beacon;

import anish.navigationapp.navigation.Map;

public interface MapDownloadCallback {
    void enterBuilding();
    void map(Map map);
    void downloadError();
}
