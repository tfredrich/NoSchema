package com.strategicgains.noschema.trie;

import java.util.*;

class SuffixTree
{
	private Node root;
	private String text;
	private static final char TERMINATION_CHAR = '$';

	public SuffixTree(String text)
	{
		this.text = text + TERMINATION_CHAR;
		this.root = new Node();
		buildTree();
	}

	private void buildTree()
	{
		for (int i = 0; i < text.length(); i++)
		{
			addSuffix(i);
		}
	}

	private void addSuffix(int start)
	{
		Node current = root;
		int i = start;
		while (i < text.length())
		{
			String suffix = text.substring(i);
			String[] edge = current.findChildren(suffix);
			if (edge == null)
			{
				current.children.put(suffix, new Node(start));
				return;
			}
			String commonPrefix = edge[0];
			Node child = current.children.get(commonPrefix);
			if (commonPrefix.length() < edge[1].length())
			{
				Node split = new Node(-1);
				split.children.put(edge[1].substring(commonPrefix.length()), child);
				current.children.put(commonPrefix, split);
				split.children.put(suffix.substring(commonPrefix.length()), new Node(start));
				return;
			}
			i += commonPrefix.length();
			current = child;
		}
	}

	public boolean contains(String pattern)
	{
		return findNode(pattern) != null;
	}

	private Node findNode(String pattern)
	{
		Node current = root;
		int i = 0;
		while (i < pattern.length() && current != null)
		{
			String suffix = pattern.substring(i);
			String[] edge = current.findChildren(suffix);
			if (edge == null)
			{
				return null;
			}
			String commonPrefix = edge[0];
			if (commonPrefix.length() > suffix.length())
			{
				return null;
			}
			i += commonPrefix.length();
			current = current.children.get(commonPrefix);
		}
		return current;
	}

	public List<Integer> search(String pattern)
	{
		Node node = findNode(pattern);
		if (node == null)
		{
			return new ArrayList<>();
		}
		List<Integer> results = new ArrayList<>();
		collectLeaves(node, results);
		return results;
	}

	private void collectLeaves(Node node, List<Integer> results)
	{
		if (node == null)
		{
			return;
		}

		if (node.children.isEmpty())
		{
			results.add(node.position);
		}
		else
		{
			for (Node child : node.children.values())
			{
				collectLeaves(child, results);
			}
		}
	}

	private static class Node
	{
		private static final int UNDEFINED = -1;
		Map<String, Node> children;
		int position;

		Node()
		{
			super();
			this.children = new HashMap<>();
			this.position = UNDEFINED;
		}

		Node(int position)
		{
			this.children = new HashMap<>();
			this.position = position;
		}

		String[] findChildren(String suffix)
		{
			for (String child : children.keySet())
			{
				String commonPrefix = longestCommonPrefix(child, suffix);
				if (!commonPrefix.isEmpty())
				{
					return new String[] { commonPrefix, child };
				}
			}
			return null;
		}

		private String longestCommonPrefix(String s1, String s2)
		{
			int minLength = Math.min(s1.length(), s2.length());
			for (int i = 0; i < minLength; i++)
			{
				if (s1.charAt(i) != s2.charAt(i))
				{
					return s1.substring(0, i);
				}
			}
			return s1.substring(0, minLength);
		}
	
		@Override
		public String toString()
		{
			return "Node [position=" + position + ", children=" + children.keySet() + "]";
		}
	}

	public static void main(String[] args)
	{
		String text = "havanabanana";
		SuffixTree tree = new SuffixTree(text);

		System.out.println("Text: " + text);
		System.out.println("Contains 'ana': " + tree.contains("ana"));
		System.out.println("Contains 'ann': " + tree.contains("ann"));

		List<Integer> positions = tree.search("a");
		System.out.println("Positions of 'a': " + positions);
	}
}
