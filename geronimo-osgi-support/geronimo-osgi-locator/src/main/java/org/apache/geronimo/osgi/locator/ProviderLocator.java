/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.osgi.locator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.geronimo.osgi.registry.api.ProviderRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class ProviderLocator {
    // our bundle context
    static private BundleContext context;
    // a service tracker for the registry service
    // NB:  This is declared as just Object to avoid classloading issues if we're running
    // outside of an OSGi environment.
    static private Object registryTracker;

    private ProviderLocator() {
        // private constructor to prevent an instance from getting created.
    }

    /**
     * initialize the tracker statics for this bundle
     *
     * @param c      The starup BundleContext.
     */
    public static void init(BundleContext c) {
        try {
            // just create a tracker for our lookup service
            // NB:  We use the hard coded name in case the registry service has not
            // been started first.  Once this does get started, then everything should
            // resolved.
            registryTracker = new ServiceTracker(context, "org.apache.geronimo.osgi.registry.api.ProviderRegistry", null);
            ((ServiceTracker)registryTracker).open();
            // do this last...it helps indicate if we have an initialized registry.
            context = c;
        } catch (Throwable e) {
            // if there were any errors, then the registry is not available.
            registryTracker = null;
        }
    }


    /**
     * Cleanup resources on bundle shutdown.
     */
    public static void destroy() {
        if (registryTracker != null) {
            // shutdown our tracking of the provider registry.
            ((ServiceTracker)registryTracker).close();
            registryTracker = null;
        }
    }


    /**
     * Locate a class by its provider id indicator. .
     *
     * @param providerId The provider id (generally, a fully qualified class name).
     *
     * @return The Class corresponding to this provider id.  Returns null
     *         if this is not registered or the indicated class can't be
     *         loaded.
     */
    static public Class<?> locate(String providerId) {
        // if not initialized in an OSGi environment, this is a failure
        if (registryTracker == null) {
            return null;
        }
        // get the service, if it exists.  NB:  if the tracker exists, then we
        // were able to load the interface class in the first place, so we don't
        // need to protect against that.
        ProviderRegistry registry = (ProviderRegistry)((ServiceTracker)registryTracker).getService();
        // it is also a failure if the service is not there.
        if (registry == null) {
            return null;
        }
        // the rest of the work is done by the registry
        return registry.locate(providerId);
    }

    /**
     * Locate all class files that match a given factory id.
     *
     * @param providerId The target provider identifier.
     *
     * @return A List containing the class objects corresponding to the
     *         provider identifier.  Returns an empty list if no
     *         matching classes can be located.
     */
    static public List<Class<?>> locateAll(String providerId) {
        // if not initialized in an OSGi environment, this is a lookup failure
        if (registryTracker == null) {
            return new ArrayList<Class<?>>();
        }
        // get the service, if it exists.  NB:  if the tracker exists, then we
        // were able to load the interface class in the first place, so we don't
        // need to protect against that.
        ProviderRegistry registry = (ProviderRegistry)((ServiceTracker)registryTracker).getService();
        // it is also a failure if the service is not there.
        if (registry == null) {
            return new ArrayList<Class<?>>();
        }
        // the rest of the work is done by the registry
        return registry.locateAll(providerId);
    }

    /**
     * Utility class for locating a class with OSGi registry
     * support.  Uses the thread context classloader as part of
     * the search order.
     *
     * @param className The name of the target class.
     *
     * @return The loaded class.
     * @exception ClassNotFoundException
     *                   Thrown if the class cannot be located.
     */
    static public Class<?> loadClass(String className) throws ClassNotFoundException {
        return loadClass(className, null, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Utility class for locating a class with OSGi registry
     * support.  Uses the thread context classloader as part of
     * the search order.
     *
     * @param className The name of the target class.
     *
     * @return The loaded class.
     * @exception ClassNotFoundException
     *                   Thrown if the class cannot be located.
     */
    static public Class<?> loadClass(String className, Class<?> contextClass) throws ClassNotFoundException {
        return loadClass(className, contextClass, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Standardized utility method for performing class lookups
     * with support for OSGi registry lookups.
     *
     * @param className The name of the target class.
     * @param loader    An optional class loader.
     *
     * @return The loaded class
     * @exception ClassNotFoundException
     *                   Thrown if the class cannot be loaded.
     */
    static public Class<?> loadClass(String className, Class<?>contextClass, ClassLoader loader) throws ClassNotFoundException {
        if (loader != null) {
            try {
                return loader.loadClass(className);
            } catch (ClassNotFoundException x) {
            }
        }
        if (contextClass != null) {
            loader = contextClass.getClassLoader();
        }
        try {
            // try again using the class context loader
            return Class.forName(className, true, loader);
        } catch (ClassNotFoundException x) {
            // last gasp, use the OSGi locator to try to find this
            Class cls = locate(className);
            if (cls == null) {
                throw x;
            }
            return cls;
        }
    }


    /**
     * Get a single service instance that matches an interface
     * definition.
     *
     * @param iface  The name of the required interface.
     * @param contextClass
     *               The class requesting the lookup (used for class resolution).
     * @param loader A class loader to use for searching for service definitions
     *               and loading classes.
     *
     * @return The service instance, or null if no matching services
     *         can be found.
     * @exception Exception Thrown for any classloading or exceptions thrown
     *                      trying to instantiate a service instance.
     */
    static public Object getService(String iface, Class<?> contextClass, ClassLoader loader) throws Exception {
        // try for a classpath locatable instance first.  If we find an appropriate class mapping,
        // create an instance and return it.
        Class<?> cls = locateServiceClass(iface, contextClass, loader);
        if (cls != null) {
            return cls.newInstance();
        }

        // if not initialized in an OSGi environment, this is a failure
        if (registryTracker == null) {
            return null;
        }
        // get the service, if it exists.  NB:  if the tracker exists, then we
        // were able to load the interface class in the first place, so we don't
        // need to protect against that.
        ProviderRegistry registry = (ProviderRegistry)((ServiceTracker)registryTracker).getService();
        // it is also a failure if the service is not there.
        if (registry == null) {
            return null;
        }
        // the rest of the work is done by the registry
        return registry.getService(iface);
    }


    /**
     * Locate a service class that matches an interface
     * definition.
     *
     * @param iface  The name of the required interface.
     * @param contextClass
     *               The class requesting the lookup (used for class resolution).
     * @param loader A class loader to use for searching for service definitions
     *               and loading classes.
     *
     * @return The located class, or null if no matching services
     *         can be found.
     * @exception Exception Thrown for any classloading exceptions thrown
     *                      trying to load the class.
     */
    static public Class<?> getServiceClass(String iface, Class<?> contextClass, ClassLoader loader) throws ClassNotFoundException {
        // try for a classpath locatable instance first.  If we find an appropriate class mapping,
        // create an instance and return it.
        Class<?> cls = locateServiceClass(iface, contextClass, loader);
        if (cls != null) {
            return cls;
        }

        // if not initialized in an OSGi environment, this is a failure
        if (registryTracker == null) {
            return null;
        }
        // get the service, if it exists.  NB:  if the tracker exists, then we
        // were able to load the interface class in the first place, so we don't
        // need to protect against that.
        ProviderRegistry registry = (ProviderRegistry)((ServiceTracker)registryTracker).getService();
        // it is also a failure if the service is not there.
        if (registry == null) {
            return null;
        }
        // the rest of the work is done by the registry
        return registry.getServiceClass(iface);
    }


    /**
     * Get a list of services that match a given interface
     * name.  This searches both the current class path and
     * the global repository for matches.
     *
     * @param iface  The name of the required interface.
     * @param contextClass
     *               The class requesting the lookup (used for class resolution).
     * @param loader A class loader to use for searching for service definitions
     *               and loading classes.
     *
     * @return A list of matching services.  Returns an empty list if there
     *         are no matches.
     * @exception Exception Thrown for any classloading or exceptions thrown
     *                      trying to instantiate a service instance.
     */
    static public List<Object> getServices(String iface, Class<?> contextClass, ClassLoader loader) throws Exception {
        List<Object> services = new ArrayList<Object>();

        // try for a classpath locatable instance first.  If we find an appropriate class mapping,
        // create an instance and return it.
        Collection<Class<?>> classes = locateServiceClasses(iface, contextClass, loader);
        if (classes != null) {
            // create an instance of each of these classes
            for (Class<?> cls : classes) {
                services.add(cls.newInstance());
            }
        }

        // if not initialized in an OSGi environment, we're finished
        if (registryTracker == null) {
            return services;
        }
        // get the service, if it exists.  NB:  if the tracker exists, then we
        // were able to load the interface class in the first place, so we don't
        // need to protect against that.
        ProviderRegistry registry = (ProviderRegistry)((ServiceTracker)registryTracker).getService();
        // it is also the end if the service is not there.
        if (registry == null) {
            return services;
        }

        // get any registered service instances now
        List<Object> globalServices = registry.getServices(iface);
        // add to our list also
        if (globalServices != null) {
            services.addAll(globalServices);
        }
        // now return the merged set
        return services;
    }


    /**
     * Get a list of service class implementations that match
     * an interface name.  This searches both the current class path and
     * the global repository for matches.
     *
     * @param iface  The name of the required interface.
     * @param contextClass
     *               The class requesting the lookup (used for class resolution).
     * @param loader A class loader to use for searching for service definitions
     *               and loading classes.
     *
     * @return A list of matching provider classes.  Returns an empty list if there
     *         are no matches.
     * @exception Exception Thrown for any classloading exceptions thrown
     *                      trying to load a provider class.
     */
    static public List<Class<?>> getServiceClasses(String iface, Class<?> contextClass, ClassLoader loader) throws Exception {
        Set<Class<?>> serviceClasses = new LinkedHashSet<Class<?>>();
        // try for a classpath locatable classes first.  If we find an appropriate class mapping,
        // add this to our return collection.
        Collection<Class<?>> classes = locateServiceClasses(iface, contextClass, loader);
        if (classes != null) {
            serviceClasses.addAll(classes);
        }

        // if not initialized in an OSGi environment, we're finished
        if (registryTracker == null) {
            return new ArrayList(serviceClasses);
        }
        // get the service, if it exists.  NB:  if the tracker exists, then we
        // were able to load the interface class in the first place, so we don't
        // need to protect against that.
        ProviderRegistry registry = (ProviderRegistry)((ServiceTracker)registryTracker).getService();
        // it is also the end if the service is not there.
        if (registry == null) {
            return new ArrayList(serviceClasses);
        }

        // get any registered service provider classes now
        List<Class<?>> globalServices = registry.getServiceClasses(iface);
        // add to our list also
        if (globalServices != null) {
            serviceClasses.addAll(globalServices);
        }
        // now return the merged set
        return new ArrayList(serviceClasses);
    }


    /**
     * Locate the first class name for a META-INF/services definition
     * of a given class.  The first matching provider is
     * returned.
     *
     * @param iface  The interface class name used for the match.
     * @param loader The classloader for locating resources.
     *
     * @return The mapped provider name, if found.  Returns null if
     *         no mapping is located.
     */
    static private String locateServiceClassName(String iface, Class<?> contextClass, ClassLoader loader) {
        // search first with the loader class path
        String name = locateServiceClassName(iface, loader);
        if (name != null) {
            return name;
        }
        // then with the context class, if there is one
        if (contextClass != null) {
            name = locateServiceClassName(iface, contextClass.getClassLoader());
            if (name != null) {
                return name;
            }
        }
        // not found
        return null;
    }


    static private String locateServiceClassName(String iface, ClassLoader loader) {
        if (loader != null) {
            try {
                // we only look at resources that match the file name, using the specified loader
                String service = "META-INF/services/" + iface;
                Enumeration<URL> providers = loader.getResources(service);

                while (providers.hasMoreElements()) {
                    List<String>providerNames = parseServiceDefinition(providers.nextElement());
                    // if there is something defined here, return the first entry
                    if (!providerNames.isEmpty()) {
                        return providerNames.get(0);
                    }
                }
            } catch (IOException e) {
            }
        }
        // not found
        return null;
    }


    /**
     * Locate the first class for a META-INF/services definition
     * of a given interface class.  The first matching provider is
     * returned.
     *
     * @param iface  The interface class name used for the match.
     * @param loader The classloader for locating resources.
     *
     * @return The mapped provider class, if found.  Returns null if
     *         no mapping is located.
     */
    static public Class<?> locateServiceClass(String iface, Class<?> contextClass, ClassLoader loader) throws ClassNotFoundException {
        String className = locateServiceClassName(iface, contextClass, loader);
        if (className == null) {
            return null;
        }

        // we found a name, try loading the class.  This will throw an exception if there is an error
        return loadClass(className, contextClass, loader);
    }


    /**
     * Locate all class names name for a META-INF/services definition
     * of a given class.
     *
     * @param iface  The interface class name used for the match.
     * @param loader The classloader for locating resources.
     *
     * @return The mapped provider name, if found.  Returns null if
     *         no mapping is located.
     */
    static private Collection<String> locateServiceClassNames(String iface, Class<?> contextClass, ClassLoader loader) {
        Set<String> names = new LinkedHashSet<String>();

        locateServiceClassNames(iface, loader, names);
        if (contextClass != null) {
            locateServiceClassNames(iface, contextClass.getClassLoader(), names);
        }

        return names;
    }


    /**
     * Locate all class names name for a META-INF/services definition
     * of a given class.
     *
     * @param iface  The interface class name used for the match.
     * @param loader The classloader for locating resources.
     *
     * @return The mapped provider name, if found.  Returns null if
     *         no mapping is located.
     */
    static void locateServiceClassNames(String iface, ClassLoader loader, Set names) {
        if (loader != null) {

            try {
                // we only look at resources that match the file name, using the specified loader
                String service = "META-INF/services/" + iface;
                Enumeration<URL> providers = loader.getResources(service);

                while (providers.hasMoreElements()) {
                    List<String>providerNames = parseServiceDefinition(providers.nextElement());
                    // just add all of these to the list
                    names.addAll(providerNames);
                }
            } catch (IOException e) {
            }
        }
    }


    /**
     * Locate all classes that map to a given provider class definition.  This will
     * search both the services directories, as well as the provider classes from the
     * OSGi provider registry.
     *
     * @param iface  The interface class name used for the match.
     * @param loader The classloader for locating resources.
     *
     * @return A list of all mapped classes, if found.  Returns an empty list if
     *         no mappings are found.
     */
    static private Collection<Class<?>> locateServiceClasses(String iface, Class<?> contextClass, ClassLoader loader) throws ClassNotFoundException {
        // get the set of names from services definitions on the classpath
        Collection<String> classNames = locateServiceClassNames(iface, contextClass, loader);
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

        // load each class and add to our return set
        for (String name : classNames) {
            classes.add(loadClass(name, contextClass, loader));
        }
        return classes;
    }


    /**
     * Parse a definition file and return the names of all included implementation classes
     * contained within the file.
     *
     * @param u      The URL of the file
     *
     * @return A list of all matching classes.  Returns an empty list
     *         if no matches are found.
     */
    static private List<String> parseServiceDefinition(URL u) {
        final String url = u.toString();
        List<String> classes = new ArrayList<String>();
        // ignore directories
        if (url.endsWith("/")) {
            return classes;
        }
        // the identifier used for the provider is the last item in the URL.
        final String providerId = url.substring(url.lastIndexOf("/") + 1);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(u.openStream(), "UTF-8"));
            // the file can be multiple lines long, with comments.  A single file can define multiple providers
            // for a single key, so we might need to create multiple entries.  If the file does not contain any
            // definition lines, then as a default, we use the providerId as an implementation class also.
            String line = br.readLine();
            while (line != null) {
                // we allow comments on these lines, and a line can be all comment
                int comment = line.indexOf('#');
                if (comment != -1) {
                    line = line.substring(0, comment);
                }
                line = line.trim();
                // if there is nothing left on the line after stripping white space and comments, skip this
                if (line.length() > 0) {
                    // add this to our list
                    classes.add(line);
                }
                // keep reading until the end.
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            // ignore errors and handle as default
        }
        return classes;
    }

    /**
     * Perform a service class discovery by looking for a
     * property in a target properties file located in the
     * java.home directory.
     *
     * @param path     The relative path to the desired properties file.
     * @param property The name of the required property.
     *
     * @return The value of the named property within the properties file.  Returns
     *         null if the property doesn't exist or the properties file doesn't exist.
     */
    public static String lookupByJREPropertyFile(String path, String property) throws IOException {
        String jreDirectory = System.getProperty("java.home");
        File configurationFile = new File(jreDirectory + File.separator + path);
        if (configurationFile.exists() && configurationFile.canRead()) {
            Properties properties = new Properties();
            InputStream in = null;
            try {
                in = new FileInputStream(configurationFile);
                properties.load(in);
                return properties.getProperty(property);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
        return null;
    }
}
