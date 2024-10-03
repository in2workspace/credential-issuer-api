package es.in2.issuer.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.dto.VerifierOauth2AccessToken;
import es.in2.issuer.domain.service.impl.JWTServiceImpl;
import es.in2.issuer.domain.service.impl.M2MTokenServiceImpl;
import es.in2.issuer.infrastructure.config.VerifierConfig;
import es.in2.issuer.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.infrastructure.config.adapter.impl.AzureConfigAdapter;
import es.in2.issuer.infrastructure.config.adapter.impl.YamlConfigAdapter;
import es.in2.issuer.infrastructure.config.properties.ApiProperties;
import es.in2.issuer.infrastructure.config.properties.VerifierProperties;
import es.in2.issuer.infrastructure.crypto.CryptoComponent;
import es.in2.issuer.infrastructure.crypto.CryptoConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

public class M2MTokenServiceImplIntegrationTest {

    public static MockWebServer mockBackEnd;

    private M2MTokenServiceImpl m2MTokenService;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());

        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();

        VerifierConfig verifierConfig = getVerifierConfig();

        ObjectMapper objectMapper = new ObjectMapper();

        JWTServiceImpl jwtService = new JWTServiceImpl(objectMapper, new CryptoComponent(new CryptoConfig(verifierConfig)));
        m2MTokenService = new M2MTokenServiceImpl(webClient, verifierConfig, jwtService,objectMapper);
    }

    private static @NotNull VerifierConfig getVerifierConfig() {
        ApiProperties apiProperties = new ApiProperties("","","yaml",null);
        AzureConfigAdapter azureConfigAdapter = new AzureConfigAdapter(null,null);
        YamlConfigAdapter yamlConfigAdapter = new YamlConfigAdapter();

        ConfigAdapterFactory configAdapter = new ConfigAdapterFactory(apiProperties,azureConfigAdapter,yamlConfigAdapter);
        VerifierProperties verifierProperties = new VerifierProperties(
                "did:key:zDnaekiwkWcXnHaW6au3BpmfWfrtVTJZrA3EHgLvcbm6EZnup",
                "https://verifier.dome-marketplace-lcl.org",
                "did:key:zDnaezLhSFWRZ1zrYQRwLxV8nDWmb2REHoUf7N3qvna1TFina",
                "ZXlKaGJHY2lPaUpTVXpJMU5pSXNJbU4wZVNJNkltcHpiMjRpTENKcmFXUWlPaUpOU1VoUlRVbEhNM0JKUnpCTlNVZDRUVk5KZDBsQldVUldVVkZFUkVKc1JWTlZaRXBXUlZaTlNVWlNWRWxGUmtWV2EwWlBVVEJXUlVsRlRrSkpSV041VFZKSmQwVkJXVVJXVVZGR1JYZHNRMDVFWXpCT1JHTXhUbXBCZUV0NlFYQkNaMDVXUWtGelRVbHJVa3BTTUd4VlVsVjNaMVpHVFdkUk1GWlRWa1ZzUjFOVlRrSldSV3hRVkdsQ1FsWldVa2xVTVVwS1ZrWnJlRXRFUVcxQ1owNVdRa0Z2VFVnd1VrcFNNR3hWVWxWM1oxUXdOR2RXUmtwV1ZURlNSbEpEUWxSU1ZrcFhVMVZPUmxWNVFsUlVSbFY0UlhwQlVrSm5UbFpDUVdOTlEyeGFhR0pIZUdoYVJ6bHpZVmRSZUVONlFVcENaMDVXUWtGWlZFRnJWbFJCYUZGQmFYRlZUMVp3ZEd0alZ6QnBiR2N6WW5FclRIRmpSRTl6TlZFOVBTSXNJbmcxZENOVE1qVTJJam9pV1VkamVucDVNVVJuYXpsbk4wMHlkR00zTjI5VGRsUjNRMWhZVUhORVVWQlhUV1pNYkMxc1NVaDVOQ0lzSW5nMVl5STZXeUpOU1VsSlJFUkRRMEptVTJkQmQwbENRV2RKVlVGSmNXeEViR0ZpV2toR2RFbHdXVTR5Tm5acE5tNUJlbkpQVlhkRVVWbEtTMjlhU1doMlkwNUJVVVZPUWxGQmQyZGlSWGhKYWtGblFtZE9Wa0pCVFUxSFZWSktVakJzVlZKVmQyZFdSazFuVVZWU1YxRlZOVVJTVlZGblVUQkZaMUo2U1hoRmFrRlJRbWRPVmtKQlZWUkRWVWt3VG5wUk1FNTZWVEpOUkVWeVRVTnJSMEV4VlVWRGQzZHBVa1ZzU0ZOV1VrWlVRMEpWVlhsQ1JGSldTbFZUVlZwS1VUQkdWVk5WT1U5SlJVWldWa1ZvVUZWcmJGVlhWRVZ2VFVOWlIwRXhWVVZEWjNkbVVrVnNTRk5XVWtaVVEwSlFWR2xDVlZWc1ZsUldSVlpGU1VaT1JsVnNXa3BSTUZaVVNVWk9UVlpVUlZSTlFrVkhRVEZWUlVKM2QwdFdiVVp6WWtkR2EySXllSEJhUkVWTVRVRnJSMEV4VlVWQ2FFMURVbFpOZDBob1kwNU5hbEYzVG1wRk0wMVVUWGxOUkVVMFYyaGpUazFxWTNkT2FrVXpUVlJOZVUxRVJUTlhha05DZFdwRlpVMUNkMGRCTVZWRlFYZDNWbEpGT1U1U1UwSkZVMVZrU2xaRlJrMUpSV3hGVWxVMVZWTldVbHBOVW1kM1JtZFpSRlpSVVVaRmR6bEtVa1ZPUmxWNU1ERk9hbFV5VGxSWk1VNXNRWGhFVkVGTVFtZE9Wa0pEYjAxQ1JWSlFWRlZWZUVkVVFWaENaMDVXUWtGUlRVVkZVa3BTTUd4VlVWVjNaMU5WVWtaVWJGSktWa1pyZUVoNlFXUkNaMDVXUWtGelRVWnJVbEJVVlZWblVUTktiRnBIVm5Wa1IyeG9Za05DU21NelRqRmFXRWw0UjBSQlYwSm5UbFpDUjBWTlJERmFRbFpGVmxSTVZVa3lUVVJaTUU1VWEzZE5SRVZOVFVGdlIwRXhWVVZEWjNkRVUxVTBlVTFSYzNkRFVWbEVWbEZSUjBWM1NrWlZla05EUVdsSmQwUlJXVXBMYjFwSmFIWmpUa0ZSUlVKQ1VVRkVaMmRKVUVGRVEwTkJaMjlEWjJkSlFrRkxaWGRHSzFoR2NYTnNObXd6VmpKVFJFMVFPRVo0YUZWcGFtVmpaa2xGWWprNFZIWkRTVmxUYkdaNGRsTjFkWHA2TjJWVU1sVm9TVVZ2U0hkdWRqZEVkVzFDUmtwR2JVNTNhVE5sS3poT1pXdFVSamxpWTNGWWQxQjVibXhNY1dsRGVGcHNlbU5CYkd3MlFUbHdaRFF3Y2pJMWFXWnFNM05hVDNCYVFteFNVbUkwTDNSMlZpOUJhbmc1ZDNSRE1VeGlXbGxoZEdFMGRXOXhVVlV2YVRCS1QwUktkR1JzVkVGUVMyOU9aWFJCZFdZM0szUnlla0pTVWtaaE5FVXhaQzlKYTNCd09GVnJkWGRLY0d4WVJtbFJXWFUxUjB3eFN6ZDRjWFE0VG14bkszWmxVWEpQWW1sWWRGSktWRlZ4TTA1TFFqZFFkR3g0T0N0blkzWlZWM0J2TkVsV2RtTkNjbnBRU205MFMyMWFTRUp1WjBOTGFFSmhZVTlPYVhwcmQwWkVSVTU2Yld4Nk4wRjNNMFZGUjJWSGNFOTZUVUo2ZW04MVdHNTVWM1U1YUdWUGQwTnFabXhwUkhOMFVGUnZZbGxvZWxOUVdrWXdkRGRsTlhCNFQyRnJVM0pMYmpaMlZWQlpjVEJMVVZsM2NEZFFkMGhpTlZWSmRYSk5hVXR1YjBndlJtaEtWSFZxYm05U1RUZ3hVa05RTkdkb1VWbHhiWGhzZUhReVNVYzRaR3RYVm1zM1FtSkRNamh3VmtOUGNIaEtXRXBUZGpScVkzcDNTMjVPUkhrNWJFRXpRa2xGVmpNeVNEaFJLMVlyZUU5NGFWcFJkV05aY0ZOV1ptTkxRbHBtZDJWWVl6WlNTbGhDWVM5eFF6TnlZbXAwWm5OWVkzTmtiazVxTjJNNWVtMVhUaXRXUTBsRFRWVk1NVloyTVVnM1pFTnVTeTlHWlRGMVFqUXljWFpJSzFFclZsVjRhbkZPV1dGTmJFWndhRXBWSzNOVFluQjJjSGRvTjNvNWNGQmhWVnBNUlhCV016ZGpaazFvTVRsRlpWRTNMemxWVEM5U1ZsRklSa1pxTDJsa1NDc3dZblZHT1hWc2NUbHZkelJGYWtjNVlVdHNaVVJHT1VvclRreGtOWGQyTmxwM1RURTRTeXRNUzNkdWNHTlhVekp6UjJ4SVRtaE5Oa1J1ZEU1bmVHZHZiRmR6TVZkbWFFRm5UVUpCUVVkcVoyZEpVRTFKU1VORGVrRk5RbWRPVmtoU1RVSkJaamhGUVdwQlFVMUNPRWRCTVZWa1NYZFJXVTFDWVVGR1NVbFViMkZOUTJ4Tk5XbEVaVUZIZEdwa1YxRllRbU5TWVRSeVRVaFJSME5EYzBkQlVWVkdRbmRGUWtKSFozZGFha0VyUW1kbmNrSm5SVVpDVVdOM1FXOVplV0ZJVWpCalJHOTJURE5DY21GVE5XdGhWMlJ3WkVkV2MyUklUWFZhV0UxMlVrVnNTRk5XVWtaVVJsSlVWVlpXUWxSRmJFZFRWVlpGVVRCR1NFMVROV3BqYmxGM1NrRlpTVXQzV1VKQ1VWVklUVUZIUjBkSGFEQmtTRUUyVEhrNWRsa3pUbmRNYlZKd1dqSnNNRnBYZURCamVUVnNZM3BEUW5kQldVUldVakJuUWtsSE5FMUpSekZOU1VkNVFtZHpja0puUlVWQldVOXVWVkZ2UkVONlEwSnZha0V2UW1kbmNrSm5SVVpDVVdORFFWSlplbUZJVWpCalNFMDJUSGs1ZDJFeWEzVmFSMnh1WVZoU2JHSklVbnBNYlZaNlRESlNkMWw1T1VWVFZXUktWa1ZXVFZaR1RtWlNSa0pFVEc1WmVVeHFSWFZqUjFKdFRVWTRSME5EYzBkQlVWVkdRbmRKUTAxR1RVMVZWVTVzWTI1U2NGcHRiR3BaVjFKMlNVZE9NVmxYZUhCYWJXeHFXVmRTZGtsSFVteEpSMXB3WTIweGFFbEhWbk5hVjA0d1kyMDVkV0ZYVG1oSlIwWXlXVmMxTmxsWFVtaEpSMUpzU1VoQ2JHTnVUblppYlVWbldtMXNlbUZYVG1oSlNGcHdZbTFPTVdKSFJtdFpWRUZRUW1kcmNrSm5SVVpDVVdOM1FWRlZSVUZuVlVGTlFqQkhRVEZWWkVwUlVWZE5RbEZIUTBOelIwRlJWVVpDZDAxRFFtZG5ja0puUlVaQ1VXTkVRa1JDUTBKblRsWklVamhGVDNwQk5VMUVaV2RPWVVGNmFHcEdiMlJJVW5kUGFUaDJXVE5LYzAxVE5YZGhNbXQxV2tkc2JtRllVbXhpU0ZKNlRHMVdla3d3VWxWVk1VWXhXVmQ0Y0ZwdGJHeGFSVTVDVW5wRmRWa3pTbk5OUWpCSFFURlZaRVJuVVZkQ1FsRmFZM1J1WjBSSFozZG1ja3hVY0ZGbmFreGxWbW95WmpCa1ozcEJUMEpuVGxaSVVUaENRV1k0UlVKQlRVTkNjMEYzUkZGWlNrdHZXa2xvZG1OT1FWRkZUa0pSUVVSblowbENRVXAxSzJGdU16WmxVVFppV1RKV2VXbzNjR0k1TlZvcldFdFhOR0ZLUjNGS2JscDZNVGxPZEM5RkwweEthSGd5ZEdwUk1rRTJlREZ6VFhWTldrUTFWRGMwYkc4M2EyUkpaVk5SVFdKUWVsTm9abEZZVXpWcVJFa3JhRXAyWTBvM1YwNU1UbnBKVm5Gd0sxUTVPVGRPYzNWbVdtOU1iME5JZDIxV2VEY3pRa2gzVm5OUFRVaElObFJtVmxWbGRESTVNVk5HU0VGRVFTdEJWVE5zY25Ga1NpOVRLMlZsTkhaV1pFTnNiVWhVVERaS05rbFFaamhJVVVrMk5IbHFkbTFIV1RWUVZGUXpkMnh3VVc5cUwxbG5lbEI2V1M5cU1uTlFWMVl3VDBwclEyczFVVkY0VldocVExSjZLM1JpTUZCdWVXNW1jVUZUYjA1U1EwUjFUMUF3VDFWdllXNTFhRTExVmxOV2Ntb3lOekJvUTJkUU1sSlVWR0ZJTWxONEwxaG9VVUZUUjIxV09YTnFhSEphWmxwTmIxTjZUM293UVhNelZGUmpVRkU0WkU1cVMxaHZZbGMzVW5sM1dFMUJTR0ZaTW1OMlVXaHpXa1JXYW1wV01tOTNhR05GYW5CTlp6UlROemhJWm5KbFduTXhXSFZIY2pnMVRUWmxPSGgyWlhCVE1IVm1RMmRKVFd4dVdFNDJNRmxNWjNsbU1rc3JRbWhPVjB0RVZHbE5kR2RCTDBwc1dtbEpPRmRVTUhFMVFXWk9VMVZDZG1KeldIcHNObTltUkUxb1QxSlZUMGdyUm5OcU5USjJTa1JIZFhkVlVrNUdhR3AxZVhocU0yZEZNVzFMYkZCTmNWUlRTV2xDYUVWMk5XaFJjbFJTVjJOeGJqTXJlR3MxZHk5dlRXeHpOMnc1V2s5eWVXVTRaVWRKVUhJck5DOUJiV3MzWTA0emVEbEpWRTgzV25sQ1NXVjVWMVpEYW1WS2NUSXdlVlZ5TWtKUVJWWkxWRTFVUVV4cGNXMWtSak5yVldJMlRUTm5ZbXBhV1ZSVEszQkpjM2hRU1cxaWMzSnJNVzltYUdSM2MxZGxjV1UyVm5CTVVFMTBielJQV0VsWFpUWkNlWGhyTm10blNuUkZlVGR3ZFhkbEt6Z3dSM2hSTTBsVVFVMXhNbVpwT1hZMVVtWkROamxUUld3eVZtdFRkMDE1VVRKR1YyNW5JaXdpVFVsSlIxWlVRME5DUkRKblFYZEpRa0ZuU1ZWRk5uQXpXRmRoY1ZZNGQybGtWREJIWjBabFkzRTVUV0pJYkRSM1JGRlpTa3R2V2tsb2RtTk9RVkZGVGtKUlFYZG5Za1Y0U1dwQlowSm5UbFpDUVUxTlIxVlNTbEl3YkZWU1ZYZG5Wa1pOWjFGVlVsZFJWVFZFVWxWUloxRXdSV2RTZWtsNFJXcEJVVUpuVGxaQ1FWVlVRMVZKTUU1NlVUQk9lbFV5VFVSRmNrMURhMGRCTVZWRlEzZDNhVkpGYkVoVFZsSkdWRU5DVlZWNVFrUlNWa3BWVTFWYVNsRXdSbFZUVlRsUFNVVkdWbFpGYUZCVmEyeFZWMVJGYjAxRFdVZEJNVlZGUTJkM1psSkZiRWhUVmxKR1ZFTkNVRlJwUWxWVmJGWlVWa1ZXUlVsR1RrWlZiRnBLVVRCV1ZFbEdUazFXVkVWVVRVSkZSMEV4VlVWQ2QzZExWbTFHYzJKSFJtdGlNbmh3V2tSRlRFMUJhMGRCTVZWRlFtaE5RMUpXVFhkSWFHTk9UV3BSZDA1VVNUVk5WRWwzVFVSUmQxZG9ZMDVOZW1OM1RsUkpNazFVU1hkTlJFMDFWMnBEUW5OVVJXbE5RMEZIUVRGVlJVRjNkMXBTUld4SVUxWlNSbFJEUWxWVmVVSkNVa1phUWxSclRrWlNRMEpFVVZOQ1NFMXFSVk5OUWtGSFFURlZSVUpTVFVwUmFsRXpUa1JSTTA1VVdYZE5VM04zUzFGWlJGWlJVVXhFUTBwRlUxVmtTbFpGVmsxSlJsSlVTVVZPUmxWc1VrcFNhMnhFVVZaU1NsUXdOR2RSVmxaVlUwVTVVMU5XVWxwTlUyZDNTbWRaUkZaUlVVdEVRamxGVTFWa1NsWkZWazFKUlRsUFNVWlNVMVpXVGxWU1ZWRm5WVEJXVTFacmJFUlNWazFuVlRCNFZrMVNUWGRGVVZsRVZsRlJTRVJCY0ZkWlYzaHpXVmRTZG1KSGJHdE5VWE4zUTFGWlJGWlJVVWRGZDBwR1ZYcERRMEZwU1hkRVVWbEtTMjlhU1doMlkwNUJVVVZDUWxGQlJHZG5TVkJCUkVORFFXZHZRMmRuU1VKQlRVOVJZVUpLUjFWdVMzWjROREJMV2tRMlJXVjFXVTFUZUVGQlkyTnpTSGxPU2xjMmNVMXVhelkzYms5UVNFSTVOMmRxVW1kdWMwcDRaV2hWT0ZGUVozaG9UMkpvY1RkclYyTXdNblpYT0c1UlNWTXljWGszTUVocVZ5dDVOa2xOWVU5MGJIbHJjMjlPV0U5amVsRnZXa051Vm5GQ1NXa3ZhMFJ6VDJoR1ZqRnlZMFZZWVdsQ1JWUXZUblZKY2xOTGRrZFpSVWxrZWtFNVNtRnhXV1JtYVM5S1VTOXNjbGxoZVVSbVVETmtOek5vYzNWeEsyeEphazR3WkRsb0szQkxZMWwzVEM5dFNVbGlTeTlqVVhkc2JFRlZiV1JrY2tGM09WZEZiWEZyYkNzMVVuVkVWM0Z3YkVSWGFHaDJjRWRLUmxCWWREUlNjVXRuWVdGV1RqVlVWWGRUTWs5SFNsTk9jVU56TmxwSksyRlRaRzVsVkdkRGNYRlJMeTg0TTJoT09WRnpiVEJ0UWpCT09FNVBPV3h4VTNCRGJWQlBhbGxIVDFSd04wbHJPR2xDTjNSbGVERlBUbmxsV0UxSWJEbDZTMFJqYVhGV01UWXlXbEp3UjNSS2JUSnlkVGcyU1ZWRFUycFFiSE54VkZoTmJsY3hOREpOUzNWbmMxY3pXRGN4V1RCeGVETkVVbFVyTTB4M1oyTktjV0ZQTVZrdk9VUXlhMUZGVVVvemRqVmFaV2xIVVdGMVVsZHhabXBxUVd0RlVtZG9Lemh0TTFkWVdFeHlibnBCYjBab2NsRmtiRUpoTVZFMk1Va3lWWEZpY1hoaVFUQmtVemxNWkU5ME5TdHVSa1pXV20wclJUZEJRV1ZXZVhJNFZXcFdWMVJrU2xGMlZFNHpkWEV3Vm10TU1HNHljSEV3TXl0SVlqUm5VRkk0ZG5Kd1JEYzVTbmxzZVZWalNWSXdVVTVKWjAxMFJVWmxOR1ZHU2l0cFF6a3JiV0psVDJwNlNGRnJiRGhhUnpVMU1WZ3lTM2syYzJ3elQwOXVaamt6V0dWa1VVUXdka2N3Y2tOWmNGSkhXaXMxTUdzd05XcHNkVXQ2VW1wamFYRkJRMmRNU0VOR1UzQmpUSGxDVTB0bmNsaGpRVEJ4YkhCWlJGUkpZbVY0T0RsVWRsSkhXVEZ1YjNkeVF6VnNiVWRPVkRocVNuSjRRMWxQV1VSQlowMUNRVUZIYWxsNlFtaE5RVGhIUVRGVlpFVjNSVUl2ZDFGR1RVRk5Ra0ZtT0hkSWQxbEVWbEl3YWtKQ1ozZEdiMEZWWjJoUGFHOTNTMVY2YlVsT05FRmhNazR4V2tKalJuaEdjbWx6ZDBoUldVUldVakJQUWtKWlJVWkpTVlJ2WVUxRGJFMDFhVVJsUVVkMGFtUlhVVmhDWTFKaE5ISk5RVFJIUVRGVlpFUjNSVUl2ZDFGRlFYZEpRbWhxUVU1Q1oydHhhR3RwUnpsM01FSkJVVEJHUVVGUFEwRm5SVUZLUjFGTGNsb3lWVE5LTDFOd1IyaFFOM3BYYW5aM1pVSlllR3BYTlhWVFpIZ3dWamR0ZDNZMGJYWkRNbFpzUXpGVWRuaEZialY1Vm01a1JWVkRjR3hIY0M5dE1GTXpRVEEzUW5SUVdqSTBXbE4xVW5jcmJVbHdkRUp0UTJoaWJsVXhkbW95UWtad1JrWlVhSEJ6VVVwSE1HdEVha1F5TTBodk5uQXpVblJOY21saU9FbHBNRkp1YjFWaWQzQlFOVTR5VEdsbFQySjFiMlE1VDFNNWNUTk5aME5zYUhrNVJqazViVTlYZGtRdmNUVjJRMVp2SzNWTVYxcDFVVFJoWTNWVVZFNTRZVFZFU0hscGFtZENLMGRIYnpKUGFFaHNaSEpUY0hBclRGSm5WVFZtYTA1TFJ6Qk1lbWhzU1VWSFpFVkNZV3d3Y0hWYUx5dFJjWFJUY25KTVJFMVVORmhRUzFkTlNqWm5jSE55TTJ4WVptSmhNRVZzTjJKaUx6YzFOblJOV1VGaVdIcHRibXRyVlhGa2FVOUpOVGR5VmtSR1ZEbEdTbmhxVm1kdk5XOVhPRmhQUzBkVFRIRk5TRE14V0dsS1EwNXZTRFZ5U2xrNFZsRXpXbTFOVTNWb09UZHJRVUZvV0hWR1NXSlJXamRHY210R01ua3JSM05MY0dJd1lUbGFWWEZHUW5KS2JIcEllRU5MYkRoVFUxUjNaa2RFWjJOd1pWQmFlRlZKU1dkUVVHTkpORzlZZDFKdlFqQklZblExTkVseVVtOUhOMnRYYXpZNFoxZ3lZMnBMVmpCWmRFaHRWbWhGUlVaeU0yUnBXbVpQTjIxQlZFRTFOSE5NV2xnNWJqRnNiM051WmpsNGNtVkZlbVJGV1ZkaWVVZFVhRlYzYkRNelRWQTJXRXhoUmxKUVpHSnVVWE5vWW5KdlpYQjZaeXR1YTNOVk5WWldTekphV2taSlYxWlpObWNyVW1oSlExaFdaR2h4YTBKd1RtMHJaVXN3SzNkVlEwRXhkRmhaZVZKTGIxTlZWbkJOUmxOQldtaHVjM2xWWlZwNllXMVFTRVJsTkVkclZHRnRUVXMwY1daWVMxRlBZamRGZEZkVlYyZzFabTlXVTNwaGNYbDJSbkJ3VlRSV1RYQXZaMHR5VUZsSVJEWmlWM0pJU2pWMlF5OUNOMWR5TDJGUWRHaE9hMmRZUmsxSFRYSlNNRDBpWFN3aWRIbHdJam9pYW05elpTSXNJbk5wWjFRaU9pSXlNREkwTFRBNUxUTXdWREV3T2pBNU9qUTVXaUlzSW1OeWFYUWlPbHNpYzJsblZDSmRmUS5leUp6ZFdJaU9pSmthV1E2YTJWNU9ucEVibUZsZWt4b1UwWlhVbG94ZW5KWlVWSjNUSGhXT0c1RVYyMWlNbEpGU0c5VlpqZE9NM0YyYm1FeFZFWnBibUVpTENKdVltWWlPakUzTURRd09UWXdNREFzSW1semN5STZJbVJwWkRwbGJITnBPbFpCVkVWVExWRXdNREF3TURBd1NpSXNJbVY0Y0NJNk1UY3pOVFk0T1RVME1Dd2lhV0YwSWpveE56QTBNRGsyTURBd0xDSjJZeUk2ZXlKQVkyOXVkR1Y0ZENJNld5Sm9kSFJ3Y3pvdkwzZDNkeTUzTXk1dmNtY3Zibk12WTNKbFpHVnVkR2xoYkhNdmRqSWlMQ0pvZEhSd2N6b3ZMM2QzZHk1bGRtbGtaVzVqWld4bFpHZGxjaTVsZFM4eU1ESXlMMk55WldSbGJuUnBZV3h6TDIxaFkyaHBibVV2ZGpFaVhTd2lhV1FpT2lJNFl6ZGhOakl4TXkwMU5EUmtMVFExTUdRdE9HVXpaQzFpTkRGbVlUa3dNRGt4T1RnaUxDSjBlWEJsSWpwYklsWmxjbWxtYVdGaWJHVkRjbVZrWlc1MGFXRnNJaXdpVEVWQlVrTnlaV1JsYm5ScFlXeE5ZV05vYVc1bElsMHNJbWx6YzNWbGNpSTZleUpwWkNJNkltUnBaRHBsYkhOcE9sWkJWRVZUTFZFd01EQXdNREF3U2lKOUxDSnBjM04xWVc1alpVUmhkR1VpT2lJeU1ESTBMVEF4TFRBeFZEQTRPakF3T2pBd0xqQXdNREF3TURBd01Gb2lMQ0oyWVd4cFpFWnliMjBpT2lJeU1ESTBMVEF4TFRBeFZEQTRPakF3T2pBd0xqQXdNREF3TURBd01Gb2lMQ0psZUhCcGNtRjBhVzl1UkdGMFpTSTZJakl3TWpRdE1USXRNekZVTWpNNk5UazZNREF1TURBd01EQXdNREF3V2lJc0ltTnlaV1JsYm5ScFlXeFRkV0pxWldOMElqcDdJbTFoYm1SaGRHVWlPbnNpYVdRaU9pSTNZbVkxTldReVpTMDFNalEzTFRRM01UUXRPVEZrTVMwNFpUSm1PR05pTnpNd1pERWlMQ0pzYVdabFgzTndZVzRpT25zaWMzUmhjblJFWVhSbFZHbHRaU0k2SWpJd01qUXRNREV0TURGVU1EZzZNREE2TURBdU1EQXdNREF3TURBd1dpSXNJbVZ1WkVSaGRHVlVhVzFsSWpvaU1qQXlOQzB4TWkwek1WUXlNem8xT1Rvd01DNHdNREF3TURBd01EQmFJbjBzSW0xaGJtUmhkR1ZsSWpwN0ltbGtJam9pWkdsa09tdGxlVHA2Ukc1aFpYcE1hRk5HVjFKYU1YcHlXVkZTZDB4NFZqaHVSRmR0WWpKU1JVaHZWV1kzVGpOeGRtNWhNVlJHYVc1aElpd2ljMlZ5ZG1salpVNWhiV1VpT2lKSmMzTjFaWEpCVUVraUxDSnpaWEoyYVdObFZIbHdaU0k2SWtGUVNTQlRaWEoyWlhJaUxDSjJaWEp6YVc5dUlqb2lkakV1TUNJc0ltUnZiV0ZwYmlJNkltaDBkSEJ6T2k4dmFYTnpkV1Z5TG1SdmJXVXRiV0Z5YTJWMGNHeGhZMlV1YjNKbklpd2lhWEJCWkdSeVpYTnpJam9pTVRJM0xqQXVNQzR4SWl3aVpHVnpZM0pwY0hScGIyNGlPaUpCVUVrZ2RHOGdhWE56ZFdVZ1ZtVnlhV1pwWVdKc1pTQkRjbVZrWlc1MGFXRnNjeUlzSW1OdmJuUmhZM1FpT25zaVpXMWhhV3dpT2lKa2IyMWxjM1Z3Y0c5eWRFQnBiakl1WlhNaUxDSndhRzl1WlNJNklpc3pORGs1T1RrNU9UazVPU0o5ZlN3aWJXRnVaR0YwYjNJaU9uc2lZMjl0Ylc5dVRtRnRaU0k2SWpVMk5UWTFOalUyVUNCS1pYTjFjeUJTZFdsNklpd2lZMjkxYm5SeWVTSTZJa1ZUSWl3aVpXMWhhV3hCWkdSeVpYTnpJam9pYW1WemRYTXVjblZwZWtCcGJqSXVaWE1pTENKdmNtZGhibWw2WVhScGIyNGlPaUpKVGpJc0lFbHVaMlZ1YVdWeXc2MWhJR1JsSUd4aElFbHVabTl5YldGamFjT3piaXdnVXk1TUxpSXNJbTl5WjJGdWFYcGhkR2x2Ymtsa1pXNTBhV1pwWlhJaU9pSldRVlJGVXkxUk1EQXdNREF3TUVvaUxDSnpaWEpwWVd4T2RXMWlaWElpT2lKSlJFTkZVeTAxTmpVMk5UWTFObEFpZlN3aWNHOTNaWElpT2x0N0ltbGtJam9pTVdFeU5qWTROalV0T1dOa1lTMDBNbU0wTFRnNE5HWXRZbVF4T0dFM09XVTRZbVprSWl3aVpHOXRZV2x1SWpvaVJFOU5SU0lzSW1aMWJtTjBhVzl1SWpvaVRHOW5hVzRpTENKaFkzUnBiMjRpT2lKdmFXUmpYMjB5YlNKOUxIc2lhV1FpT2lKaE5HWmtOREptWlMxalpXUmxMVFJsTkRndE9ERmhNUzAwT1RZMlpHVXlNakZqTmpBaUxDSmtiMjFoYVc0aU9pSkVUMDFGSWl3aVpuVnVZM1JwYjI0aU9pSkRaWEowYVdacFkyRjBhVzl1SWl3aVlXTjBhVzl1SWpvaWNHOXpkRjkyWlhKcFptbGhZbXhsWDJObGNuUnBabWxqWVhScGIyNGlmU3g3SW1sa0lqb2lOMlkxTWpRNE5qY3ROR0ZpWkMwMFlUazFMV0UyT0RRdE0yUXlaRE5oT0dVeVlqSmpJaXdpWkc5dFlXbHVJam9pUkU5TlJTSXNJbVoxYm1OMGFXOXVJam9pU1hOemRXRnVZMlVpTENKaFkzUnBiMjRpT2lKcGMzTjFaVjkyWXlKOVhTd2ljMmxuYm1WeUlqcDdJbU52YlcxdmJrNWhiV1VpT2lJMU5qVTJOVFkxTmxBZ1NtVnpkWE1nVW5WcGVpSXNJbU52ZFc1MGNua2lPaUpGVXlJc0ltVnRZV2xzUVdSa2NtVnpjeUk2SW1wbGMzVnpMbkoxYVhwQWFXNHlMbVZ6SWl3aWIzSm5ZVzVwZW1GMGFXOXVJam9pU1U0eUxDQkpibWRsYm1sbGNzT3RZU0JrWlNCc1lTQkpibVp2Y20xaFkybkRzMjRzSUZNdVRDNGlMQ0p2Y21kaGJtbDZZWFJwYjI1SlpHVnVkR2xtYVdWeUlqb2lWa0ZVUlZNdFVUQXdNREF3TURCS0lpd2ljMlZ5YVdGc1RuVnRZbVZ5SWpvaVNVUkRSVk10TlRZMU5qVTJOVFpRSW4xOWZYMHNJbXAwYVNJNklqYzBOVGt6T1dNd0xUUmtZbVl0TkRVeFlpMWhNakV6TFRNeVpHWTBaakEzWXpJMk5DSjkuRERhcFJPTGJjbUtSTk90WlJ6VEo4ZFVPcE1tQUwtQlRXZ0Q4dWExYlZEOC1qX2paMFg5bkp0NDl4ZXV0czBQRUM0Z3BJT01Jc1Flc0F2SC1MWEctZ1AtWTlCd2JWSkFHRGItSXAtYy0tSXlVak1YWGhrb0xHeFlxRm9ieU5ZU1ZteF9QREFFZXh1U0VCSmo5M3M4MDJWcmQwRnNiUkRyc25NcDYtU1FicWFSYjdCejZZTW1uMUJwUXV3c0FCSXhTa2tlRF9NalJnTHpJNU91VWZpdGthMDBZTElOb2pONmxuOHRLcXRlc05DTXVzLWZXRFJrelo2UmhjNTdjaVhOWm13NXpYeUZ5LVJEOVRMangxNlRqd3BrZ0xUaWpsRDlBa05TWDdGZWo5RWRiUHRHc0dWMjA5Z0k4c3YzSUNweTE3MTlyTVJFN2wzU1JkbUpDRWdfbkZMZzZhQ1JUUjExb1lqc2h0WWdsblktaWxYaE9FRUdkTl9zcnZITmF4ZHZxUmNPc1FDX2pERFhFY3hNRnkzUXhnbGhvSUNLLVZOdGhnLXl1ekZvMGRuWUlublFEQ0lBNXYzVm9nYlk3NFk0UE1JMHgxSXk0dUxEaS1aOW0xXzhfQlkwVHF1allfZElZNVFlU2xncnFtdjVPZmY2R2JGRlEtZVQ5TWlzTkhJU2xTYUhrcXJXWmRjVGVBUktQT0JoSmZIYlJZcnNPQk9VcW9rMlhjWndhOUwxT01naUJFNGg2S1Y5cDA0dzN0b2h0OHQwUFgwNzMzbVZJdzl2OUNpOFRQMWVzdHROenZwRzRDVHYxNEUtTXNzaHM1Mi1OQVlTeVNkUEtSRmh5RnJ1QUJaSWEzakZpTndPaDdFamdJRFJFSVo3RWNaUm02Tkg2T3RGODlLTkxvdXM=",
                new VerifierProperties.Crypto("83bd452e1e9536d244d9a440edd282d96738d9409b9168204377b5e1bba40329"),new VerifierProperties.ClientAssertion(new VerifierProperties.Token("DAYS",30)),new VerifierProperties.Paths("/oidc/token","/oidc/did"));

        return new VerifierConfig(configAdapter,verifierProperties);
    }

    @Test
    public void testGetM2MToken_Success() {
        String mockResponseBody = "{\"access_token\":\"mockAccessToken\",\"token_type\":\"Bearer\",\"expires_in\":3600}";
        mockBackEnd.enqueue(new MockResponse()
                .setBody(mockResponseBody)
                .addHeader("Content-Type", "application/json"));

        Mono<VerifierOauth2AccessToken> result = m2MTokenService.getM2MToken();

        StepVerifier.create(result)
                .expectNextMatches(token -> token.accessToken().equals("mockAccessToken"))
                .verifyComplete();
    }

}
