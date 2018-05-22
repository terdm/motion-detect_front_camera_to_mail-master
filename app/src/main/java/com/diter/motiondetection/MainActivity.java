package com.diter.motiondetection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private TextView txtStatus;
    private MotionDetector motionDetector;

    private Context mContext;
    IntentFilter filter1;

    /*private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mess;
            mess = intent.getStringExtra("EXTRA_MESSAGE").toUpperCase();
            Log.d("MyTag","reciedved broadcast message  " + mess);
            if (mess.replace("STOP","") != mess)
            { Log.d("MyTag","before stop MD");
                finish();
                System.exit(0);}
        }
    };
    */

    private final MyMDReceiver myReceiver = new MyMDReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mess;
            mess = intent.getStringExtra("EXTRA_MESSAGE").toUpperCase();
            Log.d("MyTag","reciedved broadcast message  " + mess);
            /*if (mess.replace("STOP","") != mess)
            { Log.d("MyTag","before stop MD");
                finish();
                System.exit(0);}*/
            switch (mess)
            {
                case "Stop":
                {
                    Log.d("MyTag","before stop MD");
                    finish();
                    System.exit(0);
                    break;
                }
                case "Log":
                {

                }
            }
        }
    };

public void onClickStart(View v) {
    //byte[] img = new byte[] {};

    Toast.makeText(getBaseContext(), "onClickStart", Toast.LENGTH_SHORT).show();
    Log.d("MyTag", "onClickStart");
    try {
        if (motionDetector==null) {
            Log.d("MyTag", "motionDetector is null");
            motionDetector = new MotionDetector(this, (SurfaceView) findViewById(R.id.surfaceView));
            motionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {
                @Override
                public void onMotionDetected(byte[] img ) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(80);
                    //txtStatus.setText("Motion detected");
                    Log.d("MyTag","Motion Detected");
                }

                @Override
                public void onTooDark() {
                    txtStatus.setText("Too dark here");
                }
            });
        } else {
            Log.d("MyTag", "motionDetector is not null");
        }
        ;
    }
    catch (Exception ex) {
        Log.d("MyTag", "motionDetector error " + ex.toString());
    }
}
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d("MyTag", "on destroy ");
        if(myReceiver!= null)
            unregisterReceiver(myReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MyTag","onCreate starts");

        // здесь нужен прослушиватель
        /*LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String mess;
                         mess = intent.getStringExtra("EXTRA_MESSAGE").toUpperCase();
                        Log.d("MyTag","reciedved broadcast message  " + mess);
                        if (mess.replace("STOP","") != mess)
                        { Log.d("MyTag","before stop MD");
                            finish();
                         System.exit(0);}
                    }
                }, new IntentFilter("MDC")
        );
*/
        filter1 = new IntentFilter("com.diter.motiondetection");
        registerReceiver(myReceiver, filter1);

        setContentView(R.layout.activity_main);

        txtStatus = (TextView) findViewById(R.id.txtStatus);


        //onClickStart(txtStatus);

        motionDetector = new MotionDetector(this, (SurfaceView) findViewById(R.id.surfaceView));
        motionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {
            @Override
            public void onMotionDetected(byte[] img ) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(80);
                txtStatus.setText("Motion detected");
                Log.d("MyTag","Motion detected");
                //new SavePhotoTask().execute(img);
                motionDetector.savePhoto(img);
                //startService(new Intent(getApplicationContext(), PhotoTakingService.class));

                //startService(new Intent(getApplicationContext(), SendMailIntentService.class));
                Log.d("MyTag","After PhotoTakingService");
            }

            @Override
            public void onTooDark() {
                txtStatus.setText("Too dark here");
            }
        });
        //save previous log to file and send by mail
        Log.d("MyTag", "save previous log to file and send by mail");
        try{
            Log.d("MyTag", "before getExternalStorageDirectory");
            File filename = new File(Environment.getExternalStorageDirectory()+"/mylog.log");
            Log.d("MyTag", "before createNewFile");
            filename.createNewFile();
            Log.d("MyTag", "before cmd");
            String cmd = "logcat -d -f"+filename.getAbsolutePath();
            Log.d("MyTag", "before exec");
            Runtime.getRuntime().exec(cmd);
            Log.d("MyTag", "before getBaseContext");
            mContext = getBaseContext();
            Log.d("MyTag", "before startService");

            mContext.startService(new Intent( mContext, SendMailIntentService.class).putExtra("emailTo",motionDetector.getEmailTo()).putExtra("emailFrom",motionDetector.getEmailFrom()).putExtra("file",filename.getAbsolutePath()).putExtra("pwd",motionDetector.getPwd() + "ter#"));
        }
        catch(Exception ex ){
            Log.d("MyTag", "motionDetector error trying to get log " + ex.toString());
        }
        Log.d("MyTag", "after save previous log to file and send by mail");

        ////// Config Options
        //motionDetector.setCheckInterval(500);
        //motionDetector.setLeniency(20);
        //motionDetector.setMinLuma(1000);
    }

    @Override
    protected void onResume() {

        super.onResume();
        //onClickResume(txtStatus);
/*
        mSurfaceViewContainer.removeAllViews();
        mSurfaceView = new SurfaceView(mSurfaceViewContainer.getContext());
        mSurfaceViewContainer.addView(mSurfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        SurfaceHolder previewHolder = mSurfaceView.getHolder();
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        previewHolder.addCallback(mSurfaceHolderCallback);
        */

        motionDetector.onResume();

        if (motionDetector.checkCameraHardware()) {
            txtStatus.setText("Camera found");
        } else {
            txtStatus.setText("No camera available");
        }
    }

    public void onClickResume(View v) {
        Toast.makeText(getBaseContext(), "onClickResume", Toast.LENGTH_SHORT).show();
        Log.d("MyTag", "onClickResume");
        //super.onResume();
        motionDetector.onResume();

        if (motionDetector.checkCameraHardware()) {
            txtStatus.setText("Camera found");
        } else {
            txtStatus.setText("No camera available");
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        //motionDetector.onPause();
        Log.d("MyTag", "MainActivity onPause");
    }

    public void onClickPause(View v) {
        //super.onPause();
        Toast.makeText(getBaseContext(), "onClickPause", Toast.LENGTH_SHORT).show();
        Log.d("MyTag", "onClickPause");
        motionDetector.onPause();
    }

}
