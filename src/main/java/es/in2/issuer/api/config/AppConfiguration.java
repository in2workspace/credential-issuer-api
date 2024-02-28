package es.in2.issuer.api.config;

import es.in2.issuer.api.config.properties.AppConfigurationProperties;
import es.in2.issuer.configuration.service.GenericConfigAdapter;
import es.in2.issuer.configuration.util.ConfigAdapterFactory;
import org.springframework.stereotype.Component;

@Component
public class AppConfiguration {
    private final GenericConfigAdapter genericConfigAdapter;
    private final AppConfigurationProperties appConfigurationProperties;

    public AppConfiguration(ConfigAdapterFactory configAdapterFactory, AppConfigurationProperties appConfigurationProperties){
        this.genericConfigAdapter = configAdapterFactory.getAdapter();
        this.appConfigurationProperties = appConfigurationProperties;
    }

    public String getIAMdomain() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.iamDomain());
    }

    public String getIssuerDomain() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.issuerDomain());
    }

    public String getAuthenticSourcesDomain() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.authenticSourcesDomain());
    }

    public String getKeyVaultDomain() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.keyVaultDomain());
    }

    public String getRemoteSignatureDomain() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.remoteSignatureDomain());
    }

    public String getIAMdid() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.iamDid());
    }

    public String getIssuerDid() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.issuerDid());
    }
}