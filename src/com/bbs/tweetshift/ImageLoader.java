package com.bbs.tweetshift;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.WindowManager;
import android.widget.ImageView;

public class ImageLoader {
    MemoryCache memoryCache=new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService; 
    boolean isBackground;
    Context context;
    
    public ImageLoader(Context context){
        fileCache=new FileCache(context);
        this.context = context;
        executorService=Executors.newFixedThreadPool(5);
    }
    
    final int stub_id=R.drawable.stub;
    public void DisplayImage(String url, ImageView imageView)
    {
		imageView.setImageResource(stub_id);
        imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
        }
        else
        {
            queuePhoto(url, imageView);
        }
    }
        
    
    public boolean isBackground() {
		return isBackground;
	}


	public void setBackground(boolean isBackground) {
		this.isBackground = isBackground;
	}


	private void queuePhoto(String url, ImageView imageView)
    {
        PhotoToLoad p=new PhotoToLoad(url, imageView);
//        executorService.submit(new PhotosLoader(p));
        new PhotosLoader(p).execute("");
    }
    
    public Bitmap getBitmap(String url) 
    {
        File f=fileCache.getFile(url);
        
        //from SD cache
        Bitmap b = decodeFile(f);
        if(b!=null)
            return b;
        
        //from web
        try {
            Bitmap bitmap=null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Exception ex){
           ex.printStackTrace();
           return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }
    
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageView i){
            url=u; 
            imageView=i;
        }
    }
    
    class OnVisibleListner implements OnSystemUiVisibilityChangeListener{

    	PhotoToLoad photoToLoad;
        Bitmap bitmap;
        
    	public OnVisibleListner(PhotoToLoad photoToLoad, Bitmap bitmap) {
    		this.photoToLoad = photoToLoad;
    		this.bitmap = bitmap;
		}
    	
		@SuppressWarnings("deprecation")
		@Override
		public void onSystemUiVisibilityChange(int visibility) {
			if(visibility == View.VISIBLE){
				try{
					Log.d("ImageLoader", String.valueOf(photoToLoad.imageView.getWidth()) );
					int imageViewWidth = 0;
					if( ! isBackground ){
						imageViewWidth = photoToLoad.imageView.getWidth();
					}else{
						WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
						Display display = wm.getDefaultDisplay();
						imageViewWidth = display.getWidth();
						if( bitmap.getWidth() < imageViewWidth ){
							imageViewWidth = bitmap.getWidth();
						}
					}
		        	double heightRatio = bitmap.getHeight() / bitmap.getWidth();
//		        	Log.d(tag, String.valueOf( imageViewWidth ) + "    " + String.valueOf( heightRatio ) );
		        	int imageHeight = (int) (imageViewWidth * heightRatio);
//		        	Log.d(tag, "image height is :: " + String.valueOf( imageHeight ) );
		        	Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageViewWidth,imageHeight , false);
		            memoryCache.put(photoToLoad.url, scaledBitmap);
		            photoToLoad.imageView.setImageBitmap(scaledBitmap);
//					isImageLoaded = true;
				} catch ( NullPointerException e ){
					photoToLoad.imageView.setImageResource(stub_id);
				} 		
			}
		}
    	
    }
    
    class PhotosLoader extends AsyncTask<String, Integer, String> {
        PhotoToLoad photoToLoad;
        Bitmap bitmap;
        
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
        
		@Override
		protected String doInBackground(String... params) {
			bitmap=getBitmap(photoToLoad.url);
			
            return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			try{
				int imageViewWidth = photoToLoad.imageView.getWidth();
	        	double heightRatio = bitmap.getHeight() / bitmap.getWidth(); 
//	        	Log.d(tag, String.valueOf( imageViewWidth ) + "    " + String.valueOf( heightRatio ) );
	        	int imageHeight = (int) (imageViewWidth * heightRatio);
//	        	Log.d(tag, "image height is :: " + String.valueOf( imageHeight ) );
	        	Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageViewWidth,imageHeight , false);
	            memoryCache.put(photoToLoad.url, scaledBitmap);
	            photoToLoad.imageView.setImageBitmap(scaledBitmap);
			} catch ( NullPointerException e ){
				photoToLoad.imageView.setImageResource(stub_id);
			} catch ( IllegalArgumentException e2 ){
				photoToLoad.imageView.setImageResource(stub_id);
			}
		}
    }
    
    
//    class PhotosLoader implements Runnable {
//        PhotoToLoad photoToLoad;
//        PhotosLoader(PhotoToLoad photoToLoad){
//            this.photoToLoad=photoToLoad;
//        }
//        
//        @Override
//        public void run() {
//            if(imageViewReused(photoToLoad))
//                return;
//            Bitmap bitmap= getBitmap(photoToLoad.url);
//            int imageViewWidth = photoToLoad.imageView.getWidth();
//        	double heightRatio = bitmap.getHeight() / bitmap.getWidth(); 
//        	int imageHeight = (int) (imageViewWidth * heightRatio);
//        	Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageViewWidth,imageHeight , false);
//        	
//            memoryCache.put(photoToLoad.url, scaledBitmap);
//            if(imageViewReused(photoToLoad))
//                return;
//            BitmapDisplayer bd=new BitmapDisplayer(scaledBitmap, photoToLoad);
//            Activity a=(Activity)photoToLoad.imageView.getContext();
//            a.runOnUiThread(bd);
//        }
//    }
    
    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        if(tag==null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
        public void run()
        {
            if(imageViewReused(photoToLoad))
                return;
            if(bitmap!=null){
                photoToLoad.imageView.setImageBitmap(bitmap);
            }
            else
                photoToLoad.imageView.setImageResource(stub_id);
        }
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

}
