package it.unipi.di.sam.goshopping.ui.cardlist;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;


public class BarcodeUtils extends Activity {

    public static class postImage implements Runnable {
        private Bitmap bitmap;
        private ImageView imageView;
        public postImage(ImageView imageView, Bitmap bitmap) {
            this.bitmap = bitmap;
            this.imageView = imageView;
        }

        @Override
        public void run() {
            if(bitmap!=null) {
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setImageDrawable(null);
                imageView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private int h,w;

    public void generateBarcodeImage(ImageView imageView, String barcodeFormat, String code) {

        Thread T = new Thread(new Runnable() {
            @Override
            public void run() {
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

                Log.e("logging", "w: " + w + " | h: " + h);
                try {
                    if (imageView.getMeasuredHeight() == 0 || imageView.getMeasuredWidth() == 0)
                        throw new Exception("Barcode imageView is not yet laid out");
                    BitMatrix bitMatrix = multiFormatWriter.encode(code, BarcodeFormat.valueOf(barcodeFormat), imageView.getMeasuredWidth(), imageView.getMeasuredHeight());
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                    runOnUiThread(new postImage(imageView, bitmap));
                } catch (Exception e) {
                    Log.e("errorcode", "Error: " + e.getMessage());
                    runOnUiThread(new postImage(imageView, null));
                }
            }
        });
        T.start();

    }

}
