package in.edu.vidya.vidyauniversitypress;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.AsyncTask;
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
import android.widget.Toast;
import in.edu.vidya.vidyauniversitypress.adapter.BookListAdapter;
import in.edu.vidya.vidyauniversitypress.helper.CheckForSDCard;
import in.edu.vidya.vidyauniversitypress.helper.RecyclerItemTouchHelper;
import in.edu.vidya.vidyauniversitypress.modal.PDFDoc;
import pub.devrel.easypermissions.EasyPermissions;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private static final int WRITE_REQUEST_CODE = 300;
    private static final String TAG = MainActivity.class.getSimpleName();
    private BookListAdapter mAdapter;
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.vidya_university_press);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        mAdapter = new BookListAdapter(this, getPDfs());

        RecyclerView.LayoutManager mLayoutManger = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(mLayoutManger);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);


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
                        Intent qrFragment= new Intent(MainActivity.this,ScanActivity.class);
                        startActivityForResult(qrFragment,100);
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
        ArrayList<PDFDoc> pdfDocs = new ArrayList<>();
        String folder = Environment.getExternalStorageDirectory()+ File.separator + "androiddeft/";
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == 100 && data!=null) {
            String url = data.getStringExtra("url");
            new DownloadFile(MainActivity.this).execute(url);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, MainActivity.this);
    }


    /**
     * Async Task to download file from URL
     */
    private static class DownloadFile extends AsyncTask<String, String, String> {

        private ProgressDialog progressBar;
        private String fileName;
        private String folder;
        private WeakReference<MainActivity> activityWeakReference;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        DownloadFile(MainActivity context){
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity activity = activityWeakReference.get();
            this.progressBar= new ProgressDialog(activity);
            this.progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressBar.setCancelable(false);
            this.progressBar.show();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();
                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                //Extract file name from URL
                fileName = f_url[0].substring(f_url[0].lastIndexOf('/') + 1, f_url[0].length());
                //External directory path to save file
                folder = Environment.getExternalStorageDirectory() + File.separator + "androiddeft/";
                //Create androiddeft folder if it does not exist
                File directory = new File(folder);

                if (!directory.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    directory.mkdirs();
                }
                // Output stream to write file
                OutputStream output = new FileOutputStream(folder + fileName);

                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    Log.d(TAG, "Progress: " + (int) ((total * 100) / lengthOfFile));
                    // writing data to file
                    output.write(data, 0, count);
                }
                // flushing output
                output.flush();
                // closing streams
                output.close();
                input.close();
                //return "Downloaded at: " + folder + fileName;
                return "Download Successful: Restart Application Now";
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return "Something went wrong";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            progressBar.setProgress(Integer.parseInt(progress[0]));
        }
        @Override
        protected void onPostExecute(String message) {
            this.progressBar.dismiss();
            MainActivity activity = activityWeakReference.get();
            BookListAdapter adapter = new BookListAdapter(activity, getPDfs());
            activity.recyclerView.setAdapter(adapter);
            activity.mAdapter = adapter;
        }
    }
}