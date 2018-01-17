/*
 * Copyright 2017 The Android Things Samples Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androidthings.imageclassifier;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.androidthings.imageclassifier.classifier.Classifier;
import com.example.androidthings.imageclassifier.classifier.TensorFlowImageClassifier;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImageClassifierActivity extends Activity implements ImageReader.OnImageAvailableListener {
    private static final String TAG = "ImageClassifierActivity";

    private ImagePreprocessor mImagePreprocessor;
    private CameraHandler mCameraHandler;
    private TensorFlowImageClassifier mTensorFlowClassifier;

    private Led light;
    private MusicPlayer music;
    private MyDevice myDevice;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private ImageView mImage;
    private TextView[] mResultViews;

    private AtomicBoolean mReady = new AtomicBoolean(false);
    private Gpio mReadyLED;

    private int mInterval = 10000; // 10 seconds by default, can be changed later
    private int mTimer =  600000; // 10 minutes by default, can be changed later
    private Handler mHandler;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);
        mImage = (ImageView) findViewById(R.id.imageView);
        mResultViews = new TextView[4];
        mResultViews[0] = (TextView) findViewById(R.id.result1);
        mResultViews[1] = (TextView) findViewById(R.id.result2);
        mResultViews[2] = (TextView) findViewById(R.id.result3);
        mResultViews[3] = (TextView) findViewById(R.id.result4);

        init();
    }

    private void init() {
        if (isAndroidThingsDevice(this)) {
            initPIO();
        }

        music = new MusicPlayer();
        light = new Led();
        myDevice = new MyDevice(music, light);

        mResultViews[mResultViews.length-1].setText("last drink time: "+ Integer.toString(mTimer/1000));
        mBackgroundThread = new HandlerThread("BackgroundThread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        mBackgroundHandler.post(mInitializeOnBackground);

        mHandler = new Handler();
        startRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                mTimer = mTimer - 10000;
                if (mTimer<0) {
                    mResultViews[mResultViews.length-1].setText("last drink time: "+ Integer.toString((mTimer/1000)*(-1)));
                    if (light.open() &&  music.open()) {
                        new Thread() {
                            @Override
                            public void run() {
                                myDevice.pause(1);
                                myDevice.내코드();
                                myDevice.pause(1);
                                finish();
                            }
                        }.start();
                    } else {
                        finish();
                    }
                }
                else {
                    mResultViews[mResultViews.length-1].setText("last drink time: "+ Integer.toString(mTimer/1000));
                }

                if (mReady.get()) {
                    Log.i(TAG, "Taking photo");
                    setReady(false);
                    mBackgroundHandler.post(mBackgroundClickHandler);
                } else {
                    Log.i(TAG, "Sorry, processing hasn't finished. Try again in a few seconds");
                }
//                updateStatus(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    /**
     * This method should only be called when running on an Android Things device.
     */
    private void initPIO() {
        PeripheralManagerService pioService = new PeripheralManagerService();
        try {
            mReadyLED = pioService.openGpio(BoardDefaults.getGPIOForLED());
            mReadyLED.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.w(TAG, "Could not open GPIO pins", e);
        }
    }

    private Runnable mInitializeOnBackground = new Runnable() {
        @Override
        public void run() {
            mImagePreprocessor = new ImagePreprocessor();

            mCameraHandler = CameraHandler.getInstance();
            mCameraHandler.initializeCamera(
                    ImageClassifierActivity.this, mBackgroundHandler,
                    ImageClassifierActivity.this);

            mTensorFlowClassifier = new TensorFlowImageClassifier(ImageClassifierActivity.this);

            setReady(true);
        }
    };

    private Runnable mBackgroundClickHandler = new Runnable() {
        @Override
        public void run() {
            mCameraHandler.takePicture();
        }
    };

    private void setReady(boolean ready) {
        mReady.set(ready);
        if (mReadyLED != null) {
            try {
                mReadyLED.setValue(ready);
            } catch (IOException e) {
                Log.w(TAG, "Could not set LED", e);
            }
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        final Bitmap bitmap;
        try (Image image = reader.acquireNextImage()) {
            bitmap = mImagePreprocessor.preprocessImage(image);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImage.setImageBitmap(bitmap);
            }
        });

        final List<Classifier.Recognition> results = mTensorFlowClassifier.doRecognize(bitmap);

        Log.d(TAG, "Got the following results from Tensorflow: " + results);
        setReady(true);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mResultViews.length-1; i++) {
                    if (results.size() > i) {
                        Classifier.Recognition r = results.get(i);
                        Log.i(TAG, r.getTitle());
                        if ((r.getTitle().equals("mask"))||(r.getTitle().equals("wig"))) {
                            mTimer =  600000;
                            mResultViews[mResultViews.length-1].setText("last drink time: "+ Integer.toString(mTimer/1000));
                            Log.i(TAG, "Someone came to drink water");
                        }
                        mResultViews[i].setText(r.getTitle() + " : " + r.getConfidence().toString());
                    } else {
                        mResultViews[i].setText(null);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();

        light.close();
        music.close();

        try {
            if (mBackgroundThread != null) mBackgroundThread.quit();
        } catch (Throwable t) {
            // close quietly
        }
        mBackgroundThread = null;
        mBackgroundHandler = null;

        try {
            if (mCameraHandler != null) mCameraHandler.shutDown();
        } catch (Throwable t) {
            // close quietly
        }
        try {
            if (mTensorFlowClassifier != null) mTensorFlowClassifier.destroyClassifier();
        } catch (Throwable t) {
            // close quietly
        }

    }

    /**
     * @return true if this device is running Android Things.
     *
     * Source: https://stackoverflow.com/a/44171734/112705
     */
    private boolean isAndroidThingsDevice(Context context) {
        // We can't use PackageManager.FEATURE_EMBEDDED here as it was only added in API level 26,
        // and we currently target a lower minSdkVersion
        final PackageManager pm = context.getPackageManager();
        boolean isRunningAndroidThings = pm.hasSystemFeature("android.hardware.type.embedded");
        Log.d(TAG, "isRunningAndroidThings: " + isRunningAndroidThings);
        return isRunningAndroidThings;
    }
}
