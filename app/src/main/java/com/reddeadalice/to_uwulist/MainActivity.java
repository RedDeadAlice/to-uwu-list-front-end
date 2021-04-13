package com.reddeadalice.to_uwulist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    class Note{
        public Note(int id,String note){
            this.id=id;
            this.note=note;
        }
        public int getId(){
            return id;
        }
        public String getNote(){
            return note;
        }
        private String note;
        private int id;
    }
    private LinearLayout notesLayout;
    public static final String HOST="192.168.1.108";
    public static final String PORT="8080";
    private Note[] notes;
    private SwipeRefreshLayout refreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestHandler.getInstance().init(getApplicationContext());
        notesLayout=(LinearLayout)findViewById(R.id.notes_layout);
        refreshLayout=(SwipeRefreshLayout)findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNotes();
            }
        });
        refreshNotes();
    }
    private void refreshNotes(){
        JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, "http://" + HOST + ":" + PORT + "/notes", null, (Response.Listener<JSONArray>) response -> {
            notesLayout.removeAllViews();
            int length=response.length();
            notes=new Note[length];
            try{
                for(int i=0;i<length;i++){
                    notes[i]=new Note(response.getJSONObject(i).getInt("id"),response.getJSONObject(i).getString("note"));
                }
            }catch (JSONException exception){
                Toast.makeText(getApplicationContext(),"Failed to refresh notes",Toast.LENGTH_SHORT).show();
            }
            if(notes!=null){
                for (Note note : notes) {
                    View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.note, null);
                    TextView textView = (TextView) v.findViewById(R.id.note_text);
                    textView.setText(note.note);
                    Button button=(Button)v.findViewById(R.id.note_delete);
                    button.setOnClickListener(v1 -> deleteNote(note.id));
                    notesLayout.addView(v);
                }
            }
            refreshLayout.setRefreshing(false);
        }, (Response.ErrorListener) error -> {
            Toast.makeText(getApplicationContext(),"Failed to get response from server",Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
        });
        RequestHandler.getInstance().addRequest(request);
    }
    private void deleteNote(int id){
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("id",id);
        } catch (JSONException exception) {
            Toast.makeText(MainActivity.this,"Failed to delete note,json related err",Toast.LENGTH_SHORT).show();
            return;
        }
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST,"http://" + HOST + ":" + PORT + "/delete",jsonObject,response -> {
            refreshNotes();
        },error -> {
            Toast.makeText(MainActivity.this,"Failed to delete note",Toast.LENGTH_SHORT).show();
        });
        RequestHandler.getInstance().addRequest(request);
    }
    public void showAddingFragment(View view){
        Intent intent= new Intent(this,InsertingActivity.class);
        startActivity(intent);
    }
}