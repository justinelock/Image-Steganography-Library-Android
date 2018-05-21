package com.ayush.steganographylibrary.Text;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.ayush.steganographylibrary.Utils.Crypto;
import com.ayush.steganographylibrary.Utils.Utility;
import com.ayush.steganographylibrary.Utils.Zipping;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * In this class all those method in EncodeDecode class are used to encode secret message in image.
 * All the tasks will run in background.
 */
public class TextEncoding extends AsyncTask<TextSteganography, Integer, TextSteganography> {

    //Tag for Log
    private static String TAG = TextEncoding.class.getName();

    Activity activity;

    private int maximumProgress;

    private ProgressDialog progressDialog;

    public TextEncoding(Activity activity) {
        super();
        this.activity = activity;
        this.progressDialog = new ProgressDialog(activity);
    }

    //setting progress dialog if wanted
    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    //pre execution of method
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //setting parameters of progress dialog
        if (progressDialog != null){
            progressDialog.setMessage("Loading, Please Wait...");
            progressDialog.setTitle("Encoding Message");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
        }
    }

    @Override
    protected void onPostExecute(TextSteganography textStegnography) {
        super.onPostExecute(textStegnography);

        //dismiss progress dialog
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        //Updating progress dialog
        if (progressDialog != null){
            progressDialog.show();
            progressDialog.incrementProgressBy(values[0]);
        }
    }

    @Override
    protected TextSteganography doInBackground(TextSteganography... textSteganographies) {

        //making result object
        TextSteganography result = new TextSteganography();

        Crypto encryption = null;

        maximumProgress = 0;

        if (textSteganographies.length > 0){

            TextSteganography textStegnography = textSteganographies[0];

            //If it is not already encoded

            if (!textStegnography.isEncoded()){
                //getting image bitmap
                Bitmap bitmap = textStegnography.getImage();

                //getting height and width of original image
                int originalHeight = bitmap.getHeight();
                int originalWidth = bitmap.getWidth();

                //splitting bitmap
                List<Bitmap> src_list = Utility.splitImage(bitmap);

                //encoding encrypted compressed message into image
                List<Bitmap> encoded_list = EncodeDecode.encodeMessage(src_list, textStegnography.getEncrypted_message(), new EncodeDecode.ProgressHandler() {

                    //Progress Handler
                    @Override
                    public void setTotal(int tot) {
                        maximumProgress = tot;
                        progressDialog.setMax(maximumProgress);
                        Log.d(TAG, "Total Length : " + tot);
                    }

                    @Override
                    public void increment(int inc) {
                        publishProgress(inc);
                    }

                    @Override
                    public void finished() {
                        Log.d(TAG, "Message Encoding end....");
                        progressDialog.setIndeterminate(true);
                        progressDialog.setTitle("Merging images...");
                    }
                });

                //free Memory
                for (Bitmap bitm : src_list)
                    bitm.recycle();

                //Java Garbage collector
                System.gc();

                //merging the split encoded image
                Bitmap srcEncoded = Utility.mergeImage(encoded_list, originalHeight, originalWidth);

                //Image encoded = true
                textStegnography.setEncoded(true);

                //Setting encrypted image to result
                result.setEncrypted_image(srcEncoded);
                result.setEncoded(true);
            }
            else
                Log.d(TAG, "Already Encoded");
        }

        return result;
    }
}
