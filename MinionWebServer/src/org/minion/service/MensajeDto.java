package org.minion.service;

public class MensajeDto {

	private Integer menID;
	private long menFecha;
	private String menUsuEnvia;
	private String menUsuRecibe;
	private String menTexto;
	private String menEstado;

	public MensajeDto(MensajeDao mensajeDao) {

		menID = mensajeDao.getMenID();
		menUsuEnvia = mensajeDao.getMenUsuEnvia();
		menUsuRecibe = mensajeDao.getMenUsuRecibe();
		menTexto = mensajeDao.getMenTexto();
		menEstado = mensajeDao.getMenEstado();
	}

	public MensajeDto() {

		menID = null;
		menUsuEnvia = null;
		menUsuRecibe = null;
		menTexto = null;
		menEstado = null;

	}

	public Integer getMenID() {
		return menID;
	}

	public void setMenID(Integer menID) {
		this.menID = menID;
	}

	public long getMenFecha() {
		return menFecha;
	}

	public void setMenFecha(long menFecha) {
		this.menFecha = menFecha;
	}

	public String getMenUsuEnvia() {
		return menUsuEnvia;
	}

	public void setMenUsuEnvia(String menUsuEnvia) {
		this.menUsuEnvia = menUsuEnvia;
	}

	public String getMenUsuRecibe() {
		return menUsuRecibe;
	}

	public void setMenUsuRecibe(String menUsuRecibe) {
		this.menUsuRecibe = menUsuRecibe;
	}

	public String getMenTexto() {
		return menTexto;
	}

	public void setMenTexto(String menTexto) {
		this.menTexto = menTexto;
	}

	public String getMenEstado() {
		return menEstado;
	}

	public void setMenEstado(String menEstado) {
		this.menEstado = menEstado;
	}
}
