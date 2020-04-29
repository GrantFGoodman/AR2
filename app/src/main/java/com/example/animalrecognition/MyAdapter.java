package com.example.animalrecognition;

        import android.content.Context;
        import android.graphics.Bitmap;
//import androidx.support.v4.graphics.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.Toast;

        import androidx.annotation.NonNull;
        import androidx.recyclerview.widget.RecyclerView;

        import java.io.File;
        import java.util.ArrayList;
//this is the list of the images

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private ArrayList<cell> galleryList;
    private Context context;

    public MyAdapter(Context context,ArrayList<cell>galleryList){
        this.galleryList=galleryList;
        this.context=context;

    }

    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell,viewGroup,false);
        return new MyAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        setImageFromPath(galleryList.get(i).getPath(),viewHolder.img);
        viewHolder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//            what happens when you click on an image
                Toast.makeText(context,""+galleryList.get(i).getTitle(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView img;
        public ViewHolder(View view){
            super(view);

            img = (ImageView) view.findViewById(R.id.imgs);
        }

    }

    private void setImageFromPath(String path,ImageView image){
        File imgFile =new File(path);
        if(imgFile.exists()){
            Bitmap myBitmap =ImageHelper.decodeSampledBitmapFromPath(imgFile.getAbsolutePath(),200,200);
            image.setImageBitmap(myBitmap);
        }
    }
}
