package com.strategicgains.noschema.entity;

import java.util.List;
import java.util.UUID;

/**
 * This sample entity is in no way invaded by any classes in the NoSchema library. It's a plain POJO that
 * is mapped externally into a Document entity that can be stored in the database.
 * 
 * @author tfredrich
 */
public class Flower
extends AbstractEntity
{
	private String name;
	private List<String> colors;
	private Boolean isBlooming;
	private Float height;

	public Flower() {
		super();
	}

	public Flower(UUID id, String name, Boolean isBlooming, Float height, List<String> colors) {
		super(id);
		this.name = name;
		this.isBlooming = isBlooming;
		this.height = height;
		this.colors = colors;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getIsBlooming() {
		return isBlooming;
	}

	public void setIsBlooming(Boolean isBlooming) {
		this.isBlooming = isBlooming;
	}

	public Float getHeight() {
		return height;
	}

	public void setHeight(Float height) {
		this.height = height;
	}

	public List<String> getColors() {
		return colors;
	}

	public void setColors(List<String> colors) {
		this.colors = colors;
	}

	@Override
	public String toString() {
		return "Flower [id=" + getId() + ", name=" + name + ", colors=" + colors + ", isBlooming=" + isBlooming + ", height="
				+ height + "]";
	}
}