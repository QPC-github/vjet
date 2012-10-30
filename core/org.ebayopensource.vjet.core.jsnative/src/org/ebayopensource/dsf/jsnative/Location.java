/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jsnative;

import org.ebayopensource.dsf.jsnative.anno.Function;
import org.ebayopensource.dsf.jsnative.anno.JsMetatype;
import org.ebayopensource.dsf.jsnative.anno.JsSupport;
import org.ebayopensource.dsf.jsnative.anno.JsVersion;
import org.ebayopensource.dsf.jsnative.anno.JstMultiReturn;
import org.ebayopensource.dsf.jsnative.anno.OverLoadFunc;
import org.ebayopensource.dsf.jsnative.anno.Property;
import org.mozilla.mod.javascript.IWillBeScriptable;

/**
 * JavaScript object representing window's location object.
 */
@JsSupport( JsVersion.MOZILLA_ONE_DOT_ZERO)
@JsMetatype
public interface Location extends IWillBeScriptable {
	
	// Properties
	
	/**
	 * Returns the URL from the hash sign (#)
	 * @return
	 */
	@Property String getHash();
	
	/**
	 * Sets the URL from the hash sign (#)
	 * @param hash
	 */
	@Property void setHash(String hash);
	
	/**
	 * Returns the hostname and port number of the current URL
	 * @return
	 */
	@Property String getHost();
	
	/**
	 * Sets the hostname and port number of the current URL
	 * @param host
	 */
	@Property void setHost(String host);
	
	/**
	 * Returns the hostname of the current URL
	 * @return
	 */
	@Property String getHostname();
	
	/**
	 * Sets the hostname of the current URL
	 * @param hostname
	 */
	@Property void setHostname(String hostname);
	
	/**
	 * Returns the entire URL
	 * @return
	 */
	@Property String getHref();
	
	/**
	 * Sets the entire URL
	 * @param href
	 */
	@Property void setHref(String href);
	
	/**
	 * Returns the path (relative to the host)
	 * @return
	 */
	@Property String getPathname();
	
	/**
	 * Sets the path (relative to the host)
	 * @param pathname
	 */
	@Property void setPathname(String pathname);
	
	/**
	 * Returns the port number of the URL
	 * @return
	 */
	@Property String getPort();
	
	/**
	 * Sets the port number of the URL
	 * @param port
	 */
	@Property void setPort(String port);
	
	/**
	 * Returns the protocol of the URL
	 * @return
	 */
	@Property String getProtocol();
	
	/**
	 * Sets the protocol of the URL
	 * @param protocol
	 */
	@Property void setProtocol(String protocol);
	
	/**
	 * Returns the part of the URL that follows the ? symbol, 
	 * including the ? symbol
	 * @return
	 */
	@Property String getSearch();
	
	/**
	 * Sets the part of the URL that follows the ? symbol
	 * @param search
	 */
	@Property void setSearch(String search);
	
	/**
	 * Reload the document from the current URL. forceget is a boolean, 
	 * which, when it is true, causes the page to always be reloaded from 
	 * the server. If it is false or not specified, the browser may reload 
	 * the page from its cache.
	 * @param forceGet
	 */
	@JsSupport( JsVersion.MOZILLA_ONE_DOT_ONE)
	@OverLoadFunc void reload(boolean forceGet);
	
	@JsSupport( JsVersion.MOZILLA_ONE_DOT_ONE)
	@OverLoadFunc void reload();


	/**
	 * Load the document from the provided URL
	 * @param url
	 */
	@Function void assign(String url);
	
	/**
	 * Replace the current document with the one at the provided URL. 
	 * The difference from the assign() method is that after using replace() 
	 * the current page will not be saved in session history, meaning the user 
	 * won't be able to use the Back button to navigate to it.
	 * @param url
	 */
	@JsSupport( JsVersion.MOZILLA_ONE_DOT_ONE)
	@Function void replace(String url);
	
	/**
     * Only for Rhino support
     * @param type
     * @return
     */
    @Function Object valueOf(String type);
}
