package com.gnowsis.refinder.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;

public class Util
{
	 public static String makeDataObjectUrl(String dataSourceId, String localId)
    {
        if (dataSourceId == null)
            dataSourceId = "";

        try
        {
      	  return "refinder:" + dataSourceId + ":" + URLEncoder.encode(localId, "UTF-8");
        }
        catch(UnsupportedEncodingException e)
        {
      	  // should never happen
      	  return null;
        }
    }

    public static String getDataSourceId(String dataObjectUrl)
    {
        return splitDataObjectUrl(dataObjectUrl)[1];
    }

    public static String getLocalId(String dataObjectUrl)
    {
        return splitDataObjectUrl(dataObjectUrl)[2];
    }

    private static String[] splitDataObjectUrl(String dataObjectUrl)
    {
        String[] elements = dataObjectUrl.split(":");
        if (elements.length != 3)
      	  return null;   // invalid data object URI
        return elements;
    }
    

 	public static String findUuid(String text)
 	{
 		if(text == null)
 			return null;
 		
 		Pattern pattern = Pattern.compile("([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})");
 		Matcher matcher = pattern.matcher(text);
 		if (matcher.find())
 			return matcher.group();
 		else
 			return null;
 	}
 	
 	public static String cutFragment(String url)
 	{
		if(url.contains("#"))
			url = url.substring(0, url.indexOf("#"));
		
		return url;
 	}
 	
 	public static byte[] obtainByteData(InputStream inputStream) 
 	throws IOException 
 	{
 	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
 	    byte[] bytes = new byte[512];
 	 
 	    // Read bytes from the input stream in bytes.length-sized chunks and write
 	    // them into the output stream
 	    int readBytes;
 	    while ((readBytes = inputStream.read(bytes)) > 0) {
 	        outputStream.write(bytes, 0, readBytes);
 	    }
 	 
 	    // Convert the contents of the output stream into a byte array
 	    byte[] byteData = outputStream.toByteArray();
 	 
 	    // Close the streams
 	    inputStream.close();
 	    outputStream.close();
 	 
 	    return byteData;
 	}
 	
 	public static void consume(HttpEntity entity)
 	{
 		if(entity == null)
 			return;
 		
 		try
 		{
 			InputStream is = entity.getContent();
 			is.close();
 		}
 		catch (IOException e)
 		{
 			// ignore
 		}
 	}
}
