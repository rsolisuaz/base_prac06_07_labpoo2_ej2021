package poo2.prac07;

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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ColocaDatosUsandoDataSourceTest  extends TestCase  {
    private static IDatabaseTester databaseTester;
    private final static String driverName="com.mysql.cj.jdbc.Driver";
    private final static String url="jdbc:mysql://localhost/controlescolar_ej2021";
    private final String usuario = "IngSW";
    private final String clave = "UAZsw$021";
    private final String ubicacion="localhost";
    private final String nombreBD="controlescolar_ej2021";
    private static IDatabaseConnection conndbunit;
    private static String nomPeriodo;
    private static int calificacion;
    private final static int MAX_CALIF=100;
    private final static int CALIF_CONSTRUCTOR=5;
    private final static  int CALIF_LLENA_TABLA_PERIODO=90;

    @BeforeAll
    public static void inicializa() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);
        String matricula = MainPractica06_07.matricula;
        databaseTester=new JdbcDatabaseTester(driverName,url,
                "IngSW","UAZsw$021");
        databaseTester.setOperationListener(new ColocaDatosUsandoDataSourceTest.CustomConfigurationOperationListener());
        conndbunit=databaseTester.getConnection();
        DatabaseConfig config=conndbunit.getConfig();
        config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS,true);
        QueryDataSet dataSet = new QueryDataSet(conndbunit);
        nomPeriodo = "periodoescolar_"+matricula;
        dataSet.addTable(nomPeriodo, String.format("SELECT * FROM %s WHERE id_periodo>1000000",nomPeriodo));
        //IDataSet dataSet=new FlatXmlDataSetBuilder().build(new FileInputStream("datosescolar.xml"));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.DELETE_ALL);
        databaseTester.onSetup();
    }

    @AfterAll
    public static void termina() throws Exception {
        databaseTester.setTearDownOperation(DatabaseOperation.REFRESH);
        databaseTester.onTearDown();
        System.out.printf("Calificacion Test de ColocaDatosUsandoDataSource:%d/%d\n",calificacion,MAX_CALIF);
    }

    @Test
    @Order(1)
    public void testConstructorConDatosValidos()  throws Exception {
        ColocaDatosUsandoDataSource creador=null;
        try {
            creador=new ColocaDatosUsandoDataSource(ubicacion,usuario,clave,nombreBD);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        assertNotNull(creador);
        calificacion += CALIF_CONSTRUCTOR;
    }

    @Test
    @Order(2)
    public void testConstructorConDatosInvalidos()  throws Exception {
        ColocaDatosUsandoDataSource creador=null;
        try {
            creador=new ColocaDatosUsandoDataSource(ubicacion,usuario,clave+clave,nombreBD);
        }
        catch (Exception ex) {
            //ex.printStackTrace();
        }
        assertNull(creador);
        calificacion += CALIF_CONSTRUCTOR;
    }

    @Test
    @Order(6)
    public void testTablaCarrera()  throws Exception {
        ColocaDatosUsandoDataSource creador=new ColocaDatosUsandoDataSource(ubicacion,usuario,clave,nombreBD);
        creador.llenaTablaPeriodoEscolar(false);
        ITable actualTable=conndbunit.createQueryTable("periodoescolar",
                String.format("SELECT * FROM %s",nomPeriodo));

        IDataSet expectedDataSet=new FlatXmlDataSetBuilder().build(new File("datosescolar.xml"));
        ITable expectedTable=expectedDataSet.getTable("periodoescolar");

        Assertion.assertEquals(expectedTable,actualTable);
        calificacion += CALIF_LLENA_TABLA_PERIODO;
    }


    public static class CustomConfigurationOperationListener extends DefaultOperationListener implements IOperationListener {
        @Override
        public void connectionRetrieved(IDatabaseConnection iDatabaseConnection) {
            super.connectionRetrieved(iDatabaseConnection);
            iDatabaseConnection.getConfig().setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);
        }
    }
}
