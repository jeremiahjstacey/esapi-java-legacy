package org.owasp.esapi.codecs;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.owasp.esapi.reference.MultithreadRule;

public class PercentCodecTest {
    @Rule
    public MultithreadRule multiThreader = new MultithreadRule();
	@Test
	public void testPercentDecode(){
		Codec codec = new PercentCodec();
		
		String expected = " ";
		assertEquals(expected, codec.decode("%20"));
	}
}
