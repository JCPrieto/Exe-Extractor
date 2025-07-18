package es.jklabs.utilidades;


import org.apache.commons.lang3.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class Logger {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(Logger.class.getName());
    private static final String ARCHIVO = "log_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".log";
    private static Logger logger;

    private Logger() {
        FileHandler fh;
        try {
            fh = new FileHandler(ARCHIVO, true);
            LOG.addHandler(fh);
            LOG.setUseParentHandlers(false);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            fh.setLevel(Level.ALL);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Crear archivo logs", e);
        }
    }

    public static void error(String mensaje, Exception e) {
        LOG.log(Level.SEVERE, Mensajes.getError(mensaje), e);
    }

    public static void eliminarLogsVacios() {
        File carpeta = new File(System.getProperty("user.dir"));
        File[] lista = carpeta.listFiles();
        if (lista != null) {
            Arrays.stream(lista).filter(f -> f.isFile() && f.getName().endsWith(".log") && !Strings.CS.equals
                    (f.getName(), ARCHIVO)).forEach(Logger::eliminarLogsVacios);
        }
    }

    private static void eliminarLogsVacios(File file) {
        try (FileReader fr = new FileReader(file)) {
            BufferedReader br = new BufferedReader(fr);
            String linea = br.readLine();
            if (linea == null) {
                br.close();
                Files.delete(file.toPath());
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, Mensajes.getError("lectura.logs"), e);
        }
    }

    public static void init() {
        if (logger == null) {
            logger = new Logger();
        }
    }
}
