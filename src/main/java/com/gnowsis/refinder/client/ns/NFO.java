package com.gnowsis.refinder.client.ns;

import java.net.URI;

public class NFO
{
    public static final String _base = "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#";

    public static final URI FileDataObject = URI.create(_base + "FileDataObject");

    public static final URI fileCreated = URI.create(_base + "fileCreated");
    public static final URI fileLastModified = URI.create(_base + "fileLastModified");
    public static final URI fileName = URI.create(_base + "fileName");
    public static final URI fileSize = URI.create(_base + "fileSize");
    public static final URI fileUrl = URI.create(_base + "fileUrl");

    
    public static final URI Bookmark = URI.create(_base + "Bookmark");
    public static final URI bookmarks = URI.create(_base + "bookmarks");
}
