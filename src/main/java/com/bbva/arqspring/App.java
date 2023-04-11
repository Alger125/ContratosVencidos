package com.bbva.arqspring;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.bbva.arqspring.CargaMasiva.service.CatalogoContratosService;
import com.bbva.arqspring.CargaMasiva.util.NoActiveProcess;
import com.bbva.arqspring.util.Config;

/**
 * Hello world!
 *
 */
public class App {
	
	private static Logger LogJava = Logger.getLogger(App.class);
	
	public static void main(String[] args) {
		LogJava.debug("INICIO");
		Config.setNumberProcess(207);
		Config.setNoActivo(NoActiveProcess.class);
		ApplicationContext c = Config.obtenerContexto();

		CatalogoContratosService catalogo = c.getBean(CatalogoContratosService.class);

		try {
			Date fecha1 = new Date();
			catalogo.inicio();
			Date fecha2 = new Date();
			long diferencia = fecha2.getTime() - fecha1.getTime();
			long segundos = TimeUnit.MILLISECONDS.toSeconds(diferencia);
			long minutos = TimeUnit.MILLISECONDS.toMinutes(diferencia);
			LogJava.debug("Tiempo de ejecucion ====  " + segundos + " segundos ");
			LogJava.debug("Tiempo de ejecucion ====  " + minutos + " minutos ");
			LogJava.debug("FIN DE EJECUCION");

			System.exit(0);
		} catch (Exception e) {
			String messageOutPut = "Error al ejecutar proceso";
			LogJava.debug("Error: " + messageOutPut);
			System.exit(1);
		}

	}
}
