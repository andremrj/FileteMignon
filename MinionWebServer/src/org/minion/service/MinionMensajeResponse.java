package org.minion.service;

import java.util.ArrayList;

public class MinionMensajeResponse extends MinionGenericResponse {

	private ArrayList<MensajeDto> mensajeDtoList = new ArrayList<MensajeDto>();

	public MinionMensajeResponse() {
		super();
	}

	public MinionMensajeResponse(boolean success) {
		super(success);

	}

	public ArrayList<MensajeDto> getMensajeDtoList() {
		return mensajeDtoList;
	}

	public void setMensajeDtoList(ArrayList<MensajeDto> mensajeDtoList) {
		this.mensajeDtoList = mensajeDtoList;
	}

}
