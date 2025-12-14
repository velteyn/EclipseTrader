// HTMLParser Library $Name: v1_6_20060319 $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2003 Derrick Oswald
//
// Revision Control Information
//
// $Source: /cvsroot/htmlparser/htmlparser/src/org/htmlparser/parserapplications/SiteCapturer.java,v $
// $Author: derrickoswald $
// $Date: 2005/04/12 11:27:41 $
// $Revision: 1.9 $
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//

package org.htmlparser.parserapplications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.IDN;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.BaseHrefTag;
import org.htmlparser.tags.FrameTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * Save a web site locally.
 * Illustrative program to save a web site contents locally.
 * It was created to demonstrate URL rewriting in it's simplest form.
 * It uses customized tags in the NodeFactory to alter the URLs.
 * This program has a number of limitations:
 * <ul>
 * <li>it doesn't capture forms, this would involve too many assumptions</li>
 * <li>it doesn't capture script references, so funky onMouseOver and other
 * non-static content will not be faithfully reproduced</li>
 * <li>it doesn't handle style sheets</li>
 * <li>it doesn't dig into attributes that might reference resources, so
 * for example, background images won't necessarily be captured</li>
 * <li>worst of all, it gets confused when a URL both has content and is
 * the prefix for other content,
 * i.e. http://whatever.com/top and http://whatever.com/top/sub.html both
 * yield content, since this cannot be faithfully replicated to a static
 * directory structure (this happens a lot with servlet based sites)</li>
 *</ul>
 */
public class SiteCapturer
{
    /**
     * The web site to capture.
     * This is used as the base URL in deciding whether to adjust a link
     * and whether to capture a page or not.
     */
    protected String mSource;

    /**
     * The local directory to capture to.
     * This is used as a base prefix for files saved locally.
     */
    protected String mTarget;

    /**
     * The list of pages to capture.
     * Links are added to this list as they are discovered, and removed in
     * sequential order (FIFO queue) leading to a breadth
     * first traversal of the web site space.
     */
    protected ArrayList mPages;

    /**
     * The set of pages already captured.
     * Used to avoid repeated acquisition of the same page.
     */
    protected HashSet mFinished;

    /**
     * The list of resources to copy.
     * Images and other resources are added to this list as they are discovered.
     */
    protected ArrayList mImages;

    /**
     * The set of resources already copied.
     * Used to avoid repeated acquisition of the same images and other resources.
     */
    protected HashSet mCopied;

    /**
     * The parser to use for processing.
     */
    protected Parser mParser;

    /**
     * If <code>true</code>, save resources locally too,
     * otherwise, leave resource links pointing to original page.
     */
    protected boolean mCaptureResources;

    /**
     * The filter to apply to the nodes retrieved.
     */
    protected NodeFilter mFilter;

    /**
     * Copy buffer size.
     * Resources are moved to disk in chunks this size or less.
     */
    protected final int TRANSFER_SIZE = 4096;

    /**
     * Create a web site capturer.
     */
    public SiteCapturer ()
    {
        PrototypicalNodeFactory factory;

        mSource = null;
        mTarget = null;
        mPages = new ArrayList ();
        mFinished = new HashSet ();
        mImages = new ArrayList ();
        mCopied = new HashSet ();
        mParser = new Parser ();
        factory = new PrototypicalNodeFactory ();
        factory.registerTag (new LocalLinkTag ());
        factory.registerTag (new LocalFrameTag ());
        factory.registerTag (new LocalBaseHrefTag ());
        factory.registerTag (new LocalImageTag ());
        mParser.setNodeFactory (factory);
        mCaptureResources = true;
        mFilter = null;
        mAllowedHosts = new HashSet ();
    }

    /**
     * Getter for property source.
     * @return Value of property source.
     */
    public String getSource ()
    {
        return (mSource);
    }
    
    /**
     * Setter for property source.
     * This is the base URL to capture. URL's that don't start with this prefix
     * are ignored (left as is), while the ones with this URL as a base are
     * re-homed to the local target.
     * @param source New value of property source.
     */
    public void setSource (String source)
    {
        if (source.endsWith ("/"))
            source = source.substring (0, source.length () - 1);
        mSource = source;
        try {
            URL src = new URL(mSource);
            String host = src.getHost();
            if (host != null) {
                addAllowedHost(host);
                String ascii = java.net.IDN.toASCII(host.toLowerCase());
                addAllowedHost(ascii);
            }
        } catch (MalformedURLException e) {
            // ignore
        }
    }
    
