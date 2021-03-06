package com.motty.motz.proyectoandroid.Activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motty.motz.proyectoandroid.CustomAdapters.customArrayAdapterMessages;
import com.motty.motz.proyectoandroid.DB.MessagesDatabase;
import com.motty.motz.proyectoandroid.DB.messagesDB;
import com.motty.motz.proyectoandroid.R;
import com.motty.motz.proyectoandroid.Services.asyncTaskGetMessagesInfo;
import com.motty.motz.proyectoandroid.Services.asyncTaskPostMessageInfo;
import com.motty.motz.proyectoandroid.TemplateClasses.messageTemplateClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class messages extends ListActivity {
    String result;
    List<messageTemplateClass> messageList;
    //ArrayList<messagesDB> chatMessages = new ArrayList<messagesDB>();
    //List<messagesDB> resultMessagesQuery;

    ArrayList<messageTemplateClass> chatMessages = new ArrayList<messageTemplateClass>();

    MessagesDatabase db;
    customArrayAdapterMessages messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        // Initializing db through ActiveAndroid
        //Configuration dbConfiguration = new Configuration.Builder(this).setDatabaseName("motz.db").create();
        //ActiveAndroid.initialize(this);
        //messagesDB.deleteAll();

        db = new MessagesDatabase(this, null);

        Integer idMessage = getIntent().getIntExtra("id", 0);
        String userName = getIntent().getStringExtra("name");


        TextView name = (TextView) findViewById(R.id.userName);
        name.setText(userName);

        asyncTaskGetMessagesInfo myAsyncTask = new asyncTaskGetMessagesInfo();

        try {
            result = myAsyncTask.execute(idMessage.toString(), "3").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            messageList = mapper.readValue(result, mapper.getTypeFactory().constructCollectionType(List.class, messageTemplateClass.class));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(messageList != null){
            //messagesDB.insertIntoDB(messageList);
            for(int i=0; i<messageList.size(); ++i){
                db.addMessage(new messageTemplateClass(messageList.get(i).getFrom(), messageList.get(i).getTo(),messageList.get(i).getText()));
            }
        }

        db.getMessages(chatMessages,idMessage,3);

        messagesAdapter = new customArrayAdapterMessages(this,R.layout.messages_custom_layout,chatMessages);
        setListAdapter(messagesAdapter);

        //resultMessagesQuery = messagesDB.getMessagesFromTo(idMessage, 3);
        //messagesAdapter.addAll(resultMessagesQuery);
        //setListAdapter(messagesAdapter);
    }

    public void sendMessage(View view) {
        EditText messageEditText = (EditText) findViewById(R.id.messageSendText);

        asyncTaskPostMessageInfo myAsyncTaskPost = new asyncTaskPostMessageInfo();
        String data = messageEditText.getText().toString();
        Integer id = getIntent().getIntExtra("id", 0);

        if(!data.trim().isEmpty()){
            try {
                myAsyncTaskPost.execute("3",id.toString(),data).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            //messagesDB.addMessage(data, id, 3);
            db.addMessage(new messageTemplateClass(id, 3, data));


            // Refresh page
            finish();
            startActivity(getIntent());
        }
        else{
            Toast.makeText(messages.this, "Empty Input Text", Toast.LENGTH_SHORT).show();
        }
    }

}
