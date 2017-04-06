package com.example.srikanth.androidcloud;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static android.R.attr.button;
import static android.R.attr.configChanges;
import static android.R.attr.id;

/**
 * Created by Srikanth on 4/5/2017.
 */

public class BillingPage extends AppCompatActivity {

    EditText _cardNumber;
    EditText _expDate;
    EditText _cardCode;
    EditText _amount;
    Button _pay;

    String status=null;
    String JsonResponse = null;
    String returnCode =null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        _cardNumber = (EditText)findViewById(R.id.creditCardNo);
        _expDate =(EditText)findViewById(R.id.expDate);
        _cardCode =(EditText) findViewById(R.id.cardCode);
        _amount =(EditText)findViewById(R.id.amount);

        _pay =(Button)findViewById(R.id.pay);

        _pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJSONDataToServer task = new sendJSONDataToServer();
                try {
                     task.execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                Log.i("return statement",status);

                if(status.contains("Successful.")) {
                    Log.i("Transaction successful", status);
                    new AlertDialog.Builder(BillingPage.this)
                            .setTitle("Transaction Details")
                            .setMessage(status)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                }
                else
                {
                    Log.i("Transaction failed","Ayyooo");
                    new AlertDialog.Builder(BillingPage.this)
                            .setTitle("Transaction Details")
                            .setMessage(status)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }

            }
        });

    }

    public class sendJSONDataToServer extends AsyncTask<String, Void, String>
    {
        JSONObject jPaymentDetails = storeDataInJson();
        String json = jPaymentDetails.toString();

        String statusOp;
        @Override
        protected String doInBackground(String... params) {
            try {

                URL url = new URL("https://apitest.authorize.net/xml/v1/request.api");
                HttpURLConnection con = null;
                con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");

                Writer writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), "UTF-8"));
                writer.write(json);
                Log.i("JSON String",json);
                writer.close();

                InputStream inputStream = con.getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = null;
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");

                JsonResponse = buffer.toString();
                Log.i("JSON Response",JsonResponse);
                storeJSONDataInDB(JsonResponse);
                con.disconnect();
            }
            catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }
    }

    public void storeJSONDataInDB(String JsonResponse)
    {
        String desc =null;
        try {
            JSONObject  jsonObj = new JSONObject(JsonResponse);

            JSONArray jsonStatusArray= jsonObj.getJSONObject("messages").getJSONArray("message");




            for (int i = 0; i < jsonStatusArray.length(); ++i) {
                JSONObject rec = jsonStatusArray.getJSONObject(i);
                returnCode = rec.getString("code");
                status = rec.getString("text");
                Log.i("code",returnCode);
                Log.i("status",status);
            }

            Log.i("returnCode",returnCode);
            if(returnCode.matches("I00001")) {
                Log.i("Parse Server","store in bitnami");

                ParseObject obj = new ParseObject("TransactionDetails");
                obj.put("transId", jsonObj.getJSONObject("transactionResponse").getString("transId"));
                obj.put("accountNumber", jsonObj.getJSONObject("transactionResponse").getString("accountNumber"));
                obj.put("accountType", jsonObj.getJSONObject("transactionResponse").getString("accountType"));
                obj.put("refId", jsonObj.getString("refId"));
                obj.put("status", status);

                obj.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.i("Parse Result", "Successful");
                        } else {
                            Log.i("Parse Result", "Failed");
                        }
                    }
                });
            }
            if(jsonObj.getJSONObject("transactionResponse").has("messages")) {
                Log.i("Inside","transactionResponse");
                    Log.i("Inside","messages");

                    desc = jsonObj.getJSONObject("transactionResponse").getJSONArray("messages").getJSONObject(0).getString("description");
                    status = status + desc;
            }
            else
            {
                Log.i("Inside","errorText");
                desc = jsonObj.getJSONObject("transactionResponse").getJSONArray("errors").getJSONObject(0).getString("errorText");
                status = status + desc;
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject storeDataInJson()
    {
        String CardNumber = _cardNumber.getText().toString().trim();
        String ExpiratonDate = _expDate.getText().toString().trim();
        String CVV = _cardCode.getText().toString().trim();
        String Amount = _amount.getText().toString().trim();

        JSONObject jfinal = new JSONObject();
        JSONObject jkeys =new JSONObject();
        JSONObject jCreditCardTrans = new JSONObject();

        JSONObject jTransactionReq = new JSONObject();
        JSONObject jInfo = new JSONObject();
        JSONObject jLine = new JSONObject();
        JSONObject jObjectType = new JSONObject();
        JSONObject jObjectItem = new JSONObject();
        JSONObject jObjectTax = new JSONObject();
        JSONObject jObjectduty = new JSONObject();

        try {
            jkeys.put("name","7vz8xGg3D7j");
            jkeys.put("transactionKey","4brhB8ukY2R8n98W");
            jCreditCardTrans.put("merchantAuthentication",jkeys);
            jCreditCardTrans.put("refId","123456");
            jTransactionReq.put("transactionType" ,"authCaptureTransaction");
            jTransactionReq.put("amount","5");
            jObjectType.put("cardNumber",CardNumber);
            jObjectType.put("expirationDate",ExpiratonDate);
            jObjectType.put("cardCode",CVV);
            jInfo.put("creditCard",jObjectType);
            jTransactionReq.put("payment",jInfo);
            jObjectItem.put("itemId",1);
            jObjectItem.put("name","vase");
            jObjectItem.put("description","pink color");
            jObjectItem.put("quantity","18");
            jObjectItem.put("unitPrice",Amount);
            jLine.put("lineItem",jObjectItem);
            jTransactionReq.put("lineItems",jLine);
            jObjectTax.put("amount","4.26");
            jObjectTax.put("name","level2 tax name");
            jObjectTax.put("description","level2 tax");
            jTransactionReq.put("tax",jObjectTax);
            jObjectduty.put("amount","8.55");
            jObjectduty.put("name","duty name");
            jObjectduty.put("description","duty description");
            jTransactionReq.put("duty",jObjectduty);
            jCreditCardTrans.put("transactionRequest",jTransactionReq);
            jfinal.put("createTransactionRequest",jCreditCardTrans);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("JSON String",jfinal.toString());
        return jfinal;
    }
}
