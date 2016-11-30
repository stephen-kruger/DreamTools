package com.webhiker.dreambox.api;

import java.util.Stack;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DataHandler extends DefaultHandler {
	private Logger log = Logger.getAnonymousLogger();
	// this holds the data
	private JSONObject _data, parent;
	private Stack<JSONObject> stack;
	private StringBuffer builder;
	private String currentElement;

	/**
	 * Returns the data object
	 * 
	 * @return
	 */
	public JSONObject getData() {
		return _data;
	}

	/**
	 * This gets called when the xml document is first opened
	 * 
	 * @throws SAXException
	 */
	@Override
	public void startDocument() throws SAXException {
		// log.info("Start document");
		_data = new JSONObject();
		parent = _data;
		stack = new Stack<JSONObject>();
		stack.add(_data);
	}

	/**
	 * Called when it's finished handling the document
	 * 
	 * @throws SAXException
	 */
	@Override
	public void endDocument() throws SAXException {
		stack.pop();
	}

	/**
	 * This gets called at the start of an element. Here we're also setting the
	 * booleans to true if it's at that specific tag. (so we know where we are)
	 * 
	 * @param namespaceURI
	 * @param localName
	 * @param qName
	 * @param atts
	 * @throws SAXException
	 */
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		super.startElement(namespaceURI, localName, qName, atts);
		builder = new StringBuffer();
		try {
			parent = stack.peek();
			JSONObject jo;
			if (parent.has(qName)) {
				JSONArray ja;
				if (parent.get(qName) instanceof JSONObject) {
					ja = new JSONArray();
					jo = parent.getJSONObject(qName);
					ja.put(jo);
					jo = new JSONObject();
					ja.put(jo);
				} else {
					ja = parent.getJSONArray(qName);
					jo = new JSONObject();
					ja.put(jo);
				}
				parent.put(qName, ja);
			} else {
				jo = new JSONObject();
				parent.put(qName, jo);
			}
			currentElement = qName;
			stack.add(jo);
		} catch (Throwable t) {
			t.printStackTrace();
			log.severe(t.getMessage());
		}
	}

	/**
	 * Called at the end of the element. Setting the booleans to false, so we
	 * know that we've just left that tag.
	 * 
	 * @param namespaceURI
	 * @param localName
	 * @param qName
	 * @throws SAXException
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		super.endElement(namespaceURI, localName, qName);
		try {
			String text = builder.toString().trim();
			if (text.length() > 0) {
				parent.put(currentElement, text);
				builder = new StringBuffer();
			}
			else {
				parent.put(currentElement, "");
				builder = new StringBuffer();
			}
			parent = stack.pop();
		} catch (Exception e) {
			e.printStackTrace();
			log.severe(e.getMessage());
		}
	}

	/**
	 * 
	 * @param ch
	 * @param start
	 * @param length
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		try {
			super.characters(ch, start, length);
		} catch (SAXException e) {
			e.printStackTrace();
			log.severe(e.getMessage());
		}
		String chars = new String(ch, start, length);
		builder.append(chars);
	}

}
