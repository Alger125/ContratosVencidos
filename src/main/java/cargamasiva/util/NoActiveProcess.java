package cargamasiva.util;

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

	private static Logger logg = Logger.getLogger(App.class);

	@Value("${rutas.carga.masiva.result}")
	private String resultado;

	public void exec() {

		File admitivos = new File(resultado + File.separator + "concilia_admitivos.txt");
		File dividendos = new File(resultado + File.separator + "concilia_dividendos.txt");
		File acciones = new File(resultado + File.separator + "concilia_acciones.txt");
		try {
			admitivos.createNewFile();
			dividendos.createNewFile();
			acciones.createNewFile();
		} catch (IOException e) {
			logg.debug("Proceso no activo, error al ejecutar");
		}
		if (admitivos.exists()) {
			admitivos.delete();
			logg.debug("El archivo " + admitivos.getName() + " fue eliminado.");
		}
		if (dividendos.exists()) {
			dividendos.delete();
			logg.debug("El archivo " + dividendos.getName() + " fue eliminado.");
		}

		if (acciones.exists()) {
			acciones.delete();
			logg.debug("El archivo " + acciones.getName() + " fue eliminado.");
		}
		logg.debug("El archivo " + acciones.getName() + " fue eliminado.");
	}

}
