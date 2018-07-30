package com.adibu.receh.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by AdityaBudi on 24/07/2017.
 */

public class RecehProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = RecehProvider.class.getSimpleName();

    private RecehDbHelper mDbHelper;

    //URI matcher code dengan URI datauang table
    private static final int DATAUANG = 101;
    //URI matcher code dengan URI data single di datauang table
    private static final int DATAUANG_ID = 102;

    private static final int TABLE_CONTENT = 103;

    private static final int TABLE_CONTENT_ID = 104;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The content URI of the form "content://com.adibu.receh/datauang" will map to the
        // integer code {@link #DATAUANG}. This URI is used to provide access to MULTIPLE rows
        // of the datauang table.
        sUriMatcher.addURI(RecehContract.CONTENT_AUTHORITY, RecehContract.PATH_DATAUANG, DATAUANG);

        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.adibu.receh/datauang/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(RecehContract.CONTENT_AUTHORITY, RecehContract.PATH_DATAUANG + "/#", DATAUANG_ID);

        sUriMatcher.addURI(RecehContract.CONTENT_AUTHORITY, RecehContract.PATH_TABLE_CONTENT, TABLE_CONTENT);

        sUriMatcher.addURI(RecehContract.CONTENT_AUTHORITY, RecehContract.PATH_TABLE_CONTENT + "/#", TABLE_CONTENT_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new RecehDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //Ambil database untuk read
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;
        //Mencocokan URI yang didapat sesuai kodenya
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DATAUANG:
                //untuk case yang ingin mengambil item keseluruhan
                cursor = db.query(RecehContract.RecehEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case DATAUANG_ID:
                //untuk case yang ingin mengambil item 1 row saja
                selection = RecehContract.RecehEntry._ID + "=?";
                //ambil id row yang diinginkan
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                //ambil data dari database
                cursor = db.query(RecehContract.RecehEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TABLE_CONTENT:
                //untuk case yang ingin mengambil table_content keseluruhan
                cursor = db.query(RecehContract.RecehEntry.TABLE_CONTENT_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TABLE_CONTENT_ID:
                //untuk case yang ingin mengambil tablecontent 1 row saja
                selection = RecehContract.RecehEntry.TABLE_CONTENT_ID + "=?";
                //ambil id row yang diinginkan
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                //ambil data dari database
                cursor = db.query(RecehContract.RecehEntry.TABLE_CONTENT_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }
        //biar otomastis update kalo ada perubahan di data (untuk mthod query)
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        //Mencocokan URI yang didapat sesuai kodenya
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DATAUANG:
                return RecehContract.RecehEntry.CONTENT_LIST_TYPE;
            case DATAUANG_ID:
                return RecehContract.RecehEntry.CONTENT_ITEM_TYPE;
            case TABLE_CONTENT:
                return RecehContract.RecehEntry.TABLE_CONTENT_CONTENT_LIST_TYPE;
            case TABLE_CONTENT_ID:
                return RecehContract.RecehEntry.TABLE_CONTENT_CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " +match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        //ambil database untuk write
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //Mencocokan URI yang didapat sesuai kodenya
        final int match = sUriMatcher.match(uri);
        long id;
        switch (match) {
            case DATAUANG:
                //Cek tanggal dan waktunya null apa tidak
                String datetime = contentValues.getAsString(RecehContract.RecehEntry.COLUMN_DATETIME).trim();
                if (datetime == null || datetime.length()==15) {
                    throw new IllegalArgumentException("Date and time null or in unsupported format");
                }
                //Cek title isinya null apa tidak
                String title = contentValues.getAsString(RecehContract.RecehEntry.COLUMN_TITLE).trim();
                if (title == null) {
                    throw new IllegalArgumentException("Title requires for new item");
                }
                //Cek values isinya null apa tidak
                Integer values = contentValues.getAsInteger(RecehContract.RecehEntry.COLUMN_VALUES);
                if (values == null) {
                    throw new IllegalArgumentException("Values requires for new item");
                }
                //Cek tipe transaksi sesuai apa tidak
                Integer transaction = contentValues.getAsInteger(RecehContract.RecehEntry.COLUMN_TRANSACTION);
                if (transaction == null || !RecehContract.RecehEntry.isValidTransaction(transaction)) {
                    throw new IllegalArgumentException("Transaction type requires for new item");
                }
                //masukin data ke database
                id = db.insert(RecehContract.RecehEntry.TABLE_NAME, null, contentValues);
                if (id == -1) {
                    Log.e(LOG_TAG, "Failed to insert row for: "+uri);
                    return null;
                }
                //biar otomastis update kalo ada perubahan di data (untuk mthod insert, delete, dan update)
                getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id);
            case TABLE_CONTENT:
                //Cek title isinya null apa tidak
                String c_title = contentValues.getAsString(RecehContract.RecehEntry.TABLE_CONTENT_COLUMN_TITLE).trim();
                if (c_title == null) {
                    throw new IllegalArgumentException("Title requires for new item");
                }
                id = db.insert(RecehContract.RecehEntry.TABLE_CONTENT_TABLE_NAME, null, contentValues);
                if (id == -1) {
                    Log.e(LOG_TAG, "Failed to insert row for: "+uri);
                    return null;
                }
                //biar otomastis update kalo ada perubahan di data (untuk mthod insert, delete, dan update)
                getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Insertion is not supported for: " + uri);
            //case DATAUANG_ID tidak diperlukan karena kita memasukkan data ke table, bukan ke satu row
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        //Ambil database untuk write
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //Mencocokan URI yang didapat sesuai kodenya
        final int match = sUriMatcher.match(uri);
        int id;
        switch (match) {
            case DATAUANG:
                //mendapatkan id berupa banyaknya row yang dihapus
                id = db.delete(RecehContract.RecehEntry.TABLE_NAME, selection, selectionArgs);
                //biar otomastis update kalo ada perubahan di data (untuk mthod insert, delete, dan update)
                getContext().getContentResolver().notifyChange(uri, null);
                return id;
            case DATAUANG_ID:
                //untuk case yang ingin mengambil data 1 row saja
                selection = RecehContract.RecehEntry._ID + "=?";
                //ambil id row yang diinginkan
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                //mendapatkan id berupa banyaknya row yang dihapus
                id = db.delete(RecehContract.RecehEntry.TABLE_NAME, selection, selectionArgs);
                //biar otomastis update kalo ada perubahan di data (untuk mthod insert, delete, dan update)
                getContext().getContentResolver().notifyChange(uri, null);
                return id;
            case TABLE_CONTENT_ID:
                //untuk case yang ingin mengambil data 1 row saja
                selection = RecehContract.RecehEntry.TABLE_CONTENT_ID + "=?";
                //ambil id row yang diinginkan
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                //mendapatkan id berupa banyaknya row yang dihapus
                id = db.delete(RecehContract.RecehEntry.TABLE_CONTENT_TABLE_NAME, selection, selectionArgs);
                //biar otomastis update kalo ada perubahan di data (untuk mthod insert, delete, dan update)
                getContext().getContentResolver().notifyChange(uri, null);
                return id;
            default:
                throw new IllegalArgumentException("Deletion not supported for: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        //Mencocokan URI yang didapat sesuai kodenya
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DATAUANG:
                return updateDataUang(uri, contentValues, selection, selectionArgs);
            case DATAUANG_ID:
                //untuk case yang ingin mengambil data 1 row saja
                selection = RecehContract.RecehEntry._ID + "=?";
                //ambil id row yang diinginkan
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateDataUang(uri, contentValues, selection,selectionArgs);
            case TABLE_CONTENT_ID:
                //untuk case yang ingin mengambil data 1 row saja
                selection = RecehContract.RecehEntry._ID + "=?";
                //ambil id row yang diinginkan
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateTableContent(uri, contentValues, selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Update not supported for: " + uri);
        }
    }

    private int updateDataUang(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        //Cek tanggal dan waktunya null apa tidak
        String datetime = contentValues.getAsString(RecehContract.RecehEntry.COLUMN_DATETIME).trim();
        if (datetime == null || datetime.length()==15) {
            throw new IllegalArgumentException("Date and time null or in unsupported format");
        }
        //Cek title isinya null apa tidak
        String title = contentValues.getAsString(RecehContract.RecehEntry.COLUMN_TITLE).trim();
        if (title == null) {
            throw new IllegalArgumentException("Title requires for new item");
        }
        //Cek values isinya null apa tidak
        Integer values = contentValues.getAsInteger(RecehContract.RecehEntry.COLUMN_VALUES);
        if (values == null) {
            throw new IllegalArgumentException("Values requires for new item");
        }
        //Cek tipe transaksi sesuai apa tidak
        Integer transaction = contentValues.getAsInteger(RecehContract.RecehEntry.COLUMN_TRANSACTION);
        if (transaction == null || !RecehContract.RecehEntry.isValidTransaction(transaction)) {
            throw new IllegalArgumentException("Transaction type requires for new item");
        }
        //Cek isi contentValues, kalo gaada isinya jangan di update
        if (contentValues.size() == 0) {
            return 0;
        }

        //Ambil database untuk write
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //mendapatkan row yang di update
        int id = db.update(RecehContract.RecehEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        //biar otomastis update kalo ada perubahan di data (untuk mthod insert, delete, dan update)
        getContext().getContentResolver().notifyChange(uri, null);
        return id;
    }

    private int updateTableContent(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        //Cek title isinya null apa tidak
        String title = contentValues.getAsString(RecehContract.RecehEntry.TABLE_CONTENT_COLUMN_TITLE).trim();
        if (title == null) {
            throw new IllegalArgumentException("Title requires for new data");
        }
        //Cek isi contentValues, kalo gaada isinya jangan di update
        if (contentValues.size() == 0) {
            return 0;
        }

        //Ambil database untuk write
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //mendapatkan row yang di update
        int id = db.update(RecehContract.RecehEntry.TABLE_CONTENT_TABLE_NAME, contentValues, selection, selectionArgs);
        //biar otomastis update kalo ada perubahan di data (untuk mthod insert, delete, dan update)
        getContext().getContentResolver().notifyChange(uri, null);
        return id;
    }
}
