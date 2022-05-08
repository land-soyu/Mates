package kr.co.netseason.myclebot.Security;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tbzm on 15. 9. 24.
 */
public class UibotListData implements Parcelable {

    public String mMasterRtcid;
    public String mSlaveRtcid;
    public int mDetectMode;
    public String mDetectOnOff;
    public int mVideoSaveMode;
    public int mVideoSaveTime;
    public int mDetectSensitivity;
    public int mDoTimeAfterSetting;

    public int mNoneActivityVideoSaveMode;
    public int mNoneActivityVideoSaveTime;
    public int mNoneActivityDetectSensitivity;
    public int mNoneActivityDetectTime;

    public UibotListData(String masterRtcid, String slaveRtcid, int detectMode, String detectOnoff,
                         int videoSaveMode, int videoSaveTime, int detectSensitivity, int doTimeAfterSetting, int noneActivityVideoSaveMode, int noneActivityVideoSaveTime, int noneActivityDetectSensitivity, int noneActivityDetectTime) {
        mMasterRtcid = masterRtcid;
        mSlaveRtcid = slaveRtcid;
        mDetectMode = detectMode;
        mDetectOnOff = detectOnoff;
        mVideoSaveMode = videoSaveMode;
        mVideoSaveTime = videoSaveTime;
        mDetectSensitivity = detectSensitivity;
        mDoTimeAfterSetting = doTimeAfterSetting;

        mNoneActivityVideoSaveMode = noneActivityVideoSaveMode;
        mNoneActivityVideoSaveTime = noneActivityVideoSaveTime;
        mNoneActivityDetectSensitivity = noneActivityDetectSensitivity;
        mNoneActivityDetectTime = noneActivityDetectTime;
    }

    public String getMasterRtcid() {
        if (mMasterRtcid == null || mMasterRtcid.equals("")) {
            return null;
        }
        return mMasterRtcid;
    }

    public String getSlaveRtcid() {
        if (mSlaveRtcid == null || mSlaveRtcid.equals("")) {
            return null;
        }
        return mSlaveRtcid;
    }

    public int getDetectMode() {
        return mDetectMode;
    }

    public String getDetectOnOff() {
        return mDetectOnOff;
    }

    public int getRecordingOption() {
        return mVideoSaveMode;
    }

    public int getNoneActivityRecordingOption() {
        return mNoneActivityVideoSaveMode;
    }

    public int getNoneActivityRecordingTime() {
        return mNoneActivityVideoSaveTime;
    }

    public int getRecordingTime() {
        return mVideoSaveTime;
    }

    public int getDetectSensitivity() {
        return mDetectSensitivity;
    }

    public int getNoneActivitySensitivity() {
        return mNoneActivityDetectSensitivity;
    }

    public int getNoneActivityCheckTime() {
        return mNoneActivityDetectTime;
    }

    public int getSecuritySettingTime() {
        return mDoTimeAfterSetting;
    }

    public void setDetectMode(int value) {
        mDetectMode = value;
    }

    public void setDetectOnOff(String value) {
        mDetectOnOff = value;
    }

    public void setSecureVideoSaveMode(int value) {
        mVideoSaveMode = value;
    }

    public void setVideoSaveTime(int value) {
        mVideoSaveTime = value;
    }

    public void setDetectSensitivity(int value) {
        mDetectSensitivity = value;
    }

    public void setDoTimeAfterSetting(int value) {
        mDoTimeAfterSetting = value;
    }

    public void setNoneActivityDetectSensitivity(int value) {
        mNoneActivityDetectSensitivity = value;
    }

    public void setNoneActivityDetectTime(int value) {
        mNoneActivityDetectTime = value;
    }

    public void setNoneActivityVideoSaveMode(int value) {
        mNoneActivityVideoSaveMode = value;
    }

    public void setNoneActivityVideoSaveTime(int value) {
        mNoneActivityVideoSaveTime = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMasterRtcid);
        dest.writeString(mSlaveRtcid);
        dest.writeInt(mDetectMode);
        dest.writeString(mDetectOnOff);
        dest.writeInt(mVideoSaveMode);
        dest.writeInt(mVideoSaveTime);
        dest.writeInt(mDetectSensitivity);
        dest.writeInt(mDoTimeAfterSetting);
        dest.writeInt(mNoneActivityVideoSaveMode);
        dest.writeInt(mNoneActivityVideoSaveTime);
        dest.writeInt(mNoneActivityDetectSensitivity);
        dest.writeInt(mNoneActivityDetectTime);
    }

    public static final Parcelable.Creator<UibotListData> CREATOR = new Creator<UibotListData>() {
        @Override
        public UibotListData createFromParcel(Parcel source) {
            String data1 = source.readString();
            String data2 = source.readString();
            int data3 = source.readInt();
            String data4 = source.readString();
            int data5 = source.readInt();
            int data6 = source.readInt();
            int data7 = source.readInt();
            int data8 = source.readInt();
            int data9 = source.readInt();
            int data10 = source.readInt();
            int data11 = source.readInt();
            int data12 = source.readInt();

            return new UibotListData(data1, data2, data3, data4, data5, data6, data7, data8, data9, data10, data11, data12);
        }

        @Override
        public UibotListData[] newArray(int size) {
            return new UibotListData[size];
        }
    };
}
