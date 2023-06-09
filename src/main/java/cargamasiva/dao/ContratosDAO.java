package cargamasiva.dao;

import java.io.IOException;
import java.util.List;

import com.bbva.jee.arq.spring.core.gce.ExcepcionAplicacion;

import cargamasiva.dto.FideicomisarioDTO;
import cargamasiva.dto.FideicomitenteDTO;

public interface ContratosDAO {
	public List<FideicomisarioDTO> getCtoFideicomisario() throws ExcepcionAplicacion, IOException;

	public List<FideicomitenteDTO> getCtoFideicomitente() throws ExcepcionAplicacion, IOException;

	public boolean validarCorreo(String correo);
}
