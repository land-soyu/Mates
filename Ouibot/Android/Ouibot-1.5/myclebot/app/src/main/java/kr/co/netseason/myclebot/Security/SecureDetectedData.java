package kr.co.netseason.myclebot.Security;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tbzm on 15. 10. 19.
 */
public class SecureDetectedData implements Parcelable {
    String index;
    String ouibotId;
    String filePath;
    String mode;
    long time;
    int isSelected = 0;

    public SecureDetectedData(String index, String ouibotId, String filePath, String mode, long time, int isSelected) {
        this.index = index;
        this.ouibotId = ouibotId;
        this.filePath = filePath;
        this.mode = mode;
        this.time = time;
        this.isSelected = isSelected;
    }

    public String getImagePath() {
        return filePath;
    }

    public long getTime() {
        return time;
    }

    public int getSelected() {
        return isSelected;
    }

    public void setSelected(int data) {
        isSelected = data;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(index);
        dest.writeString(ouibotId);
        dest.writeString(filePath);
        dest.writeString(mode);
        dest.writeLong(time);
        dest.writeInt(isSelected);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<SecureDetectedData> CREATOR = new Creator<SecureDetectedData>() {
        @Override
        public SecureDetectedData createFromParcel(Parcel source) {
            String data1 = source.readString();
            String data2 = source.readString();
            String data3 = source.readString();
            String data4 = source.readString();
            long data5 = source.readLong();
            int data6 = source.readInt();
            return new SecureDetectedData(data1, data2, data3, data4, data5, data6);
        }

        @Override
        public SecureDetectedData[] newArray(int size) {
            return new SecureDetectedData[size];
        }
    };
}
