package eu.tnova.nfs.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ValidationException extends Exception {

	private static final long serialVersionUID = 1L;
	private Status status;
	private boolean param;

	public ValidationException() {
		super();
		status = null;
		param = false;
	}

	public ValidationException(String message) {
		this(message,Status.BAD_REQUEST);
	}
	public ValidationException(String message, boolean param) {
		this(message,Status.BAD_REQUEST, param);
	}

	public ValidationException(Throwable cause) {
		super(cause);
		status = null;
		param = false;
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
		status = null;
		param = false;
	}
	public ValidationException(String message, Status status) {
		this(message,status,false);
	}
	public ValidationException(String message, Status status, boolean param) {
		super(message);
		this.status = status;
		this.param = param;
	}

	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public boolean isParam() {
		return param;
	}
	public void setParam(boolean param) {
		this.param = param;
	}

	public Response getResponse() {
		return Response.status(status).entity(super.getMessage()).type(MediaType.TEXT_PLAIN).build();
	}

}
