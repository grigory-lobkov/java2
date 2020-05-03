package ru.progwards.java2.lessons.patterns.proxy;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;

public class TestGPX {

    //final static IGpsStreamer streamer = new GpsStreamer();
    final static IGpsStreamer streamer = new GpsProxy(new GpsStreamer());
    final static GpsProcessor processor = new GpsProcessor(streamer);

    final static int LIMIT_COUNT = 10_000;
    static int count = 0;

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, InterruptedException {
        //Thread thread = new Thread(new GenerateData());
        //thread.start();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        AdvancedXMLHandler handler = new AdvancedXMLHandler();
        parser.parse(new File("C:\\Users\\Grigory\\IdeaProjects\\java2\\src\\ru\\progwards\\java2\\lessons\\patterns\\proxy\\GPS_track_Save-the-Elephants.gpx"), handler);

        //thread.interrupt();
        Thread.sleep(100);
        processor.interrupt();
    }

    private static class AdvancedXMLHandler extends DefaultHandler {
        private String lat, lon, time, lastElementName;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(qName.equals("trkpt")) {
                lat = attributes.getValue("lat");
                lon = attributes.getValue("lon");
            }
            lastElementName = qName;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String information = new String(ch, start, length);

            if (!information.isEmpty())
                if (lastElementName.equals("time"))
                    time = information;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("trkpt")) {
                double p1 = Double.valueOf(lat);
                double p2 = Double.valueOf(lon);
                Date p3 = Date.from(Instant.parse(time));
                try {
                    if(count++<LIMIT_COUNT)
                        streamer.add(new GPS(p1, p2, p3.getTime()));
                } catch (InterruptedException e) {
                }
            }
        }
    }

}
