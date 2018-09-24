package in.edu.vidya.vidyauniversitypress;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import java.io.File;

public class PDFActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        PDFView pdfView = findViewById(R.id.pdfView_view);
        Intent i = this.getIntent();
        String path = i.getStringExtra("path");
        String name = i.getStringExtra("name");
        Toolbar toolbar = findViewById(R.id.toolbar_pdf);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //name=name.substring(0,name.lastIndexOf('.'));

        File file = new File(path);
        if (file.canRead()){
            //Load It
            pdfView.fromFile(file)
                    .defaultPage(0)
                    .swipeHorizontal(true)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .onLoad(new OnLoadCompleteListener() {
                        @Override
                        public void loadComplete(int nbPages) {
                            //Toast.makeText(PDFActivity.this,String.valueOf(nbPages),Toast.LENGTH_LONG).show();
                        }
                    }).load();


        }
    }
}