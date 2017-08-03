package com.coolyota.demo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cy.demo.R;

/**
 * des: 
 * 
 * @author  liuwenrong
 * @version 1.0,2017/7/21 
 */
public    class Fragment_2 extends Fragment {

    private final String TAG = "Fragment_2";


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        log("   1__onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("    2__onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        log("    3_onCreateView");
        return inflater.inflate(R.layout.fragment_layout_2,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        log("   4__onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        log("   5__onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        log("   6__onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        log("   7__onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        log("   8__onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        log("   9__onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("   10__onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        log("   11__onDetach");
    }

    private void log (String methodName){
        Log.e(TAG,"-------->"+methodName);
    }
}
