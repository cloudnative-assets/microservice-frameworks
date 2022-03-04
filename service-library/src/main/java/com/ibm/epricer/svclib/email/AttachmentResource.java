package com.ibm.epricer.svclib.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Objects;

import javax.activation.DataSource;
import static java.nio.charset.StandardCharsets.UTF_8;


public class AttachmentResource implements Serializable {

	private static final long serialVersionUID = 2561840292659892729L;
	
	private final String attachmentName;
	
	// data source is not serializable, so transient
	private transient final DataSource dataSource;

	public AttachmentResource(final String attachmentName, final DataSource dataSource) {
		this.attachmentName = attachmentName;
		this.dataSource = dataSource;
	}
	
	public String getAttachmentName() {
		return attachmentName;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public String readAllData()
			throws IOException {
		return readAllData(UTF_8);
	}
	
	public byte[] readAllBytes()
			throws IOException {
		return EmailUtils.readInputStreamToBytes(getDataSourceInputStream());
	}
	
	public String readAllData(final Charset charset)
			throws IOException {
		return EmailUtils.readInputStreamToString(getDataSourceInputStream(), charset);
	}
	
	public InputStream getDataSourceInputStream() {
		try {
			return dataSource.getInputStream();
		} catch (IOException e) {
			throw new AttachmentResourceException("Error getting input stream from attachment's data source", e);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AttachmentResource that = (AttachmentResource) o;
		return Objects.equals(attachmentName, that.attachmentName) &&
				EmailUtils.isEqualDataSource(dataSource, that.dataSource);
	}

}
