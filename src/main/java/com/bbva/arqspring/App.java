package com.bbva.arqspring;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.bbva.arqspring.util.Config;

import cargamasiva.service.CatalogoContratosService;
import cargamasiva.util.NoActiveProcess;

/**
 * Hello world!
 *
 */
public class App {
	
	private static Logger log = Logger.getLogger(App.class);
	
	public static void main(String[] args) {
		log.debug("INICIO");
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
			log.info("Tiempo de ejecucion ====  " + segundos + " segundos ");
			log.info("Tiempo de ejecucion ====  " + minutos + " minutos ");
			log.info("FIN DE EJECUCION");

			System.exit(0);
		} catch (Exception e) {
			String messageOutPut = "Error al ejecutar proceso";
			log.info("Error: " + messageOutPut);
			System.exit(1);
		}
jkkjlkjlklkjl
	}
}
