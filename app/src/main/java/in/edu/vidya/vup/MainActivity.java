package in.edu.vidya.vup;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import in.edu.vidya.vup.helper.DownloadService;

import java.io.File;
import java.util.ArrayList;

import in.edu.vidya.vup.adapter.BookListAdapter;
import in.edu.vidya.vup.helper.CheckForSDCard;
import in.edu.vidya.vup.helper.DirectoryHelper;
import in.edu.vidya.vup.helper.RecyclerItemTouchHelper;
import in.edu.vidya.vup.modal.PDFDoc;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private static final int WRITE_REQUEST_CODE = 300;
    private static final String TAG = MainActivity.class.getSimpleName();
    public BookListAdapter mAdapter;
    private CoordinatorLayout coordinatorLayout;
    public RecyclerView recyclerView;
    LinearLayout emptyView;
    static ArrayList<PDFDoc> pdfDocs;
    public static String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.vidya_university_press);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        emptyView = findViewById(R.id.empty_view);
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        mAdapter = new BookListAdapter(this, getPDfs());
        AdView mAdView = findViewById(R.id.adView_banner);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        if (pdfDocs.isEmpty()){
            emptyView.setVisibility(View.VISIBLE);
            }
        RecyclerView.LayoutManager mLayoutManger = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(mLayoutManger);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);
        DirectoryHelper.createDirectory(this);


        //adding item touch helper
        //only ItemTouchHelper.LEFT added to detect Right to Left swipe
        //if you want both Right -> Left and Left -> Right
        //add pass ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT as param
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        FloatingActionButton qrButton = findViewById(R.id.qr_button);
        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckForSDCard.isSDCardPresent()){
                    //check if app has permission to write to the external storage
                    if (EasyPermissions.hasPermissions(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        if (EasyPermissions.hasPermissions(MainActivity.this,Manifest.permission.CAMERA)){
                            //Send Intent to ScanActivity
                            if (isNetworkAvailable()) {
                                Intent qrFragment = new Intent(MainActivity.this, ScanActivity.class);
                                startActivityForResult(qrFragment, 100);
                            }else{
                                Toast.makeText(MainActivity.this, "Please Check Your Internet Connection", Toast.LENGTH_LONG).show();
                            }
                        }else
                            EasyPermissions.requestPermissions(MainActivity.this,getString(R.string.write_file),WRITE_REQUEST_CODE,Manifest.permission.CAMERA);
                    }else{
                        //If permission is not present, request fro the same.
                        EasyPermissions.requestPermissions(MainActivity.this,getString(R.string.write_file),WRITE_REQUEST_CODE,Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"SD Card not found",Toast.LENGTH_LONG).show();
                }
            }
        });

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback1 = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Row is swiped from recycler view
                // remove it from adapter
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        // attaching the touch helper to recycler view
        new ItemTouchHelper(itemTouchHelperCallback1).attachToRecyclerView(recyclerView);

    }


    public static ArrayList<PDFDoc> getPDfs(){
        String name;
        pdfDocs = new ArrayList<>();
        String folder = Environment.getExternalStorageDirectory() + File.separator + "androiddeft/";
        File directory = new File(folder);
        PDFDoc pdfDoc;
        if (directory.exists()){
            //Get all the files in the folder;
            File[]  files = directory.listFiles();
            //Loop Through those files getting names and Uri
            if (files!=null) {
                for (File file : files) {
                    if (file.getPath().endsWith("pdf")) {
                        pdfDoc = new PDFDoc();
                        name = file.getName();
                        name = name.substring(0, name.lastIndexOf('.'));
                        pdfDoc.setName(name);
                        pdfDoc.setPath(file.getAbsolutePath());
                        pdfDocs.add(pdfDoc);
                    }
                }
            }
        }
        return pdfDocs;
    }

    /**
     * callback when recycler view is swiped
     * item will be removed on swiped
     * undo option will be provided in snackbar to restore the item
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof BookListAdapter.MyViewHolder) {
            // get the removed item name to display it in snack bar
            String name = getPDfs().get(viewHolder.getAdapterPosition()).getName();
            // remove the item from recycler view
            mAdapter.removeItem(viewHolder.getAdapterPosition());
            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar.make(coordinatorLayout, name + " removed from your library", Snackbar.LENGTH_LONG);
            snackbar.show();
            if (getPDfs().isEmpty()){
                emptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == 100 && data!=null) {
            url = data.getStringExtra("url");
            //Log.e("token", tokenNumber);
            Log.d("url", url);
            //Log.d("JSON String ----",DownloadService.GetBookName.getString());
            startService(DownloadService.getDownloadService(this, url, DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")));
            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            //new Done();
        }
    }

    public BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BookListAdapter adapter = new BookListAdapter(MainActivity.this, getPDfs());
            recyclerView.setAdapter(adapter);
            mAdapter = adapter;
            emptyView.setVisibility(View.GONE);
        }
    };
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, MainActivity.this);
    }


    /**
     * Checks if there is Internet accessible.
     * Based on a stackoverflow snippet
     *
     * @return True if there is Internet. False if not.
     */
    private boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = null;
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager!=null){
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}