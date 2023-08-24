package org.saavy.utility.parser;
import org.saavy.zip.ZipString;


public class ScalarItem 
{	private String id;
	private ZipString value = new ZipString();
	private int order;

	public ScalarItem()
	{	
	}
	public ScalarItem(String id, String value, int order)
	{	this.id = id;
		setValue(value);
		this.order = order;
	}
	
	public String getId()
	{	return id;
	}
	
	public String getValue()
	{	return value.toString();
	}

	public int getOrder()
	{	return order;
	}
	
	public void setId(String id)
	{	this.id = id;
	}
	
	public void setValue(String value)
	{	this.value.setString(value.trim());
	}
	
	public void setOrder(int order)
	{	this.order = order;
	}
	
	@Override
	public String toString()
	{	return String.format("[%s|%s]", id, value.toString());
	}
	
	@Override
	public boolean equals(Object obj)
	{
        boolean eq = false;
		if(obj instanceof ScalarItem)
		{	ScalarItem item = (ScalarItem)obj;
			eq = item.id.equals(this.id) &&
				  item.value.equals(this.value) && 
				  (item.getOrder()==this.order);
		}
		return eq;
	}

	@Override
	public int hashCode() 
	{	int hash = 7;
		hash = 53 * hash + (this.value != null ? this.value.hashCode() : 0);
		hash = 53 * hash + this.order;
		return hash;
	}
			  
}
