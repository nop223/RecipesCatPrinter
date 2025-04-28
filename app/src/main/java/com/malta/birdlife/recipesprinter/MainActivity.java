package com.malta.birdlife.recipesprinter;
/**
 * Created by filmon on 05/09/2016.
 */

/**
 * Google barcode API imports
 **/
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

/**
 *  MINI Termal Printer API
 */

import java.util.Objects;
import java.util.Set;
import android.content.Intent;

import com.malta.birdlife.R;
import com.zj.btsdk.BluetoothService;


import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.util.Log;



public class MainActivity extends Activity  {



    /*
        Attributi
     */
    BluetoothService btService = null;
    BluetoothDevice btDevice = null;

    Button btnPrint;
    Button btnSearch;

    public Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 2;

    private static final int REQUEST_CONNECT_DEVICE = 1;

    private int conn_flag = 0;

    private ConnectPaireDev mConnPaireDev = null;

 //   public String URLPAGE = "http:/birdlifemalta.org/";

    String pdfDocument = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //LooperThread btLooper = new LooperThread();
        //btLooper.run();
        mHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {

            @Override
            public void handleMessage(Message msg) {

                String TAG_B = "BLUETOOTH";

                switch (msg.what) {

                    case BluetoothService.MESSAGE_STATE_CHANGE: {
                        switch (msg.arg1) {

                            case BluetoothService.STATE_CONNECTED: {
                                Toast.makeText(getApplicationContext(), "Connect successful",

                                        Toast.LENGTH_SHORT).show();

                                btnPrint.setEnabled(true);

                                conn_flag = 1;

                                break;
                            }
                            case BluetoothService.STATE_CONNECTING: {
                                Log.d(TAG_B, "Connecting.....");

                                break;
                            }
                            case BluetoothService.STATE_LISTEN: {
                                Log.d(TAG_B, "Listening...");

                                break;
                            }
                            case BluetoothService.STATE_NONE: {


                                Log.d(TAG_B, "State none...");

                                break;
                            }
                        }

                        break;
                    }
                    case BluetoothService.MESSAGE_CONNECTION_LOST: {
                        Toast.makeText(getApplicationContext(), "Device connection was lost",

                                Toast.LENGTH_SHORT).show();

                        btnPrint.setEnabled(false);
                        conn_flag = 0;
                        break;
                    }
                    case BluetoothService.MESSAGE_UNABLE_CONNECT: {
                        Toast.makeText(getApplicationContext(), "Unable to connect device",

                                Toast.LENGTH_SHORT).show();

                        conn_flag = -1;

                        break;
                    }
                }

            }


        };

        btService = new BluetoothService(this, mHandler);


        // se il dispositivo non dispone di ricevitore bluetooth
        if (btService.isAvailable() == false) {

            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();

            finish();
        }
        // se il bluetooth non e' attivo
        else if (btService.isBTopen() == false) {

            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            pdfDocument = extras.getString("document");
            //The key argument here mu st match that used in the other activity
          //  Log.i("BUFFER_DOC",bufferDoc);

        }

        mConnPaireDev = new ConnectPaireDev();

        mConnPaireDev.start();

    }

    @Override
    public void onStart() {

        super.onStart();

        try {

            btnPrint = this.findViewById(R.id.btnPrint);

            btnPrint.setOnClickListener(new ClickEvent());

            btnPrint.setEnabled(false);

            btnSearch = this.findViewById(R.id.btnSearch);

            btnSearch.setOnClickListener(new ClickEvent());

            ProgressBar loading = findViewById(R.id.progressBar);

            loading.setVisibility(View.INVISIBLE);

        } catch (Exception ex) {

            Log.e("MAIN_ACTIVITY",ex.getMessage());

        }
        conn_flag = 0;

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (btService != null)
        {
            btService.stop();
        }
        btService = null;

    }

    class ClickEvent implements View.OnClickListener {

        public void onClick(View v) {

            if (v == btnSearch) {

                Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);

                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

            } else if (v == btnPrint) {

                ProgressBar loading = findViewById(R.id.progressBar);

                loading.setVisibility(View.VISIBLE);

                byte[] sendData = null;
                //PrintPicEx pg = new PrintPicEx();
                //pg.initCanvas(384); // ParameterDescription： W:Value 384px for 58seriesprinter ，Value 576px for 80seriesprinter
                //pg.initPaint();
                //pg.drawImageResource(384/3,0,getApplicationContext().getResources(),R.drawable.logox);
                //sendData = pg.printDraw();
                carPrinterDriver catPrint = new carPrinterDriver();
                sendData = catPrint.formatMessage(catPrint.FeedPaper, new int[]{10});
                btService.write(sendData);


                loading.setVisibility(View.INVISIBLE);
                }

            }

        }





    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode)
        {

            case REQUEST_ENABLE_BT:
            {
                if (resultCode == Activity.RESULT_OK)
                {

                  Toast.makeText(this, "Bluetooth open successful", Toast.LENGTH_LONG).show();

                }
                else
                {
                    Toast.makeText(this, "Bluetooth is required for this app", Toast.LENGTH_LONG).show();
                    /* ########################################### */
                    finish();
                }

                break;
            }
            case  REQUEST_CONNECT_DEVICE:
            {
                if (resultCode == Activity.RESULT_OK)
                {

                    String address = Objects.requireNonNull(data.getExtras()).getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    btDevice = btService.getDevByMac(address);
                    btService.connect(btDevice);

                }
                else
                {
                    Toast.makeText(this, "Bluetooth is required for this app", Toast.LENGTH_LONG).show();
                }

                break;
            }
        }

    }

    public static String selDeviceAddress = null;

    public class ConnectPaireDev extends Thread {

        public void run(){

            while(true)
            {
                try {
                    if (btService != null) {
                        while (btService.isBTopen()) {
                            Set<BluetoothDevice> pairedDevices = btService.getPairedDev();

                            if ((!pairedDevices.isEmpty()) && (null != MainActivity.selDeviceAddress)) {

                                for (BluetoothDevice device : pairedDevices) {

                                    if (Objects.equals(device.getAddress(), MainActivity.selDeviceAddress)) {
                                        if (conn_flag == -1 || conn_flag == 0) // se impossibile connettersi
                                        {
                                            btService.connect(device);
                                            conn_flag = 2;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }catch (Exception ex){
                    Log.d("DEVICE",ex.toString());
                }
            }
        }
    }


}
