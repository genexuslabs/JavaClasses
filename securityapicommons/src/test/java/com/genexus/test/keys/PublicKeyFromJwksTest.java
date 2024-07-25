package com.genexus.test.keys;

import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.test.commons.SecurityAPITestObject;
import junit.framework.Test;
import junit.framework.TestSuite;

public class PublicKeyFromJwksTest extends SecurityAPITestObject {

	private static String azure;
	private static String akid;
	private static String googlev3;
	private static String gv3kid;
	private static String googlev2;
	private static String gv2kid;
	private static String facebook;
	private static String fkid;


	public static Test suite() {
		return new TestSuite(Base64PublicKeyTest.class);
	}

	@Override
	public void runTest() {
		testAzure();
		testGooglev2();
		testGooglev3();
		testFacebook();
	}

	@Override
	public void setUp() {
		azure="{\"keys\":[{\"kty\":\"RSA\",\"use\":\"sig\",\"kid\":\"q-23falevZhhD3hm9CQbkP5MQyU\",\"x5t\":\"q-23falevZhhD3hm9CQbkP5MQyU\",\"n\":\"-9dXkqvGEcIba_TAwmY_ezbn6kLFl6ZMyR7XFCacMf6bBQ0CLlOy9KrDYxI5bm2XAb7qHMKb40d1CyAtO40BV1MZQdrkmLay7RFjQBV6SB24TrC4P0nNXWJCchzzlyesb6S3mqvWVaxRkTS3FkFC8UMsqg2itVcKJW1S2qQCOwF3Sug5GBPYezM_F56_ZfLBpA2aRHzE4eboWoiFjjXN9NqUELACl-DyfWXiyqehRIjuX6hG7lT2erkwWwFgFApi3W4zRtjCx5kRzZtqjDBLWYypBdBBFF8teaCjOq09RF05ZMRlzQKIORb0MxJw_XxVCzXPkKsdchZMBbH3PIuB4Q\",\"e\":\"AQAB\",\"x5c\":[\"MIIC/TCCAeWgAwIBAgIINKBA6Wcz+Q4wDQYJKoZIhvcNAQELBQAwLTErMCkGA1UEAxMiYWNjb3VudHMuYWNjZXNzY29udHJvbC53aW5kb3dzLm5ldDAeFw0yNDAzMDYxNzAzMzJaFw0yOTAzMDYxNzAzMzJaMC0xKzApBgNVBAMTImFjY291bnRzLmFjY2Vzc2NvbnRyb2wud2luZG93cy5uZXQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQD711eSq8YRwhtr9MDCZj97NufqQsWXpkzJHtcUJpwx/psFDQIuU7L0qsNjEjlubZcBvuocwpvjR3ULIC07jQFXUxlB2uSYtrLtEWNAFXpIHbhOsLg/Sc1dYkJyHPOXJ6xvpLeaq9ZVrFGRNLcWQULxQyyqDaK1VwolbVLapAI7AXdK6DkYE9h7Mz8Xnr9l8sGkDZpEfMTh5uhaiIWONc302pQQsAKX4PJ9ZeLKp6FEiO5fqEbuVPZ6uTBbAWAUCmLdbjNG2MLHmRHNm2qMMEtZjKkF0EEUXy15oKM6rT1EXTlkxGXNAog5FvQzEnD9fFULNc+Qqx1yFkwFsfc8i4HhAgMBAAGjITAfMB0GA1UdDgQWBBRRZhKrglETd/OCIUfwuGTRJkwNUTANBgkqhkiG9w0BAQsFAAOCAQEA46UCoAJlXMuxB/21GTP96KHx8zoYt2UnvCFxkLpRrkRThVBhTbd/txAYPb3eAGuhjJlNstKxfcCUaquKktQAdeGPA+Kn62DlksFaXjrcC0rGaiD+MiY2B/ATS0ebCYL3s3BJNmKKB9+1kbbcWCSQelcM2cNjJHJDqUhTyWMnTz4UK9RXwHFLq/T3yYP0qz/FZZR6RfCWzXz35SkZVObDMhPcyLgxy6CLMQotrtThsqn/2JZ7We/ehl6xCgVym6hfuv0Ju9WtZal+k46XDw2R3IoHJueB/vD0hLOqdZerwzD5OsCrTW2n3njN2nDHhCzlxGrhlleNDJhawFoxzSOuhQ==\"],\"issuer\":\"https://login.microsoftonline.com/5ec7bbf9-1872-46c9-b201-a1e181996b35/v2.0\"},{\"kty\":\"RSA\",\"use\":\"sig\",\"kid\":\"V1Y09OFMBXAZUFfJ-vWjlrH-ZSc\",\"x5t\":\"V1Y09OFMBXAZUFfJ-vWjlrH-ZSc\",\"n\":\"qinPWt_43NIzlluP8oLBaKXBHMJ0pZWNOu5yxCD_UZ7B5yrdMdzgFhr0cpDcp3vkkE97MyyIN3a2tg8UhHCtphkShbKiO36uNjc0TC-jVnhwI_mLZ1piP0gkItm26YTLw71rY9JpS2dY4CxnsHH6V8vsVaaQAGQByM6BdwUiep-7-Cn69iWybZkga_8gZAy9O1hOyBV22mKVRuWsLwLeBcahlOEGJeP4tx8vVSjKTX8tV2HJlL01JjxBojGdwtiqwr7iEOoUAwMhC9b80ASkSRcb-mU-SfGhmSP-DVgD1nXzFcu-v2bVDr4-OpLuOljzJVEz0r4W4tmf3KwToAQNcQ\",\"e\":\"AQAB\",\"x5c\":[\"MIIC/TCCAeWgAwIBAgIIdZL3187SZ0EwDQYJKoZIhvcNAQELBQAwLTErMCkGA1UEAxMiYWNjb3VudHMuYWNjZXNzY29udHJvbC53aW5kb3dzLm5ldDAeFw0yNDAzMDYyMTA4MDdaFw0yOTAzMDYyMTA4MDdaMC0xKzApBgNVBAMTImFjY291bnRzLmFjY2Vzc2NvbnRyb2wud2luZG93cy5uZXQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCqKc9a3/jc0jOWW4/ygsFopcEcwnSllY067nLEIP9RnsHnKt0x3OAWGvRykNyne+SQT3szLIg3dra2DxSEcK2mGRKFsqI7fq42NzRML6NWeHAj+YtnWmI/SCQi2bbphMvDvWtj0mlLZ1jgLGewcfpXy+xVppAAZAHIzoF3BSJ6n7v4Kfr2JbJtmSBr/yBkDL07WE7IFXbaYpVG5awvAt4FxqGU4QYl4/i3Hy9VKMpNfy1XYcmUvTUmPEGiMZ3C2KrCvuIQ6hQDAyEL1vzQBKRJFxv6ZT5J8aGZI/4NWAPWdfMVy76/ZtUOvj46ku46WPMlUTPSvhbi2Z/crBOgBA1xAgMBAAGjITAfMB0GA1UdDgQWBBQxCpcti/aMWYbvhjLCL5sKhcQ1pjANBgkqhkiG9w0BAQsFAAOCAQEAK+rZEgIu+e7ARr9IIYxMXSxJ9gy6Wnw0ZMtchoDvUufFmWnwHtj15zByNpaNRvojHWaWrfqQsk+bKzazPWXTOiM7yoLxKfzM4fYI3QP5TRizGvz5p4COLBiDB7XvOiFsm0SxJzhhba5+3J0CmDhsURfzhw7AX+OCL7arF5gPv4PMIQ8GiTlukVzWN7EhQ2DZbm74jVOYwTN74+ECynB/NZvprn0MOWmDMxD7ysi2Wv459tg1nGSMGkcIHOo2DSj18nm25QHzUjjbmkma36uw2lM/Lsjd0HsueD2nS+4eiVYqsuC4cDO/LvlRdMteuZLgvdqX35Xm608VGUPoDaFDUQ==\"],\"issuer\":\"https://login.microsoftonline.com/5ec7bbf9-1872-46c9-b201-a1e181996b35/v2.0\"},{\"kty\":\"RSA\",\"use\":\"sig\",\"kid\":\"L1KfKFI_jnXbwWc22xZxw1sUHH0\",\"x5t\":\"L1KfKFI_jnXbwWc22xZxw1sUHH0\",\"n\":\"ojzOLoPAa8DykJWn0dz9vB_lPTkwtCyw1oR8WFpszoBaG-hKmBhYLCuyyOWIoIgmxETI5-bulQzsCTR79WWxq6sNxXPTlBy4i1ihjwhnD7CrgRu0U_O8GPzMMPlQXYs_C-tt0OdPPYEYv_ZzbNkgMTNaR5WWe-d3L4nDMWrSP70Sz-8_kcmA1qaap_MT_N9a5QidD20bfOvdXttNxu6_azAVN5UYeBevrTaJHO2HRVicw_zKwJSZV26jljpsmGMiNUu2anARS2C4KM_HCIfX7cwE9A89pJ1Uh58bBL98b1NDzGkt0hVCBTilPakhR4RHP0U9dvHFo3pUJd1Qrl9AQQ\",\"e\":\"AQAB\",\"x5c\":[\"MIIC/TCCAeWgAwIBAgIIOdwuMdHVGkEwDQYJKoZIhvcNAQELBQAwLTErMCkGA1UEAxMiYWNjb3VudHMuYWNjZXNzY29udHJvbC53aW5kb3dzLm5ldDAeFw0yNDA0MTExNjA0MjZaFw0yOTA0MTExNjA0MjZaMC0xKzApBgNVBAMTImFjY291bnRzLmFjY2Vzc2NvbnRyb2wud2luZG93cy5uZXQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCiPM4ug8BrwPKQlafR3P28H+U9OTC0LLDWhHxYWmzOgFob6EqYGFgsK7LI5YigiCbERMjn5u6VDOwJNHv1ZbGrqw3Fc9OUHLiLWKGPCGcPsKuBG7RT87wY/Mww+VBdiz8L623Q5089gRi/9nNs2SAxM1pHlZZ753cvicMxatI/vRLP7z+RyYDWppqn8xP831rlCJ0PbRt8691e203G7r9rMBU3lRh4F6+tNokc7YdFWJzD/MrAlJlXbqOWOmyYYyI1S7ZqcBFLYLgoz8cIh9ftzAT0Dz2knVSHnxsEv3xvU0PMaS3SFUIFOKU9qSFHhEc/RT128cWjelQl3VCuX0BBAgMBAAGjITAfMB0GA1UdDgQWBBTXp9LEVP2CZiM0hEP5t18l/sDAfzANBgkqhkiG9w0BAQsFAAOCAQEAH5vIhhDC8d+31sm9Lo5Ohlsabmv7eA3JgXKw7x4VDFoQxrZHMw0qsQOk9bTrwJHa09o0CQkO8VZG5zJ0ZpXplRtrN8FaKCLb1qstF2P6wuZIZR1EJTJb4L5VvhaSUiFWkBfQPBDMVJd9g2CEOhW+1jIKEea0wt3AC2zUvNybhbSzDMyPNSwu0EiZLJsUMdbOxdGmCZjvIWMaddDu9GVT+KtPWYDDKs2POZoYPDGCp6ftd2+L+ma2gZCL3HvFBWjCIctsu20aHP8retkEOiVzMVbt68gDyT61VTlhBM/pxlcHMO6wfA1nQzY2qDwMCEOocmsYt3mrITolYxxyBjSVnA==\"],\"issuer\":\"https://login.microsoftonline.com/5ec7bbf9-1872-46c9-b201-a1e181996b35/v2.0\"},{\"kty\":\"RSA\",\"use\":\"sig\",\"kid\":\"UxyGE-ffaoAkooUxUFn0ar7fvDM\",\"x5t\":\"UxyGE-ffaoAkooUxUFn0ar7fvDM\",\"n\":\"2fVfr9fJb8m5eDwVp6WhzO7-ezI9OfeQPTpHKB8j2wlBJNu6oYGPC4McDysfNyKtUaYQpyckupAquEjAcP8CDjcv4B7pjdlgR25JLXuemMl8LD5bOu4s55EwmPewoHlBInWDK5rgRryoApZNQGVuI4u7Q56DNNsXo7I1qS13MmXKrwO1rp0PIetHesDNT2nnY6xjHlTGPkc-aydV8QNulStdiXOnbiV7wQzQmtqjK3AWE_oJHIuTaGwdVeqSqNaJKSCGEPb9G8J1Q-tFh0TOgqPjk5Qb_uO5X_0Fw_4OI1xeGqoMuOROi_StRXv4IvfbGnZgbsV4dOUL0kJEBRPglQ\",\"e\":\"AQAB\",\"x5c\":[\"MIIC6jCCAdKgAwIBAgIJAOnFoXSbn2jjMA0GCSqGSIb3DQEBCwUAMCMxITAfBgNVBAMTGGxvZ2luLm1pY3Jvc29mdG9ubGluZS51czAeFw0yNDAzMjQyMTQyMzJaFw0yOTAzMjQyMTQyMzJaMCMxITAfBgNVBAMTGGxvZ2luLm1pY3Jvc29mdG9ubGluZS51czCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANn1X6/XyW/JuXg8Faeloczu/nsyPTn3kD06RygfI9sJQSTbuqGBjwuDHA8rHzcirVGmEKcnJLqQKrhIwHD/Ag43L+Ae6Y3ZYEduSS17npjJfCw+WzruLOeRMJj3sKB5QSJ1gyua4Ea8qAKWTUBlbiOLu0OegzTbF6OyNaktdzJlyq8Dta6dDyHrR3rAzU9p52OsYx5Uxj5HPmsnVfEDbpUrXYlzp24le8EM0JraoytwFhP6CRyLk2hsHVXqkqjWiSkghhD2/RvCdUPrRYdEzoKj45OUG/7juV/9BcP+DiNcXhqqDLjkTov0rUV7+CL32xp2YG7FeHTlC9JCRAUT4JUCAwEAAaMhMB8wHQYDVR0OBBYEFOBFoFzi/Txj04qIPOx5PFEPf8WFMA0GCSqGSIb3DQEBCwUAA4IBAQCKdwSHnxHX2KZv63dhMugnBXR0qA6gVIOL3d60Nh7TReEhJ98DsSYtxSM9vrdMac+4m6Yg/slvRZD4jet/cH/qCjA+I+LUkrU0MEWqhf03BsEwlSY9ZGbOtWdgPwobFK7Y+M7wuPKSSrwPiBUgrG4UFpZbDVr3VYMljgYfXu3WwtshqdgslvQFQZugKLsoAaR9v1SlNRyOL6qDaqN9x3cHbIniApRjBJFrVijhZm5JRE/vyMgKl6EaBLDoQtvSIOARi8NwZi5LFB0361MR0eE18i87rvPO6cF4Vz2kg+cC9N54wf+gbINHREkbO70B1Q+QbbtlkfWR7y9Q1HMrnA3O\"],\"issuer\":\"https://login.microsoftonline.com/5ec7bbf9-1872-46c9-b201-a1e181996b35/v2.0\"},{\"kty\":\"RSA\",\"use\":\"sig\",\"kid\":\"EHu9neGZBCDyv2IYq8U5JiRMFng\",\"x5t\":\"EHu9neGZBCDyv2IYq8U5JiRMFng\",\"n\":\"w1kH9dFGdaJS8fvQulDssuuNhkczzy1Mo6IiNoC3ih3K-L_VF5TQmSkqXrovWCUlhBCfc1VPR9Cn2G4UP7Sygn0nTqXBY1NFQQZecqwGESJFIuonRqjdlDhNYXjSF_eg63KyuyLV8A-Sn05Ufuc8ax0tyrxPbkOql0pB2hmRhj94iDAFB2LBoxfEgxCG3VT0ascVYW6voTCChs2P65-4RLC-ib1w1FjuACDwsB7KZDxxaUGLfnIoLWUjmw1zCaDRiRvhxB4jQXpB64IFxaYsqxA_x8bj2JEE7qALZ2dZ3fPy9yYSAnRfaTMetgouR9x4SKy4HxUxsADMm_7p9LiRZQ\",\"e\":\"AQAB\",\"x5c\":[\"MIIC6jCCAdKgAwIBAgIJAO8yTjZIibNNMA0GCSqGSIb3DQEBCwUAMCMxITAfBgNVBAMTGGxvZ2luLm1pY3Jvc29mdG9ubGluZS51czAeFw0yNDA1MDYyMzA5MDJaFw0yOTA1MDYyMzA5MDJaMCMxITAfBgNVBAMTGGxvZ2luLm1pY3Jvc29mdG9ubGluZS51czCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMNZB/XRRnWiUvH70LpQ7LLrjYZHM88tTKOiIjaAt4odyvi/1ReU0JkpKl66L1glJYQQn3NVT0fQp9huFD+0soJ9J06lwWNTRUEGXnKsBhEiRSLqJ0ao3ZQ4TWF40hf3oOtysrsi1fAPkp9OVH7nPGsdLcq8T25DqpdKQdoZkYY/eIgwBQdiwaMXxIMQht1U9GrHFWFur6EwgobNj+ufuESwvom9cNRY7gAg8LAeymQ8cWlBi35yKC1lI5sNcwmg0Ykb4cQeI0F6QeuCBcWmLKsQP8fG49iRBO6gC2dnWd3z8vcmEgJ0X2kzHrYKLkfceEisuB8VMbAAzJv+6fS4kWUCAwEAAaMhMB8wHQYDVR0OBBYEFJ4xtCt3JpPxlUVH7ATgJGM4ofg7MA0GCSqGSIb3DQEBCwUAA4IBAQB9WAEvE3VtO5wIOtN5N/QbIU63H5QPgMW3M9nOs43AhLgwvWupxaiATyMqtK53RPvcxYPe7QwSw/xH9McXii1bOBVmc71AcjlXYfuMJ/0IMEFEUQwZDEwj+vIlg07gWh0hleehyAgMblDUQRRN+b5J+soa9LBBAooY/48F/++y4DiTzKyoWn5cV4H2kdIFVyB43XzJRqDoK1ZhplVLTc1a3K1NL1/qP9rhvtx62YDzfNh4+FTJLu31ALcUbD+Qx2m0U9wuWq3EdUzEen5DeLvhx55YD7V1BASHNYBd8lGhHk97aTw53CMGAuTELvWO+4x7dFM9autw2KvSn76n/4Ql\"],\"issuer\":\"https://login.microsoftonline.com/5ec7bbf9-1872-46c9-b201-a1e181996b35/v2.0\"}]}";
		googlev3="{\n" +
			"  \"keys\": [\n" +
			"    {\n" +
			"      \"alg\": \"RS256\",\n" +
			"      \"use\": \"sig\",\n" +
			"      \"kty\": \"RSA\",\n" +
			"      \"e\": \"AQAB\",\n" +
			"      \"kid\": \"6719678351a5faedc2e70274bbea62da2a8c4a12\",\n" +
			"      \"n\": \"oAP5OnSzKfkEV2QMm2XCuu4G8VGRBOyhKg-4H04WzYzPqM_Tmqi60Vod96JTo7SfM0OoGeNnlkWNjjBWkSS66alNLrvTNLi0A-KGeBsZiIFmrbsP6HHJfFzPd0Mci7-e11fNKecZgbC1me9PtRXFZb9JprZGFOvBiMwU0rRvh0GWYmTFj1HFjOIMAwTGOKOVGNuPjv0b3V0YaAkUNklzi4MM6qgzUb0tE0so1Ii7kBe7roMScS2USPeeJkeoPjLEbQcrT8MxOSxH-JgPLfq-zOnEJ6ERW3mtXdZCNzqmVLn5yjX5lKr5E2vgkPAHx9NLZ09fo_L9woeX_5epl6cIkQ\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"use\": \"sig\",\n" +
			"      \"e\": \"AQAB\",\n" +
			"      \"n\": \"w-l_VE4KNa22n4nsMwcabujowm924YoQQnwOz_dPYHmDI1O-r2bqw6mHmByXwii7aaeIMHJZWpmT5SkR3OYIu5RbSgiU-8JrQoplW_vZY2IqG1y5-frPC_9gnz_0qKKjtjqglCP-1AlfOdu7r5kOpkOACs5mWn4tm1K9R1EPjk2T_MMO7FkteZd8woh1fwUUuvbhPyDxBzx9EUsnGWbpTndOYc7W-EUk1jMtWBk3buLeaypVaOLWranK_XFrX-xx03BohrfinOqmftYgc0z94sxix7X1G36JZeh8-jpUhwyBPinBxOZOE_5kQn4CYM66Ygxwiws0ZJ-klG2qTi239w\",\n" +
			"      \"alg\": \"RS256\",\n" +
			"      \"kty\": \"RSA\",\n" +
			"      \"kid\": \"674dbba8faee69acae1bc1be190453678f472803\"\n" +
			"    }\n" +
			"  ]\n" +
			"}";
		googlev2="{\n" +
			"  \"keys\": [\n" +
			"    {\n" +
			"      \"kty\": \"RSA\",\n" +
			"      \"n\": \"oAP5OnSzKfkEV2QMm2XCuu4G8VGRBOyhKg-4H04WzYzPqM_Tmqi60Vod96JTo7SfM0OoGeNnlkWNjjBWkSS66alNLrvTNLi0A-KGeBsZiIFmrbsP6HHJfFzPd0Mci7-e11fNKecZgbC1me9PtRXFZb9JprZGFOvBiMwU0rRvh0GWYmTFj1HFjOIMAwTGOKOVGNuPjv0b3V0YaAkUNklzi4MM6qgzUb0tE0so1Ii7kBe7roMScS2USPeeJkeoPjLEbQcrT8MxOSxH-JgPLfq-zOnEJ6ERW3mtXdZCNzqmVLn5yjX5lKr5E2vgkPAHx9NLZ09fo_L9woeX_5epl6cIkQ==\",\n" +
			"      \"use\": \"sig\",\n" +
			"      \"e\": \"AQAB\",\n" +
			"      \"kid\": \"6719678351a5faedc2e70274bbea62da2a8c4a12\",\n" +
			"      \"alg\": \"RS256\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"alg\": \"RS256\",\n" +
			"      \"e\": \"AQAB\",\n" +
			"      \"use\": \"sig\",\n" +
			"      \"kty\": \"RSA\",\n" +
			"      \"n\": \"w-l_VE4KNa22n4nsMwcabujowm924YoQQnwOz_dPYHmDI1O-r2bqw6mHmByXwii7aaeIMHJZWpmT5SkR3OYIu5RbSgiU-8JrQoplW_vZY2IqG1y5-frPC_9gnz_0qKKjtjqglCP-1AlfOdu7r5kOpkOACs5mWn4tm1K9R1EPjk2T_MMO7FkteZd8woh1fwUUuvbhPyDxBzx9EUsnGWbpTndOYc7W-EUk1jMtWBk3buLeaypVaOLWranK_XFrX-xx03BohrfinOqmftYgc0z94sxix7X1G36JZeh8-jpUhwyBPinBxOZOE_5kQn4CYM66Ygxwiws0ZJ-klG2qTi239w==\",\n" +
			"      \"kid\": \"674dbba8faee69acae1bc1be190453678f472803\"\n" +
			"    }\n" +
			"  ]\n" +
			"}";
		facebook="{\n" +
			"   \"keys\": [\n" +
			"      {\n" +
			"         \"kid\": \"d458ab5237807dc6718901e522cebcd8e8157791\",\n" +
			"         \"kty\": \"RSA\",\n" +
			"         \"alg\": \"RS256\",\n" +
			"         \"use\": \"sig\",\n" +
			"         \"n\": \"uPyWMhNfNsO9EtiraYI0tr78vnkiJmzsmAAUd8hLHF5vPXDn683aQKZQ2Ny5lObigNmbHI5tt5y0o5m0RuZjJTj081uWm7Z901boO-p4VLwEONzjh4vTp2ZQ7aMjo17kMBzInHqz9iruWeB94dEu_LKYdQnDI6rweD_-chWWTR4mc7xbeaNozLHYzjEisSrIM3xIry2lZv5Mh334ZoahcTXGouFtU2XV_HvStXthwhoAtizQK7s2yJlBz8qlQK2lFNojRzd95f2bkynRnIvcpoF-qHZbOBTCIf-6TLp23qShs-XvbCkwHMhzvCPxcuZx3GNfCQkyTxeM5IGIMlWZ8w\",\n" +
			"         \"e\": \"AQAB\"\n" +
			"      },\n" +
			"      {\n" +
			"         \"kid\": \"ec11d50341c08e82899650e6afcc6668f2a0a420\",\n" +
			"         \"kty\": \"RSA\",\n" +
			"         \"alg\": \"RS256\",\n" +
			"         \"use\": \"sig\",\n" +
			"         \"n\": \"-rJ0HvlxiqOcwfpP6LsAYo0aaGNmohEBFr1JuWCGVvnPb3Z5Akd5w_bxQMRlOMot15IyrhWFonWCFr9H02f9E9GOEaroAj0zxQnCXcuGWb1BFN6RfoGNFpee1MqSDV3ikSIsSI3JL-z_1uBtDsQ1AtbYMKsB572v64bapW4WjDjekz0pQ-ePizVWm9mNNQxkA_fh3p1hW3KssXgnasWbKJODT5I6hnzd4whxj22oLE8xJCYTFeouk86teKI-nvK-LmaxoetBhnDn3QS5pN_oiIfDqjKPXGazeG2qwGAE8VPeISPvzYIstGbEh3NCzFEoB7a7APF1nLEo7Lco9aWjYQ\",\n" +
			"         \"e\": \"AQAB\"\n" +
			"      }\n" +
			"   ]\n" +
			"}";
		akid = "L1KfKFI_jnXbwWc22xZxw1sUHH0";
		gv3kid = "6719678351a5faedc2e70274bbea62da2a8c4a12";
		gv2kid= "6719678351a5faedc2e70274bbea62da2a8c4a12";
		fkid = "ec11d50341c08e82899650e6afcc6668f2a0a420";
	}


	public void testAzure(){
		PublicKey key = new PublicKey();
		boolean loaded = key.fromJwks(azure, akid);
		assertTrue(loaded);
		assertFalse(key.hasError());
	}

	public void testGooglev2()
	{
		PublicKey key = new PublicKey();
		boolean loaded = key.fromJwks(googlev2, gv2kid);
		assertTrue(loaded);
		assertFalse(key.hasError());
	}

	public void testGooglev3()
	{
		PublicKey key = new PublicKey();
		boolean loaded = key.fromJwks(googlev3, gv3kid);
		assertTrue(loaded);
		assertFalse(key.hasError());
	}

	public void testFacebook()
	{
		PublicKey key = new PublicKey();
		boolean loaded = key.fromJwks(facebook, fkid);
		assertTrue(loaded);
		assertFalse(key.hasError());
	}
}
