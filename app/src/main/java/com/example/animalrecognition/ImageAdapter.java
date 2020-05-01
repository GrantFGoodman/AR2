package com.example.animalrecognition;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter{

    // Pretty sure this is unused, but unsafe to delete

    private Context mContext;

    int[] imageArray ={
           R.drawable.ic_person,
            R.drawable.ic_menu_camera,
            R.drawable.ic_gallery,
            R.drawable.ic_chart,
    };

    ImageAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public int getCount(){

        return imageArray.length;
    }

    @Override
    public Object getItem(int position){

        return imageArray[position];
    }

    @Override
    public long getItemId(int position){

        return 0;
    }

    @Override
    public View getView(int position,View convertView,ViewGroup parent){
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(imageArray[position]);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setLayoutParams(new GridView.LayoutParams(340,350));
        return imageView;
    }
}
