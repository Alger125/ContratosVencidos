package com.bbva.arqspring.CargaMasiva.util;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bbva.arqspring.dao.BaseDAO;
import com.bbva.arqspring.util.NoActivo;

@Service
public class NoActiveProcess extends BaseDAO implements NoActivo{
    
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
            System.out.println("Proceso no activo, error al ejecutar");
        }
        
    }
    
    public static void eliminarFichero(File fichero) {
        if (fichero.exists()) {
            fichero.delete();
            System.out.println("El archivo " + fichero.getName() + " fue eliminado.");
        }
    }
}