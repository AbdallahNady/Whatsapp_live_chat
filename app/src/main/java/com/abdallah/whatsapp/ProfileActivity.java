package com.abdallah.whatsapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String recieverUserID, SenderUserID, current_state;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStaus;
    private Button SendMessageRequestButton, DeclineManagRequestButton;

    private DatabaseReference UserRef, ChatRequestRef, ContactsRef, NotificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");


        recieverUserID = getIntent().getExtras().get("visit_user_id").toString();
        SenderUserID = mAuth.getCurrentUser().getUid();

        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStaus = (TextView) findViewById(R.id.visit_profile_status);
        SendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        DeclineManagRequestButton = (Button) findViewById(R.id.decline_message_request_button);

        current_state = "new";

        RetriveUserInfo();


    }

    private void RetriveUserInfo() {

        UserRef.child(recieverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists())  && (dataSnapshot.hasChild("image")) ){

                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();


                    userProfileName.setText(userName);
                    userProfileStaus.setText(userstatus);

                    MangeChatRequest();
                }
                else{

                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStaus.setText(userstatus);

                    MangeChatRequest();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void MangeChatRequest() {

        ChatRequestRef.child(SenderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(recieverUserID)){
                            String request_type = dataSnapshot.child(recieverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent")){
                               current_state = "request_sent";
                               SendMessageRequestButton.setText("Cancel Chat Request");
                            }
                            else if(request_type.equals("received")) {
                                current_state = "request_received";
                                SendMessageRequestButton.setText("Accept Chat Request");

                                DeclineManagRequestButton.setVisibility(View.VISIBLE);
                                DeclineManagRequestButton.setEnabled(true);

                                DeclineManagRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatRequest();
                                    }
                                });

                            }
                        }
                        else{
                            ContactsRef.child(SenderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(recieverUserID)) {
                                                current_state = "friends";
                                                SendMessageRequestButton.setText("Remove this contact");
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        if (!SenderUserID.equals(recieverUserID)){

            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendMessageRequestButton.setEnabled(false);

                    if (current_state.equals("new")){
                        SendChatRequest();
                    }
                    if(current_state.equals("request_sent")){
                        CancelChatRequest();
                    }
                    if(current_state.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if(current_state.equals("friends")){
                        RemoveSpecificContact();
                    }
                }
            });
        }
        else {
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }



    private void RemoveSpecificContact() {
        ContactsRef.child(SenderUserID).child(recieverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            ContactsRef.child(recieverUserID).child(SenderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                SendMessageRequestButton.setEnabled(true);
                                                current_state = "new";
                                                SendMessageRequestButton.setText("Send Message");

                                                DeclineManagRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineManagRequestButton.setEnabled(false);

                                            }
                                        }
                                    });
                        }
                    }
                });


    }

    private void AcceptChatRequest() {
        ContactsRef.child(SenderUserID).child(recieverUserID)
                .child("Contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ContactsRef.child(recieverUserID).child(SenderUserID)
                                    .child("Contacts").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                 ChatRequestRef.child(SenderUserID).child(recieverUserID)
                                                         .removeValue()
                                                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                             @Override
                                                             public void onComplete(@NonNull Task<Void> task) {
                                                                 if (task.isSuccessful()){
                                                                     ChatRequestRef.child(recieverUserID).child(SenderUserID)
                                                                             .removeValue()
                                                                             .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                 @Override
                                                                                 public void onComplete(@NonNull Task<Void> task) {
                                                                                     SendMessageRequestButton.setEnabled(true);
                                                                                     current_state= "friends";
                                                                                     SendMessageRequestButton.setText("Remove this contact");

                                                                                     DeclineManagRequestButton.setVisibility(View.INVISIBLE);
                                                                                     DeclineManagRequestButton.setEnabled(false);
                                                                                 }
                                                                             });
                                                                 }
                                                             }
                                                         });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void CancelChatRequest() {

        ChatRequestRef.child(SenderUserID).child(recieverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            ChatRequestRef.child(recieverUserID).child(SenderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                SendMessageRequestButton.setEnabled(true);
                                                current_state = "new";
                                                SendMessageRequestButton.setText("Send Message");

                                                DeclineManagRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineManagRequestButton.setEnabled(false);

                                            }
                                        }
                                    });
                        }
                    }
                });
    }



    private void SendChatRequest() {

        ChatRequestRef.child(SenderUserID).child(recieverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ChatRequestRef.child(recieverUserID).child(SenderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", SenderUserID);
                                                chatNotificationMap.put("type", "request");

                                                NotificationRef.child(recieverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){

                                                                    SendMessageRequestButton.setEnabled(true);
                                                                    current_state = "requset_sent";
                                                                    SendMessageRequestButton.setText("Cancel chat request");
                                                                }
                                                            }
                                                        });
                                            }

                                        }
                                    });

                        }
                    }
                });
    }
}
