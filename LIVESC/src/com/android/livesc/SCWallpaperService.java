package com.android.livesc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class SCWallpaperService extends WallpaperService 
{

	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	String retString = "";
	String NO_DATA = "Please choose a match.";
	String NO_CONNECTIVITY = "Oouch.! No Data connectivity.";
	String LINE_SEPERATOR = "----------------";
	
	 @Override
	    public void onCreate() {
	        super.onCreate();
	    }
	 
	 @Override
	    public void onDestroy() {
	        super.onDestroy();
	    }
	
	@Override
	public Engine onCreateEngine() {
		StrictMode.setThreadPolicy(policy); 
		return new SCWallpaperEngine();
	}
	
	class SCWallpaperEngine extends Engine
	{
		private final Paint mPaint = new Paint();
		private boolean mVisible = false;
		private final Handler mHandler = new Handler();
		private final Runnable mUpdateDisplay = new Runnable() {
		
		@Override
		public void run() 
		{
			draw();
		}};
		
		SCWallpaperEngine() 
		{
	            final Paint paint = mPaint;
	            paint.setTextSize(24);
	            paint.setAntiAlias(true);
	            paint.setColor(Color.BLACK);

	     }
		@Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }
		
		@Override
		public void onDestroy() 
		{
			super.onDestroy();
			mVisible = false;
			mHandler.removeCallbacks(mUpdateDisplay);
		}
		
		@Override
		public void onVisibilityChanged(boolean visible) 
		{
			mVisible = visible;
			if (visible) 
			{
					draw();
			}
			else 
			{
				mHandler.removeCallbacks(mUpdateDisplay);
			}
		}
		
		
		@Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }
		
		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) 
		{
			super.onSurfaceCreated(holder);
			draw();
		}
		
		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) 
		{
			super.onSurfaceDestroyed(holder);
			mVisible = false;
			mHandler.removeCallbacks(mUpdateDisplay);
		}
		
		private void draw() 
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SCWallpaperService.this);
			String selectedMatch = prefs.getString("list_preference", "no_match");
			int refreshInterval = Integer.parseInt(prefs.getString("refresh_interval", "5000"));
			Canvas c = null;
			final SurfaceHolder holder = getSurfaceHolder();
			try
			{
				if(!isOnline())
				{
//					Paint p = new Paint();
//					p.setTextSize(24);
//					p.setAntiAlias(true);
//					p.setColor(Color.BLACK);
					c = holder.lockCanvas();
					if(c!=null)
					{
						c.drawRect(0, 0, c.getWidth(), c.getHeight(), mPaint);
						float w = mPaint.measureText(NO_CONNECTIVITY, 0, NO_CONNECTIVITY.length());
						int offset = (int) w / 2;
						mPaint.setColor(Color.WHITE);
						int x = c.getWidth()/2 - offset;
						int y = c.getHeight()/2;
						c.drawText(NO_CONNECTIVITY, x, y, mPaint);
					}
				}
				else
				{
					c = holder.lockCanvas();
					if (c != null) 
					{
						Paint p = new Paint();
						p.setTextSize(14);
						p.setAntiAlias(true);
						p.setColor(Color.BLACK);
						c.drawRect(0, 0, c.getWidth(), c.getHeight(), mPaint);
						p.setColor(Color.WHITE);
						
						String refreshedTime = "Time: " + appendZero(Calendar.getInstance().getTime().getHours()) + ":" +appendZero(Calendar.getInstance().getTime().getMinutes()) + ":" + appendZero(Calendar.getInstance().getTime().getSeconds()) ;
						int x = 40;
						int y = c.getHeight()/3;
						c.drawText(refreshedTime, x, y, p);
						y += 20;
						c.drawText(LINE_SEPERATOR, x, y, p);
						String str = getScore(selectedMatch);
						if(null!=str && !str.equals(""))
						{
							String[] data = str.split("\\|");
							if(null!=data && data.length>0)
							{
								if(null!=data[0] && !data[0].equals(""))
								{
									y += 40;
									c.drawText(data[0], x, y, p);
								}
								if(null!=data[1] && !data[1].equals(""))
								{
									String batScore = "";
									String bowlScore = "";
									int lastAsteriskIndex = -1;
									if(data[1].contains("*"))
									{
										lastAsteriskIndex = data[1].lastIndexOf("*");
										if(lastAsteriskIndex != -1)
										{
											batScore = data[1].substring(0,lastAsteriskIndex).trim();
											bowlScore = data[1].substring(lastAsteriskIndex+1, data[1].length() ).trim();
										}
										y += 20;
										c.drawText(batScore, x, y, p);
										y += 20;
										c.drawText(bowlScore, x, y, p);
									}
									else
									{
										y += 20;
										c.drawText(data[1], x, y, p);
									}
								}
								if(null!=data[2] && !data[2].equals(""))
								{
									y += 20;
									Paint p1 = new Paint();
									p1.setTextSize(12);
									p1.setAntiAlias(true);
									p1.setColor(Color.BLACK);
									p1.setColor(Color.WHITE);
									c.drawText(data[2], x, y, p1);
								}
								
							}
						}
						else
						{
							c.drawText(NO_DATA, x, y+80, p);
						}
					}
			}
			}
			finally 
			{
					if (c != null)
					holder.unlockCanvasAndPost(c);
			}
			mHandler.removeCallbacks(mUpdateDisplay);
			if (mVisible) 
			{
				mHandler.postDelayed(mUpdateDisplay, refreshInterval);
			}
				
		}
		
		public String appendZero(int time)
		{
			String ret = "";
			if(time>=0 && time<10)
			{
				ret = "0"+time;
			}
			else
			{
				ret = ""+time;
			}
			return ret;
		}
		
		public boolean isOnline() 
		{
		    ConnectivityManager cm =
		        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		    return cm.getActiveNetworkInfo() != null && 
		       cm.getActiveNetworkInfo().isConnectedOrConnecting();
		}
		
		public String getScore(String selectedMatch)
		{
			HttpClient client = new DefaultHttpClient();
			String url = "http://live-scorecard.appspot.com/getMatchDetail?matchName="+selectedMatch;
			url = url.replace(" ", "%20").trim();
			Log.i("URL: " , url);
			HttpGet get = new HttpGet(url);
			HttpResponse response;
			String returnString = "";
			try 
			{
				response = client.execute(get);
				if (response.getStatusLine().getStatusCode() == 200) 
				{
					HttpEntity entity = response.getEntity();
					 if (entity != null) 
					 {
						 InputStream instream = entity.getContent();
						 returnString = convertStreamToString(instream);
						 Log.i("ret",returnString);
					 }
					
				}
			}
			catch (ClientProtocolException e) 
			{
				e.printStackTrace();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			} 
			return returnString;
		}
		public String convertStreamToString(InputStream is) 
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line = null;
			try
			{
				while ((line = reader.readLine()) != null) 
				{
					sb.append(line + "\n");
				}
			}
			catch(IOException e)
			{
				
			}
			finally 
			{
				try 
				{
					is.close();
				}
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
			Log.i("JSON Data", sb.toString());
			return sb.toString();
		}
		
}
}
