package com.diter.motiondetection;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


/** Takes a single photo on service start. */
public class SendMailIntentService extends IntentService {

    String LOG_TAG = "myTag";
    String aFile;
    String pwd;
    String emailFrom;
    String emailTo;

    public SendMailIntentService() {
        super("myname");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "SendMailIntentService onHandleIntent starts " );
        aFile = intent.getStringExtra("file");
        pwd = intent.getStringExtra("pwd");
        emailFrom = intent.getStringExtra("emailFrom");
        emailTo = intent.getStringExtra("emailTo");
        Log.d(LOG_TAG, "SendMailIntentService onHandleIntent start file " + aFile.toString() );
        //sendMail(this);
        sendMailWithAttach(this);

        ////////////////////////////////////////
        /*
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
                                Log.w(TAG, "signInAnonymously:failure", task.getException());
                                myLogging("Authentication failed.");
                                updateUI(null);
                            }

                            // ...
                        }
                    });

            FirebaseStorage storage = FirebaseStorage.getInstance();
            myLogging("after FirebaseStorage.getInstance");
            // Create a storage reference from our app
            //Затем, как и в базе данных или Firestore, мы получим ссылку на корневую папку нашего хранилища.
            StorageReference rootRef = storage.getReference();
            //Тогда, как и в Realtime Database, вы можете пойти ниже по дереву с помощью
            StorageReference myRef = rootRef.child("logs/MDC");
            //Дополнительная навигация
            //Как и в случае с базами данных, у вас есть методы  getParent и getRoot для навигации.

            //Загрузка файлов в хранилище
            //До сих пор мы рассматривали ссылки на каталоги. Однако с хранилищем, в отличие от баз данных Firebase,
            // ссылки могут указывать на местоположение файла . Я покажу вам, что имею в виду.
            StorageReference logRef = myRef.child(logCatFile);

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


            FirebaseDatabase database =  FirebaseDatabase.getInstance();
            //FirebaseUser user =  mAuth.getCurrentUser();

            News news = new News("log", "log", logUri,logUri);
            //String newsId = news.getUid();
            DatabaseReference mRef =  database.getReference().child("News").push();
            mRef.setValue(news);
            //////////////////////////////////////////////////////////


        }catch (Exception ex) {
            myLogging( "firebase exception " + ex.toString());
        }
        myLogging( "onButtonLog ends");
*/
        ////////////////////////////////////////


        Log.d(LOG_TAG, "SendMailIntentService onHandleIntent end " );
        stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        showMessage("SendMailIntentService onCreate");
        Log.d(LOG_TAG, "SendMailIntentService on create starts" );

    }

    @SuppressWarnings("deprecation")
    private /*static*/ void sendMail(final Context context) {
                                                   try {


                                                       GMailSender sender = new GMailSender("mail", "pwd");
                                                       Log.d("myTag", "Before sending mail");
                                                       sender.sendMail("This is Subject",
                                                               "This is Body",
                                                               "from mail",
                                                               "to mail");
                                                       Log.d("myTag", "After sending mail");
                                                   } catch (Exception e) {
                                                       Log.d("myTag", e.toString());
                                                   }
    }

    @SuppressWarnings("deprecation")
    private /*static*/ void sendMailWithAttach(final Context context) {

        Log.d(LOG_TAG, "sendMailWithAttach pwd " + pwd);
        Mail m = new Mail(emailFrom, pwd);


        String[] toArr = {emailTo};
        m.setTo(toArr);
        m.setFrom("diterentev@gmail.com");
        m.setSubject(aFile.toString() + "battary level " + getBatteryLevel(getBaseContext()));
        m.setBody("Email body.");

        try {
            m.addAttachment(aFile);

            if(m.send()) {
                Log.d(LOG_TAG, "Email was sent successfully.");
            } else {
                Log.d(LOG_TAG, "Email was not sent.");
            }
        } catch(Exception e) {
            //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "Could not send email", e);
        }
    }


    private static void showMessage(String message) {
        Log.d("myTag", message);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    /**
     * Returns the current device battery level.
     */
    public String getBatteryLevel(Context context) {
        try {
            Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if(batteryIntent != null) {

                int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                // Error checking that probably isn't needed but I added just in case.
                if (level > -1 && scale > 0) {
                    return Float.toString(((float) level / (float) scale) * 100.0f);
                }
            }
        }
        catch(Exception e){

                Log.d(LOG_TAG, "Can't get batter level");

}

        return null;

    }
}