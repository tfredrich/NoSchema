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
package com.strategicgains.noschema.document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.strategicgains.noschema.exception.StorageException;

/**
 * This class is a DocumentObserver that compresses and decompresses the data in a Document using GZIP compression.
 * <p/>
 * This class is useful for compressing and decompressing data in a Document before and after it is stored in a storage
 * system. Trading off CPU time for storage space and can be used to reduce the amount of storage space required
 * to store a Document with a latency cost of compressing and decompressing the data.
 * <p/>
 * Note that small payloads may not benefit from compression and may actually increase in size after compression.
 * 
 * @see DocumentObserver
 * @author Todd Fredrich
 */
public class GzipDocumentObserver
extends AbstractDocumentObserver
{
	@Override
	public void afterDecoding(Document document)
	{
		if (document.getObject() == null)
		{
			return;
		}

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos))
        {
            gzip.write(document.getObject());
            gzip.flush();
        }
		catch (IOException e)
		{
			throw new StorageException(String.format("Error compressing data for document: %s", document.getType()), e);
		}

		document.setObject(baos.toByteArray());
	}

	@Override
	public void afterEncoding(Document document)
	{
		if (document.getObject() == null)
		{
			return;
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(document.getObject());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (GZIPInputStream gzip = new GZIPInputStream(bais))
		{
			byte[] buffer = new byte[1024];
			int length;
			while ((length = gzip.read(buffer)) != -1)
			{
				baos.write(buffer, 0, length);
			}

			document.setObject(baos.toByteArray());
		}
		catch (IOException e)
		{
			throw new StorageException(String.format("Error decompressing data for document: %s", document.getType()), e);
		}
	}
}
