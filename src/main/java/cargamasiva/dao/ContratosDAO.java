package com.bbva.CargaMasiva.dao;

import java.io.IOException;
import java.util.List;

import com.bbva.arqspring.CargaMasiva.dto.FideicomisarioDTO;
import com.bbva.arqspring.CargaMasiva.dto.FideicomitenteDTO;
import com.bbva.jee.arq.spring.core.gce.ExcepcionAplicacion;

public interface ContratosDAO {
	public List<FideicomisarioDTO> getCtoFideicomisario() throws ExcepcionAplicacion, IOException;

	public List<FideicomitenteDTO> getCtoFideicomitente() throws ExcepcionAplicacion, IOException;

	public boolean validarCorreo(String correo);
}
