
package org.saavy.utility.parser;

import java.util.Vector;
import java.util.logging.Logger;
public class CompositeSpec extends BlockSpec
{	private static final Logger log = Logger.getLogger("com.atlp.utility.parser.ScalarSetSpec");
	private Vector<BlockSpec> blocks;
	
	public CompositeSpec()
	{	super(BlockSpec.BLOCKTYPE_COMPOSITE);
		blocks = new Vector<BlockSpec>();
	}

	public BlockSpec[] getBlocks()
	{	return blocks.toArray(new BlockSpec[1]);
	}
	
	public void addBlock(BlockSpec blockSpec)
	{	blocks.add(blockSpec);
	}
	
	
	public void displayBlock(String tabs)
	{	
//            log.info(tabs+"Composite:");
		for(BlockSpec block : blocks)
		{	block.displayBlock(tabs+"\t");
		}
	}
	
}