package com.example.gatherup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vidyo.VidyoClient.Connector.Connector;
import com.vidyo.VidyoClient.Connector.ConnectorPkg;

public class CallActivity extends AppCompatActivity implements Connector.IConnect {

    private final String host = "prod.vidyo.io";
    private Connector connector = null;
    private FrameLayout videoFrame;
    private Button startButton, changeButton, disconnectButton;
    private Toolbar toolbar;
    private ImageView image;
    private String roomName;
    private String mToken = "";
    private View view;
    RelativeLayout myLayout;

    private static final int MY_CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        view = getWindow().getDecorView().getRootView();
        initiateUIComponents();


        roomName = getRoomName();

        ConnectorPkg.setApplicationUIContext(this);
        ConnectorPkg.initialize();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO},MY_CAMERA_REQUEST_CODE);


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
                startButton.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                changeButton.setVisibility(View.VISIBLE);
                disconnectButton.setVisibility(View.VISIBLE);
            }
        });

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCam();
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disConnect();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.mShare:

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "My Gather Up! Video room name is: " + roomName + "");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent,""));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        connector = new Connector(videoFrame, Connector.ConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Default, 15, "warning info@VidyoConnector info@VidyoClient", "", 0);
        connector.showViewAt(videoFrame,0 ,0,videoFrame.getWidth(),videoFrame.getHeight());

        if(mToken.isEmpty()){
            mToken = GenerateToken.generateProvisionToken("c7963f54d2d7471ba435cfa562aa0312", "funda" + "@" + "c9a9fa.vidyo.io", "10000", "");
        }

        connector.connect(host, mToken, ".", "" + roomName , this);
        showAlertDialogWithAutoDismiss();
    }

    private void disConnect() {
        connector.disconnect();
        connector.disable();
        Intent intent = new Intent(getApplicationContext(),RoomActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }


    @Override
    public void onSuccess() {
        //TODO(motionEvent ekle view setlendi)

        Log.i("Call: ","Success");
        Log.i("mToken: ",mToken);

        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_CANCEL:
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                changeButton.setVisibility(View.GONE);
                                disconnectButton.setVisibility(View.GONE);
                            }
                        }, 5000);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        changeButton.setVisibility(View.VISIBLE);
                        disconnectButton.setVisibility(View.VISIBLE);
                    default:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onFailure(Connector.ConnectorFailReason connectorFailReason) {
        Log.i("CallActivity","something went wrong: "+ connectorFailReason.toString());
        //  Toast.makeText(getApplicationContext(),"something went wrong: "+ connectorFailReason.toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected(Connector.ConnectorDisconnectReason connectorDisconnectReason) {
        //TODO("DISCONNECT OLUNCA BURASI CALISIR")
        Log.i("Disconnect: ","Disconnect OK!");
        Log.i("CallActivity","Disconnect: "+ connectorDisconnectReason.toString());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }



    private void showAlertDialogWithAutoDismiss() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Gather Up! Video")
                .setMessage("Joining the call, please wait...")
                .setCancelable(false).setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
            }
        }, 3000);
    }

    private void changeCam()
    {
        connector.cycleCamera();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            try{
                if(!startButton.isEnabled())
                {
                    connector.disconnect();
                    connector.disable();
                    startButton.setEnabled(true);
                    disconnectButton.setEnabled(false);
                    changeButton.setEnabled(false);
                }

                Intent intent2 = new Intent(getApplicationContext(),RoomActivity.class);
                startActivity(intent2);
                return super.onKeyDown(keyCode, event);
            }
            catch(IllegalStateException e){
                e.printStackTrace();
            }
            return true;

        }
        return super.onKeyDown(keyCode, event);
    }

    private void initiateUIComponents(){
        videoFrame = findViewById(R.id.videoFrame);
        startButton = findViewById(R.id.btn_start);
        changeButton = findViewById(R.id.btn_change);
        disconnectButton = findViewById(R.id.btn_disconnect);
        toolbar = findViewById(R.id.toolbar);
        myLayout = findViewById(R.id.myLayout);
        image = findViewById(R.id.image);


        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);
    }

    private String getRoomName(){
        Bundle extras=getIntent().getExtras();
        String roomName = extras.getString("roomName");
        return roomName;
    }
}