package com.tinystranger.lcbohelper.app;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by chris on 5/24/2014.
 */
public class ProductCompletionParser {
    public class CompletionHandler extends DefaultHandler {
        public List<String> entries;
        protected String text;

        @Override
        public void endDocument() throws SAXException {
            // Things to do on end
        }

        @Override
        public void startDocument() throws SAXException {
            entries = new ArrayList<String>();
            text = null;
        }

        @Override
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes attributes) throws SAXException {
            //Log.d("db", "start " + qName );
            text = null;
            if (qName.equalsIgnoreCase("itemName")) {
                entries.add(null);
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName)
                throws SAXException {
            //Log.d("db", "end " + qName + " " + text);
            if (qName == null || qName.length() == 0 || text == null || text.length() == 0)
                return;

            if (qName.equalsIgnoreCase("itemName"))
                entries.set(entries.size() - 1, text);
        }

        /**
         * Gets called when tags have data in between, <text>DATA</text>
         */
        @Override
        public void characters(char ch[], int start, int length) {
            // When the parser is inside the text tag, title tags and xml tags get
            // the text inside
            String frag = new String(ch, start, length);
            if (null == text)
                text = frag;
            else
                text += frag;
        }
    }

    public List<String> parse(InputStream in) throws IOException {
        try {

            SAXParserFactory saxPF = SAXParserFactory.newInstance();
            SAXParser saxP = saxPF.newSAXParser();
            XMLReader xmlR = saxP.getXMLReader();
            CompletionHandler myXMLHandler = new CompletionHandler();
            xmlR.setContentHandler(myXMLHandler);
            xmlR.parse(new InputSource(in));
            return myXMLHandler.entries;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        return null;
    }
}
