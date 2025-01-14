package com.ailuoku6.golib;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.ailuoku6.golib.Model.Login_State;
import com.ailuoku6.golib.Model.userInfo;
import com.ailuoku6.golib.server.Login;
import com.ailuoku6.golib.server.getPatcha;
import com.google.gson.Gson;
import com.jude.swipbackhelper.SwipeBackHelper;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText user_name,password,vericode;
    private ImageView Verimage;
    private Button login;
    private ProgressDialog progressDialog;

    private Login login_;
    private getPatcha patcha;
    //private Map<String,String> cookies = new HashMap<>();
    //private Login_State loginState;
    private final int UPDATA_PATCHA = 1;
    private final int JUDGE_STATE = 2;
    //private Context context;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what){
                case UPDATA_PATCHA:
                    Verimage.setImageBitmap((Bitmap) msg.obj);
                    break;
                case JUDGE_STATE:
                    Judge_state((Login_State) msg.obj);
                    break;
                default:break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SwipeBackHelper.onCreate(this);
        SwipeBackHelper.getCurrentPage(this)//获取当前页面
                .setSwipeBackEnable(true)//设置是否可滑动
                .setSwipeEdge(100)//可滑动的范围。px。200表示为左边200px的屏幕
                .setSwipeEdgePercent(0.1f)//可滑动的范围。百分比。0.2表示为左边20%的屏幕
                .setSwipeSensitivity(0.5f)//对横向滑动手势的敏感程度。0为迟钝 1为敏感
                .setClosePercent(0.8f);//触发关闭Activity百分比
        setContentView(R.layout.activity_login);
        toolbar = (Toolbar) findViewById(R.id.Login_toolbar);
        toolbar.setTitle("登陆");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        user_name = (EditText) findViewById(R.id.user_name);
        password = (EditText) findViewById(R.id.password);
        vericode = (EditText) findViewById(R.id.vericode_input);
        Verimage = (ImageView) findViewById(R.id.vericode);
        login = (Button) findViewById(R.id.login);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);

        CookiesManage.IsLoged = false;
        cleanCookies();

        login_ = new Login();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                progressDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Login_State loginState = login_.LogIn(user_name.getText().toString(),password.getText().toString(),vericode.getText().toString(),CookiesManage.cookies);
                            Message message = new Message();
                            message.what = JUDGE_STATE;
                            message.obj = loginState;
                            handler.sendMessage(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                            if(progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                        }
                    }
                }).start();

            }
        });

        Verimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Updata_patcha();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            InitData();
            ReadData();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SwipeBackHelper.onPostCreate(this);
    }

    @Override
    protected void onDestroy() {
//        Intent intent = new Intent();
//        //intent.putExtra("name",userInfo.userName);
//        setResult(0,intent);
        Log.d("cookies", "login onDestroy: ");
        super.onDestroy();
        SwipeBackHelper.onDestroy(this);
    }

    public void InitData() throws InterruptedException {

        patcha = new getPatcha();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    byte[] data;
                    data = patcha.FirstUpdataCha();

                    if (data!=null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Message message = new Message();
                        message.what = UPDATA_PATCHA;
                        message.obj = bitmap;
                        handler.sendMessage(message);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public void Updata_patcha(){

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    byte[] data;
                    data = patcha.updataCha(CookiesManage.cookies);

                    if (data!=null){
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Message message = new Message();
                        message.what = UPDATA_PATCHA;
                        message.obj = bitmap;
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void Judge_state(Login_State loginState){

        progressDialog.dismiss();

        if(loginState.getIslog()){
            Snackbar.make(findViewById(R.id.LOGIN_ACTIVITY), "登录成功!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            CookiesManage.IsLoged = true;
            SaveData();
            Intent intent = new Intent();
            intent.putExtra("name",userInfo.userName);
            setResult(1,intent);
            finish();
        }else {
            Snackbar.make(findViewById(R.id.LOGIN_ACTIVITY), loginState.getERRORINFO(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
//        Snackbar.make(this, loginState.getERRORINFO(), Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
    }

    public void cleanCookies(){
        SharedPreferences sp = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        try {
            editor.remove("cookies");
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.apply();
    }

    public void SaveData(){
        SharedPreferences sp = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        try {
            Gson gson = new Gson();
            String json = gson.toJson(CookiesManage.cookies);
            editor.putString("cookies", json);
            editor.putString("name", userInfo.userName);//在做Login的时候已经给userInfo.userName赋值
            editor.putString("user_name",user_name.getText().toString());
            editor.putString("password",password.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.apply();
    }

    public void ReadData(){
        SharedPreferences sp = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        user_name.setText(sp.getString("user_name",""));
        password.setText(sp.getString("password",""));
    }
}
