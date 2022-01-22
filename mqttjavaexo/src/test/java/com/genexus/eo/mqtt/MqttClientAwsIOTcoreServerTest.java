package com.genexus.eo.mqtt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import org.apache.commons.codec.binary.Base64;
import java.util.UUID;

import static org.junit.Assert.*;

public class MqttClientAwsIOTcoreServerTest {

    private static final Logger log = LogManager.getLogger(MqttClientAwsIOTcoreServerTest.class);

    private static String brokerUrl = "azjrqxrlastrw-ats.iot.us-east-2.amazonaws.com";
    private static String topic = "BigCheese/topic1";
    private static String payload = "This is a test";
    private static String gxproc = "";
    private static int qos = 1;

    @Test
    public void AWSIotCore_SingleConnection() {
        log.debug("Starting AWSIotCore_SingleConnection");
        MqttConfig config = getDefaultConfig();

        MqttStatus status = MqttClient.connect(brokerUrl, config);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.disconnect(status.key);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);
    }

    @Test
    public void AWSIotCore_Connect_Using_Plain_Certificates() {
        log.debug("Starting AWSIotCore_Connect_Using_Plain_Certificates");
        MqttConfig config = getDefaultConfig();
        config.caCertificate = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDQTCCAimgAwIBAgITBmyfz5m/jAo54vB4ikPmljZbyjANBgkqhkiG9w0BAQsF\n" +
                "ADA5MQswCQYDVQQGEwJVUzEPMA0GA1UEChMGQW1hem9uMRkwFwYDVQQDExBBbWF6\n" +
                "b24gUm9vdCBDQSAxMB4XDTE1MDUyNjAwMDAwMFoXDTM4MDExNzAwMDAwMFowOTEL\n" +
                "MAkGA1UEBhMCVVMxDzANBgNVBAoTBkFtYXpvbjEZMBcGA1UEAxMQQW1hem9uIFJv\n" +
                "b3QgQ0EgMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALJ4gHHKeNXj\n" +
                "ca9HgFB0fW7Y14h29Jlo91ghYPl0hAEvrAIthtOgQ3pOsqTQNroBvo3bSMgHFzZM\n" +
                "9O6II8c+6zf1tRn4SWiw3te5djgdYZ6k/oI2peVKVuRF4fn9tBb6dNqcmzU5L/qw\n" +
                "IFAGbHrQgLKm+a/sRxmPUDgH3KKHOVj4utWp+UhnMJbulHheb4mjUcAwhmahRWa6\n" +
                "VOujw5H5SNz/0egwLX0tdHA114gk957EWW67c4cX8jJGKLhD+rcdqsq08p8kDi1L\n" +
                "93FcXmn/6pUCyziKrlA4b9v7LWIbxcceVOF34GfID5yHI9Y/QCB/IIDEgEw+OyQm\n" +
                "jgSubJrIqg0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMC\n" +
                "AYYwHQYDVR0OBBYEFIQYzIU07LwMlJQuCFmcx7IQTgoIMA0GCSqGSIb3DQEBCwUA\n" +
                "A4IBAQCY8jdaQZChGsV2USggNiMOruYou6r4lK5IpDB/G/wkjUu0yKGX9rbxenDI\n" +
                "U5PMCCjjmCXPI6T53iHTfIUJrU6adTrCC2qJeHZERxhlbI1Bjjt/msv0tadQ1wUs\n" +
                "N+gDS63pYaACbvXy8MWy7Vu33PqUXHeeE6V/Uq2V8viTO96LXFvKWlJbYK8U90vv\n" +
                "o/ufQJVtMVT8QtPHRh8jrdkPSHCa2XV4cdFyQzR1bldZwgJcJmApzyMZFo6IQ6XU\n" +
                "5MsI+yMRQ+hDKXJioaldXgjUkK642M4UwtBV8ob2xJNDd2ZhwLnoQdeXeGADbkpy\n" +
                "rqXRfboQnoZsG4q5WTP468SQvvG5\n" +
                "-----END CERTIFICATE-----";
        config.clientCertificate = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDWTCCAkGgAwIBAgIUQsIicX1+2VYS/O4vd6tjvuzgfzwwDQYJKoZIhvcNAQEL\n" +
                "BQAwTTFLMEkGA1UECwxCQW1hem9uIFdlYiBTZXJ2aWNlcyBPPUFtYXpvbi5jb20g\n" +
                "SW5jLiBMPVNlYXR0bGUgU1Q9V2FzaGluZ3RvbiBDPVVTMB4XDTIxMTIzMDExNTAz\n" +
                "M1oXDTQ5MTIzMTIzNTk1OVowHjEcMBoGA1UEAwwTQVdTIElvVCBDZXJ0aWZpY2F0\n" +
                "ZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALTD08i1TIgIOWn03XI9\n" +
                "79IEjqSAmLvk4VJQz2tJlT8FYYWPCvK+3sJxzA7PwPS0knrGgixCa5rBEbVnnXkr\n" +
                "1dc7yDGzK1Esf7ygbWTCVOn3XLCx60XUvmX4wLQc7N1VGMwmmbRfM7t8yQ3vEYFp\n" +
                "hc+K17501/+Ty1fnD7Et49utos0CYCI3dXpyhg36tFdYoCU5iG9AKTxShoHmvI9c\n" +
                "aVa8jgs4BnUx7E6TBiCQBp9eCh+TbMZj5voHJ6gXX5HEx3/X85DDuxT/6r6W3eks\n" +
                "w4FHwllaUFFuzdYTYD0Zo+n/mcdWT5A6vvsdY1WLTYEXlvgBkLp+Nda8PBiCiU/i\n" +
                "7usCAwEAAaNgMF4wHwYDVR0jBBgwFoAUSraB0NMmPr68P2fgXKP67B2GHhowHQYD\n" +
                "VR0OBBYEFGdtXzC/rT2qyRgbQlOJ4dFT/ILPMAwGA1UdEwEB/wQCMAAwDgYDVR0P\n" +
                "AQH/BAQDAgeAMA0GCSqGSIb3DQEBCwUAA4IBAQC0IGqMAsciSoo87gC2cBeyNY5A\n" +
                "fUZzhtnMYaJSfvGqDPP6/dXdj3vWhCVozUyVMKBwGvBISGOryC3ezwv93ouC8/9A\n" +
                "2EguqNXcDgQddibWoaYDNfvDg+UYlR02K3OkoITtUJfzg0XoXHhuBGjy0p4o7dK/\n" +
                "EIAmrk7/ccLuwGUtfiJfm6uyEq4voLrt+B/OLFV8jCTYSzZHWU09MaNDsuWQwTRs\n" +
                "A9UX716AeFc7VrjafEitzij9rr2kEYpyzQtrDiXblKk877xnA02id+Zo9R9AzPcU\n" +
                "/HKl1yZOZOXuSJXyMVdmeKwN2prpxzNKMlaX6LkNBB9xLzQF59Qe3y5cZaF6\n" +
                "-----END CERTIFICATE-----";
        config.privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIEpQIBAAKCAQEAtMPTyLVMiAg5afTdcj3v0gSOpICYu+ThUlDPa0mVPwVhhY8K\n" +
                "8r7ewnHMDs/A9LSSesaCLEJrmsERtWedeSvV1zvIMbMrUSx/vKBtZMJU6fdcsLHr\n" +
                "RdS+ZfjAtBzs3VUYzCaZtF8zu3zJDe8RgWmFz4rXvnTX/5PLV+cPsS3j262izQJg\n" +
                "Ijd1enKGDfq0V1igJTmIb0ApPFKGgea8j1xpVryOCzgGdTHsTpMGIJAGn14KH5Ns\n" +
                "xmPm+gcnqBdfkcTHf9fzkMO7FP/qvpbd6SzDgUfCWVpQUW7N1hNgPRmj6f+Zx1ZP\n" +
                "kDq++x1jVYtNgReW+AGQun411rw8GIKJT+Lu6wIDAQABAoIBAQCZVO8FdjwHu6ov\n" +
                "5YQeDsmpxTHV+jsUTo6Oi3uWDyGpBoB1kSOBS0SbfXnRKosLR0xlFpN+xYyPBaxm\n" +
                "yxB5oyRN4Sjsd4WL4oorz41bhbgPikK2owTmBBLJXprfbRXCnRdevHNu275/D9df\n" +
                "mWWo94CIzsY4iNZbtdsrohTefwWj5ZWEuUz50ramSjrsi3zsfv4JCZf0oNDKyax2\n" +
                "tlJmGA8EpUo/ouB2covNfD9yjjedjE2FK3V4f98qNMRlcq4MfFPnNc+zMVNkwa/o\n" +
                "s24rrCDI0NtL4+WILC3KmPdfKyh403/gdIdhQXjuNvtvtDuTWxE2yZr1/4S2kUCq\n" +
                "TrPNHrwhAoGBANvilyT55kC+6Yhsh0kWU7qKHZnhm7m1Bmd4sf9vXj3+a9AjQCXF\n" +
                "WTFBWV6noBMAjQRpz414csV7WI8D06veYfRh0WvSKHwluPi8xaQ8uHhGFA/c79Hj\n" +
                "3gx74H5jz/ejO+xrmdpIZpjtTir3A6R5DH6ovsP7Fp6haXogB/h/S6MxAoGBANJ0\n" +
                "XnLlht3ZTYPwNsSklec7OWZQ7PakkdAmQ9MOlWdU9iRu/iKwlCotH85ou8/1uT3P\n" +
                "skJznnP+gnfAVilPAyqiTqaJ4NucwQlJ23jeUjSBar7f8b4aE7bV3TzHm0RgiM/h\n" +
                "+MteXKkF+gVwT8RxURNRglBgQK3g7SxutkibtpTbAoGBANj5DcYJ5eALwuvAk/pV\n" +
                "0scmlLEMw5qLe8dMfPAeV/N6g659qkXbJ/kHkAVsCPPXPk+VjZ3+tdoSrb9pukxF\n" +
                "A4mfoIPxe8uNeanQtIIs/KaKGC/TBr6pSKou7sYo/cVRyMaxIr8XZE4RFGonFuwM\n" +
                "Gn09TAidUqOziR5eTx8xlVRxAoGAZkfuGzMYD7vro2Lci0deH0o2RFayQ9CJmTrv\n" +
                "naSp0dWF5wEWe++Los/ZdGyMUq6ev76waQFeEguwhtiwR1VCObc2OFQsNy+A1a3S\n" +
                "f3S1SRVYtrmJ+JUd3a8k3cQ43st30miFjPkoWXExKPGDovpbp+LmTX/qD1eQjRal\n" +
                "erZKzx8CgYEAi2nGQvBH8NWgEBPZqB+DgSC4KdpVefjPo4acmL+E3auk/oiVTE73\n" +
                "Qc/QFj4XSzUQoVuTrw6joo6+OLcedD4GrsBavFQroKZ1Hk8FMdEM6WsJyeRJVgkI\n" +
                "jPPgAtQFJnrS2xbwgbR8FvR1GkYnupvsiQHUH0OsDigxfhI2EnU9Xy8=\n" +
                "-----END RSA PRIVATE KEY-----";

        MqttStatus status = MqttClient.connect(brokerUrl, config);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.disconnect(status.key);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);
    }

    @Test
    public void AWSIotCore_Connect_Using_Base64_Certificates() {
        log.debug("Starting AWSIotCore_Connect_Using_Plain_Certificates");
        MqttConfig config = getDefaultConfig();
        config.caCertificate = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDQTCCAimgAwIBAgITBmyfz5m/jAo54vB4ikPmljZbyjANBgkqhkiG9w0BAQsF\n" +
                "ADA5MQswCQYDVQQGEwJVUzEPMA0GA1UEChMGQW1hem9uMRkwFwYDVQQDExBBbWF6\n" +
                "b24gUm9vdCBDQSAxMB4XDTE1MDUyNjAwMDAwMFoXDTM4MDExNzAwMDAwMFowOTEL\n" +
                "MAkGA1UEBhMCVVMxDzANBgNVBAoTBkFtYXpvbjEZMBcGA1UEAxMQQW1hem9uIFJv\n" +
                "b3QgQ0EgMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALJ4gHHKeNXj\n" +
                "ca9HgFB0fW7Y14h29Jlo91ghYPl0hAEvrAIthtOgQ3pOsqTQNroBvo3bSMgHFzZM\n" +
                "9O6II8c+6zf1tRn4SWiw3te5djgdYZ6k/oI2peVKVuRF4fn9tBb6dNqcmzU5L/qw\n" +
                "IFAGbHrQgLKm+a/sRxmPUDgH3KKHOVj4utWp+UhnMJbulHheb4mjUcAwhmahRWa6\n" +
                "VOujw5H5SNz/0egwLX0tdHA114gk957EWW67c4cX8jJGKLhD+rcdqsq08p8kDi1L\n" +
                "93FcXmn/6pUCyziKrlA4b9v7LWIbxcceVOF34GfID5yHI9Y/QCB/IIDEgEw+OyQm\n" +
                "jgSubJrIqg0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMC\n" +
                "AYYwHQYDVR0OBBYEFIQYzIU07LwMlJQuCFmcx7IQTgoIMA0GCSqGSIb3DQEBCwUA\n" +
                "A4IBAQCY8jdaQZChGsV2USggNiMOruYou6r4lK5IpDB/G/wkjUu0yKGX9rbxenDI\n" +
                "U5PMCCjjmCXPI6T53iHTfIUJrU6adTrCC2qJeHZERxhlbI1Bjjt/msv0tadQ1wUs\n" +
                "N+gDS63pYaACbvXy8MWy7Vu33PqUXHeeE6V/Uq2V8viTO96LXFvKWlJbYK8U90vv\n" +
                "o/ufQJVtMVT8QtPHRh8jrdkPSHCa2XV4cdFyQzR1bldZwgJcJmApzyMZFo6IQ6XU\n" +
                "5MsI+yMRQ+hDKXJioaldXgjUkK642M4UwtBV8ob2xJNDd2ZhwLnoQdeXeGADbkpy\n" +
                "rqXRfboQnoZsG4q5WTP468SQvvG5\n" +
                "-----END CERTIFICATE-----";
        config.clientCertificate = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDWTCCAkGgAwIBAgIUQsIicX1+2VYS/O4vd6tjvuzgfzwwDQYJKoZIhvcNAQEL\n" +
                "BQAwTTFLMEkGA1UECwxCQW1hem9uIFdlYiBTZXJ2aWNlcyBPPUFtYXpvbi5jb20g\n" +
                "SW5jLiBMPVNlYXR0bGUgU1Q9V2FzaGluZ3RvbiBDPVVTMB4XDTIxMTIzMDExNTAz\n" +
                "M1oXDTQ5MTIzMTIzNTk1OVowHjEcMBoGA1UEAwwTQVdTIElvVCBDZXJ0aWZpY2F0\n" +
                "ZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALTD08i1TIgIOWn03XI9\n" +
                "79IEjqSAmLvk4VJQz2tJlT8FYYWPCvK+3sJxzA7PwPS0knrGgixCa5rBEbVnnXkr\n" +
                "1dc7yDGzK1Esf7ygbWTCVOn3XLCx60XUvmX4wLQc7N1VGMwmmbRfM7t8yQ3vEYFp\n" +
                "hc+K17501/+Ty1fnD7Et49utos0CYCI3dXpyhg36tFdYoCU5iG9AKTxShoHmvI9c\n" +
                "aVa8jgs4BnUx7E6TBiCQBp9eCh+TbMZj5voHJ6gXX5HEx3/X85DDuxT/6r6W3eks\n" +
                "w4FHwllaUFFuzdYTYD0Zo+n/mcdWT5A6vvsdY1WLTYEXlvgBkLp+Nda8PBiCiU/i\n" +
                "7usCAwEAAaNgMF4wHwYDVR0jBBgwFoAUSraB0NMmPr68P2fgXKP67B2GHhowHQYD\n" +
                "VR0OBBYEFGdtXzC/rT2qyRgbQlOJ4dFT/ILPMAwGA1UdEwEB/wQCMAAwDgYDVR0P\n" +
                "AQH/BAQDAgeAMA0GCSqGSIb3DQEBCwUAA4IBAQC0IGqMAsciSoo87gC2cBeyNY5A\n" +
                "fUZzhtnMYaJSfvGqDPP6/dXdj3vWhCVozUyVMKBwGvBISGOryC3ezwv93ouC8/9A\n" +
                "2EguqNXcDgQddibWoaYDNfvDg+UYlR02K3OkoITtUJfzg0XoXHhuBGjy0p4o7dK/\n" +
                "EIAmrk7/ccLuwGUtfiJfm6uyEq4voLrt+B/OLFV8jCTYSzZHWU09MaNDsuWQwTRs\n" +
                "A9UX716AeFc7VrjafEitzij9rr2kEYpyzQtrDiXblKk877xnA02id+Zo9R9AzPcU\n" +
                "/HKl1yZOZOXuSJXyMVdmeKwN2prpxzNKMlaX6LkNBB9xLzQF59Qe3y5cZaF6\n" +
                "-----END CERTIFICATE-----";
        config.privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIEpQIBAAKCAQEAtMPTyLVMiAg5afTdcj3v0gSOpICYu+ThUlDPa0mVPwVhhY8K\n" +
                "8r7ewnHMDs/A9LSSesaCLEJrmsERtWedeSvV1zvIMbMrUSx/vKBtZMJU6fdcsLHr\n" +
                "RdS+ZfjAtBzs3VUYzCaZtF8zu3zJDe8RgWmFz4rXvnTX/5PLV+cPsS3j262izQJg\n" +
                "Ijd1enKGDfq0V1igJTmIb0ApPFKGgea8j1xpVryOCzgGdTHsTpMGIJAGn14KH5Ns\n" +
                "xmPm+gcnqBdfkcTHf9fzkMO7FP/qvpbd6SzDgUfCWVpQUW7N1hNgPRmj6f+Zx1ZP\n" +
                "kDq++x1jVYtNgReW+AGQun411rw8GIKJT+Lu6wIDAQABAoIBAQCZVO8FdjwHu6ov\n" +
                "5YQeDsmpxTHV+jsUTo6Oi3uWDyGpBoB1kSOBS0SbfXnRKosLR0xlFpN+xYyPBaxm\n" +
                "yxB5oyRN4Sjsd4WL4oorz41bhbgPikK2owTmBBLJXprfbRXCnRdevHNu275/D9df\n" +
                "mWWo94CIzsY4iNZbtdsrohTefwWj5ZWEuUz50ramSjrsi3zsfv4JCZf0oNDKyax2\n" +
                "tlJmGA8EpUo/ouB2covNfD9yjjedjE2FK3V4f98qNMRlcq4MfFPnNc+zMVNkwa/o\n" +
                "s24rrCDI0NtL4+WILC3KmPdfKyh403/gdIdhQXjuNvtvtDuTWxE2yZr1/4S2kUCq\n" +
                "TrPNHrwhAoGBANvilyT55kC+6Yhsh0kWU7qKHZnhm7m1Bmd4sf9vXj3+a9AjQCXF\n" +
                "WTFBWV6noBMAjQRpz414csV7WI8D06veYfRh0WvSKHwluPi8xaQ8uHhGFA/c79Hj\n" +
                "3gx74H5jz/ejO+xrmdpIZpjtTir3A6R5DH6ovsP7Fp6haXogB/h/S6MxAoGBANJ0\n" +
                "XnLlht3ZTYPwNsSklec7OWZQ7PakkdAmQ9MOlWdU9iRu/iKwlCotH85ou8/1uT3P\n" +
                "skJznnP+gnfAVilPAyqiTqaJ4NucwQlJ23jeUjSBar7f8b4aE7bV3TzHm0RgiM/h\n" +
                "+MteXKkF+gVwT8RxURNRglBgQK3g7SxutkibtpTbAoGBANj5DcYJ5eALwuvAk/pV\n" +
                "0scmlLEMw5qLe8dMfPAeV/N6g659qkXbJ/kHkAVsCPPXPk+VjZ3+tdoSrb9pukxF\n" +
                "A4mfoIPxe8uNeanQtIIs/KaKGC/TBr6pSKou7sYo/cVRyMaxIr8XZE4RFGonFuwM\n" +
                "Gn09TAidUqOziR5eTx8xlVRxAoGAZkfuGzMYD7vro2Lci0deH0o2RFayQ9CJmTrv\n" +
                "naSp0dWF5wEWe++Los/ZdGyMUq6ev76waQFeEguwhtiwR1VCObc2OFQsNy+A1a3S\n" +
                "f3S1SRVYtrmJ+JUd3a8k3cQ43st30miFjPkoWXExKPGDovpbp+LmTX/qD1eQjRal\n" +
                "erZKzx8CgYEAi2nGQvBH8NWgEBPZqB+DgSC4KdpVefjPo4acmL+E3auk/oiVTE73\n" +
                "Qc/QFj4XSzUQoVuTrw6joo6+OLcedD4GrsBavFQroKZ1Hk8FMdEM6WsJyeRJVgkI\n" +
                "jPPgAtQFJnrS2xbwgbR8FvR1GkYnupvsiQHUH0OsDigxfhI2EnU9Xy8=\n" +
                "-----END RSA PRIVATE KEY-----\n";
        config.caCertificate = new String(Base64.encodeBase64(config.caCertificate.getBytes()));
        config.clientCertificate = new String(Base64.encodeBase64(config.clientCertificate.getBytes()));
        config.privateKey = new String(Base64.encodeBase64(config.privateKey.getBytes()));
        MqttStatus status = MqttClient.connect(brokerUrl, config);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.disconnect(status.key);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);
    }

    @Test
    public void AWSIotCore_SingleConnection_Publish() {
        log.debug("Starting AWSIotCore_SingleConnection_Publish");
        MqttConfig config = getDefaultConfig();

        MqttStatus status = MqttClient.connect(brokerUrl, config);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.publish(status.key, topic, payload, qos, false, 0);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.disconnect(status.key);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);
    }

    @Test
    public void AWSIotCore_SingleConnection_Subscribe() {
        log.debug("Starting AWSIotCore_SingleConnection_Subscribe");

        MqttConfig config = getDefaultConfig();

        MqttStatus status = MqttClient.connect(brokerUrl, config);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.subscribe(status.key, topic, gxproc, qos);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.disconnect(status.key);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

    }


    @Test
    public void AWSIotCore_SingleConnection_Subscribe_Publish() throws InterruptedException {
        log.debug("Starting AWSIotCore_SingleConnection_Subscribe_Publish");

        MqttConfig config = getDefaultConfig();

        MqttStatus status = MqttClient.connect(brokerUrl, config);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

        status = MqttClient.subscribe(status.key, topic, gxproc, qos);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);
        log.debug("Subscribed to AW iot core with key " + status.key.toString());

        Thread.sleep(2000);

        status = MqttClient.publish(status.key, topic, payload, qos, false, 0);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);
        log.debug("Published message to AWS iot core with key " + status.key.toString());

        log.debug("Waiting for message to arrive");
        Thread.sleep(2000);

        status = MqttClient.disconnect(status.key);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);

    }

    @Test
    public void AWSIotCore_Publish_NoConnection() {
        MqttStatus status = MqttClient.publish(UUID.randomUUID(), topic, payload, qos, false, 0);
        assertTrue(status.error);
        assertEquals("Connection key not found", status.errorMessage);
    }

    @Test
    public void AWSIotCore_Subscribe_NoConnection() {
        log.debug("Starting AWSIotCore_Subscribe_NoConnection");

        MqttStatus status = MqttClient.subscribe(UUID.randomUUID(), topic, gxproc, qos);
        assertTrue(status.error);
        assertEquals("Connection key not found", status.errorMessage);
    }

    @Test
    public void AWSIotCore_Disconnect_NoConnection() {
        MqttStatus status = MqttClient.disconnect(UUID.randomUUID());
        assertTrue(status.error);
        assertEquals("Connection key not found", status.errorMessage);
    }

    @Test
    public void AWSIotCore_Publish_TwoConnections_TwoSubscribers() throws InterruptedException {
        MqttConfig config = getDefaultConfig();

        MqttStatus status = MqttClient.connect(brokerUrl, config);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);
        UUID key1 = status.key;

        config.clientId = "BigCheese-2022";
        status = MqttClient.connect(brokerUrl, config);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertNotNull(status.key);
        UUID key2 = status.key;

        status = MqttClient.subscribe(key1, topic, gxproc, qos);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertEquals(key1, status.key);
        log.debug("Subscribed to AW iot core with key " + status.key.toString());

        status = MqttClient.subscribe(key2, topic, gxproc, qos);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertEquals(key2, status.key);
        log.debug("Subscribed to AW iot core with key " + status.key.toString());

        Thread.sleep(2000);

        status = MqttClient.publish(key1, topic, payload, qos, false, 0);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertEquals(key1, status.key);
        log.debug("Published message to AWS iot core with key " + status.key.toString());

        status = MqttClient.publish(key2, topic, payload, qos, false, 0);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertEquals(key2, status.key);
        log.debug("Published message to AWS iot core with key " + status.key.toString());

        log.debug("Waiting for message to arrive");
        Thread.sleep(2000);

        status = MqttClient.disconnect(key1);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertEquals(key1, status.key);

        status = MqttClient.disconnect(key2);
        assertFalse(status.error);
        assertEquals("", status.errorMessage);
        assertEquals(key2, status.key);

        // Check also that both keys were removed from connections collection
        status = MqttClient.disconnect(key1);
        assertTrue(status.error);
        assertEquals("Connection key not found", status.errorMessage);

        status = MqttClient.disconnect(key2);
        assertTrue(status.error);
        assertEquals("Connection key not found", status.errorMessage);
    }

    private MqttConfig getDefaultConfig() {
        MqttConfig config = new MqttConfig();
        config.allowWildcardsInTopicFilters = true;
        config.port = 8883;
        config.sessionExpiryInterval = 3600;
        config.autoReconnectDelay = 100;
        config.connectionTimeout = 5;
        config.cleanSession = true;
        config.clientId = "Martincito";
        config.sslConnection = true;
        config.privateKey = "C:\\Users\\Mvigliarolo\\Desktop\\MQTT\\AWS-Thing-Prueba2\\private.pem";
        config.caCertificate = "C:\\Users\\Mvigliarolo\\Desktop\\MQTT\\AWS-Thing-Prueba2\\AmazonRootCA1.crt";
        config.clientCertificate = "C:\\Users\\Mvigliarolo\\Desktop\\MQTT\\AWS-Thing-Prueba2\\device_certificate.crt";
        return config;
    }
}
