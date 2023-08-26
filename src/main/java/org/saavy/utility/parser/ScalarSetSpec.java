package org.saavy.utility.parser;

import java.util.Vector;
import java.util.logging.Logger;

public class ScalarSetSpec extends BlockSpec {

    private static final Logger log = Logger.getLogger("org.saavy.utility.parser.ScalarSetSpec");
    private Vector<ScalarItemSpec> items;

    public ScalarSetSpec() {
        super(BlockSpec.BLOCKTYPE_SCALARSET);
        items = new Vector<ScalarItemSpec>();
    }

    public Vector<ScalarItemSpec> getItems() {
        return items;
    }

    public void addItemSpec(String id, int group, int order) {
        items.add(new ScalarItemSpec(id, group, order));
    }

    public void displayBlock(String tabs) {
//            log.info(tabs+"ScalarSet:");
        for (ScalarItemSpec item : items) {
//            log.info(tabs + "\t" + item);
        }

    }
}
