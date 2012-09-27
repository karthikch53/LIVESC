package com.android.livesc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
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
import android.widget.Toast;

public class SCWallpaperService extends WallpaperService 
{

	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	String retString = "";
	String NO_DATA = "Please choose a match.";
	String NO_CONNECTIVITY = "Oouch.! No Data connectivity.";
	String CHECK_DATA_WIFI_SETTINGS = "Please check your Data/Wifi settings.";
	String LINE_SEPERATOR = "------------------";
	int maxWidth = 0;
	
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
		private boolean mVisible = false;
		boolean hasFirstRowData = false;
		boolean hasSecondRowData = false;
		private final Handler mHandler = new Handler();
		private final Runnable mUpdateDisplay = new Runnable() {
		
		@Override
		public void run() 
		{
			try
			{
				draw();
			}
			catch(Exception e)
			{
				if(e instanceof ConnectException)
				{
					Toast toast = Toast.makeText(getApplicationContext(), NO_CONNECTIVITY, Toast.LENGTH_LONG);
					toast.show();
				}
				else
				{
					Toast toast = Toast.makeText(getApplicationContext(), "Others", Toast.LENGTH_LONG);
					toast.show();
				}
			}
			
		}};
		
		
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
			int x=0,y=0;
			final SurfaceHolder holder = getSurfaceHolder();
			try
			{
				if(!isOnline())
				{
					c = holder.lockCanvas();
					maxWidth = c.getWidth();
					maxWidth = maxWidth - 20;
					if(c!=null)
					{
						Paint p = new Paint();
						p.setTextSize(getResources().getDimension(R.dimen.textsize));
						p.setAntiAlias(true);
						p.setColor(Color.BLACK);
						c.drawRect(0, 0, c.getWidth(), c.getHeight(), p);
						float w = p.measureText(CHECK_DATA_WIFI_SETTINGS, 0, CHECK_DATA_WIFI_SETTINGS.length());
						int offset = (int) w / 2;
						p.setColor(Color.WHITE);
						x = c.getWidth()/2 - offset;
						y = c.getHeight()/2;
						c.drawText(NO_CONNECTIVITY, x, y, p);
						y += 20;
						c.drawText(CHECK_DATA_WIFI_SETTINGS, x, y, p);
						
					}
				}
				else
				{
					c = holder.lockCanvas();
					maxWidth = c.getWidth();
					maxWidth = maxWidth - 20;
					if (c != null) 
					{
						Paint p = new Paint();
						p.setTextSize(getResources().getDimension(R.dimen.textsize));
						p.setAntiAlias(true);
						p.setColor(Color.BLACK);
						c.drawRect(0, 0, c.getWidth(), c.getHeight(), p);
						p.setColor(Color.WHITE);
						String refreshedTime = "Time: " + appendZero(Calendar.getInstance().getTime().getHours()) + ":" +appendZero(Calendar.getInstance().getTime().getMinutes()) + ":" + appendZero(Calendar.getInstance().getTime().getSeconds()) ;
						x = 20;
						y = c.getHeight()/3;
						c.drawText(refreshedTime, x, y, p);
						y += 20;
						c.drawText(LINE_SEPERATOR, x, y, p);
						if(isOnline())
						{
							String str = getScore(selectedMatch);
							if(null!=str && !str.equals("") && !str.equalsIgnoreCase("null"))
							{
								hasFirstRowData = true;
								String[] data = str.split("\\|");
								if(null!=data && data.length>0)
								{
									if(null!=data[0] && !data[0].equals(""))
									{
										if(data[0].contains(";"))
										{
											String[] arr = data[0].split(";");
											for(int i=0;i<arr.length;i++)
											{
												y += 30;
												drawMultiLineText(c,arr[i].trim(), x, y, p);
											}
										}
										else
										{
											y += 40;
											drawMultiLineText(c,data[0], x, y, p);
										}
										
										//c.drawText(data[0], x, y, p);
									}
									if(null!=data[1] && !data[1].equals(""))
									{
										hasSecondRowData = true;
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
											y += 30;
											drawMultiLineText(c,batScore, x, y, p);
											//c.drawText(batScore, x, y, p);
											y += 30;
											drawMultiLineText(c,bowlScore, x, y, p);
											//c.drawText(bowlScore, x, y, p);
										}
										else
										{
											y += 30;
											drawMultiLineText(c,data[1], x, y, p);
											//c.drawText(data[1], x, y, p);
										}
									}
									if(null!=data[2] && !data[2].equals(""))
									{
										y += 30;
										Paint p1 = new Paint();
										p1.setTextSize(getResources().getDimension(R.dimen.textsize));
										p1.setAntiAlias(true);
										p1.setColor(Color.WHITE);
										if(!hasFirstRowData || !hasSecondRowData)
										{
											Log.i("No match score", "No match and players scores");
											drawMultiLineText(c,selectedMatch, x, y, p);
											y += 30;
										}
										drawMultiLineText(c,data[2], x, y, p);
//										float descWidth = p1.measureText(data[2]);
//										if(descWidth>c.getWidth())
//										{
//											int cutOff = (int)0.75*data[2].length();
//											String firstLine = data[2].substring(0, cutOff);
//											c.drawText(firstLine, x, y, p1);
//											String secondLine = data[2].substring(cutOff,data[2].length());
//											y+=20;
//											c.drawText(secondLine, x, y, p1);
//											
//										}
//										else
//										{
//											c.drawText(data[2], x, y, p1);
//										}
									}
									
								}
							}
							else
							{
								y += 80;
								c.drawText(NO_DATA, x, y, p);
							}
				
						}
						else
						{
							y+=80;
							c.drawText(NO_CONNECTIVITY, x, y, p);
							y += 20;
							c.drawText(CHECK_DATA_WIFI_SETTINGS, x, y, p);
						}
					}
			}
			}
			catch(Exception e)
			{
				Paint p1 = new Paint();
				p1.setTextSize(getResources().getDimension(R.dimen.textsize));
				p1.setAntiAlias(true);
				p1.setColor(Color.WHITE);
				y += 20;
				c.drawText(NO_DATA, x, y, p1);
				
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
		
		private void drawMultiLineText (Canvas c,String text, float x, float y, Paint paint)
		{
			String firstLine = "";
			String secondLine = "";
			int cutOff = 0;
			int lastWhiteSpaceIndex =0;
			if(null!=text)
			{
				
				if(paint.measureText(text) > maxWidth)
				{
					cutOff = (int)(0.75*text.length());
					firstLine = text.substring(0, cutOff); 
					lastWhiteSpaceIndex = firstLine.lastIndexOf(" ");
					firstLine = firstLine.substring(0, lastWhiteSpaceIndex) + ".. ";
					c.drawText(firstLine, x, y, paint);
					y += 30;
					secondLine = " .."+ text.substring(lastWhiteSpaceIndex,text.length());
					c.drawText(secondLine, x, y, paint);
				}
				else
				{
					c.drawText(text, x, y, paint);
				}
			}
		}
		
		
		
		public boolean isOnline() 
		{
		    ConnectivityManager cm =
		        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    
		    Log.i("isOnline -- ", String.valueOf(cm.getActiveNetworkInfo() != null && 
		       cm.getActiveNetworkInfo().isConnectedOrConnecting()));

		    return cm.getActiveNetworkInfo() != null && 
		       cm.getActiveNetworkInfo().isConnectedOrConnecting();
		}
		
		public String getScore(String selectedMatch) throws Exception
		{
			HttpClient client = new DefaultHttpClient();
			String url = "http://live-scorecard.appspot.com/matchDetail?matchName="+selectedMatch;
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
				else
				{
				   Log.i("status code: ", String.valueOf(response.getStatusLine().getStatusCode()))	;
				}
			}
			catch (ClientProtocolException e) 
			{
				Log.i("LIVESC: ClientProtocolException -- ", e.getMessage());
				throw e;
			}
			catch (IOException e) 
			{
				Log.i("LIVESC: IOException -- ", e.getMessage());
				throw e;
			} 
			catch(Exception e)
			{
				Log.i("LIVESC:  ", e.getClass().getName() + " --  "+ 
						e.getMessage());
				throw e;
			}
			return returnString;
		}
		public String convertStreamToString(InputStream is) 
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String sb = "";
			String line = null;
			try
			{
				while ((line = reader.readLine()) != null) 
				{
					sb += line + "\n";
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
			return sb.equals("")||sb.trim().equalsIgnoreCase("null")?null:sb;
		}
		
}
}
