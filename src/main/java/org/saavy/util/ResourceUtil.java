/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.util;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rgsaavedra
 */
public class ResourceUtil {

    private static Pattern patternCiscoMac = Pattern.compile("[A-Fa-f0-9]{4}\\.[A-Fa-f0-9]{4}\\.[A-Fa-f0-9]{4}");
    private static Pattern patternHexStrMac = Pattern.compile("((0x)?[A-Fa-f0-9]{2}\\s+){5,7}(0x)?[A-Fa-f0-9]{2}");

    public static Object[][] getContentsFromBundle(String string) {
        ResourceBundle rb = ResourceBundle.getBundle(string);
        ArrayList keys = new ArrayList();
        Enumeration en = rb.getKeys();
        while (en.hasMoreElements()) {
            keys.add(en.nextElement());
        }
        Object[][] stringRB = new Object[keys.size()][2];
        for (int x = 0; x < keys.size(); x++) {
            String key = (String) keys.get(x);
            stringRB[x][0] = key;
            stringRB[x][1] = rb.getObject(key);
        }
        return stringRB;
    }
    private static Comparator comparator;

    public static Comparator getPortSorter() {
        if (comparator == null) {
            comparator = new Comparator() {

                public int compare(Object o1, Object o2) {
                    String s1 = String.valueOf(o1);
                    String s2 = String.valueOf(o2);
                    int[] port1 = ResourceUtil.getPortLocation(s1);
                    int[] port2 = ResourceUtil.getPortLocation(s2);

                    if (port1.length == 3 && port2.length == 3) {
                        if (port1[0] != port2[0]) {
                            return port1[0] - port2[0];
                        } else if (port1[1] != port2[1]) {
                            return port1[1] - port2[1];
                        } else if (port1[2] != port2[2]) {
                            return port1[2] - port2[2];
                        }
                    }
                    return s1.compareTo(s2);
                }
            };
        }
        return comparator;
    }

    public static String getResourceContent(String path) throws URISyntaxException, FileNotFoundException, IOException {
        return getResourceContent(ResourceUtil.class,path);
    }

    public static String getResourceContent(Class clz,String path) throws URISyntaxException, FileNotFoundException, IOException {
        BufferedReader reader = null;
        try {
            String xml = "";
            reader = new BufferedReader(new InputStreamReader(clz.getResourceAsStream(path)));
            String line = "";
            while ((line = reader.readLine()) != null) {
                xml += line + "\n";
            }
            return xml;
        } finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException iOException) {
                }
            }
        }
    }

    public static String getResourceContent(String path,String charset) throws URISyntaxException, FileNotFoundException, IOException {
        return getResourceContent(ResourceUtil.class, path, charset);
    }

    public static String getResourceContent(Class clz,String path,String charset) throws URISyntaxException, FileNotFoundException, IOException {
        BufferedReader reader = null;
        try {
            String xml = "";
            reader = new BufferedReader(new InputStreamReader(clz.getResourceAsStream(path),charset));
            String line = "";
            while ((line = reader.readLine()) != null) {
                xml += line + "\n";
            }
            return xml;
        } finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException iOException) {
                }
            }
        }
    }

    public static class DisableNewFolderChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (((JButton) e.getSource()).isEnabled()) {
                ((JButton) e.getSource()).setEnabled(false);
            }
        }
    }
    private static DisableNewFolderChangeListener disableListener = new DisableNewFolderChangeListener();

    public static void disableNewFolderButton(Container c) {
        int len = c.getComponentCount();
        for (int i = 0; i < len; i++) {
            Component comp = c.getComponent(i);
            if (comp instanceof JButton) {
                JButton b = (JButton) comp;
                Icon icon = b.getIcon();
                if (icon != null && icon == UIManager.getIcon("FileChooser.newFolderIcon")) {
                    b.setEnabled(false);
                    List<ChangeListener> ls = (List<ChangeListener>) Arrays.asList(b.getChangeListeners());
                    if (!ls.contains(disableListener)) {
                        b.addChangeListener(disableListener);
                    }
                }
            } else if (comp instanceof Container) {
                disableNewFolderButton((Container) comp);
            }
        }
    }

    public static InputStream getResourceAsStream(Class clz,String path) {
        return clz.getResourceAsStream(path);
    }

    public static InputStream getResourceAsStream(String path) {
        return getResourceAsStream(ResourceUtil.class, path);
    }

    /**
     * Converts a MAC address string from Cisco to Hex String format
     * @param string MAC address in Cisco format (hhhh.hhhh.hhhh)
     * @return MAC address in hex str format (0xhh 0xhh 0xhh 0xhh 0xhh 0xhh)
     */
    public static String toHexStrMacFormat(String string) {
        String hexstrFormat = "";
        if (patternCiscoMac.matcher(string.trim()).matches()) {
            String bytes = string.trim().replaceAll("\\.", "");
            for (int i = 0; i < bytes.length() / 2; i++) {
                hexstrFormat = hexstrFormat + "0x" + bytes.substring(i * 2, i * 2 + 2) + " ";
            }
        }
        return hexstrFormat.trim();
    }

    /**
     * Converts a MAC address string from Cisco to Hex String format
     * @param string MAC address in hex str format (0xhh 0xhh 0xhh 0xhh 0xhh 0xhh)
     * @return MAC address in Cisco format (hhhh.hhhh.hhhh)
     */
    public static String toCiscoMacFormat(String string) {
        String ciscoFormat = string;
        if (patternHexStrMac.matcher(string.trim()).matches()) {
            String[] bytes = string.trim().replaceAll("0[xX]", "").split("\\s+");
            switch (bytes.length) {
                case 6:
                    ciscoFormat = String.format("%s%s.%s%s.%s%s", bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5]).toLowerCase();
                    break;
                case 8:
                    ciscoFormat = String.format("%s%s:%s%s.%s%s.%s%s", bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]).toLowerCase();
                    break;
                default:
                    break;
            }
        }
        return ciscoFormat;
    }

    public static String shortenHostName(String name) {
        String shortName, suffix;

        if (Pattern.compile("(.+)(-\\d+)$").matcher(name).matches()) {
            shortName = name.substring(0, name.lastIndexOf("-"));
            suffix = name.substring(name.lastIndexOf("-"));
        } else {
            shortName = name;
            suffix = "";
        }
        if (shortName.length() > 13) {
            shortName = shortName.substring(0, 13) + "...";
        }
        shortName = shortName + suffix;
        return shortName;
    }

    public static String xmlEncode(String str) {
        String encoded = str.replaceAll("&", "&amp;").
                replaceAll("<", "&lt;").
                replaceAll(">", "&gt;").
                replaceAll("'", "&#39;");
        return encoded;
    }

    public static int[] getPortLocation(String desc) {
        Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
        Matcher matcher = pattern.matcher(desc);

        int[] portLoc = new int[0];
        if (matcher.find()) {
            portLoc = new int[3];
            for (int x = 1; x <= 3; x++) {
                portLoc[x - 1] = Integer.parseInt(matcher.group(x));
            }
        }
        return portLoc;
    }

    public static void main(String[] args) {
//        System.out.println(toCiscoMacFormat("0x01 0x02 0x03 0x04 0x05 0x06"));
//        System.out.println(toHexStrMacFormat("0102.0304.0506"));
        JFileChooser chooser = new JFileChooser();
        disableNewFolderButton(chooser);
        chooser.showOpenDialog(null);
    }
}
