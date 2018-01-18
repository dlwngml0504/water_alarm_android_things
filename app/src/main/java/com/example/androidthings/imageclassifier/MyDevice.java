package com.example.androidthings.imageclassifier;

import android.util.Log;

/**
 * Created by juhee on 2018. 1. 16..
 */
import static com.example.androidthings.imageclassifier.Led.ALL;
import static com.example.androidthings.imageclassifier.Led.BLUE;
import static com.example.androidthings.imageclassifier.Led.CYAN;
import static com.example.androidthings.imageclassifier.Led.GREEN;
import static com.example.androidthings.imageclassifier.Led.ORANGE;
import static com.example.androidthings.imageclassifier.Led.RED;
import static com.example.androidthings.imageclassifier.Led.VIOLET;
import static com.example.androidthings.imageclassifier.Led.WHITE;
import static com.example.androidthings.imageclassifier.Led.YELLOW;
import static com.example.androidthings.imageclassifier.MusicPlayer.Note.C;
import static com.example.androidthings.imageclassifier.MusicPlayer.Note.D;
import static com.example.androidthings.imageclassifier.MusicPlayer.Note.E;
import static com.example.androidthings.imageclassifier.MusicPlayer.Note.F;
import static com.example.androidthings.imageclassifier.MusicPlayer.Note.G;

public class MyDevice {
    private static final String TAG = MyDevice.class.getSimpleName();

    private Led light;
    private MusicPlayer music;
    private Display display;

    public MyDevice(Display display, MusicPlayer music, Led light) {
        this.display = display;
        this.music = music;
        this.light = light;
    }

    public static void pause(double pauseTimeSec) {
        try {
            Thread.sleep((long)(pauseTimeSec * 1000.0));
        } catch (InterruptedException e) {
            Log.e(TAG, "Failed to sleep", e);
        }
    }

    public void initdisplay() {
        display.open();
        display.show("HIHI");
    }

    public void 내코드() {
        display.show("COME");
        데모();
    }

    public void  stop_alarm() {
        display.show("HIHI");
        music.stop();
        light.off(0);
        light.off(1);
        light.off(2);
        light.off(3);
        light.off(4);
        light.off(5);
        light.off(6);

    }

    void 데모() {
        music.playAll(0.3, G, E, E, E, F, D, D, D, C, D, E, F, G, G, G, G);
        music.stop();

        light.on(0, RED);
        light.on(1, ORANGE);
        light.on(2, YELLOW);
        light.on(3, GREEN);
        light.on(4, CYAN);
        light.on(5, BLUE);
        light.on(6, VIOLET);
        pause(1);

        light.on(0, WHITE);
        light.on(1, RED);
        light.on(2, ORANGE);
        light.on(3, YELLOW);
        light.on(4, GREEN);
        light.on(5, CYAN);
        light.on(6, BLUE);
        pause(1);

        light.on(0, WHITE);
        light.on(1, WHITE);
        light.on(2, RED);
        light.on(3, ORANGE);
        light.on(4, YELLOW);
        light.on(5, GREEN);
        light.on(6, BLUE);
        pause(1);

        light.on(0, WHITE);
        light.on(1, WHITE);
        light.on(2, WHITE);
        light.on(3, RED);
        light.on(4, ORANGE);
        light.on(5, YELLOW);
        light.on(6, GREEN);
        pause(1);

        light.on(0, WHITE);
        light.on(1, WHITE);
        light.on(2, WHITE);
        light.on(3, WHITE);
        light.on(4, RED);
        light.on(5, ORANGE);
        light.on(6, YELLOW);
        pause(1);

        light.on(0, WHITE);
        light.on(1, WHITE);
        light.on(2, WHITE);
        light.on(3, WHITE);
        light.on(4, WHITE);
        light.on(5, RED);
        light.on(6, ORANGE);
        pause(1);

        light.on(0, WHITE);
        light.on(1, WHITE);
        light.on(2, WHITE);
        light.on(3, WHITE);
        light.on(4, WHITE);
        light.on(5, WHITE);
        light.on(6, RED);
        pause(1);

        light.on(ALL, WHITE);
        pause(1);
    }
}
