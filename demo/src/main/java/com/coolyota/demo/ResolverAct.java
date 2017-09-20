package com.coolyota.demo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.cy.demo.R;

/**
 * des: 通过ContentResolver读取内容提供者ContentProvider的数据
 *
 * @author liuwenrong
 * @version 1.0, 2017/8/22
 */
public class ResolverAct extends AppCompatActivity {
    private static final String TAG = "ResolverAct";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_resolver);
    }

    public void onClick(View view) {
        ContentResolver contentResolver = this.getContentResolver();
        Uri uri = Uri.parse("content://com.coolyota.demo.provider.MyContentProvider/friend");
        switch (view.getId()) {
            case R.id.insert:

                try {
                    ContentValues values = new ContentValues();
                    values.put("friend_name", "huahua");
                    values.put("friend_age", "24");
                    Uri uriResult = contentResolver.insert(uri, values);
                    Log.i(TAG, "onClick: uriResult = " + uriResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case R.id.query:

                try {
                    Cursor cursor = contentResolver.query(uri, new String[]{"_ID", "friend_name", "friend_age"}, null, null, "_ID desc");
                    while(cursor.moveToNext()) {
                        Log.i(TAG, "onCreate: name = " + cursor.getString(1));
                    }
                } finally {

                }


                break;
        }
    }
}
