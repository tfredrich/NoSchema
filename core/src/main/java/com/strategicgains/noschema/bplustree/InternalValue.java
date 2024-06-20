package com.strategicgains.noschema.bplustree;

public class InternalValue<V>
{
	private V leftChild;
	private V rightChild;

	public InternalValue(V leftChild, V rightChild) {
		super();
		this.leftChild = leftChild;
		this.rightChild = rightChild;
	}

	public V getLeftChild() {
		return leftChild;
	}

	public V getRightChild() {
		return rightChild;
	}
}
