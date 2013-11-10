package org.minion.service;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class MinionWSClient {

	// private static final String SOAP_ACTION = "http://service.minion.org/wakeUpPia";
	// private static final String SOAP_METHOD = "deviceRegister";
	private static final String NAMESPACE = "http://service.minion.org";
	private static final String URL = "http://serverdown.no-ip.org/MinionWebServer/services/MinionWebService.MinionWebServiceHttpSoap11Endpoint/";

	private MinionGenericResponse result = new MinionGenericResponse();

	public static void main(String[] args) {
		MinionWSClient client = new MinionWSClient();

		 client.deviceRegister("nes", "actuy");

		// client.sendAlert("nes", "Eres una friki minion, aún así te amo un montón!!");

		// client.sendMessage("mpia", "nes", "hola guapo");

		//client.getConversation("mpia", "nes");

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
			
			
			envelope.addMapping(NAMESPACE, "MinionGenericResponse", new MinionGenericResponse().getClass());
			
	
			
			
			MinionGenericResponse genericResponse = (MinionGenericResponse)envelope.bodyIn;

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

	private MinionGenericResponse sendMessage(String userEnvia, String userRecibe, String msg) {

		String SOAP_METHOD = "sendMessage";
		String SOAP_ACTION = "http://service.minion.org/sendMessage";

		SoapObject request = new SoapObject(NAMESPACE, SOAP_METHOD);
		request.addProperty("userSend", userEnvia);
		request.addProperty("userReceive", userRecibe);
		request.addProperty("msg", msg);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

		envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);

		HttpTransportSE transport = new HttpTransportSE(URL);

		try {
			transport.call(SOAP_ACTION, envelope);
			SoapObject player = (SoapObject) envelope.getResponse();
			// SoapObject player = (SoapObject) resultsRequestSOAP.getProperty("deviceRegisterResponse");
			String responseCode = player.getProperty("responseCode").toString();
			String responseDesc = player.getProperty("responseDesc").toString();

			System.out.println("responseCode:" + responseCode + "|responseDesc:" + responseDesc);

		} catch (Exception ex) {
			result = null;
			System.out.println(ex.getMessage());
		}

		return result;
	}

	private MinionGenericResponse getConversation(String userEnvia, String userRecibe) {

		String SOAP_METHOD = "getConversation";
		String SOAP_ACTION = "http://service.minion.org/getConversation";

		SoapObject request = new SoapObject(NAMESPACE, SOAP_METHOD);
		request.addProperty("userSend", userEnvia);
		request.addProperty("userReceive", userRecibe);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		
		
		envelope.addMapping(NAMESPACE, "MinionMensajeResponse", MinionMensajeResponse.class);

		envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);

		HttpTransportSE transport = new HttpTransportSE(URL);

		try {
			transport.call(SOAP_ACTION, envelope);
			SoapObject player = (SoapObject) envelope.getResponse();
			// SoapObject player = (SoapObject) resultsRequestSOAP.getProperty("deviceRegisterResponse");

			String responseCode = player.getProperty("responseCode").toString();
			String responseDesc = player.getProperty("responseDesc").toString();

			int hola = player.getPropertyCount();

			System.out.println("Numero de propiedades: " + hola);
			

			
		//	MinionMensajeResponse mensajeResponse = (Object)envelope.getResponse();

			for (int i = 0; i < hola; i++) {
				//System.out.println("Propiedad " + player.getProperty(i));
				
				String fuck = "Propiedad " + player.getProperty(i);

				if (i > 1) {
					
					String[] splitter = null;

					MensajeDto dto = new MensajeDto();
					System.out.println(fuck );
					splitter = fuck.split("Propiedad anyType");
					System.out.println(splitter[1] );

					
					
				}

			}

			System.out.println("responseCode:" + responseCode + "|responseDesc:" + responseDesc);

		} catch (Exception ex) {
			result = null;
			System.out.println(ex.getMessage());
		}

		return result;
	}

}