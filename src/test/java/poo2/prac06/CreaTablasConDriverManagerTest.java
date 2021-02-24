package poo2.prac06;

import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dbunit.*;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.*;
import poo2.prac06_07.MainPractica06_07;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreaTablasConDriverManagerTest extends TestCase {
    private static IDatabaseTester databaseTester;
    private final static String driverName="com.mysql.cj.jdbc.Driver";
    private final static String url="jdbc:mysql://localhost/controlescolar_ej2021";
    private final String usuario = "IngSW";
    private final String clave = "UAZsw$021";
    private final String ubicacion="localhost";
    private final String nombreBD="controlescolar_ej2021";
    private static IDatabaseConnection conndbunit;
    private static String nomCarrera;
    private static String nomPeriodo;
    private static int calificacion;
    private final static int MAX_CALIF=100;
    private final static int CALIF_CONST_DATOS=5;
    private final static int CALIF_CONST_CONN=5;
    private final static  int CALIF_CREA_TABLAS=20;
    private final static  int CALIF_LLENA_TABLA_CARRERA=60;

    @BeforeAll
    public static void inicializa() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);
        String matricula = MainPractica06_07.matricula;
        databaseTester=new JdbcDatabaseTester(driverName,url,
                "IngSW","UAZsw$021");
        databaseTester.setOperationListener(new CustomConfigurationOperationListener());
        conndbunit=databaseTester.getConnection();
        DatabaseConfig config=conndbunit.getConfig();
        config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS,true);
        QueryDataSet dataSet = new QueryDataSet(conndbunit);
        nomCarrera = "carrera_"+matricula;
        nomPeriodo = "periodoescolar_"+matricula;
        dataSet.addTable(nomCarrera, String.format("SELECT * FROM %s WHERE clave_carrera='Inexistente'",nomCarrera));
        //IDataSet dataSet=new FlatXmlDataSetBuilder().build(new FileInputStream("datosescolar.xml"));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.DELETE_ALL);
        databaseTester.onSetup();
    }

    @AfterAll
    public static void termina() throws Exception {
        databaseTester.setTearDownOperation(DatabaseOperation.REFRESH);
        databaseTester.onTearDown();
        System.out.printf("Calificacion Test de CreaTablasConDriverManager:%d/%d\n",calificacion,MAX_CALIF);
    }

    @Test
    @Order(1)
    public void testConstructorConDatosValidos()  throws Exception {
        CreaTablasConDriverManager creador=null;
        try {
            creador=new CreaTablasConDriverManager(ubicacion,usuario,clave,nombreBD);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        assertNotNull(creador);
        calificacion += CALIF_CONST_DATOS;
    }

    @Test
    @Order(2)
    public void testConstructorConDatosInvalidos()  throws Exception {
        CreaTablasConDriverManager creador=null;
        try {
            creador=new CreaTablasConDriverManager(ubicacion,usuario,clave+clave,nombreBD);
        }
        catch (Exception ex) {
            //ex.printStackTrace();
        }
        assertNull(creador);
        calificacion += CALIF_CONST_DATOS;
    }

    @Test
    @Order(3)
    public void testConstructorConConnValida()  throws Exception {
        CreaTablasConDriverManager creador=null;
        try {
            creador=new CreaTablasConDriverManager(conndbunit.getConnection());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        assertNotNull(creador);
        calificacion += CALIF_CONST_CONN;
    }

    @Test
    @Order(4)
    public void testConstructorConConnInvalida()  throws Exception {
        CreaTablasConDriverManager creador=null;
        try {
            creador=new CreaTablasConDriverManager(null);
        }
        catch (Exception ex) {
            //ex.printStackTrace();
        }
        assertNull(creador);
        calificacion += CALIF_CONST_CONN;
    }

    @Test
    @Order(5)
    public void testCreaTablas()  throws Exception {
        CreaTablasConDriverManager creador;
        try {
            creador=new CreaTablasConDriverManager(conndbunit.getConnection());
            creador.creaTablas(false);
        }
        catch (Exception ex) {
            //ex.printStackTrace();
        }
        Connection conn;
        Statement stmt;
        String sql="SHOW TABLES";
        List<String> nombres=new ArrayList<>();
        try {
            conn = conndbunit.getConnection();
            stmt = conn.createStatement();
            ResultSet rs= stmt.executeQuery(sql);
            while (rs.next()) {
                nombres.add(rs.getString(1));
            }
            Collections.sort(nombres);
        }
        catch (SQLException exsql) {
            exsql.printStackTrace();
            return;
        }
        if (nombres.size()==2 &&
            nombres.get(0).equals(nomCarrera) &&
            nombres.get(1).equals(nomPeriodo)) {
            calificacion += CALIF_CREA_TABLAS;
        }
        else {
            System.err.println("Deberian haber 2 tablas en la base de datos y con los nombres solicitados!");
        }
    }

    @Test
    @Order(6)
    public void testTablaCarrera()  throws Exception {
        CreaTablasConDriverManager creador=new CreaTablasConDriverManager(conndbunit.getConnection());
        creador.llenaTablaCarrera(false);
        ITable actualTable=conndbunit.createQueryTable("carrera",
                String.format("SELECT * FROM %s",nomCarrera));

        IDataSet expectedDataSet=new FlatXmlDataSetBuilder().build(new File("datosescolar.xml"));
        ITable expectedTable=expectedDataSet.getTable("carrera");

        Assertion.assertEquals(expectedTable,actualTable);
        calificacion += CALIF_LLENA_TABLA_CARRERA;
    }

    public static class CustomConfigurationOperationListener extends DefaultOperationListener implements IOperationListener {
        @Override
        public void connectionRetrieved(IDatabaseConnection iDatabaseConnection) {
            super.connectionRetrieved(iDatabaseConnection);
            iDatabaseConnection.getConfig().setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);
        }
    }
}
