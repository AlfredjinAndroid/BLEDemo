# 蓝牙学习

# 蓝牙(Bluetooth)介绍

## 基本介绍
### BluetoothAdapter

表示本地蓝牙适配器（蓝牙无线装置）。 BluetoothAdapter 是所有蓝牙交互的入口点。 利用它可以发现其他蓝牙设备，查询绑定（配对）设备的列表，使用已知的 MAC 地址实例化 BluetoothDevice，以及创建 BluetoothServerSocket 以侦听来自其他设备的通信。

### BluetoothDevice

表示远程蓝牙设备。利用它可以通过 BluetoothSocket 请求与某个远程设备建立连接，或查询有关该设备的信息，例如设备的名称、地址、类和绑定状态等。

## 蓝牙所需要的权限

```

    <!-- 使用蓝牙所需要的权限 -->
    <uses-permission android:name="android.permission.BLUETOOTH" />

    <!-- 使用扫描和设置蓝牙的权限（申明这一个权限必须申明上面一个权限） -->
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

    <!-- <uses-feature android:name="android" -->

    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

### 权限解释

权限1：使用蓝牙所需要的权限

权限2：使用扫描和设置蓝牙的权限，申明这一权限必须声明权限1

权限3、4：定位服务，使用扫描时需要，在6.0后属于危险权限，需要动态申请。

## 设置蓝牙

### 1. 获取BluetoothAdapter
所有蓝牙 Activity 都需要 BluetoothAdapter。 要获取 BluetoothAdapter，请调用静态 getDefaultAdapter() 方法。这将返回一个表示设备自身的蓝牙适配器（蓝牙无线装置）的 BluetoothAdapter。

声明:
```
private BluetoothAdapter mBluetoothAdapter;
```

获取蓝牙适配

```
mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
```

整个系统只有一个BluetoothAdapter，如果获取为空则表示系统不支持蓝牙设备。

所以可以封装如下方法判断是否支持蓝牙设备，支持则可以进行下一步操作，不支持直接退出吧(枪都没有，打什么仗)。

```
private boolean isSupportBle() {
    return mBluetoothAdapter != null;
}
```

### 2. 启用蓝牙

```
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
```

### 2.1 打开蓝牙

```
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
```
打开蓝牙时，会提示弹窗，当用户点击允许后才能进行之后的操作，否则无法进行下一步。
![打开蓝牙](http://ouuy1zpt1.bkt.clouddn.com/lanyaopen.png)

### 2.2 关闭蓝牙
```

    /**
     * 关闭蓝牙
     */
    private void closeBle() {
        mBluetoothAdapter.disable();
        showToast("关闭蓝牙");
    }
```

### 2.3 查看已配对列表
```

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
```

### 2.4 扫描设备

```
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
```

扫描设备需要开启位置服务，而在6.0中位置服务需要动态申请开启，所以需要执行以下动态申请

```
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
```

扫描到设备后，系统会默认发送广播，所以需要写一个广播接收器来接受并完成相对应的操作。

### 动态添加广播接收
```
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
```

### 设置广播过滤器
```
        IntentFilter f = new IntentFilter();
        //发现设备
        f.addAction(BluetoothDevice.ACTION_FOUND);
        //设备连接状态改变
        f.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //蓝牙设备状态改变
        f.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, f);
```

查找到后可以获取设备的相应信息

![查找设备](http://ouuy1zpt1.bkt.clouddn.com/lanyasm.png)
