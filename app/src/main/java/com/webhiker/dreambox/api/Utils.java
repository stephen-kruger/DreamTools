package com.webhiker.dreambox.api;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class Utils {

	public static JSONObject convertXML(InputStream is) throws IOException {
		try { 
			SAXParserFactory spf = SAXParserFactory.newInstance(); 
			SAXParser sp = spf.newSAXParser(); 

			XMLReader xr = sp.getXMLReader(); 

			DataHandler dataHandler = new DataHandler(); 
			xr.setContentHandler(dataHandler); 

			xr.parse(new InputSource(is)); 
			is.close();
			return dataHandler.getData(); 

		} 
		catch(Throwable ioe) { 
			ioe.printStackTrace();
		} 
		finally {
			is.close();
		}
		return null;
	}
	
	public static void validateResponse(URLConnection uconn) throws IOException {
		if (uconn==null)
			new IOException("Invalid connection specified");
		if (uconn instanceof HttpURLConnection) {
			HttpURLConnection conn = (HttpURLConnection)uconn;
			if ((conn.getResponseCode()!=204)&
					(conn.getResponseCode()!=200)) {
				throw new IOException("Unexpected reply from Dreambox :"+conn.getResponseCode()+" "+conn.getResponseMessage());
			}
		}
		else {
			throw new IOException("Unknown connection type :"+uconn.getClass().getName());
		}
	}

	public static StringBuffer getStringBuffer(InputStream entity) throws IOException {
		StringBuffer sb = new StringBuffer();
		int byteRead;
		while((byteRead=entity.read())>0) {
			sb.append((char)byteRead);
		}
		return sb;
	}

	// -----   xml utils

	/**
	 * Return an Element given a Document, tag name, and index
	 */
	public static Element getElement(Document doc , String tagName , int index ){
		//given an XML document and a tag
		//return an Element at a given index
		NodeList rows =	doc.getDocumentElement().getElementsByTagName(tagName);
		return (Element)rows.item( index );
	}

	/**
	 * Return an Element given a Element, tag name, and index
	 */
	public static Element getElement(Element element , String tagName , int index ){
		//given an XML document and a tag
		//return an Element at a given index
		NodeList rows =	element.getElementsByTagName(tagName);
		return (Element)rows.item( index );
	}

	public static String getTextElement(Element element , String tagName , int index ){
		//given an XML document and a tag
		//return an Element at a given index
		NodeList rows =	element.getElementsByTagName(tagName);
		Element e =  (Element)rows.item( index );
		if (e != null) {
			return e.getTextContent();
		}
		else {
			return "";
		}
	}

	public static String getValue( Element e , String tagName ){
		try{
			//get node lists of a tag name from a Element
			NodeList elements = e.getElementsByTagName( tagName );
			Node node = elements.item( 0 );
			NodeList nodes = node.getChildNodes();

			//find a value whose value is non-whitespace
			String s;
			for( int i=0; i<nodes.getLength(); i++){
				s = ((Node)nodes.item( i )).getNodeValue().trim();
				if(s.equals("") || s.equals("\r")) {
					continue;
				}
				else {
					return s;
				}
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}

	public static int getSize( Document doc , String tagName ){
		return getSize(doc.getDocumentElement(),tagName);
	}

	public static int getSize( Element element , String tagName ){
		NodeList rows =  element.getElementsByTagName(tagName);
		return rows.getLength();
	}

	public static void prettyPrint(Element doc, OutputStream out) {

		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer serializer;
		try {
			serializer = tfactory.newTransformer();
			//Setup indenting to "pretty print"
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			serializer.transform(new DOMSource(doc), new StreamResult(out));
		} catch (TransformerException e) {
			// this is fatal, just dump the stack and throw a runtime exception
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	public static String decode(String str) {
		return str.replace("&amp;","&").replace("&lt","<").replace("&gt",">");
	}
	
	public static void spoolJSONObject(JSONObject jo, FileOutputStream fileOutputStream) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
		writer.write(jo.toString());
		writer.close();
		
	}

	public static JSONObject loadJSON(InputStream is) throws JSONException, IOException {
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(is, "UTF-8");
		int read;
		do {
		  read = in.read(buffer, 0, buffer.length);
		  if (read>0) {
		    out.append(buffer, 0, read);
		  }
		} while (read>=0);
		return new JSONObject(out.toString());
	}
}
