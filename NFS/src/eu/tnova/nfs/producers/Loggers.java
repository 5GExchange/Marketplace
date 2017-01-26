package eu.tnova.nfs.producers;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class Loggers {
	@Produces
	Logger getLogger(InjectionPoint injectionPoint) {
		return LogManager.getLogger( injectionPoint.getMember().getDeclaringClass() );
	}
}