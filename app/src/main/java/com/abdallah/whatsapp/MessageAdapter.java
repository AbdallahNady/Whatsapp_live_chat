package com.abdallah.whatsapp;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public MessageAdapter (List<Messages> userMessagesList){

        this.userMessagesList = userMessagesList;
    }




    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageaText, receiverMessageText;
        public CircleImageView receiverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageaText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView)  itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView)  itemView.findViewById(R.id.message_profile_image);
        }

    }





    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {

        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        userRef.addValueEventListener(new ValueEventListener() {
                                          @Override
                                          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                              if(dataSnapshot.hasChild("image")){
                                                  String receiverProfileImage = dataSnapshot.child("image").getValue().toString();
                                              }

                                          }

                                          @Override
                                          public void onCancelled(@NonNull DatabaseError databaseError) {

                                          }
                                      });

        if (fromMessageType.equals("text")){
            messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessageaText.setVisibility(View.INVISIBLE);


            if (fromUserID.equals(messageSenderId)){

                messageViewHolder.senderMessageaText.setVisibility(View.VISIBLE);


                messageViewHolder.senderMessageaText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageaText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageaText.setText(messages.getMessage());
            }
            else {

                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


}
