/**
 * Copyright © 2012 Arcadia Consulting C.B. . All Rights Reserved. 
 *
 * Redistribution and use in source and binary forms, with or without modification 
 * are not permitted 
 * 
 * THIS SOFTWARE IS PROVIDED BY ARCADIA CONSULTING C.B. "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package es.arcadiaconsulting.graylog2.jboss;

import static org.junit.Assert.*;

import org.junit.Test;

public class Graylog2HandlerTest {

	@Test
	public void testSetStaticAdditionalFieldsProperties() {
		Graylog2Handler handler = new Graylog2Handler();

		handler.setStaticAdditionalFieldsProperties(null);
		assertEquals(0, handler.getStaticAdditionalFields().size());
		
		handler.setStaticAdditionalFieldsProperties("");
		assertEquals(0, handler.getStaticAdditionalFields().size());
		
		handler.setStaticAdditionalFieldsProperties("_aKey:aValue");
		assertEquals(1, handler.getStaticAdditionalFields().size());
		
		handler.setStaticAdditionalFieldsProperties("_aKey2:aValue2;_anotherKey:anotherValue");
		assertEquals(3, handler.getStaticAdditionalFields().size());

		handler.setStaticAdditionalFieldsProperties("_aKey3:aValue3with\\;character;_anotherkey2:antotherValue2with\\;character");
		assertEquals(5, handler.getStaticAdditionalFields().size());
		assertEquals("antotherValue2with;character", handler.getStaticAdditionalFields().get("_anotherkey2"));
		assertEquals("aValue3with;character", handler.getStaticAdditionalFields().get("_aKey3"));


	}

}
