package com.example.srikanth.androidcloud;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class MainActivity extends AppCompatActivity {


    Button _signIn;
    Button _Regis;
    EditText _emailText;
    EditText _passwordText;
    EditText _regemail;
    EditText _regpass;
    EditText _confpass;
    EditText _firstName;
    EditText _lastName;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            _signIn =(Button) findViewById(R.id.signIn);
            _Regis = (Button) findViewById(R.id.register);
            _emailText= (EditText) findViewById(R.id.emailId);
            _passwordText = (EditText) findViewById(R.id.passwrd);
            _regemail = (EditText) findViewById(R.id.regemailId);
            _regpass = (EditText) findViewById(R.id.regPass);
            _confpass = (EditText) findViewById(R.id.regconfPass);
            _firstName = (EditText) findViewById(R.id.firstName);
            _lastName = (EditText) findViewById(R.id.lastName);


            _signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ParseUser.logInInBackground(_emailText.getText().toString(), _passwordText.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {

                            if (user != null) {

                                _emailText.setText("");
                                _passwordText.setText("");
                                Toast.makeText(MainActivity.this,"Login Successful", Toast.LENGTH_SHORT).show();

                                Log.i("SignIn", "Login successful");
                                Intent i=new Intent(MainActivity.this,BillingPage.class);
                                startActivity(i);
                            } else {

                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                            }


                        }
                    });

                }
            });
            _Regis.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (_regemail.getText().toString().matches("") || _regpass.getText().toString().matches("") || _confpass.getText().toString().matches("") || _firstName.getText().toString().matches("")
                            || _lastName.getText().toString().matches("")) {

                        Toast.makeText(MainActivity.this, "Please fill all the details.", Toast.LENGTH_SHORT).show();

                    } else {

                        ParseUser user = new ParseUser();

                        user.setUsername(_regemail.getText().toString());
                        user.setPassword(_regpass.getText().toString());
                        user.setEmail(_regemail.getText().toString());
                        user.put("firstName",_firstName.getText().toString());
                        user.put("lastName",_lastName.getText().toString());

                        user.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {

                                    _regemail.setText("");
                                    _regpass.setText("");
                                    _confpass.setText("");
                                    _firstName.setText("");
                                    _lastName.setText("");
                                    Toast.makeText(MainActivity.this,"Registration Successful", Toast.LENGTH_SHORT).show();
                                    Log.i("Signup", "Successful");

                                } else {

                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

                    }
                }
            });
        }
    }


