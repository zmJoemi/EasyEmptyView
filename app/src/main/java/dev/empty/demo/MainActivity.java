package dev.empty.demo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import dev.joemi.emptyview.EmptyView;

public class MainActivity extends AppCompatActivity implements EmptyView.OnClickListener{

    private EmptyView emptyView;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==1){
                emptyView.setStatus(EmptyView.HIDE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emptyView= (EmptyView) findViewById(R.id.empty);
        emptyView.setOnViewClick(this);
        if (hasInternet())
            handler.sendEmptyMessageDelayed(1,2000);
        else
            emptyView.setStatus(EmptyView.OFFLINE);
    }

    public boolean hasInternet() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isAvailable() && info.isConnected();
    }

    public void emptyClick(View view){
        emptyView.setStatus(EmptyView.EMPTY);

    }

    public void errorClick(View view){
        emptyView.setStatus(EmptyView.ERROR);
    }

    public void netErrorClick(View view){
        emptyView.setStatus(EmptyView.OFFLINE);
    }

    @Override
    public void onViewClick() {
        handler.sendEmptyMessageDelayed(1,2000);
    }
}
