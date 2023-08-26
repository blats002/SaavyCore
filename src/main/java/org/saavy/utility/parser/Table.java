package org.saavy.utility.parser;

import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Table extends BlockData {

    private static final Logger log = Logger.getLogger("org.saavy.utility.parser.Table");
    //private int type; // 1)table of scalarset 2) table of table 3) table of composite
    Vector<BlockData> table = null;

    public Table(String id, int order) {
        super(BlockSpec.BLOCKTYPE_TABLE, id, order);
        table = new Vector<BlockData>();
    }

    public void add(BlockData row) {
        table.add(row);
    }

    @Override
    public String toString() {
        StringBuffer disp = new StringBuffer();
        for (BlockData row : table) {
            disp.append(getBlockId() + " row: " + row + "\n");
        }
        return disp.toString();
    }

    @Override
    public void displayData() {
//        System.out.println(toString());
    }

    @Override
    public void clearData() {
        for (BlockData block : table) {
            block.clearData();
        }
        table.clear();
    }

    @Override
    public String getItem(String path) throws IllegalArgumentException {
        String item = null;
        String[] nodes = path.split("\\.");
        String[] carcdr = path.split("\\.", 2);

        if (nodes.length < 3) {
            throw new IllegalArgumentException(String.format("Invalid node count: expected[3+] found [%d]", nodes.length));
        }

        String pathRegex = "(\\w+)\\[(\\d+)\\]";
        Pattern p = Pattern.compile(pathRegex);
        Matcher m = p.matcher(nodes[0]);

        if (m.matches()) {
            String tableName = m.group(1);
            if (tableName.equals(getBlockId())) {
                int idx = Integer.parseInt(m.group(2));
                if (idx > (table.size() - 1)) {
                    throw new IllegalArgumentException(String.format("Table [%s] index [%d] out of bounds", tableName, idx));
                }
                item = table.get(idx).getItem(carcdr[1]);
            } else {
                throw new IllegalArgumentException(String.format("Invalid node [%s]", tableName));
            }
        } else {
            throw new IllegalArgumentException(String.format("Invalid node [%s]. Expecting table[index]", nodes[0]));
        }
        return item;
    }

    @Override
    public int getSize() {
        return table.size();
    }

    @Override
    public BlockData getBlockData(String path) {
        BlockData item = null;
        int idx = Integer.parseInt(path);
        if (idx > (table.size() - 1)) {
            throw new IllegalArgumentException(String.format("Table [%s] index [%d] out of bounds", getBlockId(), idx));
        }
        item = table.get(idx);
        return item;
    }
}
