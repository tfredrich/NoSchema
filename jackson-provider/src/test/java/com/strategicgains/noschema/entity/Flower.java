/*
    Copyright 2024-2026, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
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
extends AbstractTimestampedEntity<UUID>
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