package in.edu.vidya.vup;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.ads.MobileAds;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.ref.WeakReference;
import in.edu.vidya.vup.app.AppConfig;
import in.edu.vidya.vup.helper.HttpHandler;

public class SplashActivity extends AppCompatActivity {
    private static String versionCodeApp;
    private static String versionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //Initializing AdMob SApp
        MobileAds.initialize(this, getString(R.string.adMob_app_id));
        int versionCode = BuildConfig.VERSION_CODE;
        versionCodeApp = String.valueOf(versionCode);

        if (isNetworkAvailable()) {
            new GetVersionCode(this).execute();
        }else {
            Toast.makeText(SplashActivity.this, "Please Check your Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Checks if there is Internet accessible.
     * Based on a stackoverflow snippet
     *
     * @return True if there is Internet. False if not.
     */
    private boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = null;
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager!=null){
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    private static class GetVersionCode extends AsyncTask<Void, Void, Void> {
        private String url;
        private final WeakReference<SplashActivity> activityWeakReference;

        GetVersionCode(SplashActivity context){
            activityWeakReference = new WeakReference<>(context);
        }


        @Override
        protected Void doInBackground(Void... arg0) {
            final SplashActivity activity = activityWeakReference.get();
            HttpHandler sh = new HttpHandler();
            url = AppConfig.VERSION_CODE_ENDPOINT;
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e("Company Activity ",  "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONArray array = new JSONArray(jsonStr);
                    JSONObject jsonObject = array.getJSONObject(0);
                    versionCode = jsonObject.getString("vup_version_code");
                    Log.e("versionCode", versionCode);

                } catch (final JSONException e) {
                    Log.e("SplashScreen JSON Error", "Json parsing error: " + e.getMessage());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "Json parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                }
            } else {
                Log.e("COULDNT GET", "Couldn't get json from server.");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity,
                                "Couldn't get json from server. Check LogCat for possible errors!", Toast.LENGTH_LONG).show();
                    }
                });

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            final SplashActivity activity = activityWeakReference.get();
            if (versionCode.equalsIgnoreCase(versionCodeApp)){

                int SPLASH_TIME_OUT = 1500;
                new Handler().postDelayed(new Runnable() {

                    /*
                     * Showing splash screen with a timer. This will be useful when you
                     * want to show case your app logo / company
                     */

                    @Override
                    public void run() {
                        // This method will be executed once the timer is over
                        // Start your app main activity
                        Intent intent = new Intent(activity, MainActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                    }


                },SPLASH_TIME_OUT);
            }else{
                Intent intent = new Intent(activity, UpdateVersionActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
        }
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
}