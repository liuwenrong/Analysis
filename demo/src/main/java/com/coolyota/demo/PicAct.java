/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */

package com.coolyota.demo;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.coolyota.analysis.CYAnalysis;
import com.cy.demo.R;

import org.json.JSONException;
import org.json.JSONObject;

public class PicAct extends AppCompatActivity implements View.OnClickListener {


    private static final String mPageName = "PicPage";

    @Override
    protected void onResume() {
        super.onResume();
        CYAnalysis.onResume(this, mPageName);

    }

    @Override
    protected void onPause() {
        super.onPause();
        CYAnalysis.onPause(this);
    }

    public static final String SERVER = "http://server.jeasonlzy.com/OkHttpUtils/";
    //    public static final String SERVER = "http://192.168.1.121:8080/OkHttpUtils/";
    public static final String URL_METHOD = SERVER + "method";
    public static final String SHARED_PREFERENCES_NAME = "baoliyota_spf";
    private static final String TAG = "MainAct";
    TextView tv;
    Button btn;
    int count = 0;
    private FrameLayout mPanelHolder;
    private Context mContext;
    private Button setWallpaperBtn;

    /**
     * BaoliYota begin, add
     * what(reason) 背屏的锁屏壁纸逻辑
     * liuwenrong@coolpad.com, 1.0, 2017/3/21 */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = DemoApplication.getContext();
        setContentView(R.layout.activity_pic);
        initView();


    }

    private void initView() {
        Button toDownload = (Button) findViewById(R.id.btn_to_download);
        Button ori_get_btn = (Button) findViewById(R.id.btn_register);
        tv = (TextView) findViewById(R.id.tv);
        setWallpaperBtn = (Button) findViewById(R.id.buy_btn);
        mPanelHolder = (FrameLayout) findViewById(R.id.panel_holder);

        DisplayManager dm = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        dm.getDisplays();
        Display display = dm.getDisplay(0);
        DisplayMetrics dm2 = new DisplayMetrics();
        display.getMetrics(dm2);

        setWallpaperBtn.setOnClickListener(this);
        mPanelHolder.setOnClickListener(this);
        toDownload.setOnClickListener(this);
        ori_get_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.panel_holder:
                break;
            case R.id.show_detail_text_view:

                break;

            case R.id.btn_to_download:
                startActivity(new Intent(this, DownloadAct.class));
                break;
            case R.id.btn_register:

                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put("phone", "13686461726");
                    jsonObj.put("username", "liuwenrong");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                CYAnalysis.onEvent(this, "register", "", jsonObj.toString());
                String eventIdUnLock = "unLock";
                CYAnalysis.onEvent(this, eventIdUnLock);


                break;
            case R.id.buy_btn:

                JSONObject jsonObj1 = new JSONObject();
                try {
                    jsonObj1.put("count", "1");
                    jsonObj1.put("price", "19.9");
                    jsonObj1.put("unit", "RMB");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                CYAnalysis.onEvent(this, "buy_BR_book", "", jsonObj1.toString());

                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
