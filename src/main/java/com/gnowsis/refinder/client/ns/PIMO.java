package com.gnowsis.refinder.client.ns;

import java.net.URI;

public class PIMO
{
    public static final String _base = "http://www.semanticdesktop.org/ontologies/2007/11/01/pimo#";

    public static final URI Note = URI.create(_base + "Note");
    public static final URI Person = URI.create(_base + "Person");
    public static final URI Organization = URI.create(_base + "Organization");
    public static final URI Location = URI.create(_base + "Location");
    public static final URI Topic = URI.create(_base + "Topic");
    public static final URI Event = URI.create(_base + "Event");
    public static final URI Project = URI.create(_base + "Project");
    public static final URI Task = URI.create(_base + "Task");

    public static final URI dtstart = URI.create(_base + "dtstart");
    public static final URI dtend = URI.create(_base + "dtend");
    public static final URI isRelated = URI.create(_base + "isRelated");
    public static final URI groundingOccurrence = URI.create(_base + "groundingOccurrence");
    public static final URI creator = URI.create(_base + "creator");
    public static final URI wikiText = URI.create(_base + "wikiText");
}
