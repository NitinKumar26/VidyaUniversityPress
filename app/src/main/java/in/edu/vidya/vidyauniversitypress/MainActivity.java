package in.edu.vidya.vidyauniversitypress;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import in.edu.vidya.vidyauniversitypress.helper.CheckForSDCard;
import in.edu.vidya.vidyauniversitypress.helper.CustomAdapter;
import in.edu.vidya.vidyauniversitypress.modal.PDFDoc;
import pub.devrel.easypermissions.EasyPermissions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int WRITE_REQUEST_CODE = 300;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        toolbar.setTitle(R.string.vidya_university_press);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        assert actionBar !=null;
        //actionBar.setIcon(R.drawable.ic_action_logo);
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



        final ListView gv = findViewById(R.id.gv);
        CustomAdapter adapter = new CustomAdapter(MainActivity.this, getPDfs());
        gv.setAdapter(adapter);
    }

    private ArrayList<PDFDoc> getPDfs()
    {

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==100 && resultCode==100 && data!=null) {
            String url = data.getStringExtra("url");
            new DownloadFile().execute(url);

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
    @SuppressLint("StaticFieldLeak")
    private class DownloadFile extends AsyncTask<String, String, String> {

        private ProgressDialog progressBar;
        //private TextView progressText = findViewById(R.id.textView2);
        private String fileName;
        private String folder;
        //private TextView percentageText= findViewById(R.id.textView);

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressBar= new ProgressDialog(MainActivity.this);
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
            final ListView gv = findViewById(R.id.gv);
            CustomAdapter adapter = new CustomAdapter(MainActivity.this, getPDfs());
            gv.setAdapter(adapter);
        }
    }
}