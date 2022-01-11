package com.example.echat.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.echat.adapters.UserAdapter;
import com.example.echat.databinding.ActivityUsersBinding;
import com.example.echat.listeners.UserListener;
import com.example.echat.models.User;
import com.example.echat.utilities.Constants;
import com.example.echat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserActivitu extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }


    private void setListeners()
    {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers()
    {

        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task ->
                {
                    loading(false);
                    String currintUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful()&& task.getResult() != null)
                    {
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult())
                        {
                            if(currintUserId.equals(queryDocumentSnapshot.getId()))
                            {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if(users.size()>0)
                        {
                            UserAdapter userAdapter = new UserAdapter(users,this);
                            binding.userRecyclerView.setAdapter(userAdapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                        }else {
                            showErorrMassage();
                        }
                    }else {
                        showErorrMassage();
                    }
                });
    }
    private void showErorrMassage()
    {
        binding.textErrorMassage.setText(String.format("%s","No user available"));
        binding.textErrorMassage.setVisibility(View.VISIBLE);
    }


    private  void loading(Boolean isloading)
    {
        if(isloading)
        {
            binding.progressBar.setVisibility(View.VISIBLE);

        }

        else
        {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user)
    {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}