package example.myapplication;

//General imports
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

//Drone imports
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

//Eneter imports
import eneter.messaging.diagnostic.EneterTrace;
import eneter.messaging.endpoints.typedmessages.*;
import eneter.messaging.messagingsystems.messagingsystembase.*;
import eneter.messaging.messagingsystems.tcpmessagingsystem.TcpMessagingSystemFactory;
import eneter.net.system.EventHandler;
import io.github.crazygoatstudio.mambodroid.R;

import static example.myapplication.UtilsKt.rangeAngle;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView speechtext;
    private ImageButton speechbtn;
    private ImageButton emgbtn;
    private Button North, South, East, West, Stop, Up, Down, TakeOff, Land;
    private int leapPitch, leapRoll, leapYawL;


    protected static final int RESULT_SPEECH = 1;
    private String currentcommand;
    private MiniDrone mMiniDrone;
    private ProgressDialog mConnectionProgressDialog;
    private ProgressDialog mDownloadProgressDialog;

    private TextView mBatteryLabel;


    //<editor-fold desc="Eneter variables and stuff">
    // Request message type
    // The message must have the same name as declared in the service.
    // Also, if the message is the inner class, then it must be static.
    public static class MyRequest
    {
        public String Text;
    }

    // Response message type
    // The message must have the same name as declared in the service.
    // Also, if the message is the inner class, then it must be static.
    public static class MyResponse
    {

        public float Yaw;

        public float Pitch;

        public float Roll;

        public float YawL;
    }

    // UI controls
    private Handler myRefresh = new Handler();
    private Button mySendRequestBtn;
    private TextView ctv_Pitch;
    private TextView ctv_Roll;
    private TextView ctv_Yaw;
    private EditText leapIP;


    // Sender sending MyRequest and as a response receiving MyResponse.
    private IDuplexTypedMessageSender<MyResponse, MyRequest> mySender;
    //</editor-fold>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImportLayout();

        ProgramEvents ();


        Intent intent = getIntent();
        ARDiscoveryDeviceService service = intent.getParcelableExtra(DeviceListActivity.EXTRA_DEVICE_SERVICE);
        mMiniDrone = new MiniDrone(this, service);
        mMiniDrone.addListener(mMiniDroneListener);

        //<editor-fold desc="Eneter Oncreate">
        // Get UI widgets.
        ctv_Pitch = (TextView) findViewById(R.id.tv_Pitch);
        ctv_Roll = (TextView) findViewById(R.id.tv_Roll);
        ctv_Yaw = (TextView) findViewById(R.id.tv_Yaw);



        mySendRequestBtn = (Button) findViewById(R.id.sendRequestBtn);

        // Subscribe to handle the button click.
        mySendRequestBtn.setOnClickListener(myOnSendRequestClickHandler);



        // Open the connection in another thread.
        // Note: From Android 3.1 (Honeycomb) or higher
        //       it is not possible to open TCP connection
        //       from the main thread.

        //</editor-fold>
    }
    private void ImportLayout(){
        speechbtn = (ImageButton) findViewById(R.id.spkbtn);
        emgbtn = (ImageButton) findViewById(R.id.emg);
        mBatteryLabel = (TextView) findViewById(R.id.battery);

        North = (Button) findViewById(R.id.bNorth);
        South = (Button) findViewById(R.id.bSouth);
        East = (Button) findViewById(R.id.bEast);
        West = (Button) findViewById(R.id.bWest);
        Stop = (Button) findViewById(R.id.bStop);
        Up = (Button) findViewById(R.id.bUp);
        Down = (Button) findViewById(R.id.bDown);
        TakeOff = (Button) findViewById(R.id.bTakeOff);
        Land = (Button) findViewById(R.id.bLand);
        leapIP = (EditText)findViewById(R.id.et_leapIP);


    }
    private void ProgramEvents ()
    {
        //<editor-fold desc="Drone crap listeners">
        // Ass long speechbutton event listener
        speechbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                try {
                    startActivityForResult(intent, RESULT_SPEECH);

                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Oops! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });
        // emergency  button, pretty self explanatory.
        emgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mMiniDrone.getFlyingState()) {
                    case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                    case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                        mMiniDrone.land();
                        break;
                    default:
                }
            }
        });
        North.setOnClickListener
                (new View.OnClickListener() {
                     public void onClick(View arg0) {
                             mMiniDrone.setFlag((byte) 1);
                             mMiniDrone.setPitch((byte) 20);
                     }
                 }
                );
        South.setOnClickListener
                (new View.OnClickListener() {
                     public void onClick(View arg0) {
                         mMiniDrone.setFlag((byte) 1);
                         mMiniDrone.setPitch((byte) -20);
                     }
                 }
                );
        East.setOnClickListener
                (new View.OnClickListener() {
                     public void onClick(View arg0) {
                         mMiniDrone.setFlag((byte) 1);
                         mMiniDrone.setRoll((byte) 20);
                     }
                 }
                );
        West.setOnClickListener
                (new View.OnClickListener() {
                     public void onClick(View arg0) {
                         mMiniDrone.setFlag((byte) 1);
                         mMiniDrone.setRoll((byte) -20);
                     }
                 }
                );

        Up.setOnClickListener
                (new View.OnClickListener() {
                     public void onClick(View arg0) {
                         mMiniDrone.setGaz((byte) 10);
                     }
                 }
                );
        Down.setOnClickListener
                (new View.OnClickListener() {
                     public void onClick(View arg0) {
                         mMiniDrone.setGaz((byte) -10);
                     }
                 }
                );
        TakeOff.setOnClickListener
                (new View.OnClickListener() {
                     public void onClick(View arg0) {
                         switch (mMiniDrone.getFlyingState()) {
                             case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                                 mMiniDrone.takeOff();
                                 break;
                             case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                             case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING: {
                                 mMiniDrone.land();
                                 break;
                             }
                             default:
                         }
                     }
                 }
                );
        Land.setOnClickListener
                (new View.OnClickListener() {
                     public void onClick(View arg0) {
                         switch (mMiniDrone.getFlyingState()) {
                             case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                                 mMiniDrone.takeOff();
                                 break;
                             case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                             case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING: {
                                 mMiniDrone.land();
                                 break;
                             }
                             default:
                         }
                     }
                 }
                );
        Stop.setOnClickListener
                (new View.OnClickListener() {
                     public void onClick(View arg0) {
                         mMiniDrone.setYaw((byte) 0);
                         mMiniDrone.setGaz((byte) 0);
                         mMiniDrone.setPitch((byte) 0);
                         mMiniDrone.setFlag((byte)0);
                     }
                 }
                );



        // end emergency button
        //</editor-fold>

    }
    @Override
    protected void onStart() {
        super.onStart();

        // show a loading view while the minidrone is connecting
        if ((mMiniDrone != null) && !(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mMiniDrone.getConnectionState())))
        {
            mConnectionProgressDialog = new ProgressDialog(this, R.style.AlertDialog_AppCompat);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Connecting ...");
            Toast.makeText(getApplicationContext(), "Connecting..", Toast.LENGTH_SHORT);
            mConnectionProgressDialog.setCancelable(false);
            mConnectionProgressDialog.show();

            // if the connection to the MiniDrone fails, finish the activity
            if (!mMiniDrone.connect()) {
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mMiniDrone != null) {
            mConnectionProgressDialog = new ProgressDialog(this, R.style.AlertDialog_AppCompat);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Disconnecting ...");
            mConnectionProgressDialog.setCancelable(false);
            mConnectionProgressDialog.show();

            if (!mMiniDrone.disconnect()) {
                finish();
            }
        } else {
            finish();
        }
    }
    private final MiniDrone.Listener mMiniDroneListener = new MiniDrone.Listener() {
        @Override
        public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
            switch (state) {
                case ARCONTROLLER_DEVICE_STATE_RUNNING:
                    mConnectionProgressDialog.dismiss();
                    break;

                case ARCONTROLLER_DEVICE_STATE_STOPPED:
                    // if the deviceController is stopped, go back to the previous activity
                    mConnectionProgressDialog.dismiss();
                    finish();
                    break;

                default:
                    break;
            }
        }

        public void onBatteryChargeChanged(int batteryPercentage) {
            mBatteryLabel.setText(String.format("%d%%", batteryPercentage));
        }

        @Override
        public void onPilotingStateChanged(ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state) {
            switch (state) {
                case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                    //mTakeOffLandBt.setEnabled(true);
                    //mDownloadBt.setEnabled(true);
                    break;
                case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                    //mTakeOffLandBt.setEnabled(true);
                    //mDownloadBt.setEnabled(false);
                    break;
                default:
                    //mTakeOffLandBt.setEnabled(false);
                    //mDownloadBt.setEnabled(false);
            }
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);


                    currentcommand = text.get(0);
                    if(currentcommand.toLowerCase().trim().equals("turn right")) {
                        mMiniDrone.setYaw((byte) 25);
                    }
                    if(currentcommand.toLowerCase().trim().equals("turn left")){
                        mMiniDrone.setYaw((byte) -25);
                    }
                    if(currentcommand.toLowerCase().trim().equals("stop turning")){
                        mMiniDrone.setYaw((byte) 0);
                    }
                    if(currentcommand.toLowerCase().trim().equals("forward")){
                        mMiniDrone.setFlag((byte) 1);
                        mMiniDrone.setPitch((byte) 25);
                    }
                    if(currentcommand.toLowerCase().trim().equals("backward")){
                        mMiniDrone.setFlag((byte) 1);
                        mMiniDrone.setPitch((byte) -25);
                    }
                    if(currentcommand.toLowerCase().trim().equals("stop moving")){
                        mMiniDrone.setFlag((byte) 0);
                        mMiniDrone.setPitch((byte) 0);
                    }
                    if(currentcommand.toLowerCase().trim().equals("up")){
                        mMiniDrone.setGaz((byte) 25);
                    }
                    if(currentcommand.toLowerCase().trim().equals("down")){
                        mMiniDrone.setGaz((byte) -25);
                    }
                    if(currentcommand.toLowerCase().trim().equals("stay")){
                        mMiniDrone.setGaz((byte) 0);
                    }
                    if(currentcommand.toLowerCase().trim().equals("stop")){
                        mMiniDrone.setYaw((byte) 0);
                        mMiniDrone.setGaz((byte) 0);
                        mMiniDrone.setPitch((byte) 0);
                        mMiniDrone.setFlag((byte)0);
                    }
                    if(currentcommand.toLowerCase().trim().equals("emergency")){
                        mMiniDrone.emergency();
                    }
                    if(currentcommand.toLowerCase().trim().equals("take off") ||
                            currentcommand.toLowerCase().trim().equals("land")){
                        switch (mMiniDrone.getFlyingState()) {
                            case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                                mMiniDrone.takeOff();
                                break;
                            case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                            case ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING: {
                                mMiniDrone.land();
                                /*mMiniDrone.setYaw((byte) 0);
                                mMiniDrone.setGaz((byte) 0);
                                mMiniDrone.setPitch((byte) 0);
                                mMiniDrone.setFlag((byte) 0);*/
                                break;
                            }
                            default:
                        }
                    }
                }
                break;
            }

        }
    }
    private void startThread() {
        Thread anOpenConnectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    openConnection();
                } catch (Exception err) {
                    EneterTrace.error("Open connection failed.", err);
                }
            }
        });
        anOpenConnectionThread.start();
    }


    private void openConnection() throws Exception
    {
        String ip = leapIP.getText().toString();
        if (ip.isEmpty() || ip.equals(""))
        {
            ip =  "172.17.2.89";
        }
        else if (!ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))
        {
            return;
        }



        // Create sender sending MyRequest and as a response receiving MyResponse
        IDuplexTypedMessagesFactory aSenderFactory =
                new DuplexTypedMessagesFactory();
        mySender = aSenderFactory.createDuplexTypedMessageSender(MyResponse.class, MyRequest.class);

        // Subscribe to receive response messages.
        mySender.responseReceived().subscribe(myOnResponseHandler);


        // Create TCP messaging for the communication.
        // Note: 10.0.2.2 is a special alias to the loopback (127.0.0.1)
        //       on the development machine
        IMessagingSystemFactory aMessaging = new TcpMessagingSystemFactory();
        IDuplexOutputChannel anOutputChannel =
