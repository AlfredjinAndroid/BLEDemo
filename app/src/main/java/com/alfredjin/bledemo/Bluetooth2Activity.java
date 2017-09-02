package com.alfredjin.bledemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

public class Bluetooth2Activity extends AppCompatActivity {

    private Button on, off, list, visible;
    private ListView listView;

    private int REQUEST_ACCESS_COARSE_LOCATION = 111;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth2);

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, intentFilter);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filter);
        //注册一个搜索结束时的广播
        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter2);

        IntentFilter f = new IntentFilter();
        //发现设备
        f.addAction(BluetoothDevice.ACTION_FOUND);
        //设备连接状态改变
        f.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //蓝牙设备状态改变
        f.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, f);

        initView();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        on = (Button) findViewById(R.id.btnOn);
        off = (Button) findViewById(R.id.btnOff);
        list = (Button) findViewById(R.id.btnList);
        visible = (Button) findViewById(R.id.btnVisible);
        listView = (ListView) findViewById(R.id.lv);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!isSupportBle()) {
            showToast("设备不支持");
        } else {
            initListener();
        }

        if(Build.VERSION.SDK_INT>=23){
            //判断是否有权限
            if (ContextCompat.checkSelfPermission(Bluetooth2Activity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_ACCESS_COARSE_LOCATION);
                //向用户解释，为什么要申请该权限
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    Toast.makeText(Bluetooth2Activity.this,"shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    /**
     * 初始化事件
     */
    private void initListener() {
        //开启蓝牙操作
        on.setOnClickListener((v) -> openBle());

        off.setOnClickListener((v) -> closeBle());

        list.setOnClickListener((v) -> showList());

        visible.setOnClickListener((v) -> visibleDevice());
    }

    /**
     * 查找列表
     */
    private void visibleDevice() {
        Log.e("Test", "开启扫描");
        mBluetoothAdapter.startDiscovery();
        if (mBluetoothAdapter.isDiscovering()) {
            Log.e("Test", "查找中");
        }


    }

    /**
     * 设备列表
     */
    private void showList() {
        Set<BluetoothDevice> deviceSet = mBluetoothAdapter.getBondedDevices();
        if (deviceSet.size() != 0) {
            ArrayList<String> list = new ArrayList<>();
            for (BluetoothDevice bluetoothDevice : deviceSet) {
                list.add(bluetoothDevice.getName());
            }
            listView.setAdapter(new ArrayAdapter<>
                    (this, android.R.layout.simple_list_item_activated_1, list));
        } else {
            showToast("无配对列表");
        }
    }

    /**
     * 关闭蓝牙
     */
    private void closeBle() {
        mBluetoothAdapter.disable();
        showToast("关闭蓝牙");
    }

    /**
     * 开启蓝牙操作
     */
    private void openBle() {
        if (mBluetoothAdapter.isEnabled()) {
            showToast("蓝牙已经打开，无需重复操作");
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 0);
            showToast("打开蓝牙");
        }
    }

    private boolean isSupportBle() {
        return mBluetoothAdapter != null;
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.e("Test", "开启了广播");
            String action = intent.getAction();
            Log.e("Test", "接收到的广播:" + action);
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e("Test", "查到设备" + device.getName() + "\n" + device.getAddress());
                showToast("查到设备" + device.getName() + "\n" + device.getAddress());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e("Test", "搜索完毕...");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.e("Test", "ACTION_DISCOVERY_STARTED");

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_ACCESS_COARSE_LOCATION) {
            if (permissions[0] .equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户同意使用该权限
            } else {
                // 用户不同意，向用户展示该权限作用
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //showTipDialog("用来扫描附件蓝牙设备的权限，请手动开启！");
                    return;

                }
            }
        }
    }
}
