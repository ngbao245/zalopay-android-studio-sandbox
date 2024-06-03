package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.Api.CreateOrder;

import org.json.JSONObject;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class PrintOrder extends AppCompatActivity {

    TextView txtSoluong, txtTongTien;
    Button btnThanhToan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_order);

        txtSoluong = findViewById(R.id.textViewSoluong);
        txtTongTien = findViewById(R.id.textViewTongTien);
        btnThanhToan = findViewById(R.id.buttonThanhToan);

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(2553, Environment.SANDBOX);


        Intent intent = getIntent();

        txtSoluong.setText(intent.getStringExtra("soluong"));
        Double total = intent.getDoubleExtra("total", (double) 0);
        String totalString = String.format("%.0f", total);
        txtTongTien.setText(Double.toString(total));

        btnThanhToan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateOrder orderApi = new CreateOrder();

                try {
                    JSONObject data = orderApi.createOrder(totalString);
                    String code = data.getString("return_code");
                    if (code.equals("1")) {
                        String token = data.getString("zp_trans_token");
                        ZaloPaySDK.getInstance().payOrder(PrintOrder.this, token, "demozpdk://app", new PayOrderListener() {
                            @Override
                            public void onPaymentSucceeded(final String transactionId, final String transToken, final String appTransID) {
//                                Intent intent = new Intent(PrintOrder.this, success.class);
//                                startActivity(intent);
                                Log.d("ZaloPay", "ZaloPay SDK Succeeded");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialog.Builder(PrintOrder.this)
                                                .setTitle("Payment Success")
                                                .setMessage(String.format("TransactionId: %s - TransToken: %s", transactionId, transToken))
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Intent intent = new Intent(PrintOrder.this, success.class);
                                                        startActivity(intent);
                                                    }
                                                })
                                                .setNegativeButton("Cancel", null).show();
                                    }

                                });

                            }

                            @Override
                            public void onPaymentCanceled(String zpTransToken, String appTransID) {
                                new AlertDialog.Builder(PrintOrder.this)
                                        .setTitle("User Cancel Payment")
                                        .setMessage(String.format("zpTransToken: %s \n", zpTransToken))
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent1 = new Intent(PrintOrder.this, PrintOrder.class);
                                                startActivity(intent1);

                                                Log.d("ZaloPay", "ZaloPay SDK Canceled");

                                            }
                                        })
                                        .setNegativeButton("Cancel", null).show();
                            }

                            @Override
                            public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                                new AlertDialog.Builder(PrintOrder.this)
                                        .setTitle("Payment Fail")
                                        .setMessage(String.format("ZaloPayErrorCode: %s \nTransToken: %s", zaloPayError.toString(), zpTransToken))
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.d("ZaloPay", "ZaloPay SDK Error");
                                            }
                                        })
                                        .setNegativeButton("Cancel", null).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    Log.d("ZaloPay", "ZaloPay SDK Exception");
                    e.printStackTrace();
                }

            }
        });
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
}