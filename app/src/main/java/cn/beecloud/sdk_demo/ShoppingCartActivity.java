package cn.beecloud.sdk_demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.Holder;
import com.orhanobut.dialogplus.ListHolder;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.unionpay.UPPayAssistEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.beecloud.BCAnalysis;
import cn.beecloud.BCPay;
import cn.beecloud.BCPayCallback;
import cn.beecloud.BCUtil;
import cn.beecloud.BeeCloud;


public class ShoppingCartActivity extends ActionBarActivity {

    private String[] names = new String[]{
            "衣服", "裤子", "鞋子",
    };

    private String[] descs = new String[]{
            "我的新衣服", "我的新裤子", "我的新鞋子"
    };

    private int[] imageIds = new int[]{
            R.drawable.yifu, R.drawable.kuzi, R.drawable.xiezi
    };

    // 银联支付控件的状态
    public static final int PLUGIN_VALID = 0;
    public static final int PLUGIN_NOT_INSTALLED = -1;
    public static final int PLUGIN_NEED_UPGRADE = 2;

    Button btnShopping;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        BeeCloud.setAppIdAndSecret(this, "c5d1cba1-5e3f-4ba0-941d-9b0a371fe719", "39a7a518-9ac8-4a9e-87bc-7885f33cf18c");
        BCAnalysis.setUserId("BeeCloud Android User！");
        BCAnalysis.setUserGender(true);
        BCAnalysis.setUserAge(28);

        // Defines a Handler object that's attached to the UI thread.
        // 通过Handler.Callback()可消除内存泄漏警告 By ZhuChenglin
        mHandler = new Handler(new Handler.Callback() {
            /**
             * Callback interface you can use when instantiating a Handler to avoid
             * having to implement your own subclass of Handler.
             *
             * handleMessage() defines the operations to perform when
             * the Handler receives a new Message to process.
             */
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 3) {
                    //如果用户手机没有安装银联支付控件,则会提示用户安装
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingCartActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("完成支付需要安装银联支付控件，是否安装？");

                    builder.setNegativeButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    UPPayAssistEx.installUPPayPlugin(ShoppingCartActivity.this);
                                    dialog.dismiss();
                                }
                            });

                    builder.setPositiveButton("取消",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                }
                return true;
            }
        });

        btnShopping = (Button) findViewById(R.id.btnShopping);
        btnShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DialogPlus.Gravity.BOTTOM);
            }
        });

        List<Map<String, Object>> listItems = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> listItem = new HashMap<>();
            listItem.put("icon", imageIds[i]);
            listItem.put("name", names[i]);
            listItem.put("desc", descs[i]);
            listItems.add(listItem);
        }

       /*SimpleAdapter adapter = new SimpleAdapter(this, listItems,
                R.layout.list_item_shopping_cart,
                new String[]{"name", "icon", "desc"},
                new int[]{R.id.txtViewName, R.id.imageView, R.id.txtViewDesc});*/

        ShoppingAdapter adapter = new ShoppingAdapter(this, listItems);
        ListView listView = (ListView) findViewById(R.id.lstViewShoppingCart);
        listView.setAdapter(adapter);
    }


    private void showDialog(DialogPlus.Gravity gravity) {

        Holder holder = new ListHolder();

        OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(DialogPlus dialog, View view) {
                switch (view.getId()) {
                    case R.id.header_container:
                        Toast.makeText(ShoppingCartActivity.this, "Header clicked", Toast.LENGTH_LONG).show();
                        break;
                    case R.id.footer_confirm_button:
                        Toast.makeText(ShoppingCartActivity.this, "Confirm button clicked", Toast.LENGTH_LONG).show();
                        break;
                    case R.id.footer_close_button:
                        Toast.makeText(ShoppingCartActivity.this, "Close button clicked", Toast.LENGTH_LONG).show();
                        break;
                }
                dialog.dismiss();
            }
        };

        OnItemClickListener itemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                TextView textView = (TextView) view.findViewById(R.id.text_view);
                String clickItem = textView.getText().toString();
                dialog.dismiss();

                switch (clickItem) {
                    case "微信支付":
                        Map<String, String> mapOptional = new HashMap<>();
                        String optionalKey = "android_optional_key";
                        String optionalValue = "android_optional_value";
                        if (!BCUtil.isValidIdentifier(optionalKey) || !BCUtil.isValidIdentifier(optionalValue)) {
                            return;
                        }
                        mapOptional.put(optionalKey, optionalValue);
                        BCPay.getInstance(ShoppingCartActivity.this).reqWXPaymentAsync("test", "1",
                                BCUtil.generateRandomUUID().replace("-", ""), "BeeCloud-Android", mapOptional, new BCPayCallback() {
                                    @Override
                                    public void done(boolean b, String s) {
                                        System.out.println("reqWXPaymentAsync:" + b + "|" + s);
                                    }
                                });
                        break;
                    case "支付宝支付":
                        mapOptional = new HashMap<>();
                        optionalKey = "android_optional_key";
                        optionalValue = "android_optional_value";
                        if (!BCUtil.isValidIdentifier(optionalKey) || !BCUtil.isValidIdentifier(optionalValue)) {
                            return;
                        }
                        mapOptional.put(optionalKey, optionalValue);
                        BCPay.getInstance(ShoppingCartActivity.this).reqAliPaymentAsync("test", BCUtil.generateRandomUUID().replace("-", ""),
                                "订单标题", "对一笔交易的具体描述信息", "0.01", mapOptional, new BCPayCallback() {
                                    @Override
                                    public void done(boolean b, String s) {
                                        System.out.println("btnAliPay:" + b + "|" + s);
                                    }
                                });
                        break;
                    case "银联支付":
                        mapOptional = new HashMap<>();
                        optionalKey = "android_optional_key";
                        optionalValue = "android_optional_value";
                        if (!BCUtil.isValidIdentifier(optionalKey) || !BCUtil.isValidIdentifier(optionalValue)) {
                            return;
                        }
                        mapOptional.put(optionalKey, optionalValue);
                        BCPay.getInstance(ShoppingCartActivity.this).reqUnionPaymentAsync("Android-UPPay", "Android-UPPay-body",
                                BCUtil.generateRandomUUID().replace("-", ""), "1", mapOptional, new BCPayCallback() {
                                    @Override
                                    public void done(boolean b, String s) {
                                        System.out.println("btnUPPay:" + b + "|" + s);

                                        int ret = Integer.valueOf(s);
                                        if (ret == PLUGIN_NEED_UPGRADE || ret == PLUGIN_NOT_INSTALLED) {
                                            // 需要重新安装控件
                                            Message msg = mHandler.obtainMessage();
                                            msg.what = 3;
                                            mHandler.sendMessage(msg);
                                        }
                                    }
                                });
                        break;
                }
            }
        };

        MySimpleAdapter adapter = new MySimpleAdapter(ShoppingCartActivity.this);
        showCompleteDialog(holder, gravity, adapter, clickListener, itemClickListener);
    }

    private void showCompleteDialog(Holder holder,
                                    DialogPlus.Gravity gravity,
                                    BaseAdapter adapter,
                                    OnClickListener clickListener,
                                    OnItemClickListener itemClickListener) {
        final DialogPlus dialog = new DialogPlus.Builder(this)
                .setContentHolder(holder)
                .setHeader(R.layout.header)
                .setFooter(R.layout.footer)
                .setCancelable(true)
                .setGravity(gravity)
                .setAdapter(adapter)
                .setOnClickListener(clickListener)
                .setOnItemClickListener(itemClickListener)
                .create();
        dialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shopping_cart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}