package mcfp;

import java.util.ArrayList;
import java.util.List;

public class Node<T> {

	private List<Node<T>> children = new ArrayList<>();
	private Node<T> parent;

	private T data;

	public Node(T data) {
		this.setData(data);
	}

	public Node() {
	}

	public void setData(T data) {
		this.data = data;
	}

	public void addChildren(Node<T> node) {
		this.children.add(node);
		node.parent = this;
	}

	public T getData() {
		return this.data;
	}

	public List<Node<T>> getChildren(){
		return this.children;
	}

	public Node<T> getParent(){
		return this.parent;
	}

	public boolean isBranch() {
		return !this.children.isEmpty();
	}

	public boolean isLeaf() {
		return this.children.isEmpty();
	}

	public int indexOf(Node<T> node) {
		return this.children.indexOf(node);
	}

	public void printNode() {
		printNode(this, 0);
	}

	private static void printNode(Node<?> node, int layer) {
		for(int i = 0;i < layer;i++)
			System.out.print(" ");

		System.out.println(node.data.toString());
		for(Node<?> child : node.children) {
			printNode(child, layer + 1);
		}
	}
}
