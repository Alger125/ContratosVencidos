package cargamasiva.dto;

import java.sql.Date;

public class FideicomitenteDTO {
	private Date fechaVencimiento;
	private String cveTipoNegocio;
	private String correo;
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
	public String getCorreo() {
		return correo;
	}
	public void setCorreo(String correo) {
		this.correo = correo;
	}
	@Override
    public String toString() {
        return "A0000007600000000001                                   BG102@@@"+correo+"{\"sFECHAVIGENCIA\":\"" + fechaVencimiento + "\",\"sFIDEICOMISO\":\"" + cveTipoNegocio + "\"}";

    }	
}
