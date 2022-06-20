package de.fhws.indoor.libsmartphoneindoormap.parser;

import android.app.AlertDialog;
import android.content.Context;

import org.w3c.dom.Attr;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.fhws.indoor.libsmartphoneindoormap.model.AccessPoint;
import de.fhws.indoor.libsmartphoneindoormap.model.Beacon;
import de.fhws.indoor.libsmartphoneindoormap.model.Floor;
import de.fhws.indoor.libsmartphoneindoormap.model.MacAddress;
import de.fhws.indoor.libsmartphoneindoormap.model.Map;
import de.fhws.indoor.libsmartphoneindoormap.model.RadioModel;
import de.fhws.indoor.libsmartphoneindoormap.model.UWBAnchor;
import de.fhws.indoor.libsmartphoneindoormap.model.Vec3;
import de.fhws.indoor.libsmartphoneindoormap.model.Wall;

public class XMLMapParser {
    private final Context context;

    public XMLMapParser(Context context) {
        this.context = context;
    }

    public Map parse(final InputStream inputStream) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        Map m = null;

        try {
            SAXParser parser = factory.newSAXParser();

            ParseHandler handler = new ParseHandler();
            parser.parse(inputStream, handler);
            m = handler.getResult();

        } catch (SAXParseException e) {
            new AlertDialog.Builder(context)
                    .setTitle("Parse Error")
                    .setMessage(e.getMessage() + "\n" +
                            "Line: " + e.getLineNumber() + "\n" +
                            "Column: " + e.getColumnNumber())
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // do nothing
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return m;
    }

    private static class ParseHandler extends DefaultHandler {
        private Locator locator;
        private final StringBuilder currentValue = new StringBuilder();

        private Map map = null;
        private Floor currentFloor = null;
        private Wall currentWall = null;
        private AccessPoint currentAP = null;
        private UWBAnchor currentUWB = null;
        private Beacon currentBeacon = null;

        public Map getResult() {
            return map;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        @Override
        public void startDocument() {
            System.out.println("Start Document");
        }

        @Override
        public void endDocument() {
            System.out.println("End Document");
        }

        @Override
        public void startElement(
                String uri,
                String localName,
                String qName,
                Attributes attributes) throws SAXParseException {

            // reset the tag value
            currentValue.setLength(0);

            System.out.printf("Start Element : %s%n", qName);

            try {
                switch (qName) {
                    case "map":
                        assert map == null;
                        map = new Map();
                        // get tag's attribute by name
                        String id = attributes.getValue("id");
                        System.out.printf("Staff id : %s%n", id);
                        break;

                    case "floor":
                        assert currentFloor == null;
                        currentFloor = new Floor();
                        currentFloor.setAtHeight(parseFloat(attributes, "atHeight"));
                        currentFloor.setHeight(parseFloat(attributes, "height"));
                        currentFloor.setName(attributes.getValue("name"));
                        break;

                    case "wall":
                    case "line":
                        assert currentWall == null;
                        currentWall = new Wall();
                        currentWall.p0.x = parseFloat(attributes, "x1");
                        currentWall.p0.y = parseFloat(attributes, "y1");
                        currentWall.p1.x = parseFloat(attributes, "x2");
                        currentWall.p1.y = parseFloat(attributes, "y2");
                        currentWall.thickness = parseFloat(attributes, "thickness");
                        break;

                    case "accesspoint":
                        assert currentAP == null;
                        currentAP = new AccessPoint();
                        currentAP.name = attributes.getValue("name");
                        currentAP.mac = new MacAddress(attributes.getValue("mac"));
                        currentAP.hasFtm = parseBoolean(attributes, "hasFtm", false);
                        currentAP.position = parsePosition(attributes);
                        currentAP.mdl = parseRadioModel(attributes);
                        break;

                    case "uwbanchor":
                        assert currentUWB == null;
						MacAddress uwbBleMac = null;
						try {
							uwbBleMac = new MacAddress(attributes.getValue("bleMac"));
						} catch(Exception e) {}
                        currentUWB = new UWBAnchor(
                                attributes.getValue("name"),
                                attributes.getValue("deviceId"),
                                uwbBleMac,
                                parsePosition(attributes));
                        break;

                    case "beacon":
                        assert currentBeacon == null;
                        currentBeacon = new Beacon();
                        currentBeacon.name = attributes.getValue("name");
                        currentBeacon.mac = new MacAddress(attributes.getValue("mac"));
                        currentBeacon.major = attributes.getValue("major");
                        currentBeacon.minor = attributes.getValue("minor");
                        currentBeacon.uuid = attributes.getValue("uuid");
                        currentBeacon.position = parsePosition(attributes);
                        currentBeacon.mdl = parseRadioModel(attributes);
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
                throw new SAXParseException(e.getMessage(), locator);
            }
        }

        @Override
        public void endElement(String uri,
                               String localName,
                               String qName) {

            System.out.printf("End Element : %s%n", qName);

            switch (qName) {
                case "floor":
                    assert currentFloor != null;
                    map.addFloor(currentFloor);
                    currentFloor = null;
                    break;

                case "wall":
                case "line":
                    assert currentFloor != null;
                    assert currentWall != null;
                    currentFloor.addWall(currentWall);
                    currentWall = null;
                    break;

                case "accesspoint":
                    assert currentFloor != null;
                    assert currentAP != null;
                    currentFloor.addAP(currentAP);
                    currentAP = null;
                    break;

                case "uwbanchor":
                    assert currentFloor != null;
                    assert currentUWB != null;
                    currentFloor.addUWB(currentUWB);
                    currentUWB = null;
                    break;

                case "beacon":
                    assert currentFloor != null;
                    assert currentBeacon != null;
                    currentFloor.addBeacon(currentBeacon);
                    currentBeacon = null;
                    break;

                default:
                    break;
            }
        }

        // http://www.saxproject.org/apidoc/org/xml/sax/ContentHandler.html#characters%28char%5B%5D,%20int,%20int%29
        // SAX parsers may return all contiguous character data in a single chunk,
        // or they may split it into several chunks
        @Override
        public void characters(char ch[], int start, int length) {
            // append it, works for single or multiple calls
            currentValue.append(ch, start, length);
        }

        public float parseFloat(Attributes attributes, final String name) {
            try {
                return Float.parseFloat(attributes.getValue(name));
            } catch (NumberFormatException e) {
                return Float.NaN;
            }
        }

        public boolean parseBoolean(Attributes attributes, final String name, final boolean defaultValue) {
            String value = attributes.getValue(name);
            if(value == null) { return defaultValue; }
            return Boolean.parseBoolean(value);
        }

        public Vec3 parsePosition(Attributes attributes) {
            return new Vec3(
                    parseFloat(attributes, "x"),
                    parseFloat(attributes, "y"),
                    parseFloat(attributes, "z"));
        }

        public RadioModel parseRadioModel(Attributes attributes) {
            RadioModel rm = new RadioModel();
            rm.txp = parseFloat(attributes, "mdl_txp");
            rm.exp = parseFloat(attributes, "mdl_exp");
            rm.waf = parseFloat(attributes, "mdl_waf");
            return rm;
        }
    }
}
