import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class MinionWSClient {

	// private static final String SOAP_ACTION = "http://service.minion.org/wakeUpPia";
	// private static final String SOAP_METHOD = "deviceRegister";
	private static final String NAMESPACE = "http://service.minion.org";
	private static final String URL = "http://serverdown.no-ip.org/MinionWebServer/services/MinionServices.MinionServicesHttpSoap11Endpoint/";

	private MinionGenericResponse result = new MinionGenericResponse();

	public static void main(String[] args) {
		MinionWSClient client = new MinionWSClient();

		// client.deviceRegister("mpia", "actuy");

		client.sendAlert("nes", "Eres una friki minion, aún así te amo un montón!!");

	}

	public MinionWSClient() {

	}

	private MinionGenericResponse deviceRegister(String userID, String deviceID) {

		String SOAP_METHOD = "deviceRegister";
		String SOAP_ACTION = "http://service.minion.org/deviceRegister";

		SoapObject request = new SoapObject(NAMESPACE, SOAP_METHOD);
		request.addProperty("userID", userID);
		request.addProperty("deviceID", deviceID);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);
		HttpTransportSE aht = new HttpTransportSE(URL);
		try {
			aht.call(SOAP_ACTION, envelope);
			SoapObject player = (SoapObject) envelope.getResponse();
			// SoapObject player = (SoapObject) resultsRequestSOAP.getProperty("deviceRegisterResponse");
			String fname = player.getProperty("responseCode").toString();
			String lname = player.getProperty("responseDesc").toString();

			System.out.println("responseCode:" + fname + "|responseDesc:" + lname);
		} catch (Exception ex) {
			result = null;
			System.out.println(ex.getMessage());
		}

		return result;
	}

	private MinionGenericResponse sendAlert(String userID, String msg) {

		String SOAP_METHOD = "sendAlert";
		String SOAP_ACTION = "http://service.minion.org/sendAlert";

		SoapObject request = new SoapObject(NAMESPACE, SOAP_METHOD);
		request.addProperty("userID", userID);
		request.addProperty("msg", msg);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);
		HttpTransportSE aht = new HttpTransportSE(URL);
		try {
			aht.call(SOAP_ACTION, envelope);
			SoapObject player = (SoapObject) envelope.getResponse();
			// SoapObject player = (SoapObject) resultsRequestSOAP.getProperty("deviceRegisterResponse");
			String fname = player.getProperty("responseCode").toString();
			String lname = player.getProperty("responseDesc").toString();

			System.out.println("responseCode:" + fname + "|responseDesc:" + lname);
		} catch (Exception ex) {
			result = null;
			System.out.println(ex.getMessage());
		}

		return result;
	}

}