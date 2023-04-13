package com.bbva.arqspring.CargaMasiva.util;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.jdt.internal.compiler.apt.util.ArchiveFileObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bbva.arqspring.App;
import com.bbva.arqspring.dao.BaseDAO;
import com.bbva.arqspring.util.NoActivo;

@Service
public class NoActiveProcess extends BaseDAO implements NoActivo {

	private static Logger LogJava = Logger.getLogger(App.class);

	@Value("${rutas.carga.masiva.result}")
	private String RUTARESULT;

	public void exec() {

		File admitivos = new File(RUTARESULT + File.separator + "concilia_admitivos.txt");
		File dividendos = new File(RUTARESULT + File.separator + "concilia_dividendos.txt");
		File acciones = new File(RUTARESULT + File.separator + "concilia_acciones.txt");
		eliminarFichero(admitivos);
		eliminarFichero(dividendos);
		eliminarFichero(acciones);
		try {
			admitivos.createNewFile();
			dividendos.createNewFile();
			acciones.createNewFile();
		} catch (IOException e) {
			LogJava.debug("Proceso no activo, error al ejecutar");
		}

	}

	public static boolean eliminarFichero(File fichero) {
		if (fichero.exists()) {
			fichero.delete();
			LogJava.debug("El archivo " + fichero.getName() + " fue eliminado.");
		}
		return false;
	}
	

}
