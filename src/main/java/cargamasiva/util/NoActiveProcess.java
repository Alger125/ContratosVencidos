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
			if (admitivos.createNewFile())
				logg.info("El fichero se ha creado correctamente" + admitivos);
			else
				logg.info("Error al crear el archivo" + acciones);

			if (dividendos.createNewFile())
				logg.info("El fichero se ha construido correctamente" + dividendos);
			else
				logg.info("Error alerta" + acciones);

			if (acciones.createNewFile())
				logg.info("El fichero se ha establecido correctamente" + acciones);
			else
				logg.info("Error" + acciones);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}
}
