package pdp.placeholders;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telecom.CallScreeningService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    private static final String DISPLAY_DATE_FORMAT ="dd MMMM yyyy";

    private static final String SAVED_DATE_FORMAT ="yyyy-MM-dd";

    private ProgressBar mProgressBar;
    private ImageView mMainImage;
    private EditText ETlabel;
    private WifiReceiver receiverWifi;
    private EditText dateText;
    private EditText boxtext;

    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1001;
    int netId;
    DatePicker datePicker;


    // TODO: 13.3.2018 create assign box option

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemaddition);
        mProgressBar = (ProgressBar)findViewById(R.id.progressbar);
        ETlabel = (EditText)findViewById(R.id.ETitemName);
        mMainImage = (ImageView) findViewById(R.id.imageView);
        dateText = (EditText)findViewById(R.id.datetext);
        DateFormat format = new SimpleDateFormat(DISPLAY_DATE_FORMAT);
        dateText.setText(format.format(new Date()));
        if(Objects.requireNonNull(getIntent().getExtras()).getBoolean("StartCamera",false)){
            startCamera();
        }

        try {
            String item = getIntent().getStringExtra("item");
            final String[] txt1 =item.replace(UserItems.DELIMBOX," - ").split(UserItems.DELIMLIST);
            ETlabel.setText(txt1[0]);
            DateFormat sformat = new SimpleDateFormat(SAVED_DATE_FORMAT);
            Date editDate= sformat.parse(txt1[1]);
            dateText.setText(format.format(editDate));
        }catch (Exception e){
            Log.d(TAG, "onCreate: could not find intent extras");
        }
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
        final String[] trimbox = trimboxes.toArray(new String[0]);
        final ArrayAdapter<String> boxadapter = new ArrayAdapter<String>(this,R.layout.my_spinner_style, trimbox);
        boxadapter.setDropDownViewResource(R.layout.my_spinner_style);
        final Spinner boxSpinner = (Spinner)findViewById(R.id.targetbox);
        boxSpinner.setAdapter(boxadapter);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.my_spinner_style,SpinnerList);
        adapter.setDropDownViewResource(R.layout.my_spinner_style);
        boxSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==trimbox.length-1){
                    AlertDialog.Builder builder = new AlertDialog.Builder(ItemadditionActivity.this);
                    builder
                            .setMessage(R.string.dialog_createbox)
                            .setPositiveButton(R.string.signUpAccept, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                                PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
                                        //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method

                                    }else{
                                        //WifiManager wifimanager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                        //registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                                        //wifimanager.startScan();
                                    }

                                }
                            })
                            .setNegativeButton(R.string.signUpDeny, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    boxSpinner.setSelection(0);
                                }
                            });
                    builder.create().show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mydatepickerdialog();
        }});
        FrameLayout dateFrame = (FrameLayout)findViewById(R.id.dateframe);
        dateFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mydatepickerdialog();
            }
        });
        boxtext = (EditText)findViewById(R.id.boxtext);
        boxtext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myBoxPickerDialog();
                WifiReceiver.helperWifi(ItemadditionActivity.this);
                //receiverWifi= new WifiReceiver();
                //WifiManager wifimanager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            }
        });



        ImageView btnNextItem = (ImageView)findViewById(R.id.btnNextItem);
        btnNextItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToList(boxtext.getText().toString());
                boxadapter.notifyDataSetChanged();
                ETlabel.setText("");
                mMainImage.setImageResource(android.R.color.transparent);
                startCamera();
            }
        });/*
        //Sets a function to the click on the Pick image button
        Button btnPickImage = (Button) findViewById(R.id.btnPickImage);
        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryOrCamera();
            }
        });*/
        Button btnDone = (Button)findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ETlabel.getText()!=null){
                    addItemToList(boxtext.getText().toString());
                    if (getIntent().getStringExtra("item")!=null){
                        UserItems.removeItem(getIntent().getStringExtra("item"));
                    }
                }
                finish();
            }
        });
        Button btnBack = (Button)findViewById(R.id.btnback);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //galleryOrCamera();
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
    // Adds item to the singleton list
    // TODO: 15.3.2018 Change the date option into something more custommizable
    private void addItemToList(String boxSpinner) {
        String itemstring = ETlabel.getText().toString();
        if (itemstring.trim().length() > 0) {
            // TODO: 24.3.2018 replace if statements with values from datepicker
            Calendar c = Calendar.getInstance();
            DateFormat format = new SimpleDateFormat(DISPLAY_DATE_FORMAT);
            try {
                Date date = format.parse(dateText.getText().toString());
                c.setTime(date);
                //c.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                Calendar currentDate = Calendar.getInstance(); currentDate.setTime(new Date());
                UserItems.addToList(itemstring, c, currentDate);
                if(boxSpinner != "no box" && boxSpinner!="" && boxSpinner!="Bread"){
                    String[] a = boxSpinner.split(UserItems.DASH);
                    SimpleDateFormat formatter = new SimpleDateFormat(SAVED_DATE_FORMAT);
                    User.Box newbox = new User.Box(itemstring, formatter.format((Date)c.getTime()),"value" );
                    UserItems.addBox(a[0],newbox);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }


            Snackbar sb = Snackbar.make(getWindow().getDecorView().getRootView(),"Item Added",Snackbar.LENGTH_LONG);
            sb.getView().setBackgroundResource(R.color.myGreen);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)sb.getView().getLayoutParams();
            params.gravity = Gravity.TOP;
            sb.getView().setLayoutParams(params);
            sb.show();


            Toast.makeText(getApplicationContext(),itemstring+" has been successfully added!", Toast.LENGTH_LONG).show();
        }
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
            case PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION:{
                if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Do something with granted permission
                 WifiReceiver.helperWifi(ItemadditionActivity.this);
                }
            }break;
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
    @SuppressLint("StaticFieldLeak")
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

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseHelper.getArrayList(null,null);
        //unregisterReceiver(receiverWifi);
        //WifiManager wifimanager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //wifimanager.removeNetwork(netId);

    }


    private void mydatepickerdialog() {
        LayoutInflater inflater = (LayoutInflater)getLayoutInflater();
        View customdp = inflater.inflate(R.layout.customdatepicker,null);
        final DatePicker dp =customdp.findViewById(R.id.setdate);
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.BottomOptionsDialogTheme);
        builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int year = dp.getYear();
                int month = dp.getMonth();
                int dayOfMonth = dp.getDayOfMonth();
                DateFormat format = new SimpleDateFormat(DISPLAY_DATE_FORMAT);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DATE, dayOfMonth);
                Date date = calendar.getTime();
                String a = format.format(date);
                dateText.setText(a);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setView(customdp);
        AlertDialog dg = builder.create();

        dg.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams wlp =  dg.getWindow().getAttributes();
        wlp.x=0;
        wlp.gravity=Gravity.BOTTOM;
        wlp.verticalMargin=-0;
        dg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dg.getWindow().setAttributes(wlp);
        dg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 2f);
                negativeButton.setLayoutParams(params);
                positiveButton.setLayoutParams(params);

                negativeButton.invalidate();
                positiveButton.invalidate();
            }
        });
        dg.show();
    }
    private void myBoxPickerDialog() {
        final TextView boxtext = findViewById(R.id.boxtext);
        final CharSequence[] myBoxes = UserItems.getBoxes().keySet().toArray(new CharSequence[0]);
        int checkedItem = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(ItemadditionActivity.this);
        builder.setSingleChoiceItems(myBoxes, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boxtext.setText(myBoxes[which]);


            }
        });

        AlertDialog dg = builder.create();
/*
        dg.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams wlp =  dg.getWindow().getAttributes();
        wlp.x=0;
        wlp.gravity=Gravity.BOTTOM;
        wlp.verticalMargin=-0;
        dg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dg.getWindow().setAttributes(wlp);
        dg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 2f);
                negativeButton.setLayoutParams(params);
                positiveButton.setLayoutParams(params);

                negativeButton.invalidate();
                positiveButton.invalidate();
            }
        });*/
        dg.show();
    }
}


