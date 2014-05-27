package com.tinystranger.lcbohelper.app;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class LCBOQueryParser {

    public class LCBOHandler extends DefaultHandler
    {
        public List<LCBOEntity> entries;
        protected String text;

        @Override
        public void endDocument() throws SAXException {
            // Things to do on end
        }

        @Override
        public void startDocument() throws SAXException {
            entries = new ArrayList<LCBOEntity>();
            text = null;
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
    public class StoresHandler extends LCBOHandler {

        /**
         * Gets be called on opening tags, <xml> Can have attibutes, <number
         * thenumber="VALUE">
         */

        @Override
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes attributes) throws SAXException {
            //Log.d("db", "start " + qName );
            text = null;
            if (qName.equalsIgnoreCase("store"))
            {
                entries.add(new LCBOEntity());
            }
        }

        /**
         * Gets be called on closing tags, </xml>
         */

        @Override
        public void endElement(String namespaceURI, String localName, String qName)
                throws SAXException {
            //Log.d("db", "end " + qName + " " + text);
            if (qName == null || qName.length() == 0 || text == null || text.length() == 0)
                return;

            if (qName.equalsIgnoreCase("locationNumber"))
                entries.get(entries.size() - 1).locationNumber = text;
            else if (qName.equalsIgnoreCase("itemName"))
                entries.get(entries.size() - 1).itemName = text;
            else if (qName.equalsIgnoreCase("locationName"))
                entries.get(entries.size() - 1).locationName = text;
            else if (qName.equalsIgnoreCase("locationAddress1"))
                entries.get(entries.size() - 1).locationAddress1 = text;
            else if (qName.equalsIgnoreCase("locationIntersection"))
                entries.get(entries.size() - 1).locationIntersection = text;
            else if (qName.equalsIgnoreCase("locationCityName"))
                entries.get(entries.size() - 1).locationCityName = text;
            else if (qName.equalsIgnoreCase("latitude"))
                try {
                    entries.get(entries.size() - 1).latitude = Float.parseFloat(text);
                } catch (NumberFormatException e) {
                }
            else if (qName.equalsIgnoreCase("longitude"))
                try {
                    entries.get(entries.size() - 1).longitude = Float.parseFloat(text);
                } catch (NumberFormatException e) {
                }
            else if (qName.equalsIgnoreCase("distance"))
                try {
                    entries.get(entries.size() - 1).distance = Float.parseFloat(text);
                } catch (NumberFormatException e) {
                }
            else if (qName.equalsIgnoreCase("productQuantity"))
                try {
                    entries.get(entries.size() - 1).productQuantity = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                }
            else if (qName.equalsIgnoreCase("sundayCloseHour"))
                entries.get(entries.size() - 1).sundayCloseHour = text.substring(0, 5);
            else if (qName.equalsIgnoreCase("sundayOpenHour"))
                entries.get(entries.size() - 1).sundayOpenHour = text.substring(0, 5);
            else if (qName.equalsIgnoreCase("mondayCloseHour"))
                entries.get(entries.size() - 1).mondayCloseHour = text.substring(0, 5);
            else if (qName.equalsIgnoreCase("mondayOpenHour"))
                entries.get(entries.size() - 1).mondayOpenHour = text.substring(0, 5);
            else if (qName.equalsIgnoreCase("tuesdayCloseHour"))
                entries.get(entries.size() - 1).tuesdayCloseHour = text.substring(0, 5);
            else if (qName.equalsIgnoreCase("tuesdayOpenHour"))
                entries.get(entries.size() - 1).tuesdayOpenHour = text.substring(0, 5);
            else if (qName.equalsIgnoreCase("wednesdayCloseHour"))
                entries.get(entries.size() - 1).wednesdayCloseHour = text.substring(0, 5);
            else if (qName.equalsIgnoreCase("wednesdayOpenHour"))
                entries.get(entries.size() - 1).wednesdayOpenHour = text.substring(0, 5);
            else if (qName.equalsIgnoreCase("thursdayCloseHour"))
                entries.get(entries.size() - 1).thursdayCloseHour = text.substring(0, 5);
            else if (qName.equalsIgnoreCase("thursdayOpenHour"))
                entries.get(entries.size() - 1).thursdayOpenHour = text.substring(0, 5);
            else if (qName.equalsIgnoreCase("fridayCloseHour"))
                entries.get(entries.size() - 1).fridayCloseHour = text.substring(0, 5);
            else if (qName.equalsIgnoreCase("fridayOpenHour"))
                entries.get(entries.size() - 1).fridayOpenHour = text.substring(0, 5);
            else if (qName.equalsIgnoreCase("saturdayCloseHour"))
                entries.get(entries.size() - 1).saturdayCloseHour = text.substring(0, 5);
            else if (qName.equalsIgnoreCase("saturdayOpenHour"))
                entries.get(entries.size() - 1).saturdayOpenHour = text.substring(0, 5);
            else if (qName.equalsIgnoreCase("phoneNumber1"))
                entries.get(entries.size() - 1).phoneNumber1 = text;
            else if (qName.equalsIgnoreCase("phoneAreaCode"))
                entries.get(entries.size() - 1).phoneAreaCode = text;
        }
    }

    public class ProductHandler extends LCBOHandler {

        /**
         * Gets be called on opening tags, <xml> Can have attibutes, <number
         * thenumber="VALUE">
         */

        @Override
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes attributes) throws SAXException {
            //Log.d("db", "start " + qName );
            text = null;
            if (qName.equalsIgnoreCase("product"))
            {
                entries.add(new LCBOEntity());
            }
        }

        /**
         * Gets be called on closing tags, </xml>
         */

        @Override
        public void endElement(String namespaceURI, String localName, String qName)
                throws SAXException {
            //Log.d("db", "end " + qName + " " + text);
            if (qName == null || qName.length() == 0 || text == null || text.length() == 0)
                return;

            if (qName.equalsIgnoreCase("itemNumber"))
                entries.get(entries.size()-1).itemNumber = text;
            else if (qName.equalsIgnoreCase("itemName"))
                entries.get(entries.size()-1).itemName = text;
            else if (qName.equalsIgnoreCase("productSize"))
                entries.get(entries.size()-1).productSize = text;
            else if (qName.equalsIgnoreCase("stockType"))
                entries.get(entries.size()-1).stockType = text;
            else if (qName.equalsIgnoreCase("price")) {
                try {
                    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.CANADA);
                    entries.get(entries.size() - 1).price = text;
                    text = text.replace(" ", "");
                    entries.get(entries.size() - 1).priceNumber = numberFormat.parse(text);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            else if (qName.equalsIgnoreCase("productQuantity"))
                try {
                    entries.get(entries.size() - 1).productQuantity = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                }
            else if (qName.equalsIgnoreCase("producer"))
                entries.get(entries.size()-1).producer = text;
            else if (qName.equalsIgnoreCase("producingRegion"))
                entries.get(entries.size()-1).producingRegion = text;
            else if (qName.equalsIgnoreCase("producingCountry"))
                entries.get(entries.size()-1).producingCountry = text;
            else if (qName.equalsIgnoreCase("airMiles"))
                entries.get(entries.size()-1).airMiles = Boolean.parseBoolean(text);
            else if (qName.equalsIgnoreCase("limitedTimeOffer"))
                entries.get(entries.size()-1).limitedTimeOffer = Boolean.parseBoolean(text);
            else if (qName.equalsIgnoreCase("wineStyle"))
                entries.get(entries.size()-1).wineStyle = text;
            else if (qName.equalsIgnoreCase("sweetnessDescriptor"))
                entries.get(entries.size()-1).sweetnessDescriptor = text;
            else if (qName.equalsIgnoreCase("wineVerietal"))
                entries.get(entries.size()-1).wineVerietal = text;
            else if (qName.equalsIgnoreCase("itemDescription"))
                entries.get(entries.size()-1).itemDescription = text;
            else if (qName.equalsIgnoreCase("upcNumber"))
                entries.get(entries.size()-1).upcNumber = text;

        }
    }
    // see http://www.androidsnippets.com/simple-xml-parsing

    public enum QueryType
    {
        kProducts,
        kStores
    };

    public List<LCBOEntity> parse(QueryType aType, InputStream in) throws IOException {
        try {

            SAXParserFactory saxPF = SAXParserFactory.newInstance();
            SAXParser saxP = saxPF.newSAXParser();
            XMLReader xmlR = saxP.getXMLReader();
            LCBOHandler myXMLHandler = null;
            if (aType == QueryType.kProducts) {
                myXMLHandler = new ProductHandler();
            }
            else
            {
                myXMLHandler = new StoresHandler();
            }
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