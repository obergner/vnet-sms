package vnet.sms.routingengine.core.internal;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;

import org.apache.camel.CamelContext;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.util.FileUtil;
import org.apache.camel.util.IOHelper;
import org.apache.camel.util.ObjectHelper;
import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.swissbox.tinybundles.core.TinyBundle;
import org.ops4j.pax.swissbox.tinybundles.core.TinyBundles;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import de.kalpatec.pojosr.framework.PojoServiceRegistryFactoryImpl;
import de.kalpatec.pojosr.framework.launch.BundleDescriptor;
import de.kalpatec.pojosr.framework.launch.ClasspathScanner;
import de.kalpatec.pojosr.framework.launch.PojoServiceRegistry;
import de.kalpatec.pojosr.framework.launch.PojoServiceRegistryFactory;

/**
 * Base class for OSGi Blueprint unit tests with Camel.
 */
public abstract class CamelBlueprintTestSupport extends CamelTestSupport {

	public static final long	DEFAULT_TIMEOUT	= 30000;

	private BundleContext	 bundleContext;

	private BundleDescriptor	testBundleDescriptor;

	@Before
	@Override
	public void setUp() throws Exception {
		deleteDirectory("target/test-bundle");
		createDirectory("target/test-bundle");

		// Make Felix ConfigAdmin search for configurations in an easily
		// accessible location
		final File configDir = new File(".", "target/test-classes/etc")
		        .getAbsoluteFile();
		System.setProperty("felix.cm.dir", configDir.getAbsolutePath());

		// Set some Karaf-specific default values
		System.setProperty("karaf.name", "root");
		System.setProperty("karaf.home", "target/karaf");

		// ensure pojosr stores bundles in an unique target directory
		System.setProperty("org.osgi.framework.storage", "target/bundles/"
		        + System.currentTimeMillis());

		final List<BundleDescriptor> bundles = getBundleDescriptors();
		final TinyBundle bundle = createTestBundle();
		this.testBundleDescriptor = getBundleDescriptor(
		        "target/test-bundle/test-bundle.jar", bundle);
		bundles.add(this.testBundleDescriptor);

		final Map<String, List<BundleDescriptor>> config = new HashMap<String, List<BundleDescriptor>>();
		config.put(PojoServiceRegistryFactory.BUNDLE_DESCRIPTORS, bundles);
		final PojoServiceRegistry reg = new PojoServiceRegistryFactoryImpl()
		        .newPojoServiceRegistry(config);
		this.bundleContext = reg.getBundleContext();

		super.setUp();
	}

	@After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		this.bundleContext.getBundle().stop();
		System.clearProperty("org.osgi.framework.storage");
	}

	protected TinyBundle createTestBundle() throws FileNotFoundException {
		final TinyBundle bundle = TinyBundles.newBundle();
		for (final URL url : getBlueprintDescriptors()) {
			bundle.add(
			        "OSGI-INF/blueprint/blueprint-"
			                + url.getFile().replace("/", "-"), url);
		}
		bundle.set("Manifest-Version", "2").set("Bundle-ManifestVersion", "2")
		        .set("Bundle-SymbolicName", "test-bundle")
		        .set("Bundle-Version", "0.0.0");
		return bundle;
	}

	/**
	 * Gets list of bundle descriptors. Modify this method if you wish to change
	 * default behavior.
	 * 
	 * @return List pointers to OSGi bundles.
	 * @throws Exception
	 *             If looking up the bundles fails.
	 */
	protected List<BundleDescriptor> getBundleDescriptors() throws Exception {
		// Filter Eclipse bundles that end up on the classpath when running a
		// test from inside Eclipse.
		return new ClasspathScanner()
		        .scanForBundles("(!(Bundle-SymbolicName=org.eclipse.*))");
	}

	protected BundleContext getPojoSRContext() {
		return this.bundleContext;
	}

	protected BundleDescriptor getTestBundleDescriptor() {
		return this.testBundleDescriptor;
	}

	protected Bundle findBundleBySymbolicName(final String symbolicName) {
		for (final Bundle candidate : getPojoSRContext().getBundles()) {
			// There might be directives appended to our desired bundles
			// symbolic name
			if ((candidate.getSymbolicName() != null)
			        && candidate.getSymbolicName().startsWith(symbolicName)) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * Gets the bundle descriptors as {@link URL} resources.
	 * <p/>
	 * It is preferred to override the {@link #getBlueprintDescriptor()} method,
	 * and return the location as a String, which is easier to deal with than a
	 * {@link Collection} type.
	 * 
	 * @return the bundle descriptors.
	 * @throws FileNotFoundException
	 *             is thrown if a bundle descriptor cannot be found
	 */
	protected Collection<URL> getBlueprintDescriptors()
	        throws FileNotFoundException {
		final List<URL> answer = new ArrayList<URL>();
		final String descriptor = getBlueprintDescriptor();
		if (descriptor != null) {
			// there may be more resources separated by comma
			final Iterator<Object> it = ObjectHelper.createIterator(descriptor);
			while (it.hasNext()) {
				String s = (String) it.next();
				// remove leading / to be able to load resource from the
				// classpath
				s = FileUtil.stripLeadingSeparator(s);
				final URL url = ObjectHelper.loadResourceAsURL(s);
				if (url == null) {
					throw new FileNotFoundException("Resource " + s
					        + " not found in classpath");
				}
				answer.add(url);
			}
			return answer;
		} else {
			throw new IllegalArgumentException(
			        "No bundle descriptor configured. Override getBlueprintDescriptor() or getBlueprintDescriptors() method");
		}
	}

	/**
	 * Gets the bundle descriptor from the classpath.
	 * <p/>
	 * Return the location(s) of the bundle descriptors from the classpath.
	 * Separate multiple locations by comma, or return a single location.
	 * <p/>
	 * For example override this method and return
	 * <tt>OSGI-INF/blueprint/camel-context.xml</tt>
	 * 
	 * @return the location of the bundle descriptor file.
	 */
	protected String getBlueprintDescriptor() {
		return null;
	}

	@Override
	protected CamelContext createCamelContext() throws Exception {
		return getOsgiService(CamelContext.class);
	}

	protected <T> T getOsgiService(final Class<T> type, final long timeout) {
		return getOsgiService(type, null, timeout);
	}

	protected <T> T getOsgiService(final Class<T> type) {
		return getOsgiService(type, null, DEFAULT_TIMEOUT);
	}

	protected <T> T getOsgiService(final Class<T> type, final String filter) {
		return getOsgiService(type, filter, DEFAULT_TIMEOUT);
	}

	protected <T> T getOsgiService(final Class<T> type, final String filter,
	        final long timeout) {
		ServiceTracker tracker = null;
		try {
			String flt;
			if (filter != null) {
				if (filter.startsWith("(")) {
					flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName()
					        + ")" + filter + ")";
				} else {
					flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName()
					        + ")(" + filter + "))";
				}
			} else {
				flt = "(" + Constants.OBJECTCLASS + "=" + type.getName() + ")";
			}
			final Filter osgiFilter = FrameworkUtil.createFilter(flt);
			tracker = new ServiceTracker(this.bundleContext, osgiFilter, null);
			tracker.open(true);
			// Note that the tracker is not closed to keep the reference
			// This is buggy, as the service reference may change i think
			final Object svc = tracker.waitForService(timeout);
			if (svc == null) {
				final Dictionary<?, ?> dic = this.bundleContext.getBundle()
				        .getHeaders();
				System.err.println("Test bundle headers: " + explode(dic));

				for (final ServiceReference<?> ref : asCollection(this.bundleContext
				        .getAllServiceReferences(null, null))) {
					System.err.println("ServiceReference: " + ref);
				}

				for (final ServiceReference<?> ref : asCollection(this.bundleContext
				        .getAllServiceReferences(null, flt))) {
					System.err.println("Filtered ServiceReference: " + ref);
				}

				throw new RuntimeException("Gave up waiting for service " + flt);
			}
			return type.cast(svc);
		} catch (final InvalidSyntaxException e) {
			throw new IllegalArgumentException("Invalid filter", e);
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Explode the dictionary into a <code>,</code> delimited list of
	 * <code>key=value</code> pairs.
	 */
	private static String explode(final Dictionary<?, ?> dictionary) {
		final Enumeration<?> keys = dictionary.keys();
		final StringBuffer result = new StringBuffer();
		while (keys.hasMoreElements()) {
			final Object key = keys.nextElement();
			result.append(String.format("%s=%s", key, dictionary.get(key)));
			if (keys.hasMoreElements()) {
				result.append(", ");
			}
		}
		return result.toString();
	}

	/**
	 * Provides an iterable collection of references, even if the original array
	 * is <code>null</code>.
	 */
	private static Collection<ServiceReference<?>> asCollection(
	        final ServiceReference<?>[] references) {
		return references == null ? new ArrayList<ServiceReference<?>>(0)
		        : Arrays.asList(references);
	}

	private BundleDescriptor getBundleDescriptor(final String path,
	        final TinyBundle bundle) throws Exception {
		final File file = new File(path);
		final FileOutputStream fos = new FileOutputStream(file, true);
		IOHelper.copy(bundle.build(), fos);
		IOHelper.close(fos);

		final FileInputStream fis = new FileInputStream(file);
		final JarInputStream jis = new JarInputStream(fis);
		final Map<String, String> headers = new HashMap<String, String>();
		for (final Map.Entry<Object, Object> entry : jis.getManifest()
		        .getMainAttributes().entrySet()) {
			headers.put(entry.getKey().toString(), entry.getValue().toString());
		}

		close(fis, jis);

		return new BundleDescriptor(getClass().getClassLoader(), new URL("jar:"
		        + file.toURI().toString() + "!/"), headers);
	}

	private void close(final Closeable... closeables) {
		for (final Closeable closeable : closeables) {
			close(closeable);
		}
	}

	private void close(final Closeable closeable) {
		try {
			closeable.close();
		} catch (final IOException e) {
			// Ignore;
		}
	}
}
