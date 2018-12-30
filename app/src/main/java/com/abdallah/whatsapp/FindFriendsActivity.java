package com.abdallah.whatsapp;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;

    private DatabaseReference Usersref;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        Usersref = FirebaseDatabase.getInstance().getReference().child("Users");


        FindFriendsRecyclerList = (RecyclerView) findViewById(R.id.friend_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));


        mToolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(Usersref, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, FindFriendsViewolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendsViewolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewolder holder, final int position, @NonNull Contacts model) {

                        holder.userStatus.setText(model.getStatus());
                        holder.userName.setText(model.getName());
                        holder.profileImage.setImageResource(R.drawable.profile_image);


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                String visit_user_id = getRef(position).getKey();

                                Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);
                            }
                        });

                    }






                    @NonNull
                    @Override
                    public FindFriendsViewolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View  view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup , false);
                        FindFriendsViewolder viewolder = new FindFriendsViewolder(view);
                        return viewolder;
                    }
                };

        FindFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendsViewolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        CircleImageView profileImage;

        public FindFriendsViewolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_stutas);
            profileImage = itemView.findViewById(R.id.user_profile_image);


        }
    }
}

