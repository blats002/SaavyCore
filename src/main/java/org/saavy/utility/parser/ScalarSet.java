package org.saavy.utility.parser;

import org.saavy.dom.SaavyHashMap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

public class ScalarSet extends BlockData {

    private final static Logger log = Logger.getLogger("org.saavy.utility.parser.ScalarSet");
    private SaavyHashMap<String, ScalarItem> items;

    class ScalarSetComparator implements Comparator<ScalarItem> {

        @Override
        public int compare(ScalarItem o1, ScalarItem o2) {
            return o1.getOrder() - o2.getOrder();
        }
    }

    public ScalarSet(String id, int order) {
        super(BlockSpec.BLOCKTYPE_SCALARSET, id, order);
        items = new SaavyHashMap<String, ScalarItem>();
    }

    public void put(String key, ScalarItem value) {
        items.put(key, value);
    }

    public ScalarItem get(String key) {
        return items.get(key);
    }

    public ScalarItem[] getItemsInOrder() {
        ScalarItem[] scalarItems = new ScalarItem[items.size()];
        scalarItems = (ScalarItem[]) (items.values().toArray(scalarItems));
        Arrays.sort(scalarItems, new ScalarSetComparator());
        return scalarItems;
    }

    @Override
    public String toString() {
        StringBuffer disp = new StringBuffer();

        ScalarItem[] scalarItems = getItemsInOrder();
        disp.append(getBlockId()+":\n");
        for (ScalarItem itm : scalarItems) {
            disp.append("\t"+itm + "\n");
        }
        return disp.toString();
    }

    @Override
    public void displayData() {
//        System.out.println(toString());
    }

    @Override
    public void clearData() {
        items.clear();
    }

    @Override
    public String getItem(String path) throws IllegalArgumentException {
        String item = null;
        String[] nodes = path.split("\\.");
        if (nodes.length == 1) {
            ScalarItem scalarItem = get(nodes[0]);
            if (scalarItem != null) {
                item = scalarItem.getValue();
            } else {
                throw new IllegalArgumentException(String.format("No such item [%s] in node [%s]", nodes[1], nodes[0]));
            }
        } else if (nodes.length != 2) {
            throw new IllegalArgumentException(String.format("Invalid node count: expected[2] found[%d]", nodes.length));
        } else {
            if (nodes[0].equals(getBlockId())) {
                ScalarItem scalarItem = get(nodes[1]);
                if (scalarItem != null) {
                    item = scalarItem.getValue();
                } else {
                    throw new IllegalArgumentException(String.format("No such item [%s] in node [%s]", nodes[1], nodes[0]));
                }
            } else {
                throw new IllegalArgumentException(String.format("No such node [%s]", nodes[0]));
            }
        }
        return item;
    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public BlockData getBlockData(String id) {
        return null;
    }
}
