package com.bbva.arqspring.CargaMasiva.dto;

import java.sql.Date;

public class FideicomisarioDTO {

	private Date fechaVencimiento;
	private String cveTipoNegocio;
    private Integer numContrato;
    private String nomActividad;
    private String correo;
    
	public String getCorreo() {
		return correo;
	}
	public void setCorreo(String correo) {
		this.correo = correo;
	}
	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}
	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}
	public String getCveTipoNegocio() {
		return cveTipoNegocio;
	}
	public void setCveTipoNegocio(String cveTipoNegocio) {
		this.cveTipoNegocio = cveTipoNegocio;
	}
	public Integer getNumContrato() {
		return numContrato;
	}
	public void setNumContrato(Integer numContrato) {
		this.numContrato = numContrato;
	}
	public String getNomActividad() {
		return nomActividad;
	}
	public void setNomActividad(String nomActividad) {
		this.nomActividad = nomActividad;
	}

	@Override
    public String toString() {
        return "A0000007600000000001                                   BG102@@@"+correo+"{\"sFECHAVIGENCIA\":\"" + fechaVencimiento + "\",\"sFIDEICOMISO\":\"" + cveTipoNegocio + "\"}";
    }	
}