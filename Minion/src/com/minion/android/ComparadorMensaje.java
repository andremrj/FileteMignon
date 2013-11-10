package com.minion.android;

import java.util.Comparator;

public class ComparadorMensaje implements Comparator<MensajeDto> {

	public static final String CLAVE_ID = "id";
	public static final String CLAVE_USUARIO_ENVIA = "usuarioEnvia";
	public static final String CLAVE_USUARIO_RECIBE = "usuarioRecibe";
	public static final String CLAVE_FECHA = "fecha";
	public static final String CLAVE_ESTADO = "estado";

	private String claveComparadora;
	private String hashComparado;

	public int compare(MensajeDto o1, MensajeDto o2) {
		MensajeDto objetoComparado = o1;
		MensajeDto objetoComparador = o2;

		if (hashComparado.equals(CLAVE_ID)) {
			// No vale porque se desmadra cuando la diferencia es muy grande
			// return (objetoComparado.getIdMensaje() - objetoComparador.getIdMensaje());
			if (objetoComparado.getMenID().intValue() > objetoComparador.getMenID().intValue()) {
				return 1;
			} else if (objetoComparado.getMenID().intValue() < objetoComparador.getMenID().intValue()) {
				return -1;
			} else {
				return 0;
			}
		}

		if (hashComparado.equals(CLAVE_USUARIO_ENVIA)) {
			if ((objetoComparado.getMenUsuEnvia() != null) && (objetoComparador.getMenUsuEnvia() != null)) {
				return objetoComparado.getMenUsuEnvia().compareTo(objetoComparador.getMenUsuEnvia());
			} else {
				if (objetoComparado.getMenUsuEnvia() == null) {
					return 1;
				} else {
					return 0;
				}
			}
		}

		if (hashComparado.equals(CLAVE_USUARIO_RECIBE)) {
			if ((objetoComparado.getMenUsuEnvia() != null) && (objetoComparador.getMenUsuEnvia() != null)) {
				return objetoComparado.getMenUsuEnvia().compareTo(objetoComparador.getMenUsuEnvia());
			} else {
				if (objetoComparado.getMenUsuEnvia() == null) {
					return 1;
				} else {
					return 0;
				}
			}
		}

		if (hashComparado.equals(CLAVE_FECHA)) {
			/*
			 * WFLog.i(this, "MSG: " + objetoComparado.getIdMensaje() + "| MSG2: " + objetoComparador.getIdMensaje() +
			 * ":: " + objetoComparado.getFecha() + " - " + objetoComparador.getFecha() + "==" +
			 * (objetoComparado.getFecha() - objetoComparador.getFecha()));
			 */
			// No vale porque se desmadra cuando la diferencia es muy grande
			// return (int) (objetoComparado.getFecha() - objetoComparador.getFecha());

			if (objetoComparado.getMenFecha() > objetoComparador.getMenFecha()) {
				return 1;
			} else if (objetoComparado.getMenFecha() < objetoComparador.getMenFecha()) {
				return -1;
			} else {
				return 0;
			}

			// return objetoComparado.getFecha() > objetoComparador.getFecha();
		}

		if (hashComparado.equals(CLAVE_ESTADO)) {
			if ((objetoComparado.getMenEstado() != null) && (objetoComparador.getMenEstado() != null)) {
				return objetoComparado.getMenEstado().compareTo(objetoComparador.getMenEstado());
			} else {
				if (objetoComparado.getMenEstado() == null) {
					return 1;
				} else {
					return 0;
				}
			}
		}

		return 0;
	}

	public String getHashComparado() {
		return hashComparado;
	}

	public void setHashComparado(String hashComparado) {
		this.hashComparado = hashComparado;
	}

	public String getClaveComparadora() {
		return claveComparadora;
	}

	public void setClaveComparadora(String claveComparadora) {
		this.claveComparadora = claveComparadora;
	}
}