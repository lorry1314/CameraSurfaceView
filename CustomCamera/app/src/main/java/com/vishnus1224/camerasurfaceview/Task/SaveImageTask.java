package com.vishnus1224.camerasurfaceview.Task;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.vishnus1224.camerasurfaceview.MainActivity;
import com.vishnus1224.camerasurfaceview.Utility.FileHelper;

import static com.vishnus1224.camerasurfaceview.Utility.Constant.*;

/**
 * Created by vishnu on 08/07/15.
 */
public class SaveImageTask extends AsyncTask<Bitmap, Void, FileSaveStatus> {

    private Context context;

    public SaveImageTask(Context context){
        this.context = context;
    }

    @Override
    protected FileSaveStatus doInBackground(Bitmap... params) {

        return FileHelper.saveFile(context, MEDIA_TYPE_IMAGE, params[0]);
    }

    @Override
    protected void onPostExecute(FileSaveStatus fileSaveStatus) {
        if(context instanceof MainActivity){
            ((MainActivity)context).fileSaveComplete(fileSaveStatus);
        }
    }
}
