package it.unipi.di.sam.goshopping.ui.ficardlist;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Intents;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;



import it.unipi.di.sam.goshopping.R;

public class NewCardActivity extends AppCompatActivity {


    private EditText barcodeET;
    private ImageView imageView;
    private final String manBarcodeFormat = BarcodeFormat.CODE_128.toString();
    private String autoBarcodeFormat;
    private TextView barcodeTV;
    private Boolean fromCamera = false;

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide();
        setContentView(R.layout.activity_new_card);

        Button addCardBtn = (Button) findViewById(R.id.add_card_btn);
        Button cancelBtn = (Button) findViewById(R.id.cancel_card_button);
        barcodeET = findViewById(R.id.new_card_edittext);
        imageView = findViewById(R.id.barcode_preview_image);
        barcodeTV = findViewById(R.id.barcode_text);
        Button btn_scan = (Button) findViewById(R.id.scan_btn);


        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScanOptions options = new ScanOptions();
                options.setPrompt("Volume up to flash on");
                options.setBeepEnabled(true);
                options.setOrientationLocked(true);
                options.setCaptureActivity(CaptureAct.class);
                barLauncher.launch(options);

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        addCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: add to database
            }

        });

        barcodeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(barcodeET.length()==0) {
                    imageView.setImageDrawable(null);
                    barcodeTV.setText("");

                } else
                    if(!fromCamera) {
                        generateBarcodeImage(imageView, manBarcodeFormat);
                        barcodeTV.setText(barcodeET.getText());
                    }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null) {
            fromCamera = true;
            barcodeET.setText(result.getContents());
            fromCamera = false;
            autoBarcodeFormat = result.getFormatName();
            barcodeTV.setText(result.getContents());
            generateBarcodeImage(imageView, autoBarcodeFormat);

            // Alert dialog
               /*
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    dialogInterface.dismiss();
                }
            }).show();
             */
        }
    });

    public void generateBarcodeImage(View view, String barcodeFormat) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            // TODO: check code to generate is ok with barcodeformat
            BitMatrix bitMatrix = multiFormatWriter.encode(barcodeET.getText().toString(), BarcodeFormat.valueOf(barcodeFormat), imageView.getWidth(), imageView.getHeight());
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            //Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(),imageView.getHeight(),Bitmap.Config.RGB_565);

            imageView.setImageBitmap(bitmap);
        } //catch (WriterException e) {

    //    }
        catch (WriterException e) {
            Log.e("errorcode", "caught exception. e.getCause(): "+e.getCause()+" | e.getMessage(): "+e.getMessage());
            //e.printStackTrace();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}