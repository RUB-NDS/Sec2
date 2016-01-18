package org.sec2.securityprovider.mobileclient;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.List;
import org.sec2.securityprovider.IServiceImpl;
import org.sec2.securityprovider.serviceparameter.IServiceParameter;

/**
 * Loader class for services of the security provider.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 */
final class ServiceLoader extends Provider.Service {

    /**
     * Service parameters.
     */
    private List<IServiceParameter> parameter = null;

    /**
     * Constructor for services.
     *
     * @param provider The provider that offers this service
     * @param serviceParameter The service parameters (if any)
     * @param service Service descriptor
     */
    ServiceLoader(final Provider provider,
            final List<IServiceParameter> serviceParameter,
            final ServiceDescriptor service) {
        /*
         * Provider.Service(Provider provider, String type, String algorithm,
         * String className, List<String> aliases, Map<String,String>
         * attributes) provider - the provider that offers this service type -
         * the type of this service algorithm - the algorithm name className -
         * the name of the class implementing this service aliases - List of
         * aliases or null if algorithm has no aliases attributes - Map of
         * attributes or null if this implementation has no attributes
         */
        super(provider, service.getServiceType(),
                serviceParameter.get(0).toString(),
                service.getImplementingClass().getCanonicalName(),
                null, null);
        parameter = serviceParameter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object newInstance(final Object constructorParameter)
            throws NoSuchAlgorithmException {
        Object newInstance = null;
        List<IServiceParameter> handledParams = null;

        if (parameter != null) {
            handledParams = parameter;
        }

        try {
            Class<IServiceImpl> implClass;
            Constructor<IServiceImpl> constructor;

            implClass = (Class<IServiceImpl>) Class.forName(
                    this.getClassName());
            if (handledParams != null) {
                constructor = implClass.getConstructor(List.class);
            } else {
                constructor = implClass.getConstructor();
            }

            // instanciate the new class
            newInstance = constructor.newInstance(handledParams);
        } catch (InstantiationException ex) {
            throw new NoSuchAlgorithmException(
                    MobileClientProvider.PROVIDER_NAME
                    + ": Problems during service instantiation at the "
                    + "SecurityProvider's ServiceLoader.", ex);
        } catch (IllegalAccessException ex) {
            throw new NoSuchAlgorithmException(
                    MobileClientProvider.PROVIDER_NAME
                    + ": Problems during service instantiation at the "
                    + "SecurityProvider's ServiceLoader.", ex);
        } catch (IllegalArgumentException ex) {
            throw new NoSuchAlgorithmException(
                    MobileClientProvider.PROVIDER_NAME
                    + ": Problems during service instantiation at the "
                    + "SecurityProvider's ServiceLoader.", ex);
        } catch (InvocationTargetException ex) {
            throw new NoSuchAlgorithmException(
                    MobileClientProvider.PROVIDER_NAME
                    + ": Problems during service instantiation at the "
                    + "SecurityProvider's ServiceLoader.", ex);
        } catch (NoSuchMethodException ex) {
            throw new NoSuchAlgorithmException(
                    MobileClientProvider.PROVIDER_NAME
                    + ": Problems during service instantiation at the "
                    + "SecurityProvider's ServiceLoader.", ex);
        } catch (SecurityException ex) {
            throw new NoSuchAlgorithmException(
                    MobileClientProvider.PROVIDER_NAME
                    + ": Problems during service instantiation at the "
                    + "SecurityProvider's ServiceLoader.", ex);
        } catch (ClassNotFoundException ex) {
            throw new NoSuchAlgorithmException(
                    MobileClientProvider.PROVIDER_NAME
                    + ": Problems during service instantiation at the "
                    + "SecurityProvider's ServiceLoader", ex);
        }

        return newInstance;
    }
}
