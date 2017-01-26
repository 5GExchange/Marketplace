package eu.tnova.nfs.producers;

public enum EnvDefaulValueEnum {
	STORE_PATH(EnvValue.storePath, "/usr/local/store"),
	ORCHESTRATOR_URL(EnvValue.orchestratorUrl, "https://apis.t-nova.eu/orchestrator"),
	GATEKEEPER_URL(EnvValue.gatekeeperUrl, "//http://auth.piyush-harsh.info:8000"),
	NFS_URL(EnvValue.nfsUrl, "https://apis.t-nova.eu/NFS"),
	NFS_SERVICE_KEY(EnvValue.nfsServiceKey, ""),
	;
	
	public String name;
	public String value;
	
	private EnvDefaulValueEnum(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public static String getValueByName (String name) {
		for (EnvDefaulValueEnum e: EnvDefaulValueEnum.values() ) {
			if ( e.getName().equalsIgnoreCase(name) )
				return e.getValue();
		}
		return null;
	}
	
}
