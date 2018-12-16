package com.diter.motiondetection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class MainActivity extends AppCompatActivity {
    private TextView txtStatus;
    private MotionDetector motionDetector;

    private FirebaseAuth mAuth;
    String logUri ="";
    String refreshedToken;
    String TAG = "MyTag";
    //String logCatFile;
    FirebaseUser currentUser;

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
            Log.d("MyTag","onReceive starts ");
            String mess;
            mess = intent.getStringExtra("EXTRA_MESSAGE");
            Log.d("MyTag","received broadcast message  " + mess);
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
                     saveLog();
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
        try {
            FirebaseApp.initializeApp(this);
        }
        catch (Exception ex) {
            myLogging("onCreate error  FirebaseApp.initializeApp " + ex.toString());
        }
        myLogging("onCreate after FirebaseApp.initializeApp " );
        try {
            //FirebaseApp.initializeApp(this);
            mAuth = FirebaseAuth.getInstance();
        }
        catch (Exception ex) {
            myLogging("onCreate error mAuth " + ex.toString());
        }
        myLogging("onCreate after mAuth ");

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
        myLogging("onCreate abefore FirebaseInstanceId.getInstance().getToken() ");
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        myLogging("onCreate abefore FirebaseInstanceId.getInstance().getToken() refreshedToken " + refreshedToken);


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
        saveLog();
        /*
        //save previous log to file and send by mail
        Log.d("MyTag", "save previous log to file and send by mail");
        try{
            Log.d("MyTag", "before getExternalStorageDirectory");
            File filename = new File(Environment.getExternalStorageDirectory()+"/mylog.log");
            filename.delete();
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
*/
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
    public void onClickLog(View v) {
        //super.onPause();
        Toast.makeText(getBaseContext(), "onClickLog", Toast.LENGTH_SHORT).show();
        Log.d("MyTag", "onClickLog");
        saveLog();
    }
   public void saveLog()  {

    Log.d("MyTag", "save previous log to file and send by mail");
    try {
        Log.d("MyTag", "before getExternalStorageDirectory");
        File filename = new File(Environment.getExternalStorageDirectory() + "/mylog.log");
        filename.delete();
        Log.d("MyTag", "before createNewFile");
        filename.createNewFile();
        Log.d("MyTag", "before cmd");
        String cmd = "logcat -d -f" + filename.getAbsolutePath();
        Log.d("MyTag", "before exec");
        Runtime.getRuntime().exec(cmd);
        Log.d("MyTag", "before getBaseContext");
        mContext = getBaseContext();
        Log.d("MyTag", "before startService");
        // send log via email
        mContext.startService(new Intent(mContext, SendMailIntentService.class).putExtra("emailTo", motionDetector.getEmailTo()).putExtra("emailFrom", motionDetector.getEmailFrom()).putExtra("file", filename.getAbsolutePath()).putExtra("pwd", motionDetector.getPwd() + "ter#"));
        //save log to storage
        try {
            Log.d(TAG,mAuth.toString());
        }
        catch(Exception ex){Log.d(TAG,"saveLog error mAuth " + ex.toString());
        };


        try {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInAnonymously:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "saveLog signInAnonymously:failure", task.getException());
                                myLogging("saveLog Authentication failed.");
                                updateUI(null);
                            }

                            // ...
                        }
                    });
            myLogging("saveLog after FirebaseStorage.getInstance");
            FirebaseStorage storage = FirebaseStorage.getInstance();
            myLogging("saveLog after FirebaseStorage.getInstance");
            // Create a storage reference from our app
            //Затем, как и в базе данных или Firestore, мы получим ссылку на корневую папку нашего хранилища.
            StorageReference rootRef = storage.getReference();
            myLogging("saveLog after getReference");
            //Тогда, как и в Realtime Database, вы можете пойти ниже по дереву с помощью
            StorageReference myRef = rootRef.child("logs/MD");

            //Дополнительная навигация
            //Как и в случае с базами данных, у вас есть методы  getParent и getRoot для навигации.

            //Загрузка файлов в хранилище
            //До сих пор мы рассматривали ссылки на каталоги. Однако с хранилищем, в отличие от баз данных Firebase,
            // ссылки могут указывать на местоположение файла . Я покажу вам, что имею в виду.
            //StorageReference logRef = myRef.child(logCatFile);
            StorageReference logRef = myRef.child(filename.getAbsolutePath());


            UploadTask uploadTask;

            //Этот метод принимает Uri файла и снова возвращает UploadTask.
            Uri file = Uri.fromFile(filename);
            //StorageReference riversRef = storageRef.child("images/"+file.getLastPathSegment());
            uploadTask = logRef.putFile(file);
            myLogging("after put file");


            logUri = "no url";

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    logUri = "error";
                    myLogging("NOT loaded " + exception.toString());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    logUri = downloadUrl.toString();
                    myLogging("Successful load uri " + downloadUrl.toString());
                }
            });

        }catch (Exception ex) {
            myLogging( "firebase exception " + ex.toString());
        }

        } catch (Exception ex) {
            Log.d("MyTag", "motionDetector error trying to get log " + ex.toString());
        }
        Log.d("MyTag", "after save previous log to file and send by mail");
        myLogging("saveLog ends");

    }

    public void myLogging(String s){
        Log.d(TAG, s);
    }
    private void updateUI(FirebaseUser user) {
        myLogging("updateUI starts");
        /*hideProgressDialog();
        if (user != null) {
            mStatusTextView.setText(getString(R.string.google_status_fmt, user.getEmail()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
        */
        if (user== null)
        {myLogging("user is null");}
        else
        {myLogging(user.toString());}
    }
    @Override
    public void onStart() {
        super.onStart();
        myLogging("on_start starts");
        // Check if user is signed in (non-null) and update UI accordingly.
        try{
            //FirebaseUser currentUser = mAuth.getCurrentUser();}
            FirebaseUser currentUser = mAuth.getCurrentUser();}
        catch (Exception ex) {myLogging("error in getcurrentuser " + ex.toString());}
        myLogging("before updateUI");
        updateUI(currentUser);
        myLogging("on start ends");
    }
}
