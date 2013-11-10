package org.minion.service;



public class MensajeDtoList {

	private MensajeDto [] listaMensajes=null;;

	public MensajeDtoList(int size) {
		this.listaMensajes = new MensajeDto[size];
	}

	public void addMensaje(MensajeDto mensajeDto, int pos) {
		this.listaMensajes[pos] = mensajeDto;
	}

	public MensajeDto getMensaje(int x) {
		return this.listaMensajes[x];
	}

}
