package com.bkmsx.atestopengl;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    CustomSurfaceView mCustomSurfaceView;
    CustomRenderer mCustomRenderer;
    RelativeLayout mainLayout;
    RelativeLayout.LayoutParams params;
    MediaPlayer player;
    int type = 0;
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

        params = new RelativeLayout.LayoutParams(900, 900);
//        params.leftMargin = 200;
//        params.topMargin = 300;
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mainLayout.addView(mCustomSurfaceView, params);
        mCustomRenderer.setOnSurfaceTextureListener(new CustomRenderer.OnSurfaceTextureCreated() {
            @Override
            public void onSurfaceTextureCreated() {
                startPlaying();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_normal: type = 0;
                break;
            case R.id.btn_negative: type = 1;
                break;
            case R.id.btn_grayscale: type = 2;
                break;
        }
        playVideo();
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

    public void startPlaying()
    {
        player = new MediaPlayer();

        try
        {
            AssetFileDescriptor afd = getAssets().openFd("big_buck_bunny.mp4");
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.setSurface(new Surface(mCustomRenderer.getSurfaceTexture()));
            player.setLooping(true);
            player.prepare();
            player.start();
//            player.setVolume(0f,0f);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not open input video!");
        }
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
