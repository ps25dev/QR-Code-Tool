package com.example.qrcodetool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private final String TAG = "MainActivity";
    private ImageButton imageButtonScan;
    private ImageButton imageButtonCopy;
    private TextView textViewResult;
    private ZXingScannerView mScannerView = null;
    private ViewGroup scanButtonViewGroup;
    private String qrCodeResult = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScannerView = new ZXingScannerView(this);   //initialize the scanner view
        imageButtonScan = findViewById(R.id.imageButtonScan);
        imageButtonCopy = findViewById(R.id.imageButtonCopy);
        textViewResult = findViewById(R.id.textViewResult);
        scanButtonViewGroup = (ViewGroup) ((ViewGroup) (findViewById(android.R.id.content))).getChildAt(0);
        initMainActivityUiComponents();
    }


    private void initMainActivityUiComponents(){
        imageButtonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCameraPermissionGranted()){
                    scanQRCode();
                }
                else{
                    requestCameraPermission();
                }
            }
        });

        imageButtonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(qrCodeResult!=null && !qrCodeResult.equals("")){
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Qr result", qrCodeResult);
                    clipboard.setPrimaryClip(clip);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Result not available.\nscan and try again!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public void handleResult(Result result) {
        Log.d(TAG, "scan Completed. Result="+result.getText());
        mScannerView.stopCamera();
        mScannerView.stopCameraPreview();
        setContentView(scanButtonViewGroup);
        qrCodeResult = result.getText();
        textViewResult.setText(qrCodeResult);
        //Toast.makeText(this,""+result.getText(),Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        initMainActivityUiComponents();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();
        mScannerView.stopCameraPreview();
        setContentView(scanButtonViewGroup);
    }

    @Override
    public void onBackPressed() {
        if((ViewGroup) ((ViewGroup) (findViewById(android.R.id.content))).getChildAt(0) == mScannerView){
            mScannerView.stopCamera();
            mScannerView.stopCameraPreview();
            setContentView(scanButtonViewGroup);
            qrCodeResult = "";
        }
        else {
            super.onBackPressed();
        }
    }

    private boolean isCameraPermissionGranted(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }


    private void requestCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG,"Permission not available requesting Permission!");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},MY_PERMISSION_REQUEST_USE_CAMERA);
        } else {
            Log.d(TAG, "Permission has already been granted.");
            scanQRCode();
        }
    }


    private void scanQRCode(){
        qrCodeResult = "";
        setContentView(mScannerView);
        mScannerView.setResultHandler(this); // Register handler for scan results.
        mScannerView.startCamera();         // Start camera
    }


    private final int MY_PERMISSION_REQUEST_USE_CAMERA = 0x00AF;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_USE_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission was granted.");
                    scanQRCode();
                } else {
                    Log.d(TAG, "permission is not Granted.");
                }
                return;
            }
        }
    }


}