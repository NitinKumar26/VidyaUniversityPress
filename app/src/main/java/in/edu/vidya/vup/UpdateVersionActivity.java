package in.edu.vidya.vup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class UpdateVersionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_version);

        Button updateNowButton = findViewById(R.id.update_now_button);
        updateNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=in.edu.vidya.vup"));
                startActivity(intent);
            }
        });

        AdView adView = findViewById(R.id.adView_banner_update_version);
        AdRequest adRequest = new AdRequest.Builder().build();
        //Load the requested ad
        adView.loadAd(adRequest);
    }
}
