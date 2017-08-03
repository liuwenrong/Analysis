/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */


package com.coolyota.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.coolyota.analysis.CYAnalysis;
import com.cy.demo.R;


/**
 * Created by liuwenrong on 2017/3/17.
 */

public class DownloadAct extends Activity {
    private static final String mPageName = "DownloadPage";

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
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
    }
    public void onClick(View v){


        switch (v.getId()) {
            case R.id.download_to_pic:

                startActivity(new Intent(this, PicAct.class));

                break;
            case R.id.download_to_home:
                startActivity(new Intent(this, HomeAct.class));
                break;
            case R.id.download_to_self:
//                startActivity(new Intent(this, DownloadAct.class));
//                finish();
                Log.d("Download", "65--run: 很抱歉,程序出现异常,一秒钟后重启.");
                android.os.Process.killProcess(android.os.Process.myPid());
                /*try {
                    final String command = "am force-stop com.cy.demo";
                    Process process = Runtime.getRuntime().exec(command);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                break;

        }
    }
}
