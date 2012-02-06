package com.gnowsis.refinder.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.gnowsis.refinder.client.ns.PIMO;
import com.gnowsis.refinder.client.ns.RDFS;

public class ThingFileTest
extends RefinderTestBase
{
	private Log log = LogFactory.getLog(getClass());

	@Test
	public void testUpload()
	throws IOException
	{
		this.log.debug("Creating a thing ...");
		Thing thing = new Thing(null, PIMO.Topic);
		thing.setAttribute(RDFS.label, "testthing for analyzer (Java client test -- DELETE ME)");
		thing = this.refinder.createThing(thing);
		assertNotNull(thing.getUri());

		try
		{
			this.log.debug("Download thing file ...");
			this.refinder.downloadThingFile(thing);
			fail("No exception thrown.");
		}
		catch(RefinderException e)
		{
			assertEquals(RefinderException.Status.NotFound, e.status); 
		}
		
		this.log.debug("Upload a file ...");
		byte[] fileContent = IOUtils.toByteArray(ThingFileTest.class.getResourceAsStream("/edam-sync.pdf"));
		this.log.debug("(File has " + fileContent.length + " bytes)");
		this.refinder.uploadThingFile(thing, fileContent);
		
		
		this.log.debug("Downloading file ....");
		byte[] downloadData = this.refinder.downloadThingFile(thing);
		assertEquals(fileContent.length, downloadData.length);
		
		
		this.log.debug("Deleting thing ...");
		this.refinder.deleteThing(thing);
		
		this.log.debug("Done!");
	}
}
