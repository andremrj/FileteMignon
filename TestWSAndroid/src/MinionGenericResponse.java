

public class MinionGenericResponse {

	private int responseCode;
	private String responseDesc;

	private final int OKCODE = 0;
	private final String OKDESC = "OK";

	private final int KOCODE = -1;
	private final String KODESC = "Error Interno Realizando la Operación";

	public MinionGenericResponse() {
	}

	public MinionGenericResponse(boolean success) {
		if (success) {
			setResponseCode(OKCODE);
			setResponseDesc(OKDESC);
		} else {
			setResponseCode(KOCODE);
			setResponseDesc(KODESC);
		}

	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseDesc() {
		return responseDesc;
	}

	public void setResponseDesc(String responseDesc) {
		this.responseDesc = responseDesc;
	}

}
