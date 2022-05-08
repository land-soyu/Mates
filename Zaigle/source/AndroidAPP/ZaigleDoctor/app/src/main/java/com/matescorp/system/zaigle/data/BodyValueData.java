package com.matescorp.system.zaigle.data;

import android.graphics.drawable.Drawable;

/**
 * Created by soyu on 16. 11. 9.
 */

public class BodyValueData {
    Drawable mImage;
    String mTitle ="";
    String mVlaue = "";
    Drawable mG ;

    public BodyValueData(Drawable mImage, String title, String value, Drawable gr) {
        this.mImage = mImage;
        this.mTitle = title;
        this.mVlaue = value;
        this.mG = gr;
    }

    @Override
    public String toString() {
        String str = "";
        str = str + "{";
        str = str + "\"title\":\""+mTitle+"\"";
        str = str + "\"value\":\""+mVlaue+"\"";
        str = str + "}";
        return str;
    }

    public Drawable getmImage() {        return mImage;    }
    public void setmTitle(Drawable title) {        this.mImage= mImage;    }
    public String getmTitle() {        return mTitle;    }
    public void setmTitle(String title) {        this.mTitle = title;    }
    public String getmVlaue() {        return mVlaue;    }
    public void setmVlaue(String value) {        this.mVlaue = value;    }
    public Drawable getmG() {        return mG;    }
    public void setmG(Drawable gr) {        this.mG = gr;    }
}
