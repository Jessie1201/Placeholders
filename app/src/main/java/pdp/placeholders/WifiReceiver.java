package pdp.placeholders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.ProxyInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Valtteri on 23.3.2018.
 */

public class WifiReceiver extends BroadcastReceiver {
    static WifiManager wifimanager;
    static WifiReceiver receiverWifi;
    static String previousWifi;

    public static void helperWifi(Context context){
        wifimanager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        receiverWifi = new WifiReceiver();
        context.registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifimanager.startScan();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(wifimanager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        context.registerReceiver(receiverWifi,intentFilter);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        ScanResult boxWifi = null;
        WifiInfo wifiInfo = wifimanager.getConnectionInfo();
        String currentSSID = wifiInfo.getSSID();
        final WifiConfiguration wifiConfig = new WifiConfiguration();
        if(wifimanager.getConnectionInfo().getSSID().contains("FoodGuard")){
            //context.unregisterReceiver(this);
            AlertDialog.Builder wifiad = new AlertDialog.Builder(context);
            wifiad.setTitle("Please enter details for "+currentSSID+"setup");
            wifiad.setMessage("Enter Wifi Name:");
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final EditText wifiname = new EditText(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            try{
                wifiname.setText(previousWifi.replace("\"",""));
            }catch (Exception e){}
            wifiname.setLayoutParams(lp);
            linearLayout.setLayoutParams(lp);
            linearLayout.addView(wifiname);
            final EditText wifipass = new EditText(context);
            wifipass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            wifipass.setHint("WifiPassword");
            linearLayout.addView(wifipass);
            final EditText boxname= new EditText(context);
            boxname.setHint("Boxname");
            boxname.setLayoutParams(lp);
            linearLayout.addView(boxname);
            wifiad.setView(linearLayout);
            wifiad.setPositiveButton("Done",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {
                            //I cant believe this actually worked
                            StringRequest sR1 = new StringRequest(Request.Method.GET,
                                    "\"http://10.0.1.1/wifi?",null,null);
                            String toboxURL = "http://10.0.1.1/wifisave?s="+wifiname.getText().toString()
                                    +"&p="+wifipass.getText().toString()
                                    +"&NAME="+boxname.getText().toString()
                                    +"&UID=" +UserItems.getUserid();
                            String UID=UserItems.getUserid();
                            String wifiid = wifiname.getText().toString();
                            String boxid = boxname.getText().toString();
                            final RequestQueue mRequestQueue = Volley.newRequestQueue(context);
                            mRequestQueue.add(sR1);
                            final StringRequest stringRequest = (StringRequest) new StringRequest(Request.Method.PUT,
                                    toboxURL, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d("wifiresponse",response);
                                    Toast.makeText(context,"Settings added to box successfully",Toast.LENGTH_SHORT).show();
                                    UserItems.addBox(boxname.getText().toString(),new User.Box("empty","2022-06-13","update"));

                                    wifimanager.disconnect();
                                    int netId1= wifiConfig.networkId;

                                    wifimanager.removeNetwork(netId1);
                                    wifimanager.saveConfiguration();
                                    wifimanager.reconnect();

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(context,"Connection to Foodguard failed",Toast.LENGTH_LONG).show();
                                    int netId1= wifiConfig.networkId;
                                    wifimanager.removeNetwork(netId1);

                                }
                            }).setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            mRequestQueue.add(stringRequest);
                        }
                    });
            // Setting Negative "NO" Button
            wifiad.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Write your code here to execute after dialog
                            //dialog.cancel();
                        }
                    });
            wifiad.show();
        }else{

            //final WifiManager wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            List<ScanResult> wifiList = wifimanager.getScanResults();
            for(ScanResult i:wifiList){
                if(i.SSID.contains("FoodGuard")){
                    boxWifi=i; break;
                }
            }
            if(boxWifi!=null){
                int netId =-1;
                boolean missing=true;
                for(WifiConfiguration j:wifimanager.getConfiguredNetworks()){
                    if(j.SSID.contains("FoodGuard")) {
                        missing=false;
                        netId = j.networkId;
                        break;
                    }
                }
                if (missing){
                    wifiConfig.SSID = boxWifi.SSID;
                    wifiConfig.status = WifiConfiguration.Status.ENABLED;
                    wifiConfig.preSharedKey ="\"" +"12345678"+ "\"";

                    try{
                        setStaticIpConfiguration(wifimanager,wifiConfig,InetAddress.getByName("10.0.1.1"), 24,
                                InetAddress.getByName("10.0.1.100"),
                                new InetAddress[] {InetAddress.getByName("10.0.1.3"), InetAddress.getByName("10.0.1.4")});
                    }catch (Exception e){}

                    wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    netId= wifimanager.addNetwork(wifiConfig);
                    wifimanager.disconnect();
                    wifimanager.enableNetwork(netId, true);
                    wifimanager.reconnect();
                    wifiConfig.priority=99999;


                }
                else{
                    previousWifi=currentSSID;
                    wifimanager.disconnect();
                    wifimanager.enableNetwork(netId, true);
                    //wifimanager.reconnect();

                    // TODO: 19.3.2018 set logic for allowed types of names

                }

            }else{
                Toast.makeText(context,"Could not find a box",Toast.LENGTH_SHORT).show();
            }


        }

    }

    //bigmantyrone4413 Wifi1234
    @SuppressWarnings("unchecked")
    private static void setStaticIpConfiguration(WifiManager manager, WifiConfiguration config, InetAddress ipAddress, int prefixLength, InetAddress gateway, InetAddress[] dns) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException, InstantiationException
    {
        // First set up IpAssignment to STATIC.
        Object ipAssignment = getEnumValue("android.net.IpConfiguration$IpAssignment", "STATIC");
        callMethod(config, "setIpAssignment", new String[] { "android.net.IpConfiguration$IpAssignment" }, new Object[] { ipAssignment });

        // Then set properties in StaticIpConfiguration.
        Object staticIpConfig = newInstance("android.net.StaticIpConfiguration");
        Object linkAddress = newInstance("android.net.LinkAddress", new Class<?>[] { InetAddress.class, int.class }, new Object[] { ipAddress, prefixLength });

        setField(staticIpConfig, "ipAddress", linkAddress);
        setField(staticIpConfig, "gateway", gateway);
        getField(staticIpConfig, "dnsServers", ArrayList.class).clear();
        for (int i = 0; i < dns.length; i++)
            getField(staticIpConfig, "dnsServers", ArrayList.class).add(dns[i]);

        callMethod(config, "setStaticIpConfiguration", new String[] { "android.net.StaticIpConfiguration" }, new Object[] { staticIpConfig });
        manager.updateNetwork(config);
        manager.saveConfiguration();
    }

    private static Object newInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
    {
        return newInstance(className, new Class<?>[0], new Object[0]);
    }

    private static Object newInstance(String className, Class<?>[] parameterClasses, Object[] parameterValues) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException
    {
        Class<?> clz = Class.forName(className);
        Constructor<?> constructor = clz.getConstructor(parameterClasses);
        return constructor.newInstance(parameterValues);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object getEnumValue(String enumClassName, String enumValue) throws ClassNotFoundException
    {
        Class<Enum> enumClz = (Class<Enum>)Class.forName(enumClassName);
        return Enum.valueOf(enumClz, enumValue);
    }

    private static void setField(Object object, String fieldName, Object value) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException
    {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.set(object, value);
    }

    private static <T> T getField(Object object, String fieldName, Class<T> type) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException
    {
        Field field = object.getClass().getDeclaredField(fieldName);
        return type.cast(field.get(object));
    }

    private static void callMethod(Object object, String methodName, String[] parameterTypes, Object[] parameterValues) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
    {
        Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++)
            parameterClasses[i] = Class.forName(parameterTypes[i]);

    }

}
