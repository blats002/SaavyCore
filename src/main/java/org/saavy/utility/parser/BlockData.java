package org.saavy.utility.parser;

public abstract class BlockData {

    private int blockType;
    private String blockId;
    private int blockOrder;

    public BlockData(int type, String id, int order) {
        blockType = type;
        blockId = id;
        blockOrder = order;
    }

    public int getBlockType() {
        return blockType;
    }

    public String getBlockId() {
        return blockId;
    }

    public int getBlockOrder() {
        return blockOrder;
    }
    
    public abstract BlockData getBlockData(String id);
    
    public boolean hasBlockData(String id){
        return getBlockData(id).getBlockType() != BlockSpec.BLOCKTYPE_EMPTY;
    }
    
    public abstract int getSize();

    public abstract void displayData();

    public abstract void clearData();

    public abstract String getItem(String path) throws IllegalArgumentException;
    
}
