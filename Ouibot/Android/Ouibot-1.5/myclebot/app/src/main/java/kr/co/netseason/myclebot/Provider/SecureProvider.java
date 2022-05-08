package kr.co.netseason.myclebot.Provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by tbzm on 15. 10. 19.
 */
public class SecureProvider extends ContentProvider {
    public static final String AUTHORITY = "kr.co.netseason.myclebot.provider.secureProvider";
    public static final Uri SECURE_SLAVE_TABLE_URI = Uri.parse("content://" + AUTHORITY + "/secure_slave_table_list");
    public static final Uri SECURE_MASTER_TABLE_URI = Uri.parse("content://" + AUTHORITY + "/secure_master_table_list");
    public static final Uri MESSAGE_TABLE_URI = Uri.parse("content://" + AUTHORITY + "/message_table");
    public static final Uri USER_INFO_TABLE_URI = Uri.parse("content://" + AUTHORITY + "/user_info_table");
    public static final Uri SPEM_TABLE_URI = Uri.parse("content://" + AUTHORITY + "/spem_table");
    public static final Uri PROFILE_TABLE_URI = Uri.parse("content://" + AUTHORITY + "/profile_table");
    private static final int SECURE_SLAVE_TABLE = 0;
    private static final int SECURE_MASTER_TABLE = 1;
    private static final int MESSAGE_TABLE = 2;
    private static final int USER_INFO_TABLE = 3;
    private static final int SPEM_TABLE = 4;
    private static final int PROFILE_TABLE = 5;
    private SecureSQLiteHelper mSecureHelper;
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "secure_slave_table_list", SECURE_SLAVE_TABLE);
        uriMatcher.addURI(AUTHORITY, "secure_master_table_list", SECURE_MASTER_TABLE);
        uriMatcher.addURI(AUTHORITY, "message_table", MESSAGE_TABLE);
        uriMatcher.addURI(AUTHORITY, "user_info_table", USER_INFO_TABLE);
        uriMatcher.addURI(AUTHORITY, "spem_table", SPEM_TABLE);
        uriMatcher.addURI(AUTHORITY, "profile_table", PROFILE_TABLE);
    }

    @Override
    public boolean onCreate() {
        mSecureHelper = new SecureSQLiteHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor result = null;
        switch (uriMatcher.match(uri)) {
            case SECURE_SLAVE_TABLE: {
                SQLiteDatabase database = mSecureHelper.getReadableDatabase();
                result = database.query(SecureSQLiteHelper.TABLE_SECURE_SLAVE_LIST, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case SECURE_MASTER_TABLE: {
                SQLiteDatabase database = mSecureHelper.getReadableDatabase();
                result = database.query(SecureSQLiteHelper.TABLE_SECURE_MASTER_LIST, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case MESSAGE_TABLE: {
                SQLiteDatabase database = mSecureHelper.getReadableDatabase();
                result = database.query(SecureSQLiteHelper.TABLE_MESSAGE_LIST, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case USER_INFO_TABLE: {
                SQLiteDatabase database = mSecureHelper.getReadableDatabase();
                result = database.query(SecureSQLiteHelper.TABLE_USER_INFO_LIST, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case SPEM_TABLE: {
                SQLiteDatabase database = mSecureHelper.getReadableDatabase();
                result = database.query(SecureSQLiteHelper.TABLE_SPEM_LIST, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case PROFILE_TABLE: {
                SQLiteDatabase database = mSecureHelper.getReadableDatabase();
                result = database.query(SecureSQLiteHelper.TABLE_PROFILE_LIST, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
        }
        return result;
    }

//    public Cursor queryWithGrouBy(Uri uri, String[] projection, String selection, String[] selectionArgs, String Groupby, String sortOrder) {
//        Cursor result = null;
//        switch (uriMatcher.match(uri)) {
//            case SECURE_SLAVE_TABLE: {
//                SQLiteDatabase database = mSecureHelper.getReadableDatabase();
//                result = database.query(SecureSQLiteHelper.TABLE_SECURE_SLAVE_LIST, projection, selection, selectionArgs, Groupby, null, sortOrder);
//                break;
//            }
//            case SECURE_MASTER_TABLE: {
//                SQLiteDatabase database = mSecureHelper.getReadableDatabase();
//                result = database.query(SecureSQLiteHelper.TABLE_SECURE_MASTER_LIST, projection, selection, selectionArgs, Groupby, null, sortOrder);
//                break;
//            }
//            case MESSAGE_TABLE: {
//                SQLiteDatabase database = mSecureHelper.getReadableDatabase();
//                result = database.query(SecureSQLiteHelper.TABLE_MESSAGE_LIST, projection, selection, selectionArgs, Groupby, null, sortOrder);
//                break;
//            }
//            case USER_INFO_TABLE: {
//                SQLiteDatabase database = mSecureHelper.getReadableDatabase();
//                result = database.query(SecureSQLiteHelper.TABLE_USER_INFO_LIST, projection, selection, selectionArgs, Groupby, null, sortOrder);
//                break;
//            }
//        }
//        return result;
//    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return String.valueOf(uriMatcher.match(uri));
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
            case SECURE_SLAVE_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                long newId = database.insert(SecureSQLiteHelper.TABLE_SECURE_SLAVE_LIST, null, values);

                return uri.parse(SECURE_SLAVE_TABLE_URI.toString() + "/?newid=" + newId);
            }
            case SECURE_MASTER_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                long newId = database.insert(SecureSQLiteHelper.TABLE_SECURE_MASTER_LIST, null, values);

                return uri.parse(SECURE_MASTER_TABLE_URI.toString() + "/?newid=" + newId);
            }
            case MESSAGE_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                long newId = database.insert(SecureSQLiteHelper.TABLE_MESSAGE_LIST, null, values);

                return uri.parse(MESSAGE_TABLE_URI.toString() + "/?newid=" + newId);
            }
            case USER_INFO_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                long newId = database.insert(SecureSQLiteHelper.TABLE_USER_INFO_LIST, null, values);

                return uri.parse(USER_INFO_TABLE_URI.toString() + "/?newid=" + newId);
            }
            case SPEM_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                long newId = database.insert(SecureSQLiteHelper.TABLE_SPEM_LIST, null, values);

                return uri.parse(SPEM_TABLE_URI.toString() + "/?newid=" + newId);
            }
            case PROFILE_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                long newId = database.insert(SecureSQLiteHelper.TABLE_PROFILE_LIST, null, values);

                return uri.parse(PROFILE_TABLE_URI.toString() + "/?newid=" + newId);
            }
            default:
                return null;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case SECURE_SLAVE_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                int deleted = database.delete(SecureSQLiteHelper.TABLE_SECURE_SLAVE_LIST, selection, selectionArgs);

                return deleted;

            }
            case SECURE_MASTER_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                int deleted = database.delete(SecureSQLiteHelper.TABLE_SECURE_MASTER_LIST, selection, selectionArgs);

                return deleted;

            }
            case MESSAGE_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                int deleted = database.delete(SecureSQLiteHelper.TABLE_MESSAGE_LIST, selection, selectionArgs);

                return deleted;

            }
            case USER_INFO_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                int deleted = database.delete(SecureSQLiteHelper.TABLE_USER_INFO_LIST, selection, selectionArgs);

                return deleted;

            }
            case SPEM_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                int deleted = database.delete(SecureSQLiteHelper.TABLE_SPEM_LIST, selection, selectionArgs);

                return deleted;

            }
            case PROFILE_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                int deleted = database.delete(SecureSQLiteHelper.TABLE_PROFILE_LIST, selection, selectionArgs);

                return deleted;

            }
        }
        return -1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = -1;
        switch (uriMatcher.match(uri)) {
            case SECURE_SLAVE_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                count = database.update(SecureSQLiteHelper.TABLE_SECURE_SLAVE_LIST, values, selection, selectionArgs);

                break;
            }
            case SECURE_MASTER_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                count = database.update(SecureSQLiteHelper.TABLE_SECURE_MASTER_LIST, values, selection, selectionArgs);

                break;
            }
            case MESSAGE_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                count = database.update(SecureSQLiteHelper.TABLE_MESSAGE_LIST, values, selection, selectionArgs);

                break;
            }
            case USER_INFO_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                count = database.update(SecureSQLiteHelper.TABLE_USER_INFO_LIST, values, selection, selectionArgs);

                break;
            }
            case SPEM_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                count = database.update(SecureSQLiteHelper.TABLE_SPEM_LIST, values, selection, selectionArgs);

                break;
            }
            case PROFILE_TABLE: {
                SQLiteDatabase database = mSecureHelper.getWritableDatabase();
                count = database.update(SecureSQLiteHelper.TABLE_PROFILE_LIST, values, selection, selectionArgs);

                break;
            }
        }
        return count;
    }
}
