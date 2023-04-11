package com.bbva.arqspring.CargaMasiva.service;

import java.io.IOException;

import com.bbva.jee.arq.spring.core.gce.ExcepcionAplicacion;

public interface ContratosService {
	
	String createFile(String rutaBase) throws IOException, ExcepcionAplicacion;

}
