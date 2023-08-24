
package org.saavy.utility.parser;

public class ScalarItemSpec 
{
	private String id;
	private int itemOrder;
	private int group;

	public ScalarItemSpec(String id, int group, int order)
	{	this.id = id;
		this.group = group;
		this.itemOrder = order;
	}

	public String getId()
	{	return id;
	}
	
	public int getItemOrder()
	{	return itemOrder;
	}
	
	public int getGroup()
	{	return group;
	}
	
	public void setGroup(int group)
	{	this.group = group;
	}
	
	@Override
	public String toString()
	{	return String.format("[%s,%d]", id, group);
		
	}
}
