package com.example.animalrecognition;



import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.animalrecognition.MyAdapter;
import com.example.animalrecognition.R;
import com.example.animalrecognition.cell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Gallery<allFiles> extends Fragment {

    private GalleryViewModel mViewModel;
    private RecyclerView recyclerView;

    public static Gallery newInstance() {
        return new Gallery();
    }

    //    add this block for the gallery grid view
    List<cell> allFilesPaths;

    @Override
    public void onCreate(Bundle savedInstanceStat) {
        super.onCreate(savedInstanceStat);
        setContentView(R.layout.fragment_gallery);
        recyclerView = (RecyclerView) recyclerView.findViewById(R.id.gallery);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),3);
        recyclerView.setLayoutManager(layoutManager);


        ArrayList<cell> cells = prepareData();
        MyAdapter adapter= new MyAdapter(getApplicationContext(),cells);
        recyclerView.setAdapter(adapter);
//        for the storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
        } else {
//            show the images
            showImages();
        }


    }

    private void setContentView(int gallery_fragment) {
    }

    private int checkSelfPermission(String readExternalStorage) {
    }

    //    show the images on the screen
    private void showImages() {
//        this is the folder with all the images
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() +"/Images";
        allFilesPaths = new ArrayList<>();
        allFilesPaths = listAllFiles(path);



//make the list with 3 columns

    }

    private Context getApplicationContext() {
    }


    //    prepare the images for the list
    private ArrayList<cell> prepareData(){
        ArrayList<cell> allImages =new ArrayList<>();
        for(cell c:allFilesPaths){
            cell cell=new cell();
            cell.setTitle(c.getTitle());
            cell.setPath(c.getPath());
            allImages.add(cell);
        }
        return allImages;
    }

    //    load all files from the folder
    private List<cell> listAllFiles(String pathName) {
        List<cell> allFiles = new ArrayList<>();
        File file = new File(pathName);
        File[] files = file.listFiles();
        if (files != null) {
            cell cell = new cell();
            cell.setTitle(file.getName());
            cell.setPath(file.getAbsolutePath());
            allFiles.add(cell);
        }
        return allFiles;
    };



    public void onRequestPermissionResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults){
        if(requestCode == 1000)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
//               show the images
                showImages();
            }
            else {
//                Toast.makeText()akeText(this,"Permission not granted!", Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    }

    private void finish() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(GalleryViewModel.class);
        // TODO: Use the ViewModel
    }

}

