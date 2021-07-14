package HTTPClient;

import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import HTTPClient.Cookie;

import static org.junit.Assert.assertEquals;

public class TestCookie {

	@Test
	public void parseCookieTest() throws Exception {

		HashMap<String, String> expectedResults = new HashMap<>();

		expectedResults.put("GX_CLIENT_ID=ClientValue; HTTPOnly;", "ClientValue");
		expectedResults.put("GX_CLIENT_ID=ClientValue;HTTPOnly;", "ClientValue");
		expectedResults.put("GX_CLIENT_ID=ClientValue; HTTPOnly;", "ClientValue");
		expectedResults.put("GX_CLIENT_ID=ClientValue; HTTPONLY;", "ClientValue");
		expectedResults.put("GX_CLIENT_ID=ClientValue; httponly;", "ClientValue");
		expectedResults.put("GX_CLIENT_ID=ClientValue;httponly;", "ClientValue");

		Iterator it = expectedResults.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			HTTPClient.Cookie[] cookie = Cookie.parse(pair.getKey().toString(), new RoRequestMock());
			assertEquals(pair.getValue(), cookie[0].getValue());
		}
	}


	public class RoRequestMock implements RoRequest{
		@Override
		public HTTPConnection getConnection() {
			HTTPConnection httpConnection = null;
			try {
				httpConnection = new HTTPConnection("http", "localhost", 8080);
			} catch (ProtocolNotSuppException e) {
				e.printStackTrace();
			}
			return httpConnection;
		}

		@Override
		public String getMethod() {
			return "GET";
		}

		@Override
		public String getRequestURI() {
			return "/contextpath/uri";
		}

		@Override
		public NVPair[] getHeaders() {
			return new NVPair[0];
		}

		@Override
		public byte[] getData() {
			return new byte[0];
		}

		@Override
		public HttpOutputStream getStream() {
			return null;
		}

		@Override
		public boolean allowUI() {
			return false;
		}
	}
}
