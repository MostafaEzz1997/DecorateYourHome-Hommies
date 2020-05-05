package com.example.socketandroidsecondtrial;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends AsyncTask<Void, Void, Void>
{   String type;
    String ipAddress="192.168.56.1";
    Uri imageUri;
    Client(String t, Uri imageUr ){
        type=t;
        imageUri=imageUr;
        Log.i("TAG","constructor is called");
    }
    @Override
    protected Void doInBackground(Void... arg0) {
        Log.i("TAG","function is entered");
        if(type=="shutdown"){
            try {
                Log.i("TAG","shutdown");
                Socket client = new Socket(ipAddress,9999);
                OutputStream toServer=client.getOutputStream();
                DataOutputStream out = new DataOutputStream(toServer);
                //final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                File file = new File(String.valueOf(imageUri));
                FileInputStream input = new FileInputStream(file);
                int size = input.available();

                byte[] data = new byte[size];
                input.read(data);
                out.writeInt(size);
                out.write(data);
                out.flush();
                out.close();
                input.close();
                //out.writeBytes("Shutdown");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else if(type=="restart"){
            try {
                Log.i("TAG","restart");
                Socket client = new Socket(ipAddress,9999);
                OutputStream toServer=client.getOutputStream();
                DataOutputStream out = new DataOutputStream(toServer);
                out.writeBytes("restart");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else if(type=="music"){
            try {
                Log.i("TAG","music");
                Socket client = new Socket(ipAddress,9999);
                OutputStream toServer=client.getOutputStream();
                DataOutputStream out = new DataOutputStream(toServer);
                out.writeBytes("music");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }
}
