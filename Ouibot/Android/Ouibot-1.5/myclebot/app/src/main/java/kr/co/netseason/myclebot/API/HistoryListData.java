package kr.co.netseason.myclebot.API;

/**
 * Created by Administrator on 2015-06-08.
 */

import java.text.Collator;
import java.util.Comparator;

public class HistoryListData {
    /**
     * 리스트 정보를 담고 있을 객체 생성
     */

    public String mIidx;
    public String mIcon;
    public String mName;
    public String mNumber;
    public String mDate;
    public String mMode;
    public boolean check;
    public String app;
    public String certification;

    public HistoryListData(String idx, String mIcon, String mName, String mNumber, String mDate, String mMode, String app, String certification) {
        this.mIidx = idx;
        this.mIcon = mIcon;
        this.mName = mName;
        this.mNumber = mNumber;
        this.mDate = mDate;
        this.mMode = mMode;
        this.app = app;
        this.certification = certification;
    }

    /**
     * 알파벳 이름으로 정렬
     */
    public static final Comparator<HistoryListData> ALPHA_COMPARATOR = new Comparator<HistoryListData>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(HistoryListData mListDate_1, HistoryListData mListDate_2) {
            return sCollator.compare(mListDate_1.mDate, mListDate_2.mDate);
        }
    };
}