package com.bkmsx.atestopengl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by bkmsx on 1/25/2017.
 */

public class CustomSurfaceView extends GLSurfaceView{
    private CustomRenderer customRenderer;
    Context context;
    public CustomSurfaceView(Context context, int type) {
        super(context);
        this.context = context;
        setEGLContextClientVersion(2);
        customRenderer = new CustomRenderer(context, type);
        setRenderer(customRenderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        customRenderer.processTouchEvent(event);
        return true;
    }

    public CustomRenderer getRenderer() {
        return customRenderer;
    }
}
