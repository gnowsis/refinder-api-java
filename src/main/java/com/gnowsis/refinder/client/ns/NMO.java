package com.gnowsis.refinder.client.ns;

import java.net.URI;

public class NMO
{
    public static final String _base = "http://www.semanticdesktop.org/ontologies/2007/03/22/nmo#";

    public static final URI Email = URI.create(_base + "Email");

    public static final URI to = URI.create(_base + "to");
    public static final URI cc = URI.create(_base + "cc");
    public static final URI bcc = URI.create(_base + "bcc");
    public static final URI from = URI.create(_base + "from");
    public static final URI messageSubject = URI.create(_base + "messageSubject");
    public static final URI messageId = URI.create(_base + "messageId");
    public static final URI inReplyTo = URI.create(_base + "inReplyTo");
    public static final URI sentDate = URI.create(_base + "sentDate");
}