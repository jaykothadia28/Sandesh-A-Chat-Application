package com.example.sandesChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MessageActivity extends AppCompatActivity {

    EditText mGetMessage;
    ImageButton mSendMessageButton;

    CardView mCardViewOfMessenger;
    androidx.appcompat.widget.Toolbar mMessageActivityToolbar;
    ImageView mImageViewOfMessenger;
    TextView mMessengerName;

    private String enteredMessage;
    Intent intent;
    String mReceiverName,senderName,mReceiverUid,mSenderUid;
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    String SenderRoom,receiverRoom;

    ImageButton mBackButtonMessageActivity;

    RecyclerView mRecyclerViewOfMessenger;
    FirebaseFirestore firebaseFirestore;

    String currentTime;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;

    MessagesAdapter messagesAdapter;
    ArrayList<Messages> messagesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_activty);

        mGetMessage=findViewById(R.id.GetMessage);
        mCardViewOfMessenger=findViewById(R.id.SendMessageCardView);
        mSendMessageButton = findViewById(R.id.sendButtonImageView);
        mMessageActivityToolbar = findViewById(R.id.messageActivityToolbar);
        mMessengerName = findViewById(R.id.MessengerName);
        mImageViewOfMessenger = findViewById(R.id.ImageViewOfMessenger);
        mBackButtonMessageActivity = findViewById(R.id.backButtonMessageActivity);

        intent=getIntent();

        setSupportActionBar(mMessageActivityToolbar);


        messagesArrayList = new ArrayList<>();
        mRecyclerViewOfMessenger = findViewById(R.id.recyclerViewOfMessenger);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);


        mRecyclerViewOfMessenger.setLayoutManager(linearLayoutManager);
        messagesAdapter=new MessagesAdapter(MessageActivity.this,messagesArrayList);
        mRecyclerViewOfMessenger.setAdapter(messagesAdapter);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        calendar=Calendar.getInstance();
        simpleDateFormat=new SimpleDateFormat("hh:mm a");


        mSenderUid=firebaseAuth.getUid();
        mReceiverUid=getIntent().getStringExtra("Receiver uid");
        mReceiverName=getIntent().getStringExtra("Name");

        SenderRoom=mSenderUid+mReceiverUid;
        receiverRoom=mReceiverUid+mSenderUid;

        DatabaseReference databaseReference=firebaseDatabase.getReference().child("chats").child(SenderRoom).child("messages");
        messagesAdapter=new MessagesAdapter(MessageActivity.this,messagesArrayList);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren())
                {
                    Messages messages=snapshot1.getValue(Messages.class);
                    messagesArrayList.add(messages);
                }
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mBackButtonMessageActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mMessengerName.setText(mReceiverName);
        String uri=intent.getStringExtra("Image uri");
        if(uri.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"null is received",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Picasso.get().load(uri).into(mImageViewOfMessenger);
        }

        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                enteredMessage=mGetMessage.getText().toString();
                if(enteredMessage.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Enter message first",Toast.LENGTH_SHORT).show();
                }

                else
                {
                    Date date=new Date();
                    currentTime = simpleDateFormat.format(calendar.getTime());
                    Messages messages=new Messages(enteredMessage,firebaseAuth.getUid(),currentTime,date.getTime());
                    firebaseDatabase=FirebaseDatabase.getInstance();
                    firebaseDatabase.getReference().child("chats")
                            .child(SenderRoom)
                            .child("messages")
                            .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            firebaseDatabase.getReference()
                                    .child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .push()
                                    .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }
                    });

                    mGetMessage.setText(null);
                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        messagesAdapter.notifyDataSetChanged();
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("status","Online");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(messagesAdapter!=null)
        {
            messagesAdapter.notifyDataSetChanged();
        }
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("status","Offline");
    }
}