package com.example.dodgegame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public int score, speed, jerrySpeed;
    public CountDownTimer countDownTimer;
    public long ms = 60000;
    public volatile boolean gameActive = false;
    public boolean damaged = false;
    MediaPlayer mp;
    GameSurface gameSurface;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);

        // Default speed
        speed = 10;

        // Background music
        if (mp == null) {
            mp = MediaPlayer.create(MainActivity.this, R.raw.tomjerrybgmusic);
            mp.setLooping(true);
            mp.start();
        }

        // Game timer
        countDownTimer = new CountDownTimer(ms, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                ms = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                gameActive = false;
                setContentView(R.layout.activity_main);
                onContentChanged();
                textView = findViewById(R.id.textView);
                textView.setText("GAME OVER\nFINAL SCORE: " + score);
                releaseMediaPlayer();
            }
        }.start();

        // Increase speed when you click
        gameSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    speed += 10;
                }
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    public class GameSurface extends SurfaceView implements Runnable, SensorEventListener {

        Thread gameThread;
        SurfaceHolder holder;
        Bitmap jerry, tom, jerryAngry, originalJerry;
        int x, tomX, tomY;
        float z;
        int screenWidth, screenHeight;
        Paint smallText, largeText;
        Handler handler;

        public GameSurface(Context context) {
            super(context);
            holder = getHolder();

            // Load and scale bitmaps
            originalJerry = BitmapFactory.decodeResource(getResources(), R.drawable.jerry);
            Bitmap originalTom = BitmapFactory.decodeResource(getResources(), R.drawable.tom);
            Bitmap originalJerryAngry = BitmapFactory.decodeResource(getResources(), R.drawable.jerryangry);

            // Change the width and height
            int desiredWidth = 250;
            int desiredHeight = 250;

            jerry = Bitmap.createScaledBitmap(originalJerry, desiredWidth, desiredHeight, true);
            tom = Bitmap.createScaledBitmap(originalTom, desiredWidth, desiredHeight, true);
            jerryAngry = Bitmap.createScaledBitmap(originalJerryAngry, desiredWidth, desiredHeight, true);

            tomX = (int) (Math.random() * 951) - 465;

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth = sizeOfScreen.x;
            screenHeight = sizeOfScreen.y;

            // Create thread
            gameActive = true;
            gameThread = new Thread(this);
            gameThread.start();

            smallText = new Paint();
            smallText.setColor(Color.WHITE);
            smallText.setTextSize(40);
            smallText.setAntiAlias(true);

            largeText = new Paint();
            largeText.setColor(Color.WHITE);
            largeText.setTextSize(80);
            largeText.setAntiAlias(true);

            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, sensorManager.SENSOR_DELAY_NORMAL);

            handler = new Handler(Looper.getMainLooper());
        }

        // Detect tilt
        @Override
        public void onSensorChanged(SensorEvent event) {
            z = event.values[0];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        @Override
        public void run() {
            Canvas canvas;

            Drawable bg = getResources().getDrawable(R.drawable.house, null);

            while (gameActive) {
                if (!holder.getSurface().isValid()) continue;
                canvas = holder.lockCanvas(null);
                if (canvas == null) continue;

                bg.setBounds(getLeft(), getTop(), getRight(), getBottom());
                bg.draw(canvas);

                // Draw bitmaps
                canvas.drawBitmap(jerry, (screenWidth / 2) - jerry.getWidth() / 2 + x, screenHeight - jerry.getHeight() - 150, null);
                canvas.drawBitmap(tom, (screenWidth / 2) - tom.getWidth() / 2 + tomX, tomY, null);

                // Display text
                canvas.drawText("" + ms / 1000, 20, 80, largeText);
                canvas.drawText("Score", 20, 140, smallText);
                canvas.drawText("" + score, 20, 200, smallText);

                // jerry movement
                if (z <= -2) {
                    jerrySpeed = 10;
                } else if (z >= 2) {
                    jerrySpeed = -10;
                } else if (z <= -1) {
                    jerrySpeed = 5;
                } else if (z >= 1) {
                    jerrySpeed = -5;
                } else {
                    jerrySpeed = 0;
                }

                if (x + jerrySpeed >= 420) {
                    jerrySpeed = 0;
                    x = 420;
                } else if (x + jerrySpeed <= -420) {
                    jerrySpeed = 0;
                    x = -420;
                }

                x += jerrySpeed;

                // Tom movement
                if (tomY >= screenHeight) {
                    tomY = 0;
                    tomX = (int) (Math.random() * 931) - 465;
                    score++;
                    speed += 5;
                } else {
                    tomY += speed;
                }

                // Hit detection
                int jerryCenterX = (screenWidth / 2) + x;
                int jerryCenterY = screenHeight - jerry.getHeight() / 2 - 150;
                int tomCenterX = (screenWidth / 2) + tomX;
                int tomCenterY = tomY + tom.getHeight() / 2;

                if (Math.sqrt(Math.pow(jerryCenterX - tomCenterX, 2) + Math.pow(jerryCenterY - tomCenterY, 2)) < jerry.getWidth() / 2) {
                    tomY = 0;
                    tomX = (int) (Math.random() * 951) - 465;
                    score--;

                    // Play punch sound in a separate thread
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MediaPlayer punch = MediaPlayer.create(MainActivity.this, R.raw.punch);
                            punch.start();
                            punch.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    mp.release();
                                }
                            });
                        }
                    }).start();

                    // Change jerry image
                    if (!damaged) {
                        damaged = true;
                        jerry = jerryAngry;
                        speed = 0;

                        // Revert image after 1 second
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                jerry = Bitmap.createScaledBitmap(originalJerry, jerry.getWidth(), jerry.getHeight(), true);
                                damaged = false;
                                speed = 10;
                            }
                        }, 1000);
                    }
                }

                holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
