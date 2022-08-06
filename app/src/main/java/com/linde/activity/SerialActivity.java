package com.linde.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.linde.presenter.DrugMainPresenter;
import com.linde.refrigeratormanagementsystem.R;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.Queue;

import android_serialport_api.ComBean;
import android_serialport_api.MyFunc;
import android_serialport_api.SerialHelper;
import android_serialport_api.SerialPortFinder;

public class SerialActivity extends AppCompatActivity {
    SerialControl serialCom;//串口
    TextView tvResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pup_serial_port);


        serialCom = new SerialControl();
        mSerialPortFinder= new SerialPortFinder();
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();


        ImageButton btnClose = findViewById(R.id.btnClose);

        //wait wait wait


        EditText edDevicePath=findViewById(R.id.edDevicePath);
        EditText edPort=findViewById(R.id.edPort);
        EditText edMessage=findViewById(R.id.edMessage);
        Button btnConnect=findViewById(R.id.btnConnect);
        Button btnDisConnect=findViewById(R.id.btnDisConnect);
        Button btnSend=findViewById(R.id.btnSend);
        tvResult=findViewById(R.id.tvResult);
        tvResult.setText(entryValues.toString());

        String devicePath=edDevicePath.getText().toString();
        String port=edPort.getText().toString();
        String message=edMessage.getText().toString();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serialCom.setPort(devicePath);
                serialCom.setBaudRate(port);
                OpenComPort(serialCom);

                tvResult.setText("port"+port+"devicePath="+devicePath);
            }
        });
        btnDisConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //关闭串口
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //发送16进制的字符串
                serialCom.sendTxt(message);
                tvResult.setText("发送16进制的字符串");
            }
        });


        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });


    }

    DispQueueThread DispQueue;//刷新显示线程
    SerialPortFinder mSerialPortFinder;//串口设备搜索
    //----------------------------------------------------串口控制类
    private class SerialControl extends SerialHelper {
        public SerialControl() {
        }

        @Override
        protected void onDataReceived(final ComBean ComRecData) {
            DispQueue.AddQueue(ComRecData);// 线程定时刷新显示(推荐)
        }
    }
    /**
     * 关闭串口
     */
    private void CloseComPort(SerialHelper ComPort) {
        if (ComPort != null) {
            ComPort.stopSend();
            ComPort.close();
        }
    }

    //----------------------------------------------------打开串口
    private void OpenComPort(SerialHelper ComPort) {
        try {
            ComPort.open();
        } catch (SecurityException e) {
            ShowMessage(getString(R.string.No_read_or_write_permissions));
        } catch (IOException e) {
            ShowMessage(getString(R.string.Unknown_error));
        } catch (InvalidParameterException e) {
            ShowMessage(getString(R.string.Parameter_error));
        }
    }
    //----------------------------------------------------刷新显示线程
    private class DispQueueThread extends Thread {
        private Queue<ComBean> QueueList = new LinkedList<ComBean>();
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                final ComBean ComData;
                while ((ComData = QueueList.poll()) != null) {

                    runOnUiThread(new Runnable() {
                        public void run() {
                            DispRecData(ComData);
                        }
                    });

                    try {
                        Thread.sleep(10);// 显示性能高的话，可以把此数值调小。
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        public synchronized void AddQueue(ComBean ComData) {
            QueueList.add(ComData);
        }
    }

    //----------------------------------------------------显示接收数据
    private void DispRecData(ComBean ComRecData) {
        StringBuilder sMsg = new StringBuilder();
        byte[] temp = new byte[20];
        long cardint=0;
        long wg26_1=0;
        long wg26_2=0;
        long wg34_1=0;
        long wg34_2=0;
        try {
            sMsg.append("recv: "+ MyFunc.ByteArrToHex(ComRecData.bRec));

            if(solveRecv(ComRecData.bRec, temp)==0){    //主动刷卡的数据处理
                int len = temp[0];
                byte[] cardnum = new byte[4];
                System.arraycopy( temp,1, cardnum, 0, 4);   //只保留前面4个字节卡号

                sMsg.append("\n原始卡号："+MyFunc.ByteArrToHex(cardnum)+"\n");

                cardint = Long.parseLong(MyFunc.ByteArrToHex(cardnum, 0, 4), 16);
                sMsg.append("正码转十进制："+String.format("%010d", cardint)+"\n");

                wg26_1 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,1,1), 16);
                wg26_2 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,2,2), 16);
                sMsg.append("正码转韦根26："+String.format("%03d,%05d", wg26_1, wg26_2)+"\n");

                wg34_1 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,0,2), 16);
                wg34_2 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,2,2), 16);
                sMsg.append("正码转韦根34："+String.format("%05d,%05d", wg34_1, wg34_2)+"\n");

                MyFunc.reverseByte(cardnum);
                cardint = Long.parseLong(MyFunc.ByteArrToHex(cardnum, 0, 4), 16);
                sMsg.append("反码转十进制："+String.format("%010d", cardint)+"\n");

                wg26_1 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,1,1), 16);
                wg26_2 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,2,2), 16);
                sMsg.append("反码转韦根26："+String.format("%03d,%05d", wg26_1, wg26_2)+"\n");

                wg34_1 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,0,2), 16);
                wg34_2 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,2,2), 16);
                sMsg.append("反码转韦根34："+String.format("%05d,%05d", wg34_1, wg34_2)+"\n");

            }

            ShowMessage(sMsg.toString());

        } catch (Exception ex) {
            Log.d("lalala",ex.getMessage());
        }
    }

    //------------------------------------------显示消息
    private void ShowMessage(String sMsg) {
        StringBuilder sbMsg = new StringBuilder();
//        sbMsg.append(editTextRecDisp.getText());
//        sbMsg.append(m_sdfDate.format(new Date()));
        sbMsg.append(sMsg);
        sbMsg.append("\r\n");
    /*    editTextRecDisp.setText(sbMsg);
        editTextRecDisp.setSelection(sbMsg.length(), sbMsg.length());*/
        Toast.makeText(this,sbMsg.toString(),Toast.LENGTH_LONG).show();
    }

    //识别主动刷卡数据
    private int solveRecv(byte[] bRec, byte[] retRec) {
        int sta = -1;

        if((byte)bRec[0] == 0x02){

            byte len = bRec[1];
            if(len <= bRec.length){

                byte result = MyFunc.bccCalc(bRec, 1, len-3);
                if((byte)bRec[len-2] == (byte)result){
                    retRec[0] = (byte) (len-5);
                    System.arraycopy( bRec,3,retRec, 1,len-5);
                    sta = 0;
                }
            }
        }

        return sta;
    }
}