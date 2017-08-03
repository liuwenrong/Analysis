/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */
package com.coolyota.analysis.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.coolyota.analysis.CYAnalysis;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/22
 */
public class SharedPrefUtil {
    private SharedPreferences sp = null;
    private Editor edit = null;

    public SharedPrefUtil(Context context) {

        if (context != null) {
            this.sp = context.getSharedPreferences("coolyota_SharedPref", Context.MODE_PRIVATE);
        } else {
            sp = CYAnalysis.contextAppWR.get().getSharedPreferences("coolyota_SharedPref", Context.MODE_PRIVATE);
        }
        this.edit = this.sp.edit();
    }

    public void setValue(String key, long value) {
        this.edit.putLong(key, value);
        this.edit.commit();
    }

    public void removeKey(String key) {
        this.edit.remove(key);
        this.edit.commit();
    }

    public void setValue(String key, String value) {
        this.edit.putString(key, value);
        this.edit.commit();
    }

    public void setValue(String key, Boolean value) {
        this.edit.putBoolean(key, value.booleanValue());
        this.edit.commit();
    }

    public long getValue(String key, long defaultValue) {
        return this.sp.getLong(key, defaultValue);
    }

    public String getValue(String key, String defaultValue) {
        return this.sp.getString(key, defaultValue);
    }

    public Boolean getValue(String key, Boolean defaultValue) {
        return Boolean.valueOf(this.sp.getBoolean(key, defaultValue.booleanValue()));
    }
}
