package eu.tnova.nfs.ws.orchestrator;

import java.io.Serializable;

@SuppressWarnings("serial")
public class OrchestratorVNF implements Serializable {
	private Integer vnfdId;
	private OrchestratorOperationTypeEnum operation;
	private String userToker;

	public OrchestratorVNF() {
	}
	public OrchestratorVNF(Integer vnfdId, OrchestratorOperationTypeEnum operation, String userToker) {
		super();
		this.vnfdId = vnfdId;
		this.operation = operation;
		this.userToker = userToker;
	}
	public Integer getVnfdId() {
		return vnfdId;
	}
	public void setVnfdId(Integer vnfdId) {
		this.vnfdId = vnfdId;
	}
	public OrchestratorOperationTypeEnum getOperation() {
		return operation;
	}
	public void setOperation(OrchestratorOperationTypeEnum operation) {
		this.operation = operation;
	}
	public String getUserToker() {
		return userToker;
	}
	public void setUserToker(String userToker) {
		this.userToker = userToker;
	}

	@Override
	public String toString() {
		return "[vnfd=" + vnfdId + ", operation=" + operation + "]";
	}
}
