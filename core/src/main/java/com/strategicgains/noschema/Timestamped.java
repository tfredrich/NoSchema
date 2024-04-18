package com.strategicgains.noschema;

import java.util.Date;

public interface Timestamped
{
	/**
	 * Returns the creation date of the document.
	 * 
	 * @return The creation date of the document.
	 */
	Date getCreatedAt();

	/**
	 * Sets the creation date of the document.
	 * 
	 * @param createdAt The creation date to be set.
	 */
	public void setCreatedAt(Date createdAt);

	/**
	 * Returns the update date of the document.
	 * 
	 * @return The update date of the document.
	 */
	Date getUpdatedAt();

	/**
	 * Sets the update date of the document.
	 * 
	 * @param updatedAt The update date to be set.
	 */
	void setUpdatedAt(Date updatedAt);
}
