package com.bbva.arqspring;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationContext;

import com.bbva.arqspring.CargaMasiva.service.CatalogoContratosService;
import com.bbva.arqspring.CargaMasiva.util.NoActiveProcess;
import com.bbva.arqspring.util.Config;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		System.out.println("INICIO");

		Config.setNumberProcess(207);
		Config.setNoActivo(NoActiveProcess.class);
		ApplicationContext c = Config.obtenerContexto();
		System.out.println("ACTIVO");
		CatalogoContratosService catalogo = c.getBean(CatalogoContratosService.class);

		try {
			Date fecha1 = new Date();
			catalogo.inicio();
			Date fecha2 = new Date();
			long diferencia = fecha2.getTime() - fecha1.getTime();
			long segundos = TimeUnit.MILLISECONDS.toSeconds(diferencia);
			long minutos = TimeUnit.MILLISECONDS.toMinutes(diferencia);
			System.out.println("Tiempo de ejecucion ====  " + segundos + " segundos ");
			System.out.println("Tiempo de ejecucion ====  " + minutos + " minutos ");
			System.out.println("FIN DE EJECUCION");
			System.out.println("jkfhjdkhfjkdhfjkd");
			System.exit(0);
		} catch (Exception e) {
			String messageOutPut = "Error al ejecutar proceso";
			System.out.println("Error: " + messageOutPut);
			System.exit(1);
		}
		
	}
}
