package org.saavy.utility.parser;


import org.saavy.dom.SaavyHashMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexParser {

    private static RegexParser instance;

    private RegexParser() {
    }

    public static RegexParser getInstance() {
        if (instance == null) {
            instance = new RegexParser();
        }
        return instance;
    }
    private static final Logger log = Logger.getLogger("com.atlp.utility.parser.RegexParser");
    private File xmlFile = null;
    private XMLStreamReader xmlReader = null;
    private CharSequence inputData = null;
    private XMLInputFactory factory = XMLInputFactory.newInstance();

    public void setXmlFile(String xmlFileName) throws IllegalArgumentException {
        xmlFile = new File(xmlFileName);
        if (!xmlFile.exists()) {
            throw new IllegalArgumentException("File does not exist.");
        }
    }

    private boolean goToElement(String name) {
        boolean ok;
        if (xmlReader.getEventType() == XMLStreamReader.START_ELEMENT && xmlReader.getLocalName().equals(name)) {
            ok = true;
        } else {
            ok = goToNextElement(name);
        }
        return ok;
    }

    private boolean goToNextElement(String name) {
        boolean ok = false;

        try {
            while (xmlReader.hasNext()) {
                if (xmlReader.next() == XMLStreamReader.START_ELEMENT) {
                    ok = xmlReader.getLocalName().equals(name);
                    break;
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return ok;
    }

    /**
     * Skip xml children of current tag
     * @throws XMLStreamException
     */
    private void skipToEnd() throws XMLStreamException {
        int depth = 1;
        while (xmlReader.hasNext()) {
            int evt = xmlReader.nextTag();
            switch (evt) {
                case XMLStreamReader.START_ELEMENT:
                    depth++;
                    break;
                case XMLStreamReader.END_ELEMENT:
                    depth--;
                    break;
                default:
                    break;
            }
            if (depth == 0) {
                break;
            }
        }
    }

    private int goToDataEntry(String command) throws XMLStreamException {
        int ok = 0;
        boolean done = false;

        while (xmlReader.hasNext()) {
            int evt = xmlReader.nextTag();
            switch (evt) {
                case XMLStreamReader.START_ELEMENT:
                    if (xmlReader.getLocalName().equals("cmd")) {
                        String subcmd = xmlReader.getAttributeValue(null, "token");
                        if (subcmd.equals("") || subcmd.equals(command)) {
                            done = true;
                            ok = subcmd.equals("") ? 2 : 1;
                        } else {
                            skipToEnd();
                        }
                    } else {
                        skipToEnd();
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    done = true;
                    break;
                default:
                    break;
            }
            if (done) {
                break;
            }
        }
        return ok;
    }

    /**
     * Find appropriate data entry from the xml
     * @return 0: no entry found 1: entry found 2: entry found with params
     */
    private int findDataEntry(String subcmd) {
        int result = 0;
        String[] subcmdTokens = subcmd.split("\\s+", 2);
//        log.fine("finding subcommand: " + subcmdTokens[0]);

        try {
            result = goToDataEntry(subcmdTokens[0]);
//            log.fine("goToDataEntry(" + subcmdTokens[0] + ") result is " + result);

            if (result != 0) {
                switch (result) {
                    case 1:
                        if (subcmdTokens.length > 1) {
//                            log.fine("recusrively parsing " + subcmdTokens[1]);
                            result = findDataEntry(subcmdTokens[1]);
                        }
                        break;
                    case 2:
                        break;
                    default:
                        break;
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
//            log.severe(e.getMessage());
        }
        return result;
    }

    private boolean findDataBlock() {
        boolean found = false;

        String tags[] = {"scalarset", "table", "composite"};
        try {
            //fisrt find the correct <data> entry, qualified by the "condition" regex




            search:
            while (xmlReader.hasNext()) {
                int evt = xmlReader.next();
                if (evt == XMLStreamReader.END_ELEMENT && xmlReader.getLocalName().equals("data")) {
                    break;
                }
					 if (evt == XMLStreamReader.END_ELEMENT && xmlReader.getLocalName().equals("composite")) {
						 break;
					 }
                if (evt == XMLStreamReader.START_ELEMENT) {
                    String tagname = xmlReader.getLocalName();
                    for (String tag : tags) {
                        if (tag.equalsIgnoreCase(tagname)) {
                            found = true;
                            break search;
                        }
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
//            log.severe(e.getMessage());
        }
        return found;
    }

    private BlockSpec generateBlockSpec() throws XMLStreamException, CLIParserException {
        BlockSpec spec = null;
        if (findDataBlock()) {
            String tagname = xmlReader.getLocalName();
//            log.fine("found block: " + tagname);
            if (tagname.equalsIgnoreCase("scalarset")) {
                ScalarSetSpec ssSpec = new ScalarSetSpec();
                ssSpec.setBlockId(xmlReader.getAttributeValue(null, "id"));
                ssSpec.setBlockOrder(Integer.parseInt(xmlReader.getAttributeValue(null, "order")));
                ssSpec.setRegex(xmlReader.getAttributeValue(null, "regex"));

                while (xmlReader.hasNext()) {
                    int evt = xmlReader.next();
                    if (evt == XMLStreamReader.START_ELEMENT) {
                        if (xmlReader.getLocalName().equalsIgnoreCase("item")) {
                            ssSpec.addItemSpec(xmlReader.getAttributeValue(null, "id"),
                                    Integer.parseInt(xmlReader.getAttributeValue(null, "group")),
                                    Integer.parseInt(xmlReader.getAttributeValue(null, "order")));
                        }
                    } else if (evt == XMLStreamReader.END_ELEMENT) {
                        if (xmlReader.getLocalName().equalsIgnoreCase("scalarset")) {
                            break;
                        }
                    }
                }
                spec = ssSpec;
            } else if (tagname.equalsIgnoreCase("table")) {
                TableSpec tabspec = new TableSpec();
                tabspec.setBlockId(xmlReader.getAttributeValue(null, "id"));
                tabspec.setBlockOrder(Integer.parseInt(xmlReader.getAttributeValue(null, "order")));
                tabspec.setRegex(xmlReader.getAttributeValue(null, "regex"));
                tabspec.setRowRegex(xmlReader.getAttributeValue(null, "row"));
                String rowSplit = xmlReader.getAttributeValue(null, "rowsplit");
                if(rowSplit != null && !rowSplit.isEmpty()){
                    tabspec.setRowSplitRegex(rowSplit);
                }
                BlockSpec rowSpec = generateBlockSpec();
                tabspec.setRowSpec(rowSpec);
                spec = tabspec;
            } else if (tagname.equalsIgnoreCase("composite")) {
					 if(xmlReader.getEventType()==XMLStreamReader.END_ELEMENT)
					 {	return null;
					 }
                CompositeSpec cmpspec = new CompositeSpec();
                cmpspec.setBlockId(xmlReader.getAttributeValue(null, "id"));
                cmpspec.setBlockOrder(Integer.parseInt(xmlReader.getAttributeValue(null, "order")));
                cmpspec.setRegex(xmlReader.getAttributeValue(null, "regex"));
                while (true) {
                    BlockSpec part = generateBlockSpec();
                    if (part == null) {
                        break;
                    } else {
                        cmpspec.addBlock(part);
                    }
                }
                spec = cmpspec;
            }
        }
        return spec;
    }

    private BlockSpec generateDataSpec(String subcmd) throws XMLStreamException, CLIParserException {
        BlockSpec spec = null;


        if (findDataEntry(subcmd) > 0) {
            while (xmlReader.hasNext()) {
                int evt = xmlReader.next();

                if (evt == XMLStreamReader.END_ELEMENT && xmlReader.getLocalName().equalsIgnoreCase("cmd")) {
//                    log.fine("Reached end of command spec. No <data> entry applicable for input");
                    throw new CLIParserException("No parser spec applicable for input");
                }

                if (evt == XMLStreamReader.START_ELEMENT && xmlReader.getLocalName().equalsIgnoreCase("data")) {
                    String condition = xmlReader.getAttributeValue(null, "condition");
                    Pattern p = Pattern.compile(condition, Pattern.DOTALL | Pattern.MULTILINE);
                    Matcher m = p.matcher(inputData);

                    if (m.find()) {
                        String id = xmlReader.getAttributeValue(null, "id");
//                        log.fine(String.format("Found regex spec id [%s]", id));
                        break;
                    }

                }
            }



            spec = generateBlockSpec();
        }
        return spec;
    }

    public BlockData extractData(BlockSpec spec, CharSequence input) {
        BlockData data = null;

        Pattern p = Pattern.compile(spec.getRegex(), Pattern.DOTALL | Pattern.MULTILINE);
        Matcher m = p.matcher(input);

        if (m.find()) {
            //String domain = input.substring(m.start(),m.end());
            String domain = m.group(1);
            switch (spec.getBlockType()) {
                case BlockSpec.BLOCKTYPE_SCALARSET:
                     {
                        ScalarSetSpec ssSpec = (ScalarSetSpec) spec;
                        ScalarSet set = new ScalarSet(ssSpec.getBlockId(), ssSpec.getBlockOrder());
                        for (ScalarItemSpec itemSpec : ssSpec.getItems()) {
                            String value = m.group(itemSpec.getGroup());
                            ScalarItem item = new ScalarItem(itemSpec.getId(), value, itemSpec.getItemOrder());
                            set.put(item.getId(), item);
                        }
                        data = set;
                    }
                    break;
                case BlockSpec.BLOCKTYPE_TABLE:
                     {
                        TableSpec tabSpec = (TableSpec) spec;
                        Table table = new Table(tabSpec.getBlockId(), tabSpec.getBlockOrder());
                        BlockSpec rowSpec = tabSpec.getRowSpec();
                        Pattern rowPttn = Pattern.compile(tabSpec.getRowRegex(), Pattern.DOTALL | Pattern.MULTILINE);
                        Matcher rowMtch = rowPttn.matcher(domain);
                        while (rowMtch.find()) {
                            String rowStr = domain.substring(rowMtch.start(), rowMtch.end());
                            if(rowMtch.groupCount() == 1){
                                rowStr = rowMtch.group(1);
                            }
                            BlockData row = extractData(rowSpec, rowStr);
                            table.add(row);
                        }
                        data = table;
                    }
                    break;
                case BlockSpec.BLOCKTYPE_COMPOSITE:
                     {
                        CompositeSpec cmpSpec = (CompositeSpec) spec;
                        Composite cmp = new Composite(cmpSpec.getBlockId(), cmpSpec.getBlockOrder());

                        for (BlockSpec partSpec : cmpSpec.getBlocks()) {
                            BlockData part = extractData(partSpec, domain);
                            cmp.put(partSpec.getBlockId(), part);
                        }
                        data = cmp;
                    }
                    break;
                default:

                    break;

            }

        }
        if (data == null) {
            data = new BlockData(BlockSpec.BLOCKTYPE_EMPTY, spec.getBlockId(), spec.getBlockOrder()) {
                @Override
                public int getSize() {
                    return 0;
                }
                @Override
                public void displayData() {
                }
                @Override
                public void clearData() {
                }
                @Override
                public String getItem(String path) throws IllegalArgumentException {
                    return "";
                }
                @Override
                public BlockData getBlockData(String id) {
                    return null;
                }

                @Override
                public boolean hasBlockData(String id) {
                   return false;
                }

                @Override
                public String toString() {
                    return getBlockId()+":EMPTY";
                }
                
            };
        }
        return data;
    }
    private SaavyHashMap<String, String> urls = new SaavyHashMap<String, String>();

    public BlockData parseCommand(String family, String command, CharSequence input) throws IOException, FileNotFoundException, XMLStreamException, CLIParserException {
        return parseCommand(this, family, command, input);
    }

    public BlockData parseCommand(Object obj,String family, String command, CharSequence input) throws IOException, FileNotFoundException, XMLStreamException, CLIParserException {
        BlockData data = null;
        inputData = input;
        // split primary command from subcommands
        String[] commandTokens = command.split("\\s+", 2);
//        log.fine("Parsing Command: " + commandTokens[0]);
        String subcommand = commandTokens.length > 1 ? commandTokens[1] : "";

        // open xml file based on command
        //setXmlFile("scripts/x800/"+commandTokens[0]+".xml");
        if (!urls.containsKey(family)) {
            throw new FileNotFoundException(String.format("can't find parsing script for %s]", family));
        }
        URL url = obj.getClass().getResource(String.format(urls.get(family) + (!urls.get(family).endsWith("/") ? "/" : "") + "%s.xml", commandTokens[0]));
        if (url == null) {
            throw new FileNotFoundException(String.format("can't find parsing script [scripts/%s/%s.xml]", family, commandTokens[0]));
        }

        //FileInputStream xmlStream = new FileInputStream(xmlFile);
        InputStream xmlStream = url.openStream();
        xmlReader = factory.createXMLStreamReader(xmlStream);

        // find correct cli entry
        if (!goToElement("cli")) {
            throw new XMLStreamException("can't find element 'cli'");
        }
        BlockSpec dataSpec = generateDataSpec(subcommand);
        dataSpec.displayBlock("");

        data = extractData(dataSpec, input);

        return data;
    }

    public String getScriptPath(String family) {
        return urls.get(family);
    }

    public void addScriptPath(String family, String path) {
        this.urls.put(family, path);
    }
	 
//	 	public static void main(String[] args)
//	{
//int buffsize=5000;
//		RegexParser rp = new RegexParser();
//		try
//		{	
//			FileReader reader = new FileReader("input2.txt");
////			FileReader reader = new FileReader("ping2.txt");
//			char[] buffer = new char[buffsize];
//			int bytesRead = reader.read(buffer, 0, buffsize );
//			reader.close();
//
//			String input = String.valueOf(buffer,0,bytesRead);
//
////			String input = 
////"PING 192.168.100.3 (192.168.100.3) 56(84) bytes of data.\n"+
////"64 bytes from 192.168.100.3: icmp_seq=1 ttl=64 time=6.40 ms\n"+
////"64 bytes from 192.168.100.3: icmp_seq=2 ttl=64 time=2.11 ms\n"+
////"64 bytes from 192.168.100.3: icmp_seq=3 ttl=64 time=2.10 ms\n"+
////"64 bytes from 192.168.100.3: icmp_seq=4 ttl=64 time=2.11 ms\n"+
////"64 bytes from 192.168.100.3: icmp_seq=5 ttl=64 time=2.12 ms\n"+
////"\n"+
////"--- 192.168.100.3 ping statistics ---\n"+
////"5 packets transmitted, 5 received, 0% packet loss, time 4000ms\n"+
////"rtt min/avg/max/mdev = 2.103/2.973/6.407/1.717 ms";
//			
//			
//			//BlockData extract = rp.parseCommand("show system", input);
//			rp.addScriptPath("x800", "/com/atlp/utility/parser/scripts/x800/");
//			BlockData extract = rp.parseCommand("x800","show system", input);
//
//
//			extract.displayData();
////			String pingtime = extract.getItem("cmp1.pinginfo.time");
////			log.debug(String.format("ping time: [%s]", pingtime));
////			log.fine(String.format("ping time: [%s]", pingtime));
////			String version = extract.getItem("cmp1.sysinfo.version");
////			log.debug(String.format("version: [%s]", version));
////			String used = extract.getItem("cmp1.stacks[1].stack.stackinfo.used");
////			log.debug(String.format("stack[1].used: [%s]", used));
////			String serial = extract.getItem("cmp1.stacks[1].stack.boards[1].boardinfo.serial");
////			log.debug(String.format("stack[1].used: [%s]", serial));
//		}
//		catch(Exception e)
//		{	
////			log.error(e);
//			e.printStackTrace();
//		}
//		
//	}
}
