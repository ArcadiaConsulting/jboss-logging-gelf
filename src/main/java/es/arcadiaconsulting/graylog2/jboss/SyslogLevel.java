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

/**
 * Enumeration that defines the log levels available in syslog
 * @author <a href="mailto:ggomez@arcadiaconsulting.es">ggomez</a>
 * 23/12/2013
 *
 */
public enum SyslogLevel {
	
	EMERGENCY_SEVERITY(0), 
	ALERT_SEVERITY(1), 
	CRITICAL_SEVERITY(2), 
	ERROR_SEVERITY(3),
	WARNING_SEVERITY(4),
	NOTICE_SEVERITY(5),
	INFO_SEVERITY(6),
	DEBUG_SEVERITY(7);
	
	protected int numericValue;
	
	private SyslogLevel(int numericValue) {
		this.numericValue = numericValue;
	}

	public int getNumericValue() {
		return numericValue;
	}
	
	public String getNumericValueAsString() {
		return String.valueOf(numericValue);
	}

}
