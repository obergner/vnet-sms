package vnet.sms.routingengine.blueprint.test;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IOHelper {

	private static final transient Logger	LOG	              = LoggerFactory
	                                                                  .getLogger(IOHelper.class);

	private static final int	          DEFAULT_BUFFER_SIZE	= 1024 * 4;

	private IOHelper() {
		// Utility Class
	}

	public static int copy(final InputStream input, final OutputStream output)
	        throws IOException {
		return copy(input, output, DEFAULT_BUFFER_SIZE);
	}

	public static int copy(final InputStream input, final OutputStream output,
	        final int bufferSize) throws IOException {
		int realBufferSize = bufferSize;
		int avail = input.available();
		if (avail > 262144) {
			avail = 262144;
		}
		if (avail > realBufferSize) {
			realBufferSize = avail;
		}

		final byte[] buffer = new byte[realBufferSize];
		int n = input.read(buffer);
		int total = 0;
		while (-1 != n) {
			output.write(buffer, 0, n);
			total += n;
			n = input.read(buffer);
		}
		output.flush();
		return total;
	}

	/**
	 * Closes the given resource if it is available, logging any closing
	 * exceptions to the given log
	 * 
	 * @param closeable
	 *            the object to close
	 * @param name
	 *            the name of the resource
	 * @param log
	 *            the log to use when reporting closure warnings
	 */
	public static void close(final Closeable closeable, final String name,
	        final Logger log) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (final IOException e) {
				if (log != null) {
					if (name != null) {
						log.warn(
						        "Cannot close: " + name + ". Reason: "
						                + e.getMessage(), e);
					} else {
						log.warn("Cannot close. Reason: " + e.getMessage(), e);
					}
				}
			}
		}
	}

	/**
	 * Closes the given resource if it is available.
	 * 
	 * @param closeable
	 *            the object to close
	 * @param name
	 *            the name of the resource
	 */
	public static void close(final Closeable closeable, final String name) {
		close(closeable, name, LOG);
	}

	/**
	 * Closes the given resource if it is available.
	 * 
	 * @param closeable
	 *            the object to close
	 */
	public static void close(final Closeable closeable) {
		close(closeable, null, LOG);
	}

	public static void close(final Closeable... closeables) {
		for (final Closeable closeable : closeables) {
			close(closeable);
		}
	}

	/**
	 * Recursively delete a directory, useful to zapping test data
	 * 
	 * @param file
	 *            the directory to be deleted
	 */
	public static void deleteDirectory(final String file) {
		deleteDirectory(new File(file));
	}

	/**
	 * Recursively delete a directory, useful to zapping test data
	 * 
	 * @param file
	 *            the directory to be deleted
	 */
	public static void deleteDirectory(final File file) {
		int tries = 0;
		final int maxTries = 5;
		boolean exists = true;
		while (exists && (tries < maxTries)) {
			recursivelyDeleteDirectory(file);
			tries++;
			exists = file.exists();
			if (exists) {
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					// Ignore
				}
			}
		}
		if (exists) {
			throw new RuntimeException("Deletion of file " + file + " failed");
		}
	}

	private static void recursivelyDeleteDirectory(final File file) {
		if (!file.exists()) {
			return;
		}

		if (file.isDirectory()) {
			final File[] files = file.listFiles();
			for (final File file2 : files) {
				recursivelyDeleteDirectory(file2);
			}
		}
		final boolean success = file.delete();
		if (!success) {
			LOG.warn("Deletion of file: " + file.getAbsolutePath() + " failed");
		}
	}

	/**
	 * create the directory
	 * 
	 * @param file
	 *            the directory to be created
	 */
	public static void createDirectory(final String file) {
		final File dir = new File(file);
		dir.mkdirs();
	}

}
