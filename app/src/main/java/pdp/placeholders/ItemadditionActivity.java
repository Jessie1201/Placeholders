package pdp.placeholders;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** This activity performs the addition of new items.
 It uses Cloud Vision api for the images, otherwise it just adds items to the singleton list.
 Further reading: https://cloud.google.com/vision/docs/detecting-labels#vision-label-detection-java
 * */

public class ItemadditionActivity extends AppCompatActivity {
    private static final String CLOUD_VISION_API_KEY = "AIzaSyA6SKDhErSdOqFPP5RNhCtbquwArrku7q0";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";

    private static final String TAG = ItemadditionActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    private ProgressBar mProgressBar;
    private ImageView mMainImage;
    private EditText ETlabel;
    private TextView textview;

    // TODO: 13.3.2018 create assign box option

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemaddition);
        mProgressBar = (ProgressBar)findViewById(R.id.progressbar);
        ETlabel = (EditText)findViewById(R.id.ETitemName);
        mMainImage = (ImageView) findViewById(R.id.imageView);
        textview = (TextView)findViewById(R.id.textviewhelper);

        // TODO: 15.3.2018 create a search after user inputs item name 
        //Creates the time dropdown menu options
        String[] SpinnerList ={getString(R.string.short_term), getString(R.string.medium_term), getString(R.string.long_term)};
        Object[] boxes =UserItems.getInstance().getBoxes().keySet().toArray();
        ArrayList<String> trimboxes =new ArrayList<>();
        trimboxes.add("no box");
        for (Object box:boxes){
            trimboxes.add(box.toString());
        }
        trimboxes.add("New Box");
        String[] trimbox = trimboxes.toArray(new String[0]);
        ArrayAdapter<String> boxadapter = new ArrayAdapter<String>(this,R.layout.my_spinner_style, trimbox);
        boxadapter.setDropDownViewResource(R.layout.my_spinner_style);
        final Spinner boxSpinner = (Spinner)findViewById(R.id.targetbox);
        boxSpinner.setAdapter(boxadapter);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.my_spinner_style,SpinnerList);
        adapter.setDropDownViewResource(R.layout.my_spinner_style);
        final Spinner termSpinner = (Spinner)findViewById(R.id.targettime);
        termSpinner.setAdapter(adapter);


        Button btnNextItem = (Button)findViewById(R.id.btnNextItem);
        btnNextItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToList(termSpinner,boxSpinner);
                ETlabel.setText("");
                mMainImage.setImageResource(android.R.color.transparent);
            }
        });
        //Sets a function to the click on the Pick image button
        Button btnPickImage = (Button) findViewById(R.id.btnPickImage);
        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryOrCamera();
            }
        });
        Button btnDone = (Button)findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ETlabel.getText()!=null){
                    addItemToList(termSpinner, boxSpinner);}
                finish();
            }
        });
        //galleryOrCamera();
    }
    // Adds item to the singleton list
    // TODO: 15.3.2018 Change the date option into something more custommizable 
    private void addItemToList(Spinner termSpinner, Spinner boxSpinner) {
        String itemstring = ETlabel.getText().toString();
        if (itemstring.trim().length() > 0) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            if (termSpinner.getSelectedItem().toString() == getString(R.string.short_term)) {
                c.add(Calendar.DATE, 7);
            }
            if (termSpinner.getSelectedItem().toString() == getString(R.string.medium_term)) {
                c.add(Calendar.DATE, 30);
            }
            if (termSpinner.getSelectedItem().toString() == getString(R.string.long_term)) {
                c.add(Calendar.DATE, 364);
            }
            /*if(boxSpinner.getSelectedItem().toString() != "no box"){
                int a = boxSpinner.getSelectedItemPosition()-1;
                SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
                String d = formatter.format(c.getTime());
                UserItems.getInstance().removeBox(a);
                UserItems.getInstance().addBox(boxSpinner.getSelectedItem().toString(), itemstring, d,"update");
            }*/
            Calendar currentDate = Calendar.getInstance(); currentDate.setTime(new Date());
            UserItems.addToList(itemstring, c, currentDate);

            Toast.makeText(getApplicationContext(),"Item Added", Toast.LENGTH_LONG).show();
        }
    }
    // Creates the prompt when picking an image
    private void galleryOrCamera() { //creates a popup asking gallery or camera
        AlertDialog.Builder builder = new AlertDialog.Builder(ItemadditionActivity.this);
        builder
                .setMessage(R.string.dialog_select_prompt)
                .setPositiveButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openGallery();
                    }
                })
                .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startCamera();
                    }
                });
        builder.create().show();
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, GALLERY_IMAGE_REQUEST);
    }

    public void startCamera() {//starts the camera
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra("android.intent.extra.quickCapture",true);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName()
                    + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        } //once done moves to onActivityResult to get picture
    }

    public void onRequestPermissionsResult( //makes sure  that it does not crash if permissions do not exist
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    openGallery();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //once activities are done, they return a result
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case GALLERY_IMAGE_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    uploadImage(data.getData());
                    Toast.makeText(ItemadditionActivity.this,"image uri should be set",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ItemadditionActivity.this,"did not pick image",Toast.LENGTH_SHORT).show();
                    }
                break;
            case CAMERA_IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri photoUri = FileProvider.getUriForFile(this,getApplicationContext().getPackageName() + ".provider", getCameraFile());
                    uploadImage(photoUri);
                } else{Toast.makeText(ItemadditionActivity.this,"Pick image first",Toast.LENGTH_SHORT).show();}
                break;
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }
    /** Makes sure that the image that should be sent to cloud vision exists */
    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                640);
                callCloudVision(bitmap);
                mMainImage.setImageBitmap(bitmap);
                mMainImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mMainImage.setColorFilter(240);
                Toast.makeText(this, "Getting image content", Toast.LENGTH_LONG).show();

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }
    /** Scales down the image that needs to be sent.
     The rest of this file is for the computer vision
     */
    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        //Makes sure picture is right size for google vision api
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }
    /** This function does most of the vision tasks. */
    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        mProgressBar.setVisibility(View.VISIBLE);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {

                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setMaxResults(1);
                            add(labelDetection);
                            Feature logoDetection = new Feature();
                            logoDetection.setType("TEXT_DETECTION");
                            logoDetection.setMaxResults(1);
                            add(logoDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) { //once it recieves a result
                ETlabel.setText(result);
                if( result==null){Toast.makeText(getApplicationContext(), "could not detect",
                        Toast.LENGTH_LONG).show();}
                mProgressBar.setVisibility(View.GONE);

                textview.setText(result);
            }
        }.execute();
    }
