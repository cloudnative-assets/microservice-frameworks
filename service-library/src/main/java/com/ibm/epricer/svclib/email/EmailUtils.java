package com.ibm.epricer.svclib.email;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

import javax.activation.DataSource;

class EmailUtils {
	
	/**
	 * Uses standard JDK java to read an inputstream to String using the given encoding (in {@link ByteArrayOutputStream#toString(String)}).
	 */
	static String readInputStreamToString(final InputStream inputStream, final Charset charset)
			throws IOException {
		final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int result = bufferedInputStream.read();
		while (result != -1) {
			byteArrayOutputStream.write((byte) result);
			result = bufferedInputStream.read();
		}
		return byteArrayOutputStream.toString(charset.name());
	}
	
	/**
	 * Uses standard JDK java to read an inputstream to byte[].
	 */
	static byte[] readInputStreamToBytes(final InputStream inputStream)
			throws IOException {
		try (InputStream is = inputStream) {
			byte[] targetArray = new byte[is.available()];
			is.read(targetArray);
			return targetArray;
		}
	}
	
	static boolean isEqualDataSource(final DataSource a, final DataSource b) {
		return (a == b) || (a != null && b != null &&
				Objects.equals(a.getName(), b.getName()) &&
				Objects.equals(a.getContentType(), b.getContentType()));
	}

}