//                aMessaging.createDuplexOutputChannel("tcp://0.tcp.ngrok.io:13150");
                aMessaging.createDuplexOutputChannel("tcp://"+ip+":8060/");
        Log.v(TAG," Trying to connect to: tcp://"+ip+":8060/");

        // Attach the output channel to the sender and be able to send
        // messages and receive responses.
        mySender.attachDuplexOutputChannel(anOutputChannel);
    }

    private void onSendRequest(View v)
    {
        // Create the request message.
        MyRequest aRequestMsg = new MyRequest();
//        aRequestMsg.Text = myMessageTextEditText.getText().toString();
        aRequestMsg.Text ="hola";

        // Send the request message.
        for(int i=0; i<10; i++) {
            try {
                mySender.sendRequestMessage(aRequestMsg);
                Log.v(TAG, "Succesfull sendRequest");
                break;
            } catch (Exception err) {
                EneterTrace.error("Sending the message failed.", err);
            }
        }

    }
    private void onResponseReceived(Object sender, final
    TypedResponseReceivedEventArgs<MyResponse> e)
    {
        // Display the result - returned number of characters.
        // Note: Marshal displaying to the correct UI thread.
        myRefresh.post(new Runnable()
        {
            @Override
            public void run()
            {


                //<editor-fold desc="Pitch">
                leapPitch = rangeAngle(e.getResponseMessage().Pitch);
                ctv_Pitch.setText(Integer.toString(leapPitch));
                //</editor-fold>
                //<editor-fold desc="Roll">
                leapRoll = rangeAngle(e.getResponseMessage().Roll);
                ctv_Roll.setText(Integer.toString(leapRoll));
                //</editor-fold>
                // <editor-fold desc="YawL">
                leapYawL = rangeAngle(e.getResponseMessage().YawL);
                ctv_Yaw.setText(Integer.toString(leapYawL));
                //</editor-fold>
                if (leapPitch == 0 && leapRoll == 0)
                {
                    mMiniDrone.setYaw((byte) 0);
                    mMiniDrone.setGaz((byte) 0);
                    mMiniDrone.setPitch((byte) 0);
                    mMiniDrone.setFlag((byte)0);
                }
                else{
                    mMiniDrone.setFlag((byte) 1);
                    mMiniDrone.setPitch((byte) leapPitch);
                    mMiniDrone.setRoll((byte) leapRoll);
                }
                if (leapYawL == 0)
                {
                    mMiniDrone.setGaz((byte)leapYawL);
                }
                else
                {
                    mMiniDrone.setGaz((byte) 0);
                }

//                ctv_Pitch.setText(Float.toString(e.getResponseMessage().Pitch));
//                ctv_Roll.setText(Float.toString(e.getResponseMessage().Roll));
                ctv_Yaw.setText(Float.toString(e.getResponseMessage().Yaw));
            }
        });
    }

    private EventHandler<TypedResponseReceivedEventArgs<MyResponse>> myOnResponseHandler

            = new EventHandler<TypedResponseReceivedEventArgs<MyResponse>>()
    {
        @Override
        public void onEvent(Object sender,
                            TypedResponseReceivedEventArgs<MyResponse> e)
        {
            onResponseReceived(sender, e);
        }
    };
    private View.OnClickListener myOnSendRequestClickHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            startThread();
            onSendRequest(v);
        }
    };


}