/** converts the response from cloud vision into a string*/
    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = ""; String message1 = "";
        Double szbiggest = 0.0;Double sz2biggest = 0.0; int counter = 0;
        List<EntityAnnotation> labels1 = response.getResponses().get(0).getTextAnnotations();
        if (labels1 != null) { //gets the 2 biggest words in the picture
            double[] exes = {0,0,0,0}; double[] ys = {0,0,0,0};double sz =0; double[] helper = {0,0,0,0,0,0};
            for (EntityAnnotation label : labels1) {
                for (int i=0; i<4; i++){
                    exes[i] = label.getBoundingPoly().getVertices().get(i).getX();
                    ys[i] = label.getBoundingPoly().getVertices().get(i).getY();
                }
                helper[0] = Math.pow(exes[1]-exes[0],2);
                helper[1] = Math.pow(ys[1]-ys[0],2);
                helper[2] = Math.pow(exes[1]-exes[0],2);
                helper[3] = Math.pow(ys[1]-ys[0],2);
                helper[4] = Math.pow(helper[0]+helper[1],.5);
                helper[5] = Math.pow(helper[2]+helper[3],.5);
                sz = helper[4]*helper[5];
                if(sz>szbiggest && counter!=0 && sz>sz2biggest){
                    if(sz>szbiggest){
                        sz2biggest = szbiggest;
                        szbiggest = sz;
                        message1 = message;
                        message= String.format(Locale.US, "%s ", label.getDescription());
                    }else{
                        sz2biggest = sz;
                        message1 = String.format(Locale.US, "%s ", label.getDescription());
                    }
                }
                counter += 1;
            }
        }
        message = message+message1;
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) { //gets the first label to the
            for (EntityAnnotation label : labels) {
                if (label.getScore()>0.71 && label.getDescription().contains("product")==false){
                    message+= String.format(Locale.US, "%s ", label.getDescription());
                    }
            }
        } else {
            //message += " ";
        }
        return message;
    }

}

