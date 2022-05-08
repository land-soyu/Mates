package kr.co.netseason.myclebot.API;

/**
 * Created by Administrator on 2015-06-08.
 */

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageListData implements Parcelable {
    /**
     * 리스트 정보를 담고 있을 객체 생성
     */

    public static final int IMAGE_TYPE = 0;
    public static final int VIDEO_TYPE = 1;
    public static final int MESSAGE_TYPE = 2;

    public static final int SEND_COMPLETE = 0;
    public static final int SENDING = 1;
    public static final int SEND_FAIL = 2;

    public static final int UNREAD = 0;
    public static final int READED = 1;

    public static final int SEND_FLAG_ME = 0;
    public static final int SEND_FLAG_YOU = 1;

    public static final int UNCHECKED = 0;
    public static final int CHECKED = 1;

    public String rtcid;
    public String peerRtcid;
    public String peerRtcidName;
    public String messageData;
    public int sendFlag;
    public long time;
    public int readable;
    public int checked;
    public int type;
    public int sendState;

    public MessageListData(String rtcid, String peerRtcid, String peerRtcidName, String messageData, int sendFlag, long time, int readable, int checked, int type, int sendState) {
        this.rtcid = rtcid;
        this.peerRtcid = peerRtcid;
        this.peerRtcidName = peerRtcidName;
        this.messageData = messageData;
        this.sendFlag = sendFlag;
        this.time = time;
        this.readable = readable;
        this.checked = checked;
        this.type = type;
        this.sendState = sendState;
    }

    public int getSendState() {
        return sendState;
    }

    public void setSendState(int value) {
        sendState = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getChecked() {
        return checked;
    }

    public void setChecked(int value) {
        checked = value;
    }

    public String getRTCID() {
        return rtcid;
    }

    public String getPeerRtcid() {
        return peerRtcid;
    }

    public void setPeerRtcidName(String name) {
        peerRtcidName = name;
    }

    public String getPeerRtcidName() {
        return peerRtcidName;
    }

    public String getMessageData() {
        return messageData;
    }

    public int getSendFlag() {
        return sendFlag;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTimeStringFormat() {

        SimpleDateFormat formatterY = new SimpleDateFormat("yyyy");
        String cYear = formatterY.format(new Date(System.currentTimeMillis()));
        String dYear = formatterY.format(time);

        SimpleDateFormat formatterM = new SimpleDateFormat("MM");
        String cMonth = formatterM.format(new Date(System.currentTimeMillis()));
        String dMonth = formatterM.format(time);

        SimpleDateFormat formatterD = new SimpleDateFormat("dd");
        String cDay = formatterD.format(new Date(System.currentTimeMillis()));
        String dDay = formatterD.format(time);

//        if (!cYear.equals(dYear)) {
//            return new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(time));
//
//        } else if (!cMonth.equals(dMonth)) {
//            return new SimpleDateFormat("MM/dd HH:mm").format(new Date(time));
//        } else if (!cDay.equals(dDay)) {
//            return new SimpleDateFormat("dd HH:mm").format(new Date(time));
//        } else {
        return new SimpleDateFormat("HH:mm").format(new Date(time));
//        }
    }

    public int getReadable() {
        return readable;
    }

    public void setReadable(int isRead) {
        readable = isRead;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(rtcid);
        dest.writeString(peerRtcid);
        dest.writeString(peerRtcidName);
        dest.writeString(messageData);
        dest.writeInt(sendFlag);
        dest.writeLong(time);
        dest.writeInt(readable);
        dest.writeInt(checked);
        dest.writeInt(type);
        dest.writeInt(sendState);
    }

    public static final Parcelable.Creator<MessageListData> CREATOR = new Creator<MessageListData>() {
        @Override
        public MessageListData createFromParcel(Parcel source) {
            String data2 = source.readString();
            String data3 = source.readString();
            String data4 = source.readString();
            String data5 = source.readString();
            int data6 = source.readInt();
            long data7 = source.readLong();
            int data8 = source.readInt();
            int data9 = source.readInt();
            int data10 = source.readInt();
            int data11 = source.readInt();
            return new MessageListData(data2, data3, data4, data5, data6, data7, data8, data9, data10, data11);
        }

        @Override
        public MessageListData[] newArray(int size) {
            return new MessageListData[size];
        }
    };
}