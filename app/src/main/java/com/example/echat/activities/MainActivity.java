package com.example.echat.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.example.echat.adapters.RecentConversionAdapter;
import com.example.echat.databinding.ActivityMainBinding;
import com.example.echat.listeners.ConversionListeners;
import com.example.echat.models.ChatMessage;
import com.example.echat.models.User;
import com.example.echat.utilities.Constants;
import com.example.echat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListeners
{
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversions;
    private RecentConversionAdapter conversionAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUserDetails();
        getToken();
        setListeners();
        listenConverstions();
    }

    private void init()
    {
        conversions = new ArrayList<>();
        conversionAdapter = new RecentConversionAdapter(conversions,this);
        binding.conversionRecycleView.setAdapter(conversionAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners()
    {
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(),UserActivitu.class))
                );
    }
     private void loadUserDetails()
     {
         binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
         byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
         Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
         binding.imageProfile.setImageBitmap(bitmap);
     }

     private void showToast(String massage)
     {
         Toast.makeText(getApplicationContext(),massage,Toast.LENGTH_LONG).show();
     }

     private void listenConverstions()
     {
         database.collection(Constants.KEY_COLLECTIONS_CONVERSIONS)
                 .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                 .addSnapshotListener(eventListener);

         database.collection(Constants.KEY_COLLECTIONS_CONVERSIONS)
                 .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                 .addSnapshotListener(eventListener);
     }



     private final EventListener<QuerySnapshot> eventListener = (value , error) ->
     {
         if (error != null) {
             return;
         }
         if (value != null) {
             for (DocumentChange documentChange : value.getDocumentChanges()) {
                 if (documentChange.getType() == DocumentChange.Type.ADDED) {
                     String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                     String reciverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                     ChatMessage chatMessage = new ChatMessage();
                     chatMessage.senderId = senderId;
                     chatMessage.receiverId = reciverId;
                     if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                         chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                         chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                         chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                     } else {
                         chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                         chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                         chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);

                     }
                     chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                     chatMessage.dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                     conversions.add(chatMessage);

                 } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                     for (int i = 0; i < conversions.size(); i++) {
                         String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                         String receivedId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                         if (conversions.get(i).senderId.equals(senderId) && conversions.get(i).receiverId.equals(receivedId)) {
                             conversions.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                             conversions.get(i).dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                             break;
                         }
                     }
                 }
             }
             Collections.sort(conversions, (obj1, obj2) -> obj2.dataObject.compareTo(obj1.dataObject));
             conversionAdapter.notifyDataSetChanged();
             binding.conversionRecycleView.smoothScrollToPosition(0);
             binding.conversionRecycleView.setVisibility(View.VISIBLE);
             binding.progressBar.setVisibility(View.GONE);

         }

    };

     private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
     }

     private void updateToken(String token)
     {
         FirebaseFirestore database = FirebaseFirestore.getInstance();
         preferenceManager.putString(Constants.KEY_FCM_TOKEN,token);

         DocumentReference documentReference =
                 database.collection(Constants.KEY_COLLECTION_USERS)
                         .document(preferenceManager.getString(Constants.KEY_USER_ID));
         documentReference.update(Constants.KEY_FCM_TOKEN,token)
                 .addOnFailureListener(e -> showToast("Unable to update Token"));
     }

     private void signOut()
     {
         showToast("Signed Out...");
         FirebaseFirestore database = FirebaseFirestore.getInstance();
         DocumentReference documentReference =
                 database.collection(Constants.KEY_COLLECTION_USERS).document(
                         preferenceManager.getString(Constants.KEY_USER_ID)
                 );
         HashMap<String , Object> updates = new HashMap<>();
         updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
         documentReference.update(updates)
                 .addOnSuccessListener(unused -> {
                     preferenceManager.clear();
                     startActivity(new Intent(getApplicationContext(),SignUpActivity.class));
                     finish();
                 })
                 .addOnFailureListener(e -> showToast("Unable to Sign Out"));
     }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }
}