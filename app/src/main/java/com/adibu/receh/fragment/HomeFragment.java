package com.adibu.receh.fragment;


import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.adibu.receh.DetailActivity;
import com.adibu.receh.R;
import com.adibu.receh.data.RecehContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    HistoryCursorAdapter mCursorAdapter;
    ListView itemListView;
    private static final int RECEH_LOADER = 0;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        itemListView = (ListView)rootView.findViewById(R.id.list_view);
        mCursorAdapter = new HistoryCursorAdapter(getContext(), null, 0);
        itemListView.setAdapter(mCursorAdapter);
        View emptyView = rootView.findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                Uri selectedDataUri = ContentUris.withAppendedId(RecehContract.RecehEntry.CONTENT_URI, id);
                intent.setData(selectedDataUri);
                startActivity(intent);
            }
        });
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onResume() {
        getLoaderManager().destroyLoader(RECEH_LOADER);
        getLoaderManager().initLoader(RECEH_LOADER, null, this);
        super.onResume();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String[] projection = {
                RecehContract.RecehEntry._ID,
                RecehContract.RecehEntry.COLUMN_TITLE,
                RecehContract.RecehEntry.COLUMN_VALUES,
                RecehContract.RecehEntry.COLUMN_TRANSACTION
        };

        /*//kalo -1 ambil semua item (DEBUG)
        if (RecehContract.RecehEntry.SELECTED_TABLE_CONTENT_ID == -1) {
            return new CursorLoader(getContext(),
                    RecehContract.RecehEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);
        }*/
        //else {
            String selection = RecehContract.RecehEntry.COLUMN_TABLE_CONTENT_ID + "=?";
            String[] selectionArgs = new String[] {String.valueOf(RecehContract.RecehEntry.SELECTED_TABLE_CONTENT_ID)};
            return new CursorLoader(getContext(),
                    RecehContract.RecehEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null);
        //}
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mCursorAdapter.swapCursor(null);
    }

    class HistoryCursorAdapter extends CursorAdapter {

        public HistoryCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return LayoutInflater.from(context).inflate(R.layout.list_home_item, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView titleTV = (TextView)view.findViewById(R.id.history_item_title);
            TextView valuesTV = (TextView)view.findViewById(R.id.history_item_value);
            ImageView transactionIV = (ImageView) view.findViewById(R.id.history_item_transaction);

            String title = cursor.getString(cursor.getColumnIndex(RecehContract.RecehEntry.COLUMN_TITLE));
            Integer values = cursor.getInt(cursor.getColumnIndex(RecehContract.RecehEntry.COLUMN_VALUES));
            Integer transaction = cursor.getInt(cursor.getColumnIndex(RecehContract.RecehEntry.COLUMN_TRANSACTION));

            titleTV.setText(title);
            valuesTV.setText(values.toString());

            if (transaction == RecehContract.RecehEntry.TRANSACTION_INCOME) {
                transactionIV.setImageResource(R.drawable.ic_income);
            }

            else{
                transactionIV.setImageResource(R.drawable.ic_outcome);
            }
        }
    }
}
