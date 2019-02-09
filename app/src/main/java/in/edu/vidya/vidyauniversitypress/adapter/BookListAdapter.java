package in.edu.vidya.vidyauniversitypress.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.File;
import java.util.List;
import in.edu.vidya.vidyauniversitypress.MainActivity;
import in.edu.vidya.vidyauniversitypress.PDFActivity;
import in.edu.vidya.vidyauniversitypress.R;
import in.edu.vidya.vidyauniversitypress.modal.PDFDoc;


public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.MyViewHolder> {

    private Context context;
    private List<PDFDoc> bookList;


    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nameTextView;
        public RelativeLayout viewBackground, viewForeground;

        MyViewHolder(View view){
            super(view);
            nameTextView = view.findViewById(R.id.nameTxt);
            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);
        }
    }

    public BookListAdapter(Context context, List<PDFDoc> bookList){
        this.context = context;
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.model, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final PDFDoc item = bookList.get(position);
        holder.nameTextView.setText(item.getName());
        holder.viewForeground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPDFView(item.getPath(), item.getName());
            }
        });

    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void removeItem(int position){
        bookList.remove(position);
        PDFDoc pdfDoc = MainActivity.getPDfs().get(position);
        //notify the item removed by the position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        String folder = Environment.getExternalStorageDirectory()+ File.separator + "androiddeft/";
        File directory = new File(folder);
        File[] files = directory.listFiles();
        for (File file : files){
            String filePath = file.getAbsolutePath();
            if (filePath.equals(pdfDoc.getPath())){
                file.delete();
            }

        }
        notifyItemRemoved(position);
    }
    //OPEN PDF VIEW
    private void openPDFView(String path,String name)
    {
        Intent i = new Intent(context, PDFActivity.class);
        i.putExtra("path",path);
        i.putExtra("name", name);
        context.startActivity(i);
    }
}
