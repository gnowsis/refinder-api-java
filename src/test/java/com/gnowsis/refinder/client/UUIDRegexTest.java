package com.gnowsis.refinder.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.gnowsis.refinder.client.Util;

public class UUIDRegexTest
{
	@Test
	public void test1()
	{
      Pattern pattern = Pattern.compile("([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})");

      String url = "http://localhost:8000/things/936fd398-1635-11df-b440-002332d0b742/#t";
      Matcher matcher = pattern.matcher(url);
      assertTrue(matcher.find());
      assertEquals("936fd398-1635-11df-b440-002332d0b742", matcher.group());
      assertEquals("936fd398-1635-11df-b440-002332d0b742", Util.findUuid(url));
      matcher.reset();
      
      url = "http://localhost:8000/things/12354e70-bc16-11df-851a-0800200c9a66/#t";
      matcher = pattern.matcher(url);
      assertTrue(matcher.find());
      assertEquals("12354e70-bc16-11df-851a-0800200c9a66", matcher.group());
      assertEquals("12354e70-bc16-11df-851a-0800200c9a66", Util.findUuid(url));
      matcher.reset();

      url = "http://localhost:8000/things/12354e70-11df-851a-0800200c9a66/#t";
      assertFalse(pattern.matcher(url).find());
      try
      {	
      	matcher.group();
      	fail();
      }
      catch(IllegalStateException e)
      {
      	// good
      }
      assertNull(Util.findUuid(url));
      matcher.reset();
   }
}
