package com.abdallah.whatsapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageBut;
    private EditText userMessadeInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, groupNameRef, GroupMessageKeyRef;


    private String currentGroupNmae, currentUSERid, currentUserName, currentTime, currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        currentGroupNmae = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupNmae, Toast.LENGTH_SHORT).show();



        mAuth = FirebaseAuth.getInstance();
        currentUSERid = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupNmae);





        InitilizeFeildea();
        
        GetUserInfo();


        sendMessageBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saaveMessageInfoToDtatbase();

                userMessadeInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                DisplayMessages(dataSnapshot);
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



    private void InitilizeFeildea() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupNmae);

        sendMessageBut = (ImageButton) findViewById(R.id.send_message_button);
        userMessadeInput = (EditText) findViewById(R.id.inpuut_group_message);
        displayTextMessages = (TextView) findViewById(R.id.group_chat_text_display);

        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);

    }


    private void GetUserInfo() {
        UserRef.child(currentUSERid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void saaveMessageInfoToDtatbase() {

        String message = userMessadeInput.getText().toString();
        String messageKey = groupNameRef.push().getKey();

        if (TextUtils.isEmpty(message)){
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormate = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormate.format(calForDate.getTime());


            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormate = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormate.format(calForTime.getTime());


            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = groupNameRef.child(messageKey);


            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap);

        }
    }



    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator  iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();


            displayTextMessages.append(chatName+" :\n" + chatMessage + "\n" + chatTime + "      " + chatDate + "\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
