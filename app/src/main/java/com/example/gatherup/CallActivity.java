package com.example.gatherup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vidyo.VidyoClient.Connector.Connector;
import com.vidyo.VidyoClient.Connector.ConnectorPkg;

import org.json.JSONException;
import org.json.JSONObject;

public class CallActivity extends AppCompatActivity implements Connector.IConnect {

    private Connector connector;
    private FrameLayout videoFrame;
    private Button startButton, changeButton, disconnectButton;
    private Toolbar toolbar;
    private String roomName;
    private String mToken;

    private View view;

    //Runtime Permissions
    private String[] PERMISSIONS = { android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO };
    private PermissionUtility mPermissions;
    private MyNetworkReceiver mNetworkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        view = getWindow().getDecorView().getRootView();
        initiateUIComponents();

        roomName = getRoomName();

        ConnectorPkg.setApplicationUIContext(this);
        ConnectorPkg.initialize();

        mNetworkReceiver = new MyNetworkReceiver(this);
        mPermissions = new PermissionUtility(this, PERMISSIONS); //Runtime permissions

        //getVidyoToken();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
                startButton.setVisibility(View.GONE);
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
                Disconnect();
            }
        });

        //Runtime Permissions
        if(mPermissions.arePermissionsEnabled()){
            vidyoStart();
            Log.d("TAG", "Permission granted 1");
        } else {
            mPermissions.requestMultiplePermissions();
        }
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
                sendIntent.putExtra(Intent.EXTRA_TEXT, "My Gather Up! Video room name is: " + roomName + "     Also you can click link: https://gatherupvideo.xyz/vidyo.html?room=" + roomName +"");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent,""));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //Runtime permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(mPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            vidyoStart();
            Log.d("TAG", "Permission granted 2");
        }
    }

    //===============================================| onPause(), onResume(), onStop()
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void vidyoStart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                connector = new Connector(videoFrame, Connector.ConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Default, 2, "warning info@VidyoClient info@VidyoConnector", "", 0);
                connector.showViewAt(videoFrame, 0, 0, videoFrame.getWidth(), videoFrame.getHeight());
            }
        }, 3000);
    }

    public void connect() {
        connector = new Connector(videoFrame, Connector.ConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Default, 16, "", "", 0);
        connector.showViewAt(videoFrame,0 ,0,videoFrame.getWidth(),videoFrame.getHeight());
        mToken = "cHJvdmlzaW9uAGZ1bmRhZXNlQGNvbS5leGFtcGxlLmdhdGhlcnVwADYzNzU5ODI3OTU2AAAzOTUzZjUzYzNiZDQ3ZGFiMjY5YjFmMDI1MDIwZjg4MmI4NDBhYzkzZDQ4YTljMmUyNmI4YTRkMjVjNzE5ZmJmYjlkYjc1OGY0ZjYxMjJlZTAxZDI4NWQ1MDg2YjI4NjY=";
        connector.connect("prod.vidyo.io", mToken, ".", "roomName", this);
        showAlertDialogWithAutoDismiss();
    }

    private void Disconnect() {
        connector.disconnect();
        connector.disable();
        Intent intent1 = new Intent(getApplicationContext(),RoomActivity.class);
        startActivity(intent1);
    }


    @Override
    public void onSuccess() {
        //TODO(motionEvent ekle view setlendi)
    }

    @Override
    public void onFailure(Connector.ConnectorFailReason connectorFailReason) {
        Log.i("CallActivity","something went wrong: "+ connectorFailReason.toString());
      //  Toast.makeText(getApplicationContext(),"something went wrong: "+ connectorFailReason.toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected(Connector.ConnectorDisconnectReason connectorDisconnectReason) {
        //TODO("DISCONNECT OLUNCA BURASI CALISIR")
    }

    public void getVidyoToken() {
        String url = "https://us-central1-vidyoio.cloudfunctions.net/getVidyoToken";
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    mToken = jsonObject.getString("token");
                    Log.d("token", mToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("TAG", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage());
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
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

        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);
    }

    private String getRoomName(){
        Bundle extras=getIntent().getExtras();
        String roomName = extras.getString("roomName");
        return roomName;
    }
}