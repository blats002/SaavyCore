/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.dom;


import org.saavy.platform.net.Packet;
import org.saavy.zip.ZipString;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rgsaavedra
 */
public class SaavyElement {

    private ArrayList<SaavyElement> guiElements;
    private SaavyElement parent;
    private SaavyHashMap<String, Object> props;
    private ZipString text;

    public SaavyElement(String name){
        this();
        setName(name);
    }

    public SaavyElement(String name, String text){
        this(name);
        setText(text);
    }

    public SaavyElement() {
        guiElements = new ArrayList<SaavyElement>(0);
        props = new SaavyHashMap<String, Object>();
        props.put("", "");
        text = new ZipString();
    }

    private void copyAttributes(SaavyElement element) {
        props.clear();
        for (String key : element.props.keySet()) {
            setRawAttribute(key, element.getRawObjectAttribute(key, null));
        }
//        props = (HashMap<String, Object>) element.props.clone();
    }

    public String getAttribute(int i) {
        return String.valueOf(new ArrayList(props.values()).get(i));
    }

    public ArrayList<SaavyElement> getChildrenWithAttributeRegex(String attribute, String regex) {
        ArrayList<SaavyElement> list = new ArrayList<SaavyElement>();
        for (SaavyElement guiElement : guiElements) {
            if (guiElement.getAttribute(attribute).matches(regex)) {
                list.add(guiElement);
            }
        }
        return list;
    }

    public Object getObjAttribute(int i) {
        return new ArrayList(props.values()).get(i);
    }

    public SaavyElement getChild(String name, int index) {
        return getChildren(name).get(index);
    }

    public SaavyElement getChild(String name) {
        for (SaavyElement guiElement : guiElements) {
            if (guiElement.getName().equalsIgnoreCase(name)) {
                return guiElement;
            }
        }
        return new SaavyElement();
    }

    public boolean hasChildren() {
        return guiElements.size() > 0;
    }

    public boolean hasChildren(String name) {
        return getChildren(name).size() > 0;
    }

    public ArrayList<SaavyElement> getChildren(String name) {
        ArrayList<SaavyElement> list = new ArrayList<SaavyElement>();
        for (SaavyElement guiElement : guiElements) {
            if (guiElement.getName().equalsIgnoreCase(name)) {
                list.add(guiElement);
            }
        }
        return list;
    }

    public SaavyElement removeChild(String string) {
        for (int x = 0; x < guiElements.size(); x++) {
            SaavyElement guiElement = guiElements.get(x);
            if (guiElement.getName().equalsIgnoreCase(string)) {
                return guiElements.remove(x);
            }
        }
        return null;
    }

    public void removeChildren() {
        guiElements.clear();
    }

    public void removeChildren(String string) {
        for (SaavyElement removeChild : getChildren(string)) {
            guiElements.remove(removeChild);
        }
    }

    public void setName(String name) {
        if (name != null) {
            setRawAttribute("", name.toLowerCase());
        }
    }

    public String getName() {
        return (String) getRawObjectAttribute("", null);
    }

    public void addChildren(int index, SaavyElement element) {
        if (element.getParent() == null) {
            element.setParent(this);
        }
        guiElements.add(index, element);
    }

    public void addChildren(SaavyElement element) {
        if (element.getParent() == null) {
            element.setParent(this);
        }
        guiElements.add(element);
    }

    public ArrayList<SaavyElement> getChildren() {
        return guiElements;
    }

    public void setParent(SaavyElement parent) {
        this.parent = parent;
    }

    public SaavyElement getParent() {
        return this.parent;
    }

    public boolean hasAttribute(String key) {
        return props.containsKey(key.toLowerCase());
    }

    public String getAttribute(String key) {
        return getAttribute(key, "");
    }

    public String getAttribute(String key, String defaultValue) {
        Object obj = getObjectAttribute(key, defaultValue);
        return obj != null ? String.valueOf(obj) : null;
    }
//    private ArrayList<Reference> keysReference = new ArrayList<Reference>();

    public SaavyElement addAttribute(String key, String value){
        setAttribute(key, (String)value);
        return this;
    }

    public SaavyElement addAttribute(String key, Object value){
        setAttribute(key,(Object)value);
        return this;
    }

    public void setAttribute(String key, Object obj) {
        if (key == null || key.length() == 0) {
            throw new InvalidParameterException("Key value not allowed");
        }
        setRawAttribute(key, obj);
    }

    public void setAttribute(String key, String value) {
        setAttribute(key, (Object) value);
    }

    public int getIntAttribute(String key, int defaultValue) {
        Object obj = getObjectAttribute(key);
        String nt = "";
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else {
            nt = String.valueOf(obj);
        }
        int retInt = defaultValue;
        try {
            retInt = Integer.parseInt(nt);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
        return retInt;
    }

    public Object getObjectAttribute(String key) {
        return getObjectAttribute(key, null);
    }

    public Object getObjectAttribute(String key, Object defaultValue) {
        if (key == null || key.length() == 0) {
            throw new InvalidParameterException("Key value not allowed");
        }
        return getRawObjectAttribute(key, defaultValue);
    }

    public String getXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<").append(getName());

        for (Object obj : props.keySet()) {
            String key = (String) obj;
            if (!key.isEmpty()) {
                if(props.get(key) instanceof SaavyElement){
                    xml.append(" ").append(key).append("='").append(((SaavyElement)props.get(key)).getXML()).append("'");
                }else if(props.get(key) instanceof Packet){
                    xml.append(" ").append(key).append("='").append(((Packet)props.get(key)).getPacketElement().getXML()).append("'");
                }else{
                    xml.append(" ").append(key).append("='").append(props.get(key)).append("'");
                }
            }
        }

        if (hasChildren() || !getText().isEmpty()) {
            xml.append(">");
            for (SaavyElement child : getChildren()) {
                xml.append("\n").append(child.getXML());
            }
            if (!getText().isEmpty()) {
                xml.append(getText());
            }
            xml.append("\n</").append(getName()).append(">");
        } else {
            xml.append("/>");
        }
        return xml.toString();
    }

    public static SaavyElement createXML(String xml) {
        SaavyElement element = new SaavyElement();
        try {
            element = parseXML(new ByteArrayInputStream(xml.getBytes()));
        } catch (XMLStreamException ex) {
            Logger.getLogger(SaavyElement.class.getName()).log(Level.SEVERE, xml, ex);
        }
        return element;
    }
    private static XMLInputFactory factory = XMLInputFactory.newInstance();

    public static SaavyElement parseXML(InputStream in) throws XMLStreamException {
        XMLEventReader eventReader = factory.createXMLEventReader(in);
        XMLEvent event = eventReader.nextEvent();

        if (eventReader.hasNext()) {
            if (event.isStartDocument()) {
                SaavyElement retVal = null;
                try {
                    retVal = parseXML(null, null, eventReader.nextEvent(), eventReader, 0);
                    return retVal;
                } finally {
                    retVal = null;
                }
            }
        }

        return null;
    }

    public static SaavyElement parseXML(StartElement parent, SaavyElement guiParent, XMLEvent event, XMLEventReader eventReader, int index) throws XMLStreamException {

        SaavyElement guiElementTemp = null;
        try {
            boolean cont = true;
            String current = "";
            StartElement startElement = null;
            do {
                if (startElement == null && event.isStartElement()) {
                    startElement = event.asStartElement();
                    current = startElement.getName().toString();
                    if (guiElementTemp == null) {
                        guiElementTemp = new SaavyElement();
                    }
                    guiElementTemp.setParent(guiParent);
                    Iterator i = startElement.getAttributes();
                    guiElementTemp.setName(startElement.getName().toString());
                    while (i.hasNext()) {
                        Attribute attribute = (Attribute) i.next();
                        if (attribute.getValue().matches("\\[.*\\]")) {
                            StringTokenizer tokens = new StringTokenizer(attribute.getValue().substring(1, attribute.getValue().length() - 1), ",");
                            ArrayList<String> indices = new ArrayList<String>();
                            while (tokens.hasMoreTokens()) {
                                indices.add(tokens.nextToken().trim());
                            }
                            guiElementTemp.setAttribute(attribute.getName().toString(), indices);
                        } else {
                            guiElementTemp.setAttribute(attribute.getName().toString(), attribute.getValue());
                        }
                    }
                    event = eventReader.nextEvent();
                } else if (event.isStartElement()) {
                    SaavyElement child = parseXML(startElement, guiElementTemp, event, eventReader, index++);
                    if (child != null) {
                        guiElementTemp.addChildren(child);
                    }
                    event = eventReader.nextEvent();
                } else if (event.isCharacters()) {
                    guiElementTemp.appendText(event.asCharacters().toString());
                    event = eventReader.nextEvent();
                } else if (event.isEndElement()) {
                    if (current.equalsIgnoreCase(event.asEndElement().getName().toString())) {
                        cont = false;
                    }
                } else if (event.isEndDocument()) {
                    cont = false;
                } else {
                    event = eventReader.nextEvent();
                }
            } while (cont);

            return guiElementTemp;
        } finally {
            guiElementTemp = null;
        }
    }

    private void appendText(String text) {
        String stringBuff = this.text.toString();
        this.text.setString(stringBuff + text);
    }

    public CharSequence getCharSequence() {
        return getText();
    }

    public String getText() {
        return text.toString();
    }

    public void setText(String text) {
        this.text.setString(text);
    }

    public SaavyElement copyElement() {
        SaavyElement copy = new SaavyElement();
        copy.copyAttributes(this);
        copy.setName(getName());
        copy.setText(getText());
        for (SaavyElement child : getChildren()) {
            copy.addChildren(child.copyElement());
        }
        return copy;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (guiElements != null) {
                for (SaavyElement guiElement : guiElements) {
                    guiElement.finalize();
                }
                guiElements.clear();
                guiElements = null;
            }
            if (text != null) {
                text.destroy();
                text = null;
            }
            parent = null;
//            if (keysReference != null) {
//                for (Reference ref : keysReference) {
//                    ref.clear();
//                }
//                keysReference.clear();
//                keysReference = null;
//            }
            if (props != null) {
                props.clear();
                props = null;
            }
            super.finalize();
        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    private Object getRawObjectAttribute(String key, Object defaultValue) {
        key = key.toLowerCase();
        if (props != null && props.containsKey(key)) {
            Object obj = props.get(key);
            if (obj instanceof ZipString) {
                obj = ((ZipString) obj).toString();
            }
            return obj;
        } else {
            return defaultValue;
        }
    }

    private void setRawAttribute(String key, Object obj) {
        if (obj != null) {
            key = key.toLowerCase();
//            if (!props.containsKey(key)) {
//                keysReference.add(new HardReference(this,key));
//            }
            if (obj instanceof String) {
                ZipString string = new ZipString();
                string.setString((String) obj);
                props.put(key, string);
            } else {
                props.put(key, obj);
            }
        }
    }

    public void removeAttribute(String key) {
        props.remove(key);
    }
}
