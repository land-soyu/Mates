package kr.co.netseason.myclebot.API;

/**
 * Created by Administrator on 2015-06-08.
 */

import java.text.Collator;
import java.util.Comparator;

public class ContactListData {
    /**
     * 리스트 정보를 담고 있을 객체 생성
     */

    // 제목
    public String mName;

    // 날짜
    public String mNumber;

    // 아이콘
    public int mCCTV;

    // 아이콘
    public int mPET;

    // 아이콘
    public int mMessage;

    public String request;

    public String app;

    public ContactListData(String mName, String mNumber, int mCCTV, int mPET, int mMessage, String request, String app) {
        this.mName = mName;
        this.mNumber = mNumber;
        this.mCCTV = mCCTV;
        this.mPET = mPET;
        this.mMessage = mMessage;
        this.request = request;
        this.app = app;
    }

    /**
     * 알파벳 이름으로 정렬
     */
    public static final Comparator<ContactListData> ALPHA_COMPARATOR = new Comparator<ContactListData>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(ContactListData mListDate_1, ContactListData mListDate_2) {
            return sCollator.compare(mListDate_1.mName, mListDate_2.mName);
        }
    };
}