
package org.saavy.utility.parser;

public abstract class BlockSpec 
{	
	public static final int BLOCKTYPE_SCALARSET = 100;
	public static final int BLOCKTYPE_TABLE = 200;
	public static final int BLOCKTYPE_COMPOSITE = 300;
        public static final int BLOCKTYPE_EMPTY = 400;
	
	private final int blockType;
	private String blockId;
	private int blockOrder;
	protected String regex;
	
	public BlockSpec(int blockType)
	{	this.blockType = blockType;
	}

	public String getBlockId()
	{	return blockId;
	}
	
	public int getBlockOrder()
	{	return blockOrder;
	}

	public void setBlockId(String blockId)
	{	this.blockId = blockId;
		
	}
	
	public void setBlockOrder(int blockOrder)
	{	this.blockOrder = blockOrder;
	}
	
	public int getBlockType()
	{	return blockType;
	}
	
	protected String getRegex()
	{	return regex;
	}

	protected void setRegex(String regex)
	{	this.regex = regex;
	}
	
	public abstract void displayBlock(String tabs);
	
}
