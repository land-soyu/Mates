package com.matescorp.soyu.farmkinggate.adapter;

/**
 * Created by soyu on 17. 9. 25.
 */

public class ListViewItem {

    private String no;
    private String history_no ;
    private int ararm ;
    private String temp ;
    private int move ;

    public String getNo() {        return no;    }
    public void setNo(String no) {        this.no = no;    }
    public String getHistory_no() {        return history_no;    }
    public void setHistory_no(String history_no) {        this.history_no = history_no;    }
    public int getArarm() {        return ararm;    }
    public void setArarm(int ararm) {        this.ararm = ararm;    }
    public String getTemp() {        return temp;    }
    public void setTemp(String temp) {        this.temp = temp;    }
    public int getMove() {        return move;    }
    public void setMove(int move) {        this.move = move;    }

    public ListViewItem() {
        no = "";
        history_no = "";
        ararm = 0;
        temp = "";
        move = 0;
    }
    public ListViewItem(String n, String h_no, int ar, String te, int mo) {
        no = n;
        history_no = h_no;
        ararm = ar;
        temp = te;
        move = mo;
    }
}
