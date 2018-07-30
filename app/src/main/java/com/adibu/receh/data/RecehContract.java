package com.adibu.receh.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by AdityaBudi on 24/07/2017.
 */

public class RecehContract {

    public static final String CONTENT_AUTHORITY = "com.adibu.receh";

    //basis untuk semua Uri yang bakal digunakan ke content provider aplikasi ini
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Path untuk masuk ke data table
    public static final String PATH_DATAUANG = RecehEntry.TABLE_NAME;

    //Path untuk masuk ke tablecontent table
    public static final String PATH_TABLE_CONTENT = RecehEntry.TABLE_CONTENT_TABLE_NAME;


    private RecehContract() {}

    public static class RecehEntry implements BaseColumns {

        //--------------
        //Tablecontent

        //Uri untuk akses table_content di provider
        public static final Uri TABLE_CONTENT_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TABLE_CONTENT);

        //The MIME type of the {@link #CONTENT_URI} for a list of tablecontent.
        public static final String TABLE_CONTENT_CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TABLE_CONTENT;

        //The MIME type of the {@link #CONTENT_URI} for a single table_content.
        public static final String TABLE_CONTENT_CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TABLE_CONTENT;

        //Nama table untuk databasenya
        public static final String TABLE_CONTENT_TABLE_NAME = "tablecontent";

        //Judul item
        //Type: TEXT
        public static final String TABLE_CONTENT_COLUMN_TITLE = "title";

        //Unique Id untuk setiap row data yang ada
        //Type: INTEGER
        public static final String TABLE_CONTENT_ID = BaseColumns._ID;

        public static long SELECTED_TABLE_CONTENT_ID = 0;

        public static void setSelectedTableContentId(long id) {
            SELECTED_TABLE_CONTENT_ID = id;
        }

        //--------------


        //Uri untuk akses item di provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_DATAUANG);

        //The MIME type of the {@link #CONTENT_URI} for a list of item.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DATAUANG;

        //The MIME type of the {@link #CONTENT_URI} for a single item.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DATAUANG;

        //Nama table untuk databasenya
        public static final String TABLE_NAME = "datauang";

        //Unique Id untuk setiap row item yang ada
        //Type: INTEGER
        //COLUMN 1
        public static final String _ID = BaseColumns._ID;

        //Tanggal item. tidak harus sesuai tanggal dibuat item
        //Type: TEXT
        //COLUMN 2
        public static final String COLUMN_DATETIME = "datetime";

        //Judul data
        //Type: TEXT
        //COLUMN 3
        public static final String COLUMN_TITLE = "title";

        //Banyaknya uang yang ingin dimasukkan kedalam item
        //Type: INTEGER
        //COLUMN 4
        public static final String COLUMN_VALUES = "valuess";

        //Tipe transaksi item
        //Data yang dimasukkan hanya boleh dua pilihan: income(1) atau outcome(2)
        //Type: INTEGER
        //COLUMN 5
        public static final String COLUMN_TRANSACTION = "transactionn";

        //Deskripsi tambahan dari item yang ingin dimasukkan
        //Type: TEXT
        //COLUMN 6
        public static final String COLUMN_DESCRIPTION = "description";

        //id item untuk tablecontent yang mana
        //Type: INTEGER
        //COLUMN 7
        public static final String COLUMN_TABLE_CONTENT_ID = "tablecontent_id";


        //Kemungkinan pilihan untuk COLUMN_TRANSACTION
        public static final int TRANSACTION_INCOME = 1;
        public static final int TRANSACTION_OUTCOME = 2;

        public static boolean isValidTransaction (int transaction) {
            if (transaction == TRANSACTION_INCOME || transaction == TRANSACTION_OUTCOME) {
                return true;
            }
            return false;
        }
    }
}
