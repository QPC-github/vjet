/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.active.client;

import java.net.URL;

import org.ebayopensource.dsf.active.event.IBrowserBinding;
import org.ebayopensource.dsf.jsnative.Location;

public class ALocation extends ActiveObject implements Location {

	private static final long serialVersionUID = 1L;
//	private static final String[] s_properties = 
//	{
//		"hash",
//		"host",
//		"hostname",
//		"href",
//		"pathname",
//		"port",
//		"protocol",
//		"search"
//	};
//	
//	private static final String[] s_methodNames =
//	{
//		"assign",
//		"reload",
//		"replace"
//	};
	
	private AWindow m_window;
	// Property: hash, R/W 
	private String m_hash = null;
	// Property: host, R/W
	private String m_host = null;
	// Property: hostname, R/W
	private String m_hostname = null;
	// Property: href, R/W
	private String m_href = null;
	// Property: pathname, R/W 
	private String m_pathname = null;
	// Property: port, R/W 
	private String m_port = null;
	// Property: protocol, R/W 
	private String m_protocol = null;
	// Property: search, R/W 
	private String m_search = null;

	public ALocation(AWindow window) {
		m_window = window;
		populateScriptable(ALocation.class, window == null ? null : window.getBrowserType());
	}

	public String getHash() {
		if (m_hash != null) {
			return m_hash;
		}
		if (m_window == null || m_window.getURL() == null) {
			return "";
		}
		String r = m_window.getURL().getRef();
		if (r == null) {
			r = "";
		} else {
			r = "#" + r;
		}
		return r;
	}

	public String getHost() {
		if (m_host != null) {
			return m_host;
		} 
		if (m_window == null || m_window.getURL() == null) {
			return "";
		}
		URL url = m_window.getURL();
		int portNumber = url.getPort();
		String portStr = "";
		if (portNumber > 0) {
			portStr = ":" + portNumber;
		}
		return url.getHost() + portStr;
	}

	public String getHostname() {
		if (m_hostname != null) {
			return m_hostname;
		}
		if (m_window == null || m_window.getURL() == null) {
			return "";
		}
		String h = m_window.getURL().getHost();
		if (h == null) {
			h = "";
		}
		return h;
	}

	public String getHref() {
		if (m_href != null) {
			return m_href;
		} 
		if (m_window == null || m_window.getURL() == null) {
			return "";
		}
		String href = m_window.getURL().toString();
		if (href == null) {
			href = "";
		}
		return href;
	}

	public String getPathname() {
		if (m_pathname != null) {
			return m_pathname;
		} 
		if (m_window == null || m_window.getURL() == null) {
			return "";
		}
		String p = getURLPath(m_window.getURL().toString());
		if (p == null) {
			p = "";
		}
		return p;
		
	}

	public String getPort() {
		if (m_port != null) {
			return m_port;
		}
		if (m_window == null || m_window.getURL() == null) {
			return "";
		}
		int portNumber = m_window.getURL().getPort();
		if (portNumber > 0) {
			return String.valueOf(portNumber);
		} else {
			return "";
		}
	}

	public String getProtocol() {
		if (m_protocol != null) {
			return m_protocol;
		} 
		if (m_window == null || m_window.getURL() == null) {
			return "";
		}
		String p = m_window.getURL().getProtocol();
		if (p == null) {
			p = "";
		} else {
			p = p + ":";
		}
		return p;
		
	}

	public String getSearch() {
		if (m_search != null) {
			return m_search;
		} 
		if (m_window == null || m_window.getURL() == null) {
			return "";
		}
		String q = getURLQuery(m_window.getURL().toString());
		if (q == null) {
			q = "";
		}
		return q;
		
	}
	
	public Object valueOf(String type) {
		if (type.equals("boolean")) {
			return Boolean.TRUE;
		}
		else if (type.equals("string") || type.equals("undefined")) {
			return getHref();
		}
		return null;
	}

	public void setHash(String hash) {
		this.m_hash = hash;
		if (getBrowserBinding() != null) {
			getBrowserBinding().setDocumentProperty(
					"location.hash", "\""+hash+"\"");
		}
	}

	public void setHost(String host) {
		this.m_host = host;
		if (getBrowserBinding() != null) {
			getBrowserBinding().setDocumentProperty(
					"location.host", "\""+host+"\"");
		}
	}

	public void setHostname(String hostname) {
		this.m_hostname = hostname;
		if (getBrowserBinding() != null) {
			getBrowserBinding().setDocumentProperty(
					"location.hostname", "\""+hostname+"\"");
		}
	}

	public void setHref(String href) {
		this.m_href = href;
		if (getBrowserBinding() != null) {
			getBrowserBinding().setDocumentProperty(
					"location.href", "\""+href+"\"");
		}
	}

	public void setPathname(String pathname) {
		this.m_pathname = pathname;
		if (getBrowserBinding() != null) {
			getBrowserBinding().setDocumentProperty(
					"location.pathname", "\""+pathname+"\"");
		}
	}

	public void setPort(String port) {
		this.m_port = port;
		if (getBrowserBinding() != null) {
			getBrowserBinding().setDocumentProperty(
					"location.port", "\""+port+"\"");
		}
	}

	public void setProtocol(String protocol) {
		this.m_protocol = protocol;
		if (getBrowserBinding() != null) {
			getBrowserBinding().setDocumentProperty(
					"location.protocol", "\""+protocol+"\"");
		}
	}

	public void setSearch(String search) {
		this.m_search = search;
		if (getBrowserBinding() != null) {
			getBrowserBinding().setDocumentProperty(
					"location.search", "\""+search+"\"");
		}
	}
	
	public void assign(String url) {
		if (getBrowserBinding() != null) {
			getBrowserBinding().locationAssign(url);
		}
	}

	public void reload() {
		if (getBrowserBinding() != null) {
			getBrowserBinding().locationReload(false);
		}
	}
	
	public void reload(boolean forceGet) {
		if (getBrowserBinding() != null) {
			getBrowserBinding().locationReload(forceGet);
		}
	}

	public void replace(String url) {
		if (getBrowserBinding() != null) {
			getBrowserBinding().locationReplace(url);
		}
	}
	
	public AWindow getWindow() {
		return m_window;
	}
	
	
	
	@Override
	public String toString() {
		return getHref();
	}

	private String getURLPath(String u) {
		if (u == null || u.trim().length() <= 0)
			return "";

		String s = u;
		int i = s.lastIndexOf(';');
		if (i > -1)
			s = s.substring(0, i);

		i = s.lastIndexOf('#');
		if (i > -1)
			s = s.substring(0, i);

		i = s.indexOf("://");
		if (i > -1)
			s = s.substring(i + 3);

		i = s.indexOf('/');
		if (i > -1)
			s = s.substring(i);
		else
			s = "/";

		i = s.indexOf('?');
		if (i > -1)
			s = s.substring(0, i);

		return (s);
	}
	
	private IBrowserBinding getBrowserBinding() {
		if (m_window == null || m_window.getBrowserBinding() == null) {
			return null;
		}
		return m_window.getBrowserBinding();
	}
	
	private String getURLQuery(String u) {
		if (u == null || u.trim().length() <= 0)
			return "";

		String s = u;
		int i = s.indexOf('?');
		if (i < 0)
			return "";

		s = s.substring(i);
		i = s.lastIndexOf('#');
		if (i > -1)
			s = s.substring(0, i);

		return (s);
	}
}
