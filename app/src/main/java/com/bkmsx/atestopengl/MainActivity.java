package com.bkmsx.atestopengl;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    CustomSurfaceView mCustomSurfaceView;
    CustomRenderer mCustomRenderer;
    RelativeLayout mainLayout;
    RelativeLayout.LayoutParams params;
    MediaPlayer player;
    int type = 0;
    String videoPath;
    String video1 = "outputFile.mp4";
    String video2 = "p2.mp4";
    SeekBar seekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCustomSurfaceView = new CustomSurfaceView(this, type);
        mCustomRenderer = mCustomSurfaceView.getRenderer();
        setContentView(R.layout.activity_main);
        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        findViewById(R.id.btn_normal).setOnClickListener(this);
        findViewById(R.id.btn_negative).setOnClickListener(this);
        findViewById(R.id.btn_grayscale).setOnClickListener(this);
        findViewById(R.id.btn_video_1).setOnClickListener(this);
        findViewById(R.id.btn_video_2).setOnClickListener(this);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        seekBar.setProgress(100);

        params = new RelativeLayout.LayoutParams(900, 900);
//        params.leftMargin = 200;
//        params.topMargin = 300;
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mainLayout.addView(mCustomSurfaceView, params);
        videoPath = getInternalStorage() + video1;
        mCustomRenderer.setOnSurfaceTextureListener(new CustomRenderer.OnSurfaceTextureCreated() {
            @Override
            public void onSurfaceTextureCreated() {
                startPlaying();
            }
        });
    }

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float red = (float) progress /100;
            mCustomRenderer.setRed(red);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_normal:
                type = 0;
                playVideo();
                break;
            case R.id.btn_negative:
                type = 1;
                playVideo();
                break;
            case R.id.btn_grayscale:
                type = 2;
                playVideo();
                break;
            case R.id.btn_video_1:
                player.stop();
                player.release();
                videoPath = getInternalStorage() + video1;
                startPlaying();
                break;
            case R.id.btn_video_2:
                player.stop();
                player.release();
                videoPath = getInternalStorage() + video2;
                startPlaying();
                break;
        }

    }

    private void playVideo() {
        mainLayout.removeView(mCustomSurfaceView);
        mCustomSurfaceView = new CustomSurfaceView(this, type);
        mCustomRenderer = mCustomSurfaceView.getRenderer();
        mCustomRenderer.setOnSurfaceTextureListener(new CustomRenderer.OnSurfaceTextureCreated() {
            @Override
            public void onSurfaceTextureCreated() {
                player.setSurface(new Surface(mCustomRenderer.getSurfaceTexture()));
            }
        });
        mainLayout.addView(mCustomSurfaceView, params);
    }

    private String getInternalStorage() {
        return Environment.getExternalStorageDirectory().toString() + "/";
    }

    public void startPlaying() {
        player = MediaPlayer.create(this, Uri.parse(videoPath));

//            AssetFileDescriptor afd = getAssets().openFd("big_buck_bunny.mp4");
//            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        player.setSurface(new Surface(mCustomRenderer.getSurfaceTexture()));
        player.setLooping(true);
//            player.prepare();
        player.start();
//            player.setVolume(0f,0f);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mCustomSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCustomSurfaceView.onPause();
    }
}