    /**
     * Getter for property target.
     * @return Value of property target.
     */
    public String getTarget ()
    {
        return (mTarget);
    }
    
    /**
     * Setter for property target.
     * This is the local directory under which to save the site's pages.
     * @param target New value of property target.
     */
    public void setTarget (String target)
    {
        mTarget = target;
    }

    /**
     * Getter for property captureResources.
     * If <code>true</code>, the images and other resources referenced by
     * the site and within the base URL tree are also copied locally to the
     * target directory. If <code>false</code>, the image links are left 'as
     * is', still refering to the original site.
     * @return Value of property captureResources.
     */
    public boolean getCaptureResources ()
    {
        return (mCaptureResources);
    }
    
    /**
     * Setter for property captureResources.
     * @param capture New value of property captureResources.
     */
    public void setCaptureResources (boolean capture)
    {
        mCaptureResources = capture;
    }
    
    
    /** Getter for property filter.
     * @return Value of property filter.
     *
     */
    public NodeFilter getFilter ()
    {
        return (mFilter);
    }
    
    /** Setter for property filter.
     * @param filter New value of property filter.
     *
     */
    public void setFilter (NodeFilter filter)
    {
        mFilter = filter;
    }
    protected HashSet mAllowedHosts;
    public void setAllowedHosts (java.util.Collection hosts)
    {
        mAllowedHosts.clear ();
        if (hosts != null)
            mAllowedHosts.addAll (hosts);
    }
    public void addAllowedHost (String host)
    {
        if (host != null)
            mAllowedHosts.add (host.toLowerCase ());
    }
    
