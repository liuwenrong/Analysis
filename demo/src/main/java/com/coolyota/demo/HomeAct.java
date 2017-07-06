package com.coolyota.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.coolyota.analysis.CYAnalysis;
import com.coolyota.analysis.tools.ApiConstants;
import com.cy.demo.BuildConfig;
import com.cy.demo.R;

/**
 * des: 
 * 
 * @author  liuwenrong
 * @version 1.0,2017/5/25 
 */
public class HomeAct extends AppCompatActivity {

    public static final String TAG = "HomeAct";
    private float density;
    private int dpi;
    private DisplayMetrics displayMetrics;
    private static final String mPageName = "HomePage";
    private static Handler handler;

    static {
        HandlerThread localHandlerThread = new HandlerThread("CYAnalysis");
        localHandlerThread.start();
        handler = new Handler(localHandlerThread.getLooper());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: ");
        displayMetrics = getResources().getDisplayMetrics();
        density = displayMetrics.density;
        dpi = displayMetrics.densityDpi;
//        CYAnalysis.setUploadEnabled(false);
        CYAnalysis.init(this, ApiConstants.KEY_BLauncher, BuildConfig.APPLICATION_ID);
//        Thread thread = new UploadHistoryLog(this);
//        handler.post(thread);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CYAnalysis.onResume(this, mPageName);
        String eventIdUnLock = "unLock";
        CYAnalysis.onEvent(this, eventIdUnLock);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CYAnalysis.onPause(this);
    }

    public void onClick(View v){

        TextView tv = (TextView)v;

        Toast.makeText(this, tv.getText() + "的size = " + tv.getTextSize()
                +", width = " + tv.getMeasuredWidth() +",height = "
                + tv.getMeasuredHeight() + ",density = " + density +
                ",xdpi = " + displayMetrics.xdpi + ",ydpi = " + displayMetrics.ydpi +
                ", dpi = " + dpi, Toast.LENGTH_SHORT).show();

        Log.e(TAG, "onClick: tv = " + tv.getText().toString());

        switch (v.getId()) {
            case R.id.home_to_pic:

                startActivity(new Intent(this, PicAct.class));

                break;
            case R.id.home_to_download:
                startActivity(new Intent(this, DownloadAct.class));

                break;

        }
    }

}
