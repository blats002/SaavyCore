package org.saavy.utility.parser;

//import org.apache.log4j.*;

import java.util.logging.Logger;

public class TableSpec extends BlockSpec {

    private static final Logger log = Logger.getLogger("com.atlp.utility.parser.ScalarSetSpec");
    private BlockSpec rowSpec;
    private String rowRegex;

    public TableSpec() {
        super(BlockSpec.BLOCKTYPE_TABLE);
    }

    public BlockSpec getRowSpec() {
        return rowSpec;
    }

    public void setRowSpec(BlockSpec rowSpec) {
        this.rowSpec = rowSpec;
    }

    public String getRowRegex() {
        return rowRegex;
    }

    public void setRowRegex(String rowRegex) {
        this.rowRegex = rowRegex;
    }

    public void displayBlock(String tabs) {
//        log.info(tabs + "Table:");
        rowSpec.displayBlock(tabs + "\t");
    }

    private boolean rowSplitBoolean = false;
    private String rowSplitRegex;

    public void setRowSplitRegex(String rowSplit) {
        rowSplitBoolean = true;
        rowSplitRegex = rowSplit;
    }

    public boolean hasRowSplit(){
        return rowSplitBoolean;
    }

    public String getRowSplitRegex(){
        return rowSplitRegex;
    }
}