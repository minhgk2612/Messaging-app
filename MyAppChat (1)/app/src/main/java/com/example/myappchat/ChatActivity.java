package com.example.myappchat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private String receiverId, receiverName, senderRoom, receivedRoom;
    private DatabaseReference dbReferenceSender, dbReferenceReceiver, userReference;
    private ImageView sendBtn;
    private EditText messageText;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userReference = FirebaseDatabase.getInstance().getReference("user");
        receiverId = getIntent().getStringExtra("id");
        receiverName = getIntent().getStringExtra("name");

        if (TextUtils.isEmpty(receiverId) || TextUtils.isEmpty(receiverName)) {
            Log.e("ChatActivity", "Recipient information missing");
            Toast.makeText(this, "Recipient information missing", Toast.LENGTH_SHORT).show();
            finish();
        }
        getSupportActionBar().setTitle(receiverName);

        senderRoom = FirebaseAuth.getInstance().getUid() + receiverId;
        receivedRoom = receiverId + FirebaseAuth.getInstance().getUid();

        sendBtn = findViewById(R.id.sendMassageIcon);
        messageAdapter = new MessageAdapter(this);
        recyclerView = findViewById(R.id.chatrecycler);
        messageText = findViewById(R.id.messageEdit);

        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbReferenceSender = FirebaseDatabase.getInstance().getReference("chats").child(senderRoom);
        dbReferenceReceiver = FirebaseDatabase.getInstance().getReference("chats").child(receivedRoom);

        // Listen for new messages in the receiver's room
        dbReferenceReceiver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MessageModel> messages = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    messages.add(messageModel);
                }
                messageAdapter.clear();
                messageAdapter.addAll(messages);
                messageAdapter.notifyDataSetChanged();
                // Scroll to the latest message
                recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatActivity", "Database error: " + error.getMessage());
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageText.getText().toString().trim();
                if (!TextUtils.isEmpty(message)) {
                    SendMessage(message);
                } else {
                    Toast.makeText(ChatActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendMessage(String message) {
        try {
            String messageId = UUID.randomUUID().toString();
            MessageModel messageModel = new MessageModel(messageId, FirebaseAuth.getInstance().getUid(), message);

            // Update sender's UI
            messageAdapter.add(messageModel);
            messageAdapter.notifyDataSetChanged();

            // Save message in both sender's and receiver's rooms
            saveMessageInDatabase(dbReferenceSender, messageId, messageModel);
            saveMessageInDatabase(dbReferenceReceiver, messageId, messageModel);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Scroll to the latest message
                    recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
                    messageText.getText().clear();
                }
            });

        } catch (Exception e) {
            handleSendMessageFailure("Error sending message", e);
        }
    }

    private void saveMessageInDatabase(DatabaseReference reference, String messageId, MessageModel messageModel) {
        reference.child(messageId).setValue(messageModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Optional: Handle success if needed
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleSendMessageFailure("Failed to send message", e);
                    }
                });
    }

    private void handleSendMessageFailure(String message, Exception e) {
        Log.e("SendMessage", message, e);
        Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();
        e.printStackTrace();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ChatActivity.this, SigninActivity.class));
            finish();
            return true;
        }
        return false;
    }
}
