package com.abdallah.whatsapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId, messageReceiverName, messageReceiverImage, messageSenderID;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ImageButton sendMessageButton;
    private EditText messageInputText;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessageslist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();

        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();


        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
//        messageReceiverImage = getIntent().getExtras().get("visit_Image").toString();



        InitilizeCotrollers();

        userName.setText(messageReceiverName);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void InitilizeCotrollers() {

        ChatToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_Last_seen);
        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        sendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        messageInputText = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessageslist = (RecyclerView) findViewById(R.id.private_message_list_of_user);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageslist.setLayoutManager(linearLayoutManager);
        userMessageslist.setAdapter(messageAdapter);
    }


    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageSenderID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child("userState").hasChild("state")){
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online")){
                                userLastSeen.setText("online");
                            }
                            else if (state.equals("offline")){
                                userLastSeen.setText("Last seen:  " + date + time);
                            }

                        }
                        else {
                            userLastSeen.setText("offline");
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessageslist.smoothScrollToPosition(userMessageslist.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }





    private void sendMessage(){
        String messageText = messageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText)){

            Toast.makeText(this, "Please write your message...", Toast.LENGTH_SHORT).show();
        }
        else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverId;
            String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef =RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverId).push();

            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);

            Map messageBodyDatails = new HashMap();
            messageBodyDatails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
            messageBodyDatails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

            RootRef.updateChildren(messageBodyDatails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message sent successful", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "erorr", Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });

        }
    }
}
