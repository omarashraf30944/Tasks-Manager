package com.example.project1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.project1.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    TasksAdapter adapter;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    ActivityMainBinding binding;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        Log.i(TAG, "onSuccess: " + token);
                    }
                });

        getUserData();

        getUserTasks("active");

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);

                int itemId = item.getItemId();

                if(itemId == R.id.item_done){
                    getUserTasks("done");
                    Toast.makeText(MainActivity.this, "done clicked", Toast.LENGTH_SHORT).show();
                }else if(itemId == R.id.item_archive){
                    getUserTasks("archive");
                    Toast.makeText(MainActivity.this, "archive clicked", Toast.LENGTH_SHORT).show();
                }else if(itemId == R.id.item_tasks){
                    getUserTasks("active");
                    Toast.makeText(MainActivity.this, "tasks clicked", Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });



    }

    private void getUserData() {
        firestore.collection("omarUsers")
                .document(firebaseAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phone");
                        String email = documentSnapshot.getString("email");

                        Log.i(TAG, "onSuccess: " + name);
                        Log.i(TAG, "onSuccess: " + phone);
                        Log.i(TAG, "onSuccess: " + email);


                        User user = documentSnapshot.toObject(User.class);
                        Log.i(TAG, "onSuccess: " + user.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = e.getLocalizedMessage();
                        Log.i(TAG, "onFailure: " + errorMessage);
                    }
                });
    }


    private void updateTaskOnFirestore(String status, String taskId) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);

        firestore.collection("tasks")
                .document(firebaseAuth.getCurrentUser().getUid())
                .collection("myTasks")
                .document(taskId)
                .update(map);
    }


    ITasks iTasks = new ITasks() {
        @Override
        public void onDoneClick(Task task) {
            updateTaskOnFirestore("done", task.getId());
            adapter.taskList.remove(task);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onArchiveClick(Task task) {
            updateTaskOnFirestore("archive", task.getId());
            adapter.taskList.remove(task);
            adapter.notifyDataSetChanged();
        }
    };

    public void openInsertSheet(View view) {
        new InsertFragment().show(getSupportFragmentManager(), "dialog");
//        ImagePicker.with(this)
//                .crop()                    //Crop image(Optional), Check Customization for more option
//                .compress(1024)            //Final image size will be less than 1 MB(Optional)
//                .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
//                .start();
    }

    private void getUserTasks(String status) {
        firestore.collection("tasks")
                .document(firebaseAuth.getCurrentUser().getUid())
                .collection("myTasks")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Task> tasks = new ArrayList<>();

                        for(DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                            Task task = snapshot.toObject(Task.class);
                            tasks.add(task);
                        }

                        adapter = new TasksAdapter(tasks,MainActivity.this,iTasks);
                        binding.rvTasks.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMessage = e.getLocalizedMessage();
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });

    }

}

































