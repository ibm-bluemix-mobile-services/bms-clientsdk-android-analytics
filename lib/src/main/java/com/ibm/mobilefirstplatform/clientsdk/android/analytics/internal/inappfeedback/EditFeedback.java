package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inappfeedback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.mobilefirstplatform.clientsdk.android.R;
import com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.MFPAnalyticsActivityLifecycleListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EditFeedback extends Activity{

    private ImageView imageView;
    private ImageButton drawButton;
    private ImageButton eraseButton;
    private ImageButton commentButton;
    private EditText editText;
    private TextView commentTextLable;
    private View editGroup;

    private Bitmap bitmapMaster;
    private Canvas canvasMaster;

    private int prvX, prvY;
    private Paint paintDraw;
    private Paint paintBlur;

    private boolean drawToggle;
    private boolean eraseToggle;
    private boolean commentToggle;
    private boolean allowClick;
    private boolean fileSaved;
    private int count;
    private String instanceName;
    private int imageWidth;
    private int imageHeight;
    private List<String> commentList;
    private boolean isEdited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_feedback);

        Button sendButton;

        count = 0;
        commentList = new ArrayList<>();
        isEdited = false;
        fileSaved = false;

        drawToggle=false;
        eraseToggle=false;
        commentToggle=false;
        allowClick =true;

        imageView = (ImageView) findViewById(R.id.imageView);
        drawButton = (ImageButton) findViewById(R.id.drawButton);
        eraseButton = (ImageButton) findViewById(R.id.eraseButton);
        commentButton = (ImageButton) findViewById(R.id.commentButton);
        editText = (EditText) findViewById(R.id.edit_text);
        commentTextLable = (TextView) findViewById(R.id.comment_text);
        editGroup = findViewById(R.id.textLayout);

        editGroup.setVisibility(View.GONE);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        //getWindow().setLayout((int)(width*0.9),(int)(height*0.9));
        getWindow().setLayout(width,height);

        imageView.getLayoutParams().height = (int)(getWindowManager().getDefaultDisplay().getHeight() * 0.90);
        imageView.getLayoutParams().width = (int)(getWindowManager().getDefaultDisplay().getWidth() * 0.95);

        Bundle extras = getIntent().getExtras();
        instanceName = extras.getString("imagename");
        imageWidth = extras.getInt("imageWidth");
        imageHeight = extras.getInt("imageHeight");
        loadImageFromLocalStore(Utility.getImageFileName(instanceName));

        paintDraw = new Paint();
        paintDraw.setStyle(Paint.Style.FILL);
        paintDraw.setColor(Color.parseColor("#FF9052"));
        paintDraw.setStrokeWidth(10);

        paintBlur = new Paint();
        paintBlur.setStyle(Paint.Style.FILL);
        paintBlur.setColor(Color.parseColor("#7998a8"));
        paintBlur.setStrokeWidth(50);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    String comment = editText.getText().toString();
                    commentList.add(comment);

                    allowClick =true;
                    editGroup.setVisibility(View.GONE);
                    handled = true;
                }
                return handled;
            }
        });

        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setText("Send");

        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
            if(allowClick) {
                if (isEdited) {
                    saveImageAndComment();
                    sendAppFeedback();
                } else {
                    //Stay or Dissmiss
                    AlertDialog alertDialog = new AlertDialog.Builder(EditFeedback.this).create();
                    alertDialog.setTitle("Send Feedback");
                    alertDialog.setMessage("Nothing to send, since no comments added. Do you want to exit?");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No, Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes, Exit",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Utility.discardFeedbackFiles(instanceName);
                                    EditFeedback.this.finish();
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }
            }
            }
        });

        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Change the icon
                if(!drawToggle && allowClick){
                    drawButton.setImageResource(R.drawable.draw_yellow);
                    drawToggle=true;
                    eraseButton.setImageResource(R.drawable.erase_black);
                    eraseToggle=false;
                    commentButton.setImageResource(R.drawable.comment_black);
                    commentToggle=false;
                }else{
                    drawButton.setImageResource(R.drawable.draw_black);
                    drawToggle=false;
                }
            }
        });

        eraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Change the icon
                if(!eraseToggle && allowClick){
                    eraseButton.setImageResource(R.drawable.erase_yellow);
                    eraseToggle=true;
                    drawButton.setImageResource(R.drawable.draw_black);
                    drawToggle=false;
                    commentButton.setImageResource(R.drawable.comment_black);
                    commentToggle=false;
                }else{
                    eraseButton.setImageResource(R.drawable.erase_black);
                    eraseToggle=false;
                }
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Change the icon
                if(!commentToggle && allowClick){
                    commentButton.setImageResource(R.drawable.comment_yellow);
                    commentToggle=true;
                    drawButton.setImageResource(R.drawable.draw_black);
                    drawToggle=false;
                    eraseButton.setImageResource(R.drawable.erase_black);
                    eraseToggle=false;
                }else{
                    if(allowClick){
                        commentButton.setImageResource(R.drawable.comment_black);
                        commentToggle=false;
                    }
                }
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(drawToggle || eraseToggle || commentToggle ){
                    int action = event.getAction();
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            prvX = x;
                            prvY = y;
                            drawOnScreenshot((ImageView) v, bitmapMaster, prvX, prvY, x, y);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if(!commentToggle) drawOnScreenshot((ImageView) v, bitmapMaster, prvX, prvY, x, y);
                            prvX = x;
                            prvY = y;
                            break;
                        case MotionEvent.ACTION_UP:
                            if(!commentToggle) drawOnScreenshot((ImageView) v, bitmapMaster, prvX, prvY, x, y);
                            break;
                        default:
                            break;
                    }
                }
                return true;
            }
        });
    }

    private void saveImageAndComment(){
        if(!fileSaved){
            Utility.saveIamgeToLocalStore(bitmapMaster, Utility.getImageFileName(instanceName));

            //ScreenFeedback.json
            JSONObject screenFeedBackJSON = new JSONObject();

            String screenName = Utility.getScreenName(instanceName);
            String deviceID = Utility.getDeviceID(MFPInAppFeedBackListner.getContext());
            String timeCreated  = Utility.getTimeCreated(instanceName);
            String id = deviceID+ "_" + screenName +"_" + timeCreated;
            String jsonFileName = Utility.getJSONfileName(instanceName);

            JSONArray commentJSON = new JSONArray();
            for(String comment: commentList){
                commentJSON.put(comment);
            }

            try {
                screenFeedBackJSON.put("id", id);
                //screenFeedBackJSON.put("timeSent", null);
                screenFeedBackJSON.put("comments", commentJSON);
                screenFeedBackJSON.put("screenName", instanceName);
                screenFeedBackJSON.put("screenWidth", imageWidth);
                screenFeedBackJSON.put("screenHeight", imageHeight);
                screenFeedBackJSON.put("sessionID", MFPAnalyticsActivityLifecycleListener.getAppSessionID());
                screenFeedBackJSON.put("username", MFPInAppFeedBackListner.getUserIdentity());
            } catch (JSONException e) {
                //No chance of getting here
            }

            //AppFeedBackSummary.json
            JSONObject appFeedBacksummaryJSON = new JSONObject();
            try{
                String afbs = Utility.convertFileToString("AppFeedBackSummary.json");
                if(!"".equals(afbs)){
                    appFeedBacksummaryJSON = new JSONObject(afbs);
                    JSONArray savedArray = (JSONArray) appFeedBacksummaryJSON.get("saved");
                    savedArray.put(instanceName);
                }else{
                    //create new instance
                    appFeedBacksummaryJSON.put("saved", new JSONArray().put(instanceName));
                    appFeedBacksummaryJSON.put("send", new JSONObject());
                }
            }catch (JSONException je){
                //
            }

            Utility.addDataToFile(jsonFileName, screenFeedBackJSON.toString(), false);
            Utility.addDataToFile("AppFeedBackSummary.json", appFeedBacksummaryJSON.toString(), false);
            fileSaved=true;
        }
    }

    private void drawOnScreenshot(ImageView iv, Bitmap bm,
                                  float x0, float y0, float x, float y){
        if(x<0 || y<0 || x > iv.getWidth() || y > iv.getHeight()){
            //outside ImageView
            return;
        }else{
            isEdited = true;
            float ratioWidth = (float)bm.getWidth()/(float)iv.getWidth();
            float ratioHeight = (float)bm.getHeight()/(float)iv.getHeight();

            if(commentToggle && allowClick){
                allowClick =false;

                Paint pCircle = new Paint();
                pCircle.setColor(Color.parseColor("#FF9052"));
                canvasMaster.drawCircle(x * ratioWidth, y * ratioHeight,  Utility.dipToPixels(getApplicationContext(),20), pCircle);

                int textSize = (int)Utility.dipToPixels(getApplicationContext(),20);
                int dp4 = (int)Utility.dipToPixels(getApplicationContext(),4);

                count +=1;
                String drawText = ""+(count);
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setTextSize(textSize);
                canvasMaster.drawText(drawText, x * ratioWidth-dp4, y * ratioHeight+dp4, paint);

                //Enable edittext
                commentTextLable.setText("Comment #"+count);
                editGroup.setVisibility(View.VISIBLE);
                editText.setText("");
            }else{
                canvasMaster.drawLine(
                        x0 * ratioWidth,
                        y0 * ratioHeight,
                        x * ratioWidth,
                        y * ratioHeight,
                        ((eraseToggle)? paintBlur: paintDraw));
            }
            imageView.invalidate();
        }
    }

    private void loadImageFromLocalStore(String imageFilename){
        Bitmap tempBitmap = Utility.getTempBitMap(this,imageFilename);
        bitmapMaster = Utility.getMasterBitMap(tempBitmap);
        canvasMaster = Utility.getCanvas(tempBitmap,bitmapMaster);
        imageView.setImageBitmap(bitmapMaster);
    }

    public void closeActivity(View v) {
        if(allowClick){
            if(isEdited){
                saveImageAndComment();
                AlertDialog alertDialog = new AlertDialog.Builder(EditFeedback.this).create();
                alertDialog.setTitle("Close Feedback");
                alertDialog.setMessage("Do you want to Send or Discard the Feedback before exit?");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Discard",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Utility.discardFeedbackFiles(instanceName);
                                EditFeedback.this.finish();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Send",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                sendAppFeedback();
                            }
                        });
                alertDialog.setCancelable(false);
                alertDialog.show();
            }else{
                Utility.discardFeedbackFiles(instanceName);
                EditFeedback.this.finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==200) {
            finish();
        }
    }

    private void sendAppFeedback(){
        SendAppFeedback.sendLogsToServer(true);
        finish();

        Toast toast = Toast.makeText(getApplicationContext(), "THANK YOU FOR THE FEEDBACK!", Toast.LENGTH_LONG);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(20);
        messageTextView.setTextColor(Color.BLACK);
        group.setBackgroundColor(Color.WHITE);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER, 0, 0);
        toast.show();
    }
}
