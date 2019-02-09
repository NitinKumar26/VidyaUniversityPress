package in.edu.vidya.vidyauniversitypress.helper;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import in.edu.vidya.vidyauniversitypress.MainActivity;
import in.edu.vidya.vidyauniversitypress.app.AppConfig;
import in.edu.vidya.vidyauniversitypress.adapter.BookListAdapter;

public class DownloadService extends IntentService
{

    private static final String DOWNLOAD_PATH = "com.spartons.androiddownloadmanager_DownloadSongService_Download_path";
    private static final String DESTINATION_PATH = "com.spartons.androiddownloadmanager_DownloadSongService_Destination_path";
    static String pathName ="";

    public DownloadService() {
        super("DownloadService");
    }

    public static Intent getDownloadService(final @NonNull Context callingClassContext, final @NonNull String downloadPath, final @NonNull String destinationPath) {
        return new Intent(callingClassContext, DownloadService.class)
                .putExtra(DOWNLOAD_PATH, downloadPath)
                .putExtra(DESTINATION_PATH, destinationPath);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String downloadPath = intent.getStringExtra(DOWNLOAD_PATH);
            String destinationPath = intent.getStringExtra(DESTINATION_PATH);
            startDownload(downloadPath, destinationPath);
        }
    }


    private void startDownload(String downloadPath, String destinationPath) {
        pathName = runT(downloadPath);
        Log.e("Path ---------", pathName);
        Uri uri = Uri.parse(downloadPath);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);  // Tell on which network you want to download file.
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);  // This will show notification on top when downloading the file.
        request.setTitle("Downloading Your Book"); // Title for notification.
        request.setVisibleInDownloadsUi(false);
        request.setDestinationInExternalPublicDir(destinationPath, pathName+".pdf");  // Storage directory path
        ((DownloadManager) Objects.requireNonNull(getSystemService(Context.DOWNLOAD_SERVICE))).enqueue(request); // This will start downloading
    }
     String runT(String uri){
         try{
             String result="";
             String bookName="";
             String tokenNumber = uri.substring(uri.lastIndexOf('/') + 1, uri.length() - 4);
             Log.e("Path ---------", tokenNumber);
             URL url = new URL("http://vidyauniversitypress.com/FindBookName.php?TN="+Integer.parseInt(tokenNumber));

             HttpURLConnection http = (HttpURLConnection) url.openConnection();
             http.setRequestMethod("GET");
             http.setDoInput(true);

             InputStream ips = http.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(ips,"ISO-8859-1"));
             String line ="";
             while((line = reader.readLine()) != null){
                 result += line;
                 Log.d("Message----",line);
             }
             /*----------------------------------*/

             try{
                 JSONObject jsonObject = new JSONObject(result);
                 bookName = jsonObject.getString("BookName");
                 Log.e("bookName", bookName);
                 return bookName;

             }catch (final JSONException e) {Log.e("COMAPANY JSON EXCEPTION", "Json parsing error: " + e.getMessage());
             }

             /*-----------------------------------*/
             reader.close();
             ips.close();
             http.disconnect();
             Log.d("Book Name -- ",bookName);
         }
         catch (MalformedURLException e) {
             e.getMessage();
         } catch (IOException e) {
             e.getMessage();
         }
         return "Book";
     }
}