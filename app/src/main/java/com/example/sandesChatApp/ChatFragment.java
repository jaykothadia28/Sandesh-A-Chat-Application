package com.example.sandesChatApp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

public class ChatFragment extends Fragment {

    FirebaseFirestore firebaseFirestore;
    LinearLayoutManager linearLayoutManager;
    FirebaseAuth firebaseAuth;
    ImageView mUserImageView;
    FirestoreRecyclerAdapter<FirebaseModel, NotViewHolder> chatAdapter;
    RecyclerView recyclerView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chatfragment, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerView);

        Query query = firebaseFirestore.collection("Users").whereNotEqualTo("uid", firebaseAuth.getUid());

        FirestoreRecyclerOptions<FirebaseModel> allUsers =
                new FirestoreRecyclerOptions.Builder<FirebaseModel>().setQuery(query, FirebaseModel.class).build();

        chatAdapter = new FirestoreRecyclerAdapter<FirebaseModel, NotViewHolder>(allUsers) {
            @Override
            protected void onBindViewHolder(@NonNull NotViewHolder notViewHolder, int i, @NonNull FirebaseModel firebaseModel) {

                notViewHolder.uniqueUserName.setText(firebaseModel.getName());
                String uri = firebaseModel.getImage();

                Picasso.get().load(uri).into(mUserImageView);
                if(firebaseModel.getStatus().equals("Online"))
                {
                    notViewHolder.userStatus.setText(firebaseModel.getStatus());
                    notViewHolder.userStatus.setTextColor(Color.GREEN);
                }
                else
                {
                    notViewHolder.userStatus.setText(firebaseModel.getStatus());
                }

                notViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(getActivity(),"Item clicked !!!",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), MessageActivity.class);
                        intent.putExtra("Name", firebaseModel.getName());
                        intent.putExtra("Receiver uid", firebaseModel.getUid());
                        intent.putExtra("Image uri", firebaseModel.getImage());
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public NotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_view_layout, parent, false);
                return new NotViewHolder(v);
            }
        };
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(chatAdapter);

        return view;
    }

    public class NotViewHolder extends RecyclerView.ViewHolder{

        private TextView uniqueUserName;
        private TextView userStatus;

        public NotViewHolder(@NonNull View itemView) {
            super(itemView);

            uniqueUserName = itemView.findViewById(R.id.chatName);
            userStatus = itemView.findViewById(R.id.userStatus);
            mUserImageView = itemView.findViewById(R.id.userImageView);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        chatAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(chatAdapter!=null)
        {
            chatAdapter.stopListening();
        }
    }
}
