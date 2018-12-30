package com.abdallah.whatsapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {


    private View privateChatView;
    private RecyclerView chatList;

    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currntUserId;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatView =  inflater.inflate(R.layout.fragment_chats, container, false);


        mAuth = FirebaseAuth.getInstance();
        currntUserId = mAuth.getCurrentUser().getUid();

        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currntUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatList = (RecyclerView) privateChatView.findViewById(R.id.chats_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return privateChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, chatViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, chatViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final chatViewHolder holder, int position, @NonNull Contacts model) {

                        final String userIDs = getRef(position).getKey();
  //                      final String[] retImage = {"default_image"};


                        UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){
                                    if (dataSnapshot.hasChild("image")){
//                                          retImage[0] = dataSnapshot.child("image").getValue().toString();
                                    }

                                    final String retName = dataSnapshot.child("name").getValue().toString();
                                    final String retStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(retName);


                                    if (dataSnapshot.child("userState").hasChild("state")){
                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                        if (state.equals("online")){
                                            holder.userStatus.setText("online");
                                        }
                                        else if (state.equals("offline")){
                                            holder.userStatus.setText("Last seen:  " + date + time);
                                        }

                                    }
                                    else {
                                        holder.userStatus.setText("offline");
                                    }


                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", userIDs);
                                            chatIntent.putExtra("visit_user_name", retName);
//                                            chatIntent.putExtra("user_image", retImage[0]);

                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public chatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        return new chatViewHolder(view);
                    }
                };
        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class  chatViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        ImageView profileImage;

        public chatViewHolder (@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_stutas);
            profileImage = itemView.findViewById(R.id.user_profile_image);
        }
    }
}
