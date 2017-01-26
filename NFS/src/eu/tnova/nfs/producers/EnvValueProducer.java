package eu.tnova.nfs.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

@ApplicationScoped
public class EnvValueProducer {
	@Produces
	@EnvValue("")
    public String configValueProducer(InjectionPoint ip) {
		EnvValue name = ip.getAnnotated().getAnnotation(EnvValue.class);
		if ( name==null || name.value().isEmpty() )
			return null;
		String var=System.getProperty(name.value());
		if ( var!=null )
			return var;
		var=System.getenv(name.value());
		if ( var!=null )
			return var;
		return EnvDefaulValueEnum.getValueByName(name.value());
	}
}
