package ca.surveillancerights.surveillancewatch.httpd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import android.content.res.AssetManager;

public class AssetsHTTPD extends NanoHTTPD {
	
	AssetManager am;
	String wwwroot;

	public AssetsHTTPD(int port, AssetManager am, String wwwroot) throws IOException {
		super(port, null);
		this.am = am;
		this.wwwroot = wwwroot;
	}
	
	public Response serveFile(String uri, Properties header, File homeDir,
			boolean allowDirectoryListing) {
		Response res = null;

		
		if (res == null) {
			// Remove URL arguments
			uri = uri.trim().replace(File.separatorChar, '/');
			if (uri.indexOf('?') >= 0)
				uri = uri.substring(0, uri.indexOf('?'));

			// Prohibit getting out of current directory
			if (uri.startsWith("..") || uri.endsWith("..")
					|| uri.indexOf("../") >= 0)
				res = new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT,
						"FORBIDDEN: Won't serve ../ for security reasons.");
		}

		InputStream f = null;
		long fileLen = -1;
		try {
			f = am.open(wwwroot + uri);
			fileLen = f.available();
		
			//File f = new File(homeDir, uri);
			if (res == null && fileLen <= 0)
				res = new Response(HTTP_NOTFOUND, MIME_PLAINTEXT,
						"Error 404, file not found.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res = new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT,
					e.getMessage());
		}

		try {
			if (res == null) {
				// Get MIME type from file name extension, if possible
				String mime = null;
				int dot = uri.lastIndexOf('.');
				if (dot >= 0)
					mime = (String) theMimeTypes.get(uri
							.substring(dot + 1).toLowerCase());
				if (mime == null)
					mime = MIME_DEFAULT_BINARY;

				// Calculate etag
				String etag = Integer.toHexString((uri).hashCode());

				// Support (simple) skipping:
				long startFrom = 0;
				long endAt = -1;
				String range = header.getProperty("range");
				if (range != null) {
					if (range.startsWith("bytes=")) {
						range = range.substring("bytes=".length());
						int minus = range.indexOf('-');
						try {
							if (minus > 0) {
								startFrom = Long.parseLong(range.substring(0,
										minus));
								endAt = Long.parseLong(range
										.substring(minus + 1));
							}
						} catch (NumberFormatException nfe) {
						}
					}
				}

				// Change return code and add Content-Range header when skipping
				// is requested
				
				
				if (range != null && startFrom >= 0) {
					if (startFrom >= fileLen) {
						res = new Response(HTTP_RANGE_NOT_SATISFIABLE,
								MIME_PLAINTEXT, "");
						res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
						res.addHeader("ETag", etag);
					} else {
						if (endAt < 0)
							endAt = fileLen - 1;
						long newLen = endAt - startFrom + 1;
						if (newLen < 0)
							newLen = 0;

						final long dataLen = newLen;
						f.skip(startFrom);

						res = new Response(HTTP_PARTIALCONTENT, mime, f);
						res.addHeader("Content-Length", "" + dataLen);
						res.addHeader("Content-Range", "bytes " + startFrom
								+ "-" + endAt + "/" + fileLen);
						res.addHeader("ETag", etag);
					}
				} else {
					if (etag.equals(header.getProperty("if-none-match")))
						res = new Response(HTTP_NOTMODIFIED, mime, "");
					else {
						res = new Response(HTTP_OK, mime, f);
						res.addHeader("Content-Length", "" + fileLen);
						res.addHeader("ETag", etag);
					}
				}
			}
		} catch (IOException ioe) {
			res = new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT,
					"FORBIDDEN: Reading file failed.");
		}

		res.addHeader("Accept-Ranges", "bytes"); // Announce that the file
													// server accepts partial
													// content requestes
		return res;
	}

}
