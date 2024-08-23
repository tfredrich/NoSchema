package com.strategicgains.noschema.trie;

import java.util.HashMap;
import java.util.Map;

public class SuffixTrieNode
{
	private Map<String, SuffixTrieNode> children;
	private int startIndex;
	private int endIndex;
	private SuffixTrieNode nextSuffix;

	public SuffixTrieNode(int start, int end)
	{
		children = new HashMap<>();
		startIndex = start;
		endIndex = end;
		nextSuffix = null;
	}

	public Map<String, SuffixTrieNode> getChildren()
	{
		return children;
	}

	public boolean isLeaf()
    {
        return nextSuffix == null;
    }

	public boolean hasChildren()
	{
		return !children.isEmpty();
	}

	public int getStartIndex()
	{
		return startIndex;
	}

	public int getEndIndex()
	{
		return endIndex;
	}

	public SuffixTrieNode getNextSuffix()
	{
		return nextSuffix;
	}

	public void setNextSuffix(SuffixTrieNode node)
	{
		this.nextSuffix = node;
	}

	public void add(String word)
	{
		SuffixTrieNode node = this;
		SuffixTrieNode lastNode = null;

		for (int i = 0; i < word.length(); i++)
		{
			String substring = word.substring(i);

			if (!node.getChildren().containsKey(substring))
			{
				SuffixTrieNode newNode = node.addChild(substring, i, word.length());

				if (lastNode != null)
				{
					lastNode.setNextSuffix(newNode);
				}

				lastNode = newNode;
			}

			node = node.getChild(substring);
		}
	}

	public boolean search(String word)
	{
		SuffixTrieNode node = this;

		for (int i = 0; i < word.length(); i++)
		{
			String substring = word.substring(i);

			if (!node.getChildren().containsKey(substring))
			{
				return false;
			}

			node = node.getChild(substring);
		}

		return true;
	}

	private SuffixTrieNode getChild(String substring)
	{
		return children.get(substring);
	}

	private SuffixTrieNode addChild(String substring, int i, int length)
	{
		SuffixTrieNode node = new SuffixTrieNode(i, length);
        children.put(substring, node);
        return node;
	}
}
