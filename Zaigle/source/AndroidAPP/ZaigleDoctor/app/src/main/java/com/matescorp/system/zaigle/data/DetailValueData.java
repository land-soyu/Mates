package com.matescorp.system.zaigle.data;

/**
 * Created by soyu on 16. 11. 9.
 */

public class DetailValueData {
    String mTitle ="";
    String mVlaue = "";
    String mG ="";

    public DetailValueData(String title, String value, String gr) {
        this.mTitle = title;
        this.mVlaue = value;
        this.mG = gr;
    }

    public DetailValueData(String title, String gr) {
        this.mTitle = title;
        this.mG = gr;

    }



    @Override
    public String toString() {
        String str = "";
        str = str + "{";
        str = str + "\"title\":\""+mTitle+"\"";
        str = str + "\"value\":\""+mVlaue+"\"";
        str = str + "\"gr\":\""+mG+"\"";
        str = str + "}";
        return str;
    }


    public String getmTitle() {        return mTitle;    }
    public void setmTitle(String title) {        this.mTitle = title;    }
    public String getmVlaue() {        return mVlaue;    }
    public void setmVlaue(String value) {        this.mVlaue = value;    }
    public String getmG() {        return mG;    }
    public void setmG(String gr) {        this.mG = gr;    }
}
