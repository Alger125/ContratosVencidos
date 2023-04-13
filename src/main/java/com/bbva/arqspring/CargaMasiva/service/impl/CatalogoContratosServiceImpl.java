package com.bbva.arqspring.CargaMasiva.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bbva.arqspring.dao.BaseDAO;
import com.bbva.jee.arq.spring.core.gce.ExcepcionAplicacion;

import cargamasiva.service.CatalogoContratosService;
import cargamasiva.service.ContratosService;

@Service
public class CatalogoContratosServiceImpl extends BaseDAO implements CatalogoContratosService {

	@Autowired
	private ContratosService contratosService;

	@Value("${rutas.carga.masiva.result}")
	private String RUTARESULT;

	public int inicio() throws ExcepcionAplicacion {
		int exitCode = 0;

		try {
			contratosService.createFile(RUTARESULT);

		} catch (Exception e) {
			exitCode = 1;
			logger.error("Error al ejecutar el proceso - ConciliacionCCMP - EJECUCION ConciliacionCCMP 0185: ");
			throw new ExcepcionAplicacion();
		}
		return exitCode;
	}

}
