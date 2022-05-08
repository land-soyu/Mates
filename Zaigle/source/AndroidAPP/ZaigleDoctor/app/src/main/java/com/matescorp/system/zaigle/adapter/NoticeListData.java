package com.matescorp.system.zaigle.adapter;

/**
 * Created by Administrator on 2015-06-08.
 */

public class NoticeListData {

    // 제목
    public String mNoticeTitle;

    // 아이콘
    public int mMessage;

    public String mNoticeData;


    public NoticeListData(String mNoticeTitle, String mNoticeData) {
        this.mNoticeTitle = mNoticeTitle;
        this.mNoticeData = mNoticeData;
    }

    /**
     * 알파벳 이름으로 정렬
     */
//    public static final Comparator<NoticeListData> ALPHA_COMPARATOR = new Comparator<NoticeListData>() {
//        private final Collator sCollator = Collator.getInstance();
//
//        @Override
//        public int compare(NoticeListData mListDate_1, NoticeListData mListDate_2) {
//            return sCollator.compare(mListDate_1.mNoticeDate, mListDate_2.mNoticeDate);
//        }
//    };
}