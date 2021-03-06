package com.example.gatherup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.Call;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class RoomActivity extends AppCompatActivity {

    VideoView videoView;
    EditText roomName;
    Button btnLogin;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        videoView = findViewById(R.id.video_view);
        roomName = findViewById(R.id.room_name);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);

        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.video;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
        videoView.start();

        roomName.getText().toString().toLowerCase();

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.start();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomNameControl();
            }
        });

        roomName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    roomNameControl();
                }
                return false;
            }
        });
    }

    private void roomNameControl(){
        if(roomName.length() == 0){
            roomName.setError("Please,enter room name!");
        } else if(roomName.getText().toString().length() < 5){
            roomName.setError("Room name can not be less than 5 letters");
        }else{
            btnLogin.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            sendRoomNameToCallScreen();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            try{
                AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Do you want to really close Gather UP?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);


                    }

                }).setNegativeButton("No", new DialogInterface.OnClickListener() {


                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Let's talk somebody else!", Toast.LENGTH_SHORT).show();
                    }
                });

                alertDialogBuilder.create().show();

                return super.onKeyDown(keyCode, event);
            }
            catch(IllegalStateException e){
                e.printStackTrace();
            }
            return true;

        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    protected void onResume() {
        super.onResume();
        videoView.start();
    }

    public void sendRoomNameToCallScreen()
    {
        Intent intent = new Intent(getApplicationContext(), CallActivity.class);
        intent.putExtra("roomName","" + roomName.getText());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}