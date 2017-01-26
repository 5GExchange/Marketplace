package eu.tnova.nfs.producers;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import static java.lang.annotation.RetentionPolicy.*;
import static java.lang.annotation.ElementType.*;

@Qualifier
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER, TYPE})
public @interface EnvValue {
	public final static String storePath = "nfs.storePath";
	public final static String orchestratorUrl = "orchestrator.url";
	public final static String gatekeeperUrl = "gatekeeper.url";
	public final static String nfsUrl = "nfs.url";
	public final static String nfsServiceKey = "nfs.serviceKey";

	// Excludes this value from being considered for injection point matching
	// Avoid specifying a default value, We WANT a value every time.
	@Nonbinding String value() default "";
}
