package com.genexus.util;
import java.util.concurrent.ConcurrentHashMap;

public class DoubleLinkedQueue
{
	private LinkedNode first, last, empty;
	private ConcurrentHashMap<Object, LinkedNode> itemNodesMapping = new ConcurrentHashMap<Object, LinkedNode>();

	public DoubleLinkedQueue()
	{
		empty = new LinkedNode();
		first = last = empty;
	}
	
	public synchronized void insert(Object item)
	{
		LinkedNode node = new LinkedNode();
		node.item = item;
		
		node.prev = empty;
		node.next = first;
		itemNodesMapping.put(item, node);
		
		if(first == empty)
		{
			last = node;			
		}
		else
		{
			node.next.prev = node;
		}		
		first = node;
	}
	
	public synchronized void moveToStart(Object item)
	{
		LinkedNode node = (LinkedNode)itemNodesMapping.get(item);
		if(node == null)
		{ // Si el objeto no esta en la lista, lo meto
			insert(item);
			return;
		}
		if(node == first)
		{ // Si ya era el primer nodo termino aqui
			return;
		}
		
		// Primero 'quito' el nodo de donde este
		node.next.prev = node.prev;
		node.prev.next = node.next;
	
		// Ahora coloco el nodo al comienzo
		node.prev = empty;
		node.next = first;
		first.prev = node;
		first = node;
	}
	
	public synchronized Object takeFromStart()
	{
		if(isEmpty())
		{
			return null;
		}
		Object item = first.item;
		if(first == last)
		{
			first = last = empty;
		}
		else
		{
			first = first.next;
		}
		itemNodesMapping.remove(item);
		return item;
	}
	
	public synchronized Object takeFromEnd()
	{
		if(isEmpty())
		{
			return null;
		}
		Object item = last.item;
		if(first == last)
		{
			first = last = empty;
		}
		else
		{
			last = last.prev;
		}
		itemNodesMapping.remove(item);
		return item;
	}
	
	public synchronized void remove(Object item)
	{
		LinkedNode node = (LinkedNode)itemNodesMapping.remove(item);
		if(node == null)
		{ // Si el objeto no esta en la lista, termino aqui
			return;
		}
		if(node == first)
		{ // Si ya era el primer nodo
			first = last = empty;
			return;
		}
		
		// Quito el nodo de donde este
		node.next.prev = node.prev;
		node.prev.next = node.next;
	}
	
	public boolean isEmpty()
	{
		return first == empty;
	}
	
	class LinkedNode
	{
		LinkedNode next;
		LinkedNode prev;
		Object item;
	}			
}
