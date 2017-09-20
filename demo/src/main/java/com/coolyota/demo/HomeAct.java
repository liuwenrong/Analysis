package com.coolyota.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.coolyota.analysis.CYAnalysis;
import com.coolyota.analysis.network.TestInterceptor;
import com.coolyota.analysis.tools.MyMessage;
import com.coolyota.analysis.tools.UploadFileUtil;
import com.coolyota.demo.fragment.Fragment_1;
import com.coolyota.demo.fragment.Fragment_2;
import com.coolyota.demo.fragment.Fragment_3;
import com.coolyota.demo.fragment.Fragment_4;
import com.coolyota.demo.fragment.Fragment_5;
import com.cy.demo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * des: 研究ViewPager中Fragment的生命周期
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
//        setContentView(R.layout.activity_view_pager);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: ");
        displayMetrics = getResources().getDisplayMetrics();
        density = displayMetrics.density;
        dpi = displayMetrics.densityDpi;

//        BB.getName();

//        Thread thread = new UploadHistoryLog(this);
//        handler.post(thread);
//        initView();
    }

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private void initView() {
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.vp);

        final List <Fragment> data = new ArrayList<>();
        data.add(new Fragment_1());
        data.add(new Fragment_2());
        data.add(new Fragment_3());
        data.add(new Fragment_4());
        data.add(new Fragment_5());

        viewPager.setOffscreenPageLimit(1);
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()){

            @Override
            public int getCount() {
                return data.size();
            }

            @Override
            public Fragment getItem(int position) {
                return data.get(position);
            }
        };
//        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);



//        adapter.setFragmentList(data);
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0 ; i < adapter.getCount() ; i ++){
            tabLayout.getTabAt(i).setText("Tab_"+(i+1));
        }
    }

    class ViewPagerAdapter extends PagerAdapter {

        List<Fragment> mData;
        public ViewPagerAdapter(FragmentManager fm){
//            super(fm);
        }
        public void setFragmentList(List<Fragment> data) {
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return false;
        }
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

       /* Toast.makeText(this, tv.getText() + "的size = " + tv.getTextSize()
                +", width = " + tv.getMeasuredWidth() +",height = "
                + tv.getMeasuredHeight() + ",density = " + density +
                ",xdpi = " + displayMetrics.xdpi + ",ydpi = " + displayMetrics.ydpi +
                ", dpi = " + dpi, Toast.LENGTH_SHORT).show();*/

//        Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();

        Log.e(TAG, "onClick: tv = " + tv.getText().toString());

        switch (v.getId()) {
            case R.id.home_to_pic:

//                startActivity(new Intent(this, PicAct.class));
//                UploadFileUtil.isSystemApp(getContext());
                CYAnalysis.setUploadEnabled(true);
                UploadFileUtil.uploadFile(TestInterceptor.Test_Url, new HashMap<String, Object>(), new File(""), new UploadFileUtil.CallbackMessage() {
                    @Override
                    public void callbackMsg(MyMessage message) {

                    }
                });

                break;
            case R.id.home_to_download:
                startActivity(new Intent(this, DownloadAct.class));

                break;

        }

//        throw new NullPointerException();
    }

}
