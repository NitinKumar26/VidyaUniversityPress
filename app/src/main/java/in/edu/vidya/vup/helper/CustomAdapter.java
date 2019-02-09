package in.edu.vidya.vup.helper;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import in.edu.vidya.vup.modal.PDFDoc;
import in.edu.vidya.vup.PDFActivity;
import java.util.ArrayList;
import in.edu.vidya.vup.R;

/**
 * Created by Oclemy on 7/28/2016 for ProgrammingWizards Channel and http://www.camposha.com.
 */
public class CustomAdapter extends BaseAdapter {

    private final Context c;
    private final ArrayList<PDFDoc> pdfDocs;

    public CustomAdapter(Context c, ArrayList<PDFDoc> pdfDocs) {
        this.c = c;
        this.pdfDocs = pdfDocs;
    }

    @Override
    public int getCount() {
        return pdfDocs.size();
    }

    @Override
    public Object getItem(int i) {
        return pdfDocs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null)
        {
            //INFLATE CUSTOM LAYOUT
            view= LayoutInflater.from(c).inflate(R.layout.model,viewGroup,false);
        }

        final PDFDoc pdfDoc= (PDFDoc) this.getItem(i);

        TextView nameTxt= view.findViewById(R.id.nameTxt);
        //ImageView img= (ImageView) view.findViewById(R.id.pdfImage);

        //BIND DATA
        nameTxt.setText(pdfDoc.getName());
        //img.setImageResource(R.drawable.book_png);

        //VIEW ITEM CLICK
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPDFView(pdfDoc.getPath(),pdfDoc.getName());
            }
        });
        return view;
    }

    //OPEN PDF VIEW
    private void openPDFView(String path,String name)
    {
        Intent i=new Intent(c,PDFActivity.class);
        i.putExtra("path",path);
        i.putExtra("name", name);
        c.startActivity(i);
    }
}

