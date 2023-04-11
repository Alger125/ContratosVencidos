package com.bbva.arqspring.CargaMasiva.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbva.arqspring.CargaMasiva.dao.ContratosDAO;
import com.bbva.arqspring.CargaMasiva.dto.FideicomisarioDTO;
import com.bbva.arqspring.CargaMasiva.dto.FideicomitenteDTO;
import com.bbva.arqspring.CargaMasiva.service.ContratosService;
import com.bbva.jee.arq.spring.core.gce.ExcepcionAplicacion;

@Service
public class ContratosServiceImpl implements ContratosService {

	@Autowired
	ContratosDAO contratosDAO;

	String ruta = "";
	FileWriter escribir = null;

	public String createFile(String rutaBase) throws IOException, ExcepcionAplicacion {

		List<FideicomisarioDTO> fideicomisarios = new ArrayList<FideicomisarioDTO>();
		List<FideicomitenteDTO> fideicomitentes = new ArrayList<FideicomitenteDTO>();

		ruta = rutaBase;
		String salto = "\r\n";
		Date date = new Date();
		DateFormat hourdateFormat = new SimpleDateFormat("yyyyMMdd");
		String fechaActual = hourdateFormat.format(date);
		File file = new File(ruta + File.separator + "MMFID_D01_" + fechaActual + "_GCVE_ECONTVENC.TXT");
		int contador = 1;
		try {
			escribir = new FileWriter(file, false);
			fideicomisarios = contratosDAO.getCtoFideicomisario();
			fideicomitentes = contratosDAO.getCtoFideicomitente();

			for (FideicomisarioDTO fideicomisario : fideicomisarios) {
				escribir.write(fideicomisario.toString());
				escribir.write(salto);
				contador++;
			}
			for (FideicomitenteDTO fideicomitente : fideicomitentes) {
				escribir.write(fideicomitente.toString());
				escribir.write(salto);
			}
			escribir.close();
		} catch (ExcepcionAplicacion e) {
			System.out.println("Error al escribir el archivo");
		} finally {
			escribir.close();
		}
		return file.getAbsolutePath().toString();
	}
}
