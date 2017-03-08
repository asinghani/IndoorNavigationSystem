package anish.navigationapp.navigation;

public interface NavigationCallback {
    void move(int direction); // 0 = forward, 1 = right, 2 = left, 3 = back
    void reroute();
    void arrived();
    void searching();
}
