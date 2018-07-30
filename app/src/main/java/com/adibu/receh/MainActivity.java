package com.adibu.receh;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adibu.receh.data.RecehContract;
import com.adibu.receh.fragment.HomeFragment;
import com.adibu.receh.fragment.StatsFragment;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private EditText mAddDataDialogEditText;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mToggle;
    private ListView mDataListView;
    private NavigationCursorAdapter mCursorAdapter;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    private View previouslySelectedItem = null;

    private static final int NAVIGATION_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //restore title kalo rotate
        if(savedInstanceState!=null) {
            setTitle(savedInstanceState.getString("TITLE"));
        }

        //bikin actionbar sendiri biar navigation drawer bisa overlay
        Toolbar toolbar = (Toolbar)findViewById(R.id.main_action_bar);
        setSupportActionBar(toolbar);

        //bikin ikon drawer di actiobar (yang garis tiga)
        mDrawerLayout = (DrawerLayout)findViewById(R.id.activity_main_drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.main_activity_open_drawer, R.string.main_activity_close_drawer);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //bikin fab
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(RecehContract.RecehEntry.SELECTED_TABLE_CONTENT_ID == 0) {
                    Toast.makeText(MainActivity.this, getString(R.string.fab_toast_unknown_data), Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(MainActivity.this, EditActivity.class));
                }
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(MainActivity.this, "Developed by Aditya Budi Laksono", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        //munculin data di nav_drawer
        mDataListView = (ListView)findViewById(R.id.main_navigation_list_view);
        mCursorAdapter = new NavigationCursorAdapter(this, null, 0);
        mDataListView.setAdapter(mCursorAdapter);
        getSupportLoaderManager().initLoader(NAVIGATION_LOADER, null, this);

        //bikin onclicklistener add data yang ada di nav_drawer
        final LinearLayout addData = (LinearLayout)findViewById(R.id.main_navigation_add_data);
        addData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewData(true);
            }
        });

        //bikin clicklistener buat data yang ada di nav_drawer
        mDataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //set tablecontent_id sesuai data yang dipilih
                RecehContract.RecehEntry.setSelectedTableContentId(id);

                //set title sesuai data yang dipilih
                TextView titleTV = (TextView)view.findViewById(R.id.navigation_item_title_textview);
                setTitle(titleTV.getText());

                refreshViewPager();
            }
        });

        //munculin fragment di layar
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        // Create an adapter that knows which fragment should be shown on each page
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        // Set the adapter onto the view pager
        mViewPager.setAdapter(mViewPagerAdapter);

        //munculin tab di layar sesuai jumlah fragment
        final TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        /*//List semua item dari semua data (DEBUG)
        TextView myData = (TextView)findViewById(R.id.myData);
        myData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecehContract.RecehEntry.setSelectedTableContentId(-1);

                //set title sesuai data yang dipilih
                TextView titleTV = (TextView)view.findViewById(R.id.navigation_item_title_textview);
                setTitle("ALL ITEM (DEBUG)");

                //refresh viewpager sesuai data yang dipilih
                mDrawerLayout.closeDrawer(GravityCompat.START);
                int currentfragment = mViewPager.getCurrentItem();
                mViewPager.setAdapter(null);
                mViewPager.setAdapter(mViewPagerAdapter);
                mViewPager.setCurrentItem(currentfragment);

                //dishighlight data yang sebelumnya kepilih
                if (previouslySelectedItem != null)
                {
                    previouslySelectedItem.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));
                }
                previouslySelectedItem = view;
            }
        });*/

    }

    //Mengatasi Default Title kalo di rotate
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("TITLE", getTitle().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                RecehContract.RecehEntry.TABLE_CONTENT_COLUMN_TITLE,
                RecehContract.RecehEntry.TABLE_CONTENT_ID

        };
        return new CursorLoader(this,
                RecehContract.RecehEntry.TABLE_CONTENT_CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);

        //Kalo belum ada data, bikin alertdialog buat bikin data baru
        if(mDataListView.getAdapter().getCount() == 0) {
            createNewData(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private void createNewData(boolean cancellable) {
        //bikin alert dialog buat ngasih nama datanya
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(R.layout.main_navigation_edit_data_dialog);
        builder.setTitle(getString(R.string.main_navigation_add_data_dialog_title));
        builder.setPositiveButton(getString(R.string.main_navigation_add_data_dialog_add),null);
        if(cancellable == false) {
            builder.setCancelable(false);
        }
        else {
            builder.setNegativeButton(getString(R.string.main_navigation_add_data_dialog_cancel), null);
        }

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final DialogInterface d = dialogInterface;

                //clicklistener add button
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Dialog f = (Dialog) d;
                        mAddDataDialogEditText = (EditText)f.findViewById(R.id.main_navigation_add_data_title_edit_text);
                        String title = mAddDataDialogEditText.getText().toString().trim();

                        if(title.isEmpty()) {
                            mAddDataDialogEditText.setError(getString(R.string.edit_activity_empty_title));
                        }
                        else {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(RecehContract.RecehEntry.TABLE_CONTENT_COLUMN_TITLE, title);

                            // Insert a new data into the provider, returning the content URI for the new data.
                            Uri newUri = getContentResolver().insert(RecehContract.RecehEntry.TABLE_CONTENT_CONTENT_URI, contentValues);
                            if (newUri == null) {
                                // If the new content URI is null, then there was an error with insertion.
                                Toast.makeText(MainActivity.this, getString(R.string.main_activity_error_add_new_data),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Otherwise, the insertion was successful and we can display a toast.
                                Toast.makeText(MainActivity.this, getString(R.string.main_activity_success_add_new_data),
                                        Toast.LENGTH_SHORT).show();
                            }
                            d.dismiss();
                        }
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void refreshViewPager() {
        //refresh viewpager sesuai data yang dipilih
        mDrawerLayout.closeDrawer(GravityCompat.START);
        int currentfragment = mViewPager.getCurrentItem();
        mViewPager.setAdapter(null);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setCurrentItem(currentfragment);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private String[] tabTitles = {getString(R.string.main_activity_tab_home),getString(R.string.main_activity_tab_stats)};
        final int pageCount = tabTitles.length;

        private ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new HomeFragment();
                default:
                    return new StatsFragment();
            }
        }

        @Override
        public int getCount() {
            return pageCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }



    private class NavigationCursorAdapter extends CursorAdapter {

        private NavigationCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return LayoutInflater.from(context).inflate(R.layout.list_navigation_item, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView titleTV = (TextView)view.findViewById(R.id.navigation_item_title_textview);

            //ambil data cursor
            final String title = cursor.getString(cursor.getColumnIndex(RecehContract.RecehEntry.TABLE_CONTENT_COLUMN_TITLE));
            final int data_id = cursor.getInt(cursor.getColumnIndex(RecehContract.RecehEntry.TABLE_CONTENT_ID));

            //set title sesuai data yang dipilih
            titleTV.setText(title);

            //click listener 3dot item di listview
            ImageView more = (ImageView)view.findViewById(R.id.navigation_item_more);
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String rename = getString(R.string.rename);
                    String delete = getString(R.string.delete);
                    CharSequence[] items = new CharSequence[] {rename, delete};

                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(title);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position) {
                            switch (position) {
                                case 0: //Rename
                                    renameDataDialog(title, data_id);
                                    break;
                                case 1: //Delete
                                    deleteDataWithConfirmationDialog(data_id);
                                    break;
                            }
                        }
                    });
                    builder.show();
                }
            });
        }
        private void renameDataDialog(final String title, final int id){
            //bikin alert dialog buat ngasih nama datanya
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setView(R.layout.main_navigation_edit_data_dialog);
            builder.setTitle(getString(R.string.rename) + " \"" + title + "\"");
            builder.setNegativeButton(getString(R.string.main_navigation_add_data_dialog_cancel), null);
            builder.setPositiveButton(getString(R.string.main_navigation_add_data_dialog_add),null);

            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    final DialogInterface d = dialogInterface;

                    //clicklistener add button
                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Dialog f = (Dialog) d;
                            mAddDataDialogEditText = (EditText)f.findViewById(R.id.main_navigation_add_data_title_edit_text);
                            String titleRename = mAddDataDialogEditText.getText().toString().trim();

                            if(titleRename.isEmpty()) {
                                mAddDataDialogEditText.setError(getString(R.string.edit_activity_empty_title));
                            }
                            else {
                                CharSequence actionBarTitle = getTitle();

                                ContentValues contentValues = new ContentValues();
                                contentValues.put(RecehContract.RecehEntry.TABLE_CONTENT_COLUMN_TITLE, titleRename);
                                Uri selectedDataUri = ContentUris.withAppendedId(RecehContract.RecehEntry.TABLE_CONTENT_CONTENT_URI, id);

                                // Insert a new data into the provider, returning the content URI for the new data.
                                int updateRow = getContentResolver().update(selectedDataUri, contentValues, null, null);
                                if (updateRow == 0) {
                                    // If the new content URI is null, then there was an error with insertion.
                                    Toast.makeText(MainActivity.this, getString(R.string.main_activity_error_rename_data),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    // Otherwise, the insertion was successful and we can display a toast.
                                    Toast.makeText(MainActivity.this, getString(R.string.main_activity_success_rename_data),
                                            Toast.LENGTH_SHORT).show();

                                    //ubah actionBarTitle sesuai dengan data yang direname jika data yang di rename adalah data yang sedang dipilih
                                    if(actionBarTitle.equals(title)) {
                                        setTitle(titleRename);
                                    }
                                }
                                d.dismiss();
                            }
                        }
                    });
                }
            });
            alertDialog.show();
        }

        private void deleteDataWithConfirmationDialog(final int id) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(R.string.main_naviation_item_delete_confirmation_title);
            builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Uri selectedDataUri = ContentUris.withAppendedId(RecehContract.RecehEntry.TABLE_CONTENT_CONTENT_URI, id);
                    int deletedRowDataTitle = getContentResolver().delete(selectedDataUri, null, null);

                    String selection = RecehContract.RecehEntry.COLUMN_TABLE_CONTENT_ID + "=?";
                    String[] selectionArgs = {String.valueOf(id)};
                    int deletedRowDataItem = getContentResolver().delete(RecehContract.RecehEntry.CONTENT_URI, selection, selectionArgs);


                    if (deletedRowDataTitle == 0 && deletedRowDataItem == 0) {
                        Toast.makeText(MainActivity.this, getString(R.string.main_activity_error_delete_data), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, getString(R.string.main_activity_success_delete_data), Toast.LENGTH_SHORT).show();
                        setTitle("Receh");
                        RecehContract.RecehEntry.setSelectedTableContentId(0);

                        refreshViewPager();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
}