    /**
     * Returns <code>true</code> if the link is one we are interested in.
     * @param link The link to be checked.
     * @return <code>true</code> if the link has the source URL as a prefix
     * and doesn't contain '?' or '#'; the former because we won't be able to
     * handle server side queries in the static target directory structure and
     * the latter because presumably the full page with that reference has
     * already been captured previously. This performs a case insensitive
     * comparison, which is cheating really, but it's cheap.
     */
    protected boolean isToBeCaptured (String link)
    {
        try {
            URL linkUrl = new URL(link);
            URL sourceUrl = new URL(getSource());

            // Allow only http and https schemes
            String linkProtocol = linkUrl.getProtocol();
            if (!"http".equalsIgnoreCase(linkProtocol) && !"https".equalsIgnoreCase(linkProtocol)) {
                return false;
            }

            // Canonicalize and compare hosts using IDN (toASCII for unicode hosts)
            String sourceHost = sourceUrl.getHost();
            String linkHost = linkUrl.getHost();
            if (sourceHost == null || linkHost == null) return false;
            
            String canonicalSourceHost = IDN.toASCII(sourceHost).toLowerCase();
            String canonicalLinkHost = IDN.toASCII(linkHost).toLowerCase();

            // Only allow exact host match (same-origin policy)
            if (!canonicalSourceHost.equals(canonicalLinkHost)) {
                return false;
            }

            // Enforce identical port (explicit or default)
            int sourcePort = sourceUrl.getPort() != -1 ? sourceUrl.getPort() : sourceUrl.getDefaultPort();
            int linkPort = linkUrl.getPort() != -1 ? linkUrl.getPort() : linkUrl.getDefaultPort();
            if (sourcePort != linkPort) {
                return false;
            }

            // Enforce same protocol
            if (!sourceUrl.getProtocol().equalsIgnoreCase(linkUrl.getProtocol())) {
                return false;
            }

            // Disallow URLs with query or fragment
            if (linkUrl.getQuery() != null || linkUrl.getRef() != null) {
                return false;
            }
            // Optionally: Disallow userinfo (avoid links like http://user@host/)
            if ((linkUrl.getUserInfo() != null) && !linkUrl.getUserInfo().isEmpty()) {
                return false;
            }
            // Extra Step: Optionally, check that the resolved InetAddress matches source's
            //           (Improves defense if DNS results are different for source and destination)
            //           (Uncomment below if strict IP-level binding is desired)
            /*
            try {
                InetAddress srcAddr = InetAddress.getByName(sourceHost);
                InetAddress linkAddr = InetAddress.getByName(linkHost);
                if (!srcAddr.equals(linkAddr)) {
                    return false;
                }
            } catch (UnknownHostException ignored) {
                return false;
            }
            */

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns <code>true</code> if the link contains text/html content.
     * @param link The URL to check for content type.
     * @return <code>true</code> if the HTTP header indicates the type is
     * "text/html".
     * @exception ParserException If the supplied URL can't be read from.
     */
    protected boolean isHtml (String link)
        throws
            ParserException
    {
        URL url;
        URLConnection connection;
        String type;
        boolean ret;

        try
        {
            if (!isSafeDestinationUrl(link, getSource())) {
                return false;
            }
            URI linkUri = new URI(link);
            URI originUri = new URI(getSource());
            String path = linkUri.getPath() == null ? "" : linkUri.getPath();
            String basePath = originUri.getPath() == null ? "" : originUri.getPath();
            if (basePath.length() > 0 && !path.startsWith(basePath)) {
                return false;
            }
            int port = originUri.getPort();
            if (port == -1) {
                port = originUri.getScheme().equalsIgnoreCase("https") ? 443 : 80;
            }
            url = new URL(originUri.getScheme(), originUri.getHost(), port, path);
            connection = url.openConnection();
            type = connection.getContentType ();
            if (type == null)
                ret = false;
            else
                ret = type.startsWith ("text/html");
        }
        catch (IOException e)
        {
            throw new ParserException ("URL " + link + " has a problem", e);
        }
        catch (URISyntaxException e)
        {
            throw new ParserException ("URL " + link + " is invalid", e);
        }
        
        return (ret);
    }
    private boolean isSafeDestinationUrl(String link, String origin) {
        try {
            URI linkUri = new URI(link);
            URI originUri = new URI(origin);
            String scheme = linkUri.getScheme();
            if (scheme == null) return false;
            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) return false;
            String linkHost = linkUri.getHost();
            String originHost = originUri.getHost();
            if (linkHost == null || originHost == null) return false;
            String lh = linkHost.toLowerCase();
            if ("localhost".equals(lh) || "::1".equals(lh)) return false;
            String linkHostAscii = IDN.toASCII(lh);
            String originHostAscii = IDN.toASCII(originHost.toLowerCase());
            if (linkHostAscii.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) return false;
            InetAddress linkAddr = InetAddress.getByName(linkHost);
            InetAddress originAddr = InetAddress.getByName(originHost);
            if (linkAddr.isAnyLocalAddress() || linkAddr.isLoopbackAddress() || linkAddr.isLinkLocalAddress() || linkAddr.isSiteLocalAddress()) return false;
            if (!linkAddr.equals(originAddr)) return false;
            if (!mAllowedHosts.isEmpty()) {
                if (!mAllowedHosts.contains(linkHostAscii)) return false;
            }
            String linkPath = linkUri.getPath() == null ? "" : linkUri.getPath();
            String originPath = originUri.getPath() == null ? "" : originUri.getPath();
            if (originPath.length() > 0 && !linkPath.startsWith(originPath)) return false;
            if (linkUri.getQuery() != null || linkUri.getFragment() != null) return false;
            int lp = linkUri.getPort();
            if (lp == -1) lp = "https".equalsIgnoreCase(scheme) ? 443 : 80;
            int op = originUri.getPort();
            String os = originUri.getScheme();
            if (op == -1) op = "https".equalsIgnoreCase(os) ? 443 : 80;
            if (!scheme.equalsIgnoreCase(os)) return false;
            return lp == op;
        } catch (URISyntaxException | UnknownHostException e) {
            return false;
        }
    }

    /**

    /**
     * Converts a link to local.
     * A relative link can be used to construct both a URL and a file name.
     * Basically, the operation is to strip off the base url, if any,
     * and then prepend as many dot-dots as necessary to make
     * it relative to the current page.
     * A bit of a kludge handles the root page specially by calling it
     * index.html, even though that probably isn't it's real file name.
     * This isn't pretty, but it works for me.
     * @param link The link to make relative.
     * @param current The current page URL, or empty if it's an absolute URL
     * that needs to be converted.
     * @return The URL relative to the current page.
     */
    protected String makeLocalLink (String link, String current)
    {
        int i;
        int j;
        String ret;

        if (link.equals (getSource ()) || (!getSource ().endsWith ("/") && link.equals (getSource () + "/")))
            ret = "index.html"; // handle the root page specially
        else if (link.startsWith (getSource ())
                && (link.length () > getSource ().length ()))
            ret = link.substring (getSource ().length () + 1);
        else
            ret = link; // give up
            
        // make it relative to the current page by prepending "../" for
        // each '/' in the current local path
        if ((null != current)
            && link.startsWith (getSource ())
            && (current.length () > getSource ().length ()))
        {
            current = current.substring (getSource ().length () + 1);
            i = 0;
            while (-1 != (j = current.indexOf ('/', i)))
            {
                ret = "../" + ret;
                i = j + 1;
            }
        }

        return (ret);
    }

    /**
     * Unescape a URL to form a file name.
     * Very crude.
     * @param raw The escaped URI.
     * @return The native URI.
     */
    protected String decode (String raw)
    {
        int length;
        int start;
        int index;
        int value;
        StringBuffer ret;
        
        ret = new StringBuffer (raw.length ());

        length = raw.length ();
        start = 0;
        while (-1 != (index = raw.indexOf ('%', start)))
        {   // append the part up to the % sign
            ret.append (raw.substring (start, index));
            // there must be two hex digits after the percent sign
            if (index + 2 < length)
            {
                try
                {
                    value = Integer.parseInt (raw.substring (index + 1, index + 3), 16);
                    ret.append ((char)value);
                    start = index + 3;
                }
                catch (NumberFormatException nfe)
                {
                    ret.append ('%');
                    start = index + 1;
                }
            }
            else
            {   // this case is actually illegal in a URI, but...
                ret.append ('%');
                start = index + 1;
            }
        }
        ret.append (raw.substring (start));
        
        return (ret.toString ());
    }

    /**
     * Copy a resource (image) locally.
     * Removes one element from the 'to be copied' list and saves the
     * resource it points to locally as a file.
     */
    protected void copy ()
    {
        String link;
        String raw;
        String name;
        File file;
        File dir;
        URL source;
        byte[] data;
        InputStream in;
        FileOutputStream out;
        int read;

        link = (String)mImages.remove (0);
        mCopied.add (link);

        if (getCaptureResources ())
        {
            raw = makeLocalLink (link, "");
            name = decode (raw);
            file = new File (getTarget (), name);
            System.out.println ("copying " + link + " to " + file.getAbsolutePath ());
            // ensure directory exists
            dir = file.getParentFile ();
            if (!dir.exists ())
                dir.mkdirs ();
            try
            {
                source = new URL (link);
                data = new byte [TRANSFER_SIZE];
                try
                {
                    in = source.openStream ();
                    try
                    {
                        out = new FileOutputStream (file);
                        try
                        {
                            while (-1 != (read = in.read (data, 0, data.length)))
                                out.write (data, 0, read);
                        }
                        finally
                        {
                            out.close ();
                        }
                    }
                    catch (FileNotFoundException fnfe)
                    {
                        fnfe.printStackTrace ();
                    }
                    finally
                    {
                        in.close ();
                    }
                }
                catch (FileNotFoundException fnfe)
                {
                    System.err.println ("broken link " + fnfe.getMessage () + " ignored");
                }
            }
            catch (MalformedURLException murle)
            {
                murle.printStackTrace ();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace ();
            }
        }
    }
 
    /**
     * Process a single page.
     * @param filter The filter to apply to the collected nodes.
     * @exception ParserException If a parse error occurs.
     */
    protected void process (NodeFilter filter)
        throws
            ParserException
    {
        String url;
        int bookmark;
        NodeList list;
        NodeList robots;
        MetaTag robot;
        String content;
        File file;
        File dir;
        PrintWriter out;

        // get the next URL and add it to the done pile
        url = (String)mPages.remove (0);
        System.out.println ("processing " + url);
        mFinished.add (url);

        try
        {
            bookmark = mPages.size ();
            // fetch the page and gather the list of nodes
            mParser.setURL (url);
            try
            {
                list = new NodeList ();
                for (NodeIterator e = mParser.elements (); e.hasMoreNodes (); )
                    list.add (e.nextNode ()); // URL conversion occurs in the tags
            }
            catch (EncodingChangeException ece)
            {
                // fix bug #998195 SiteCatpurer just crashed
                // try again with the encoding now set correctly
                // hopefully mPages, mImages, mCopied and mFinished won't be corrupted
                mParser.reset ();
                list = new NodeList ();
                for (NodeIterator e = mParser.elements (); e.hasMoreNodes (); )
                    list.add (e.nextNode ());
            }

            // handle robots meta tag according to http://www.robotstxt.org/wc/meta-user.html
            // <meta name="robots" content="index,follow" />
            // <meta name="robots" content="noindex,nofollow" />
            robots = list.extractAllNodesThatMatch (
                new AndFilter (
                    new NodeClassFilter (MetaTag.class),
                    new HasAttributeFilter ("name", "robots")), true);
            if (0 != robots.size ())
            {
                robot = (MetaTag)robots.elementAt (0);
                content = robot.getAttribute ("content").toLowerCase ();
                if ((-1 != content.indexOf ("none")) || (-1 != content.indexOf ("nofollow")))
                    // reset mPages
                    for (int i = bookmark; i < mPages.size (); i++)
                        mPages.remove (i);
                if ((-1 != content.indexOf ("none")) || (-1 != content.indexOf ("noindex")))
                    return;
            }
    
            if (null != filter)
                list.keepAllNodesThatMatch (filter, true);

            // save the page locally
            file = new File (getTarget (), makeLocalLink (url, ""));
            dir = file.getParentFile ();
            if (!dir.exists ())
                dir.mkdirs ();
            else if (!dir.isDirectory ())
            {
                dir = new File (dir.getParentFile (), dir.getName () + ".content");
                if (!dir.exists ())
                    dir.mkdirs ();
                file = new File (dir, file.getName ());
            }
                
            try
            {
                out = new PrintWriter (new FileOutputStream (file));
                for (int i = 0; i < list.size (); i++)
                    out.print (list.elementAt (i).toHtml ());
                out.close ();
            }
            catch (FileNotFoundException fnfe)
            {
                fnfe.printStackTrace ();
            }
        }
        catch (ParserException pe)
        {
            String message;
            
            // this exception handling is suboptimal,
            // but it recognizes resources that aren't text/html
            message = pe.getMessage ();
            if ((null != message) && (message.endsWith ("does not contain text")))
            {
                if (!mCopied.contains (url))
                    if (!mImages.contains (url))
                        mImages.add (url);
                mFinished.remove (url);
            }
            else
                throw pe;
        }
    }

    /**
     * Link tag that rewrites the HREF.
     * The HREF is changed to a local target if it matches the source.
     */
    class LocalLinkTag extends LinkTag
    {
        public void doSemanticAction ()
            throws
                ParserException
        {
            boolean html;
            String link;

            // get the link
            link = getLink ();
            // check if it needs to be captured
            if (isToBeCaptured (link))
            {
                // add the link to a list to be processed
                if (mFinished.contains (link))
                    html = true;
                else if (mPages.contains (link))
                    html = true;
                else if (mCopied.contains (link))
                    html = false;
                else if (mImages.contains (link))
                    html = false;
                else
                {   // this test is expensive, do it reluctantly
                    html = isHtml (link);
                    if (html)
                        mPages.add (link);
                    else
                        mImages.add (link);
                }
                // alter the link
                if (html || (!html && getCaptureResources ()))
                    link = makeLocalLink (link, mParser.getLexer ().getPage ().getUrl ());
                setLink (link);
            }
        }
    }

    /**
     * Frame tag that rewrites the SRC URLs.
     * The SRC URLs are mapped to local targets if they match the source.
     */
    class LocalFrameTag extends FrameTag
    {
        public void doSemanticAction ()
            throws
                ParserException
        {
            boolean html;
            String link;

            // get the link
            link = getFrameLocation ();
            // check if it needs to be captured
            if (isToBeCaptured (link))
            {
                // add the link to a list to be processed
                if (mFinished.contains (link))
                    html = true;
                else if (mPages.contains (link))
                    html = true;
                else if (mCopied.contains (link))
                    html = false;
                else if (mImages.contains (link))
                    html = false;
                else
                {   // this test is expensive, do it reluctantly
                    html = isHtml (link);
                    if (html)
                        mPages.add (link);
                    else
                        mImages.add (link);
                }
                // alter the link
                if (html || (!html && getCaptureResources ()))
                    link = makeLocalLink (link, mParser.getLexer ().getPage ().getUrl ());
                setFrameLocation (link);
            }
        }
    }

    /**
     * Image tag that rewrites the SRC URL.
     * If resources are being captured the SRC is mapped to a local target if
     * it matches the source, otherwise it is convered to a full URL to point
     * back to the original site.
     */
    class LocalImageTag extends ImageTag
    {
        public void doSemanticAction ()
            throws
                ParserException
        {
            String image;
            
            // get the image url
            image = getImageURL ();
            // check if it needs to be captured
            if (isToBeCaptured (image))
            {   // add the image to the list needing to be copied
                if (!mCopied.contains (image))
                    if (!mImages.contains (image))
                        mImages.add (image);
                if (getCaptureResources ())
                    image = makeLocalLink (image, mParser.getLexer ().getPage ().getUrl ());
                // alter the link
                setImageURL (image);
            }
        }
    }

    /**
     * Base tag that doesn't show.
     * The toHtml() method is overridden to return an empty string,
     * effectively shutting off the base reference.
     */
    class LocalBaseHrefTag extends BaseHrefTag
    {
        // we don't want to have a base pointing back at the source page
        public String toHtml ()
        {
            return ("");
        }
    }

    /**
     * Perform the capture.
     */
    public void capture ()
    {
       
        mPages.clear ();
        mPages.add (getSource ());
        while (0 != mPages.size ())
            try
            {
                process (getFilter ());
                while (0 != mImages.size ())
                    copy ();
            }
            catch (ParserException pe)
            {   // this exception handling is suboptimal,
                // but it messages correctly about broken links
                Throwable throwable;
                
                throwable = pe.getThrowable ();
                if (null != throwable)
                {
                    throwable = throwable.getCause ();
                    if (throwable instanceof FileNotFoundException)
                        System.err.println ("broken link " + ((FileNotFoundException)throwable).getMessage () + " ignored");
                    else
                        pe.printStackTrace ();
                }
                else
                    pe.printStackTrace ();
            }
    }

    /**
     * Mainline to capture a web site locally.
     * @param args The command line arguments.
     * There are three arguments the web site to capture, the local directory
     * to save it to, and a flag (true or false) to indicate whether resources
     * such as images and video are to be captured as well.
     * These are requested via dialog boxes if not supplied.
     * @exception MalformedURLException If the supplied URL is invalid.
     * @exception IOException If an error occurs reading the page or resources.
     */
    public static void main (String[] args)
        throws
            MalformedURLException,
            IOException
    {
        SiteCapturer worker;
        String url;
        JFileChooser chooser;
        URL source;
        String path;
        File target;
        Boolean capture;
        int ret;
        
        worker = new SiteCapturer ();
        if (0 >= args.length)
        {
            url = (String)JOptionPane.showInputDialog (
                null,
                "Enter the URL to capture:",
                "Web Site",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "http://htmlparser.sourceforge.net/wiki");
            if (null != url)
                worker.setSource (url);
            else
                System.exit (1);
        }
        else
            worker.setSource (args[0]);
        if (1 >= args.length)
        {
            url = worker.getSource ();
            source = new URL (url);
            path = new File (new File ("." + File.separator), source.getHost () + File.separator).getCanonicalPath ();
            target = new File (path);
            chooser = new JFileChooser (target);
            chooser.setDialogType (JFileChooser.SAVE_DIALOG);
            chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
            chooser.setSelectedFile (target); // this doesn't frickin' work
            chooser.setMultiSelectionEnabled (false);
            chooser.setDialogTitle ("Target Directory");
            ret = chooser.showSaveDialog (null);
            if (ret == JFileChooser.APPROVE_OPTION)
                worker.setTarget (chooser.getSelectedFile ().getAbsolutePath ());
            else
                System.exit (1);
        }
        else
            worker.setTarget (args[1]);
        if (2 >= args.length)
        {
            capture = (Boolean)JOptionPane.showInputDialog (
                null,
                "Should resources be captured:",
                "Capture Resources",
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[] { Boolean.TRUE, Boolean.FALSE},
                Boolean.TRUE);
            if (null != capture)
                worker.setCaptureResources (capture.booleanValue ());
            else
                System.exit (1);
        }
        else
            worker.setCaptureResources ((Boolean.valueOf (args[2]).booleanValue ()));
        worker.capture ();
        
        System.exit (0);
    }
}
