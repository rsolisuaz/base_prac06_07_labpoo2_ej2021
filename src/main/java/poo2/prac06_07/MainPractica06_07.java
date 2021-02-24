package poo2.prac06_07;

import poo2.prac06.CreaTablasConDriverManager;
import poo2.prac07.ColocaDatosUsandoDataSource;

public class MainPractica06_07 {
    // TODO
    // Al siguiente String ponle como valor tu matricula (en vez de xxxx)
    public static final String matricula="xxxx";
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            CreaTablasConDriverManager creadorPrac06 =
                    new CreaTablasConDriverManager(
                            "localhost", "IngSW",
                            "UAZsw$021", "controlescolar_ej2021");
            creadorPrac06.creaTablas(true);
            creadorPrac06.llenaTablaCarrera(true);
            creadorPrac06.close();
        }
        catch (ClassNotFoundException ec) {
            System.err.print("No se encontro el driver de MySQL: ");
            System.err.println(ec.getMessage());
            System.exit(1);
        }
        catch(Exception ex) {
            System.err.println("Error al llamar los metodos de CreaTablasConDriverManager: "
                    + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }

        try {
            ColocaDatosUsandoDataSource creadorPrac07 =
                    new ColocaDatosUsandoDataSource(
                            "localhost", "IngSW",
                            "UAZsw$021", "controlescolar_ej2021");
            creadorPrac07.llenaTablaPeriodoEscolar(true);
            creadorPrac07.close();
        }
        catch(Exception ex) {
            System.err.println("Error al llamar los metodos de ColocaDatosUsandoDataSource: "
                    + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
