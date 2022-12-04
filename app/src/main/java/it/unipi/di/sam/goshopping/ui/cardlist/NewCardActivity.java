package it.unipi.di.sam.goshopping.ui.cardlist;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import it.unipi.di.sam.goshopping.AppMain;
import it.unipi.di.sam.goshopping.MainActivity;
import it.unipi.di.sam.goshopping.R;

public class NewCardActivity extends AppCompatActivity {

    private EditText barcodeET;
    private ImageView imageView;
    private String barcodeFormat;
    private TextView barcodeTV;
    private TextView cardName;
    private Button addCardBtn;
    private Button cancelBtn;
    private ImageButton btn_scan;
    private Button remCardBtn;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_new_card);

        addCardBtn = findViewById(R.id.add_card_btn);
        cancelBtn = findViewById(R.id.cancel_card_button);
        barcodeET = findViewById(R.id.new_card_edittext);
        imageView = findViewById(R.id.barcode_preview_image);
        barcodeTV = findViewById(R.id.barcode_text);
        btn_scan = findViewById(R.id.scan_btn);
        cardName = findViewById(R.id.card_name);
        remCardBtn = findViewById(R.id.rem_card_btn);
        radioGroup = (RadioGroup) findViewById(R.id.color_radio_group);


        Bundle b = getIntent().getExtras();
        if(b != null) { // edit card use case
            barcodeET.setText(b.getString("code"));
            cardName.setText(b.getString("name"));
            barcodeTV.setText(b.getString("code"));
            barcodeFormat = b.getString("format");
            color = b.getInt("color");

            String hexColor = String.format("#%06X",(0xFFFFFF & color));
            radioButton = (RadioButton) radioGroup.findViewWithTag(hexColor);
            if(radioButton != null) radioButton.setChecked(true);
            else Toast.makeText(this, R.string.edit_card_color_load_error, Toast.LENGTH_LONG).show();


            addCardBtn.setText(R.string.update);
            remCardBtn.setVisibility(View.VISIBLE);

            remCardBtn.setOnClickListener(view -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle(R.string.remove_card);
                builder.setMessage(getString(R.string.remove_card_confirm_message)+" "+b.getString("name"));
                builder.setPositiveButton(R.string.confirm, (dialogInterface, i) -> {
                    AppMain.getDb().removeCard(b.getInt("id"), b.getInt("rv_pos"));
                    dialogInterface.dismiss();
                    finish();
                }).setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss()).show();

            });

            // If imageView has been created generate immediately the barcode, otherwise wait until it is created
            if(imageView.isLaidOut())
                CLFragment.bcUtils.generateBarcodeImage(imageView, barcodeFormat, barcodeET.getText().toString());
            else
                imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        CLFragment.bcUtils.generateBarcodeImage(imageView, barcodeFormat, barcodeET.getText().toString());
                    }
                });
        }
        else { // new card use case
            addCardBtn.setText(R.string.add);
            remCardBtn.setVisibility(View.GONE);
        }

        // scan barcode with camera
        btn_scan.setOnClickListener(view -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt(getString(R.string.camera_scan_flash_hint));
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(CaptureAct.class);
            barLauncher.launch(options);

        });

        // unlocks update button
        radioGroup.setOnCheckedChangeListener((group, checkedId) ->
                addCardBtn.setEnabled(cardName.getText().toString().trim().length() > 0
                && barcodeET.getText().toString().trim().length() > 0));


        // go back
        cancelBtn.setOnClickListener(view -> finish());
        // add new card to database
        addCardBtn.setOnClickListener(view -> {
            radioButton = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
            color = radioButton.getButtonTintList().getDefaultColor(); // update color only if color radio button was found

            if (b == null) // add mode
                AppMain.getDb().addCard(cardName.getText().toString(), barcodeET.getText().toString(), barcodeFormat, color);
            else // edit mode/
                AppMain.getDb().updateCard(b.getInt("id"), cardName.getText().toString(), barcodeET.getText().toString(), barcodeFormat, color, b.getInt("rv_pos"));
            finish();
        });
        // barcode has changed
        barcodeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                addCardBtn.setEnabled(cardName.getText().toString().trim().length() > 0 // enable/disable button
                        && barcodeET.getText().toString().trim().length() > 0);
                if (barcodeET.length() == 0) { // EditText empty
                    imageView.setImageDrawable(null);
                    barcodeTV.setText("");
                } else if (barcodeET.hasFocus()) { // EditText not empty and modified by user (currently on focus)
                    barcodeFormat = BarcodeFormat.CODE_128.toString(); // sets default barcode format
                    CLFragment.bcUtils.generateBarcodeImage(imageView, barcodeFormat, barcodeET.getText().toString());
                    barcodeTV.setText(barcodeET.getText());
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        // name has changed
        cardName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                addCardBtn.setEnabled(cardName.getText().toString().trim().length() > 0
                        && barcodeET.getText().toString().trim().length() > 0);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

    }

    // Get barcode from camera activity
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null) {
            barcodeET.setText(result.getContents());
            barcodeFormat = result.getFormatName();
            barcodeTV.setText(result.getContents());
            CLFragment.bcUtils.generateBarcodeImage(imageView, barcodeFormat, barcodeET.getText().toString());
        }
    });



    @Override
    protected void onDestroy() { super.onDestroy(); }

    @Override
    protected void onPause() { super.onPause(); }

    @Override
    protected void onResume() { super.onResume(); }


}