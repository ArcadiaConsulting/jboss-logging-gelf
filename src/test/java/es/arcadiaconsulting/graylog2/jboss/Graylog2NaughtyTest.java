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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import es.arcadiaconsulting.graylog2.jboss.Graylog2Handler;

public class Graylog2NaughtyTest {
	
	public static void main(String[] args) {
        Logger logger = Logger.getLogger(Graylog2NaughtyTest.class.getName());
        logger.setLevel(java.util.logging.Level.ALL);
        Graylog2Handler handler = new Graylog2Handler();
        handler.setGraylog2ServerPort(12203);
        handler.setGraylog2ServerHost("megalopoli.ceei.arcadiaconsulting.es");
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        logger.log(Level.SEVERE, "An error", new Throwable("the exeception message"));
		
	}

}
