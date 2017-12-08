
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import eneter.messaging.diagnostic.EneterTrace;
import eneter.messaging.endpoints.typedmessages.*;
import eneter.messaging.messagingsystems.messagingsystembase.*;
import eneter.messaging.messagingsystems.tcpmessagingsystem.TcpMessagingSystemFactory;
import eneter.net.system.EventHandler;
import io.github.crazygoatstudio.mambodroid.R;

import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import static example.myapplication.UtilsKt.rangeAngle;

public class DebugActivity extends AppCompatActivity {
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
    }

    // UI controls
    private Handler myRefresh = new Handler();
    private EditText myMessageTextEditText;
    private EditText myResponseEditText;
    private Button mySendRequestBtn;
    private TextView ctv_Pitch;
    private TextView ctv_Roll;
    private TextView ctv_Yaw;
    private Switch vsw_Leap;


    // Sender sending MyRequest and as a response receiving MyResponse.
    private IDuplexTypedMessageSender<MyResponse, MyRequest> mySender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        // Get UI widgets.
        myMessageTextEditText = (EditText) findViewById(R.id.messageTextEditText);
        myResponseEditText = (EditText) findViewById(R.id.messageLengthEditText);
        mySendRequestBtn = (Button) findViewById(R.id.sendRequestBtn);
        ctv_Pitch = (TextView) findViewById(R.id.tv_Pitch);
        ctv_Roll = (TextView) findViewById(R.id.tv_Roll);
        ctv_Yaw = (TextView) findViewById(R.id.tv_Yaw);

        // Subscribe to handle the button click.
        mySendRequestBtn.setOnClickListener(myOnSendRequestClickHandler);





        // Subscribe to handle the button click.


        // Open the connection in another thread.
        // Note: From Android 3.1 (Honeycomb) or higher
        //       it is not possible to open TCP connection
        //       from the main thread.
        Thread anOpenConnectionThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    openConnection();
                }
                catch (Exception err)
                {
                    EneterTrace.error("Open connection failed.", err);
                }
            }
        });
        anOpenConnectionThread.start();
    }
    @Override
    public void onDestroy()
    {
        // Stop listening to response messages.
        mySender.detachDuplexOutputChannel();

        super.onDestroy();
    }

    private void openConnection() throws Exception
    {
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
                aMessaging.createDuplexOutputChannel("tcp://172.17.2.89:8060/");

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
        try
        {
            mySender.sendRequestMessage(aRequestMsg);
        }
        catch (Exception err)
        {
            EneterTrace.error("Sending the message failed.", err);
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
                ctv_Pitch.setText(Integer.toString(rangeAngle(e.getResponseMessage().Pitch)));
                //</editor-fold>
                //<editor-fold desc="Roll">
                ctv_Roll.setText(Integer.toString(rangeAngle(e.getResponseMessage().Roll)));
                //</editor-fold>

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

    private OnClickListener myOnSendRequestClickHandler = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            onSendRequest(v);
        }
    };
}
