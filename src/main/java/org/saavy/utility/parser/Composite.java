package org.saavy.utility.parser;

import org.saavy.dom.SaavyHashMap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

public class Composite extends BlockData {

    private static final Logger log = Logger.getLogger("org.saavy.utility.parser.Composite");
    private SaavyHashMap<String, BlockData> composite;

    class DataBlockComparator implements Comparator<BlockData> {

        @Override
        public int compare(BlockData o1, BlockData o2) {
            return o1.getBlockOrder() - o2.getBlockOrder();
        }
    }
    // use interface
    public Composite(String id, int order) {
        super(BlockSpec.BLOCKTYPE_COMPOSITE, id, order);
        composite = new SaavyHashMap<String, BlockData>();
    }

    public void put(String key, BlockData value) {
        composite.put(key, value);
    }

    public BlockData get(String key) {
        return composite.get(key);
    }

    @Override
    public String toString() {
        StringBuffer disp = new StringBuffer();

        BlockData[] blocks = getItemsInOrder();
        for (BlockData block : blocks) {
            disp.append("composite:" + getBlockId() + "\n" + block.toString() + "\n");
        }
        return disp.toString();
    }

    @Override
    public void displayData() {
//        System.out.println(toString());
    }

    public BlockData[] getItemsInOrder() {
        BlockData[] blocks = new BlockData[composite.size()];
        blocks = (BlockData[]) (composite.values().toArray(blocks));
        Arrays.sort(blocks, new DataBlockComparator());
        return blocks;
    }

    @Override
    public void clearData() {
        for (String key : composite.keySet()) {
            composite.get(key).clearData();
        }
        composite.clear();
    }

    @Override
    public String getItem(String path) {
        String item = null;
        String[] nodes = path.split("\\.");
        String[] carcdr = path.split("\\.", 2);

        if (nodes.length < 3) {
            throw new IllegalArgumentException(String.format("Invalid node count: expected[3+] found [%d]", nodes.length));
        }

        if (!nodes[0].equals(getBlockId())) {
            throw new IllegalArgumentException(String.format("No such node [%s]", nodes[0]));
        }

        if (nodes[1].indexOf("[") != -1) {
            nodes[1] = nodes[1].substring(0, nodes[1].indexOf("["));
        }
        BlockData next = composite.get(nodes[1]);

        if (next == null) {
            throw new IllegalArgumentException(String.format("No node [%s] in composite [%s]", nodes[1], nodes[0]));
        }

        item = next.getItem(carcdr[1]);
        return item;
    }

    @Override
    public int getSize() {
        return composite.size();
    }

    @Override
    public BlockData getBlockData(String path) {
        BlockData item = null;
        String[] nodes = path.split("\\.");
        String[] carcdr = path.split("\\.", 2);

//        if (nodes.length < 3) {
//            throw new IllegalArgumentException(String.format("Invalid node count: expected[3+] found [%d]", nodes.length));
//        }

//        if (!nodes[0].equals(getBlockId())) {
//            throw new IllegalArgumentException(String.format("No such node [%s]", nodes[0]));
//        }

        if (nodes[0].indexOf("[") != -1) {
            item = composite.get(nodes[0].substring(0, nodes[0].indexOf("["))).getBlockData(nodes[0].substring(nodes[0].indexOf("[")+1,nodes[0].indexOf("]")));
            
        }else{
            item = composite.get(nodes[0]);
        }
        
        if (item == null) {
            throw new IllegalArgumentException(String.format("No node [%s] in composite [%s]", nodes[1], nodes[0]));
        }
        if(carcdr.length > 1){
            item = item.getBlockData(carcdr[1]);
        }
        return item;
    }
}
