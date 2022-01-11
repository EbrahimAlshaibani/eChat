package com.example.echat.activities;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.echat.adapters.ChatAdapter;
import com.example.echat.databinding.ActivityChatBinding;
import com.example.echat.models.ChatMessage;
import com.example.echat.models.User;
import com.example.echat.network.ApiClient;
import com.example.echat.network.ApiService;
import com.example.echat.utilities.Constants;
import com.example.echat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {
    private ActivityChatBinding binding;
    private User reciverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isResiverAvailabile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        inti();
        listenMessages();
    }
    private void inti()
    {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapEncodedString(reciverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }


    private void sendMessage()
    {
        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID,reciverUser.id);
        message.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP,new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversionId != null)
        {
            updateConversion(binding.inputMessage.getText().toString());

        }else
        {
            HashMap<String ,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,reciverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME,reciverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,reciverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }
        if(!isResiverAvailabile)
        {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(reciverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME,preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN,preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA,data);
                body.put(Constants.REMOTE_MSG_REGISTRATION,tokens);

                sendNotification(body.toString());


            }catch (Exception exception)
            {
                showToast(exception.getMessage());
            }
        }
        binding.inputMessage.setText(null);
    }
    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }


    private void sendNotification(String messageBody)
    {
        ApiClient.getclient().create(ApiService.class).sendMessage(
                Constants.getRemoteHsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
            if(response.isSuccessful()){
                try {
                    if(response.body()!= null)
                    {
                        JSONObject responesJson = new JSONObject(response.body());
                        JSONArray result = responesJson.getJSONArray("result");
                        if(responesJson.getInt("failure")==1)
                        {
                            JSONObject erorr = (JSONObject) result.get(0);
                            showToast(erorr.getString("erorr"));
                            return;
                        }
                    }

                }catch (JSONException e)
                {
                    e.printStackTrace();

                }
                showToast("Notification sent");

            }else {
                showToast("Erorr : "+ response.code());
            }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }


private void listenAvailablelityOfResiver()
{
    database.collection(Constants.KEY_COLLECTION_USERS).document(
            reciverUser.id
    ).addSnapshotListener(ChatActivity.this,(value, error) -> {
        if(error != null)
        {
            return;
        }
        if(value != null)
        {
            if(value.getLong(Constants.KEY_AVAILABILITY)!= null)
            {
                int availbality = Objects.requireNonNull(
                        value.getLong(Constants.KEY_AVAILABILITY)
                ).intValue();
                isResiverAvailabile = availbality == 1;
            }
            reciverUser.token = value.getString((Constants.KEY_FCM_TOKEN));

            if(reciverUser.image==null)
            {
                reciverUser.image= value.getString(Constants.KEY_IMAGE);
                chatAdapter.setReceiverProfileImage(getBitmapEncodedString(reciverUser.image));
                chatAdapter.notifyItemRangeChanged(0,chatMessages.size());
            }

        }
        if (isResiverAvailabile)
        {
            binding.textAvalibality.setVisibility(View.VISIBLE);
        }else{
            binding.textAvalibality.setVisibility(View.GONE);
        }


    });
}


    private void listenMessages()
    {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,reciverUser.id)
                .addSnapshotListener(eventListener);

        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,reciverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error)->
    {
if(error!=null)
{
    return;
}
if(value!=null)
{
    int count = chatMessages.size();
    for(DocumentChange documentChange : value.getDocumentChanges()){
        if(documentChange.getType()==DocumentChange.Type.ADDED)
        {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
            chatMessage.receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
            chatMessage.message= documentChange.getDocument().getString(Constants.KEY_MESSAGE);
            chatMessage.dateTime= getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
            chatMessage.dataObject=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
            chatMessages.add(chatMessage);
        }

       }
    Collections.sort(chatMessages,(obj1,obj2) -> obj1.dataObject.compareTo(obj2.dataObject));
    if(count==0)
    {
        chatAdapter.notifyDataSetChanged();
    }else
    {
        chatAdapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
        binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size()-1);
    }
    binding.chatRecyclerView.setVisibility(View.VISIBLE);

    }
binding.progressBar.setVisibility(View.GONE);
if(conversionId==null)
{
    checkForConversion();
}
};


    private Bitmap getBitmapEncodedString(String encodedImage)
    {if(encodedImage!=null)
    {
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }else {
        return null;
    }



    }

    private void loadReceiverDetails()
    {
        reciverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(reciverUser.name);
    }
    private void setListeners()
    {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v->onBackPressed());
        binding.layoutSend.setOnClickListener(v->sendMessage());
    }

private String getReadableDateTime(Date date)
{
    return new SimpleDateFormat("MMMM dd,yyyy - hh:mm a", Locale.getDefault()).format(date);
}
private void addConversion(HashMap<String,Object> conversion )
{
    database.collection(Constants.KEY_COLLECTIONS_CONVERSIONS)
            .add(conversion)
            .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
}

private void updateConversion(String message)
{
    DocumentReference documentReference =
            database.collection(Constants.KEY_COLLECTIONS_CONVERSIONS).document(conversionId);
    documentReference.update(
            Constants.KEY_LAST_MESSAGE,message,
            Constants.KEY_TIMESTAMP,new Date()

    );
}


    private void checkForConversion()
    {
        if(chatMessages.size() !=0 )
        {
            checkForConversionsRemotly(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    reciverUser.id
            );
            checkForConversionsRemotly(
                    reciverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)

            );

        }
    }

private void checkForConversionsRemotly(String senderId ,String reciverId)
{
    database.collection(Constants.KEY_COLLECTIONS_CONVERSIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID,reciverId)
            .get()
            .addOnCompleteListener(conversionOnCompleateListener);
}

private final OnCompleteListener<QuerySnapshot> conversionOnCompleateListener = task ->{
        if(task.isSuccessful() && task.getResult()!=null &&  task.getResult().getDocuments().size() > 0)
        {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailablelityOfResiver();
    }
}