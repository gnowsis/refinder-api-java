package com.gnowsis.refinder.client.ns;

import java.net.URI;

public class NAO
{
    public static final String _base = "http://www.semanticdesktop.org/ontologies/2007/08/15/nao#";

    public static final URI description = URI.create(_base + "description");
    public static final URI created = URI.create(_base + "created");
    public static final URI lastModified = URI.create(_base + "lastModified");
    public static final URI normalizedRating = URI.create(_base + "normalizedRating");
}