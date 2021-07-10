/*
    DDSI - SEMINARIO 2
    GRUPO 2-B2
    ANTONIO CARLOS MARTINEZ GARCIA
    JAVIER RAMIREZ PULIDO
    PABLO NUÑEZ TEJERO
    PEDRO PADILLA REYES
    SANTIAGO PADILLA ALVAREZ
*/

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ddsi_sem_2;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Savepoint;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.sql.Timestamp;

/**
 *
 * @author Pablo Nuñez Tejero
 */
public class DDSI_SEM_2 {
    
    private static void cambiar_dinero_patr(Connection con) throws SQLException{
    	PreparedStatement stmt= null;
    	//Statement s= null;
    	Scanner sc= new Scanner(System.in);
   	String id_patr;
        float dinero;

        System.out.println("Introducir ID del patrocinador:");
        id_patr= sc.nextLine();

        System.out.println("Introducir el dinero:");
        dinero= sc.nextFloat();
        
        	try{
                    stmt = con.prepareStatement("UPDATE PATROCINA SET dinero_patr=? WHERE id_entidad=?");
                    stmt.setFloat(1,dinero);
                    stmt.setString(2,id_patr);
                    stmt.executeUpdate();
           	 
        	}catch(SQLException e){
                    System.err.println("ERROR: Ha fallado la actualización del dinero del patrocinador indicado.");
        	}    	 
    }

    
    private static void borrar_tablas(Connection con) throws SQLException{
        PreparedStatement stmt= null;
        DatabaseMetaData md= con.getMetaData();
        ResultSet r= null;
        boolean exists;
        exists= md.getTables("practbd.oracle0.ugr.es", null,"DETALLE_PEDIDO", null).next();
        if(exists){
            System.out.println("1");
            stmt= con.prepareStatement("DROP TABLE DETALLE_PEDIDO");
            stmt.executeUpdate();
        }
        exists= md.getTables("practbd.oracle0.ugr.es", null,"PEDIDO", null).next();
        if(exists){
            System.out.println("2");
            stmt= con.prepareStatement("DROP TABLE PEDIDO");
            stmt.executeUpdate();
        }
        exists= md.getTables("practbd.oracle0.ugr.es", null,"STOCK", null).next();
        if(exists){
            System.out.println("3");
            stmt= con.prepareStatement("DROP TABLE STOCK");
            stmt.executeUpdate();
        }        
    }
    
    private static void crear_tablas(Connection con){
        PreparedStatement stmt= null;
        try{
            stmt= con.prepareStatement("CREATE TABLE STOCK(Cproducto NUMBER(4) PRIMARY KEY, Cantidad INT NOT NULL)");
            stmt.executeUpdate();
            stmt= con.prepareStatement("CREATE TABLE PEDIDO(Cpedido NUMBER(4) PRIMARY KEY, Ccliente NUMBER(4) NOT NULL, Fecha_Pedido DATE NOT NULL)");
            stmt.executeUpdate();
            stmt= con.prepareStatement("CREATE TABLE DETALLE_PEDIDO(Cpedido NUMBER(4) REFERENCES PEDIDO(Cpedido), Cproducto NUMBER(4) REFERENCES STOCK(Cproducto), Cantidad INT NOT NULL, PRIMARY KEY(Cpedido,Cproducto))");
            stmt.executeUpdate();
        }catch(SQLException sqle){
            System.out.println("Error en la ejecución: " + sqle.getErrorCode() + " " + sqle.getMessage());
        }
    }
    
    private static void asignar_tuplas(Connection con) throws SQLException{
        PreparedStatement stmt= null;
        stmt= con.prepareStatement("INSERT INTO STOCK VALUES(?,?)");
        try{
            int codigo= 1000;
            int cantidad= 10;

            for(int i=0; i<10; ++i){
                stmt.setInt(1,codigo);
                stmt.setInt(2,cantidad);
                codigo++;
                stmt.executeUpdate();
            }
        }catch(SQLException sqle){
            System.out.println("Error en la ejecución: " + sqle.getErrorCode() + " " + sqle.getMessage());
        }
    }

    private static void defecto_tuplas(Connection con) throws SQLException{
        borrar_tablas(con);
        crear_tablas(con);
        asignar_tuplas(con);
    }
    
    private static void borrar_pedido(Connection con) throws SQLException{
        PreparedStatement stmt= null;
        Statement s= null;
        Scanner sc= new Scanner(System.in);
        int cpedido;
        System.out.println("Introducir codigo de pedido.");
        cpedido= sc.nextInt();
                        
        s = con.prepareStatement("SELECT cantidad FROM DETALLE_PEDIDO where DETALLE_PEDIDO.Cpedido = " + cpedido);                        
        ResultSet rs = s.executeQuery("SELECT cantidad FROM DETALLE_PEDIDO where DETALLE_PEDIDO.Cpedido = " + cpedido);
        ArrayList<Integer> cantidades= new ArrayList<>();
        int i=0;           
        while(rs.next()){
            cantidades.add(rs.getInt(1));
            i++;
        }
        rs.close();
        s = con.prepareStatement("SELECT Cproducto FROM DETALLE_PEDIDO where DETALLE_PEDIDO.Cpedido = " + cpedido);                        
        ResultSet rs_dos = s.executeQuery("SELECT Cproducto FROM DETALLE_PEDIDO where DETALLE_PEDIDO.Cpedido = " + cpedido);
        ArrayList<Integer> cproductos= new ArrayList<>();           
        while(rs_dos.next()){
            cproductos.add(rs_dos.getInt(1));
        }        
        rs_dos.close();
        ResultSet rs_tres= null;
        for(int j=0; j<i; j++){
            s= con.prepareStatement("SELECT cantidad FROM STOCK WHERE STOCK.Cproducto = "+cproductos.get(j));
            rs_tres = s.executeQuery("SELECT cantidad FROM STOCK WHERE STOCK.Cproducto = "+cproductos.get(j));
            rs_tres.next();
            int cantidad_stock= rs_tres.getInt(1);
            cantidad_stock+= cantidades.get(j);
            try{
                stmt = con.prepareStatement("UPDATE STOCK SET Cantidad=? WHERE Cproducto=?");
                stmt.setInt(1,cantidad_stock);
                stmt.setInt(2,cproductos.get(j));
                stmt.executeUpdate();
                System.out.println(" El stock disponible de "+ cproductos.get(j) +" después de hacer la operacion es de " + cantidad_stock);
            }catch(SQLException e){
                System.err.println("ERROR: Ha fallado la actualizacion del stock.");
            }         
        }
        rs_tres.close();
        s= con.prepareStatement("DELETE FROM DETALLE_PEDIDO WHERE DETALLE_PEDIDO.Cpedido = "+ cpedido);
        s.executeQuery("DELETE FROM DETALLE_PEDIDO WHERE DETALLE_PEDIDO.Cpedido = "+ cpedido);
        s= con.prepareStatement("DELETE FROM PEDIDO WHERE PEDIDO.Cpedido = "+ cpedido);
        s.executeQuery("DELETE FROM PEDIDO WHERE PEDIDO.Cpedido = "+ cpedido);    
    }
    
    private static void aniadir_detalle_producto(Connection con, int cpedido) throws SQLException{
                        
        Scanner sc = new Scanner(System.in);
        PreparedStatement stmt = null;
        int cproducto;
        int cantidad;

        System.out.println("Introducir codigo de producto.");
        cproducto= sc.nextInt();
                                        
        Statement s = null;
                        
        s = con.prepareStatement("SELECT cantidad FROM STOCK where STOCK.Cproducto = " + cproducto);

        ResultSet rs = s.executeQuery("SELECT cantidad FROM STOCK where STOCK.Cproducto = " + cproducto);

        int stock = 0;

        if(rs.next()){
            stock = rs.getInt(1);
            System.out.println("El stock disponible antes del pedido es " + stock);
        }

        System.out.println("Introducir cantidad del producto.");
        cantidad= sc.nextInt();

        if(stock >= cantidad){
           boolean funciona = true;
           stmt= null;
           try{
                stmt= con.prepareStatement("INSERT INTO DETALLE_PEDIDO VALUES(?,?,?)");
                stmt.setInt(1,cpedido);
                stmt.setInt(2,cproducto);
                stmt.setInt(3,cantidad);
                stmt.executeUpdate();
           }
           catch(SQLException e){
                System.err.println("ERROR: Ha fallado la insercion de detalle-pedido.");
                funciona = false;     
           }   

           stmt= null;
           if(funciona){
                try{
                     stmt = con.prepareStatement("UPDATE STOCK SET Cantidad=? WHERE Cproducto=?");
                     stock -= cantidad;
                     stmt.setInt(1,stock);
                     stmt.setInt(2,cproducto);
                     stmt.executeUpdate();
                     System.out.println(" El stock disponible después de hacer el pedido es de " + stock);
                }
                catch(SQLException e){
                     System.err.println("ERROR: Ha fallado la actualizacion del stock.");
                }
           }

        }
    }
 
    private static void dar_alta_pedido(Connection con) throws SQLException{
            int opcion= 0;
            int cpedido;
            int ccliente;
            Scanner sc= new Scanner(System.in);
            PreparedStatement stmt= null;
            
            Savepoint antes_pedido= con.setSavepoint("antes_pedido");
                       
            System.out.println("Introducir codigo de pedido.");
            cpedido= sc.nextInt();
            System.out.println("Introducir codigo de cliente.");
            ccliente= sc.nextInt();
            System.out.println("Introducir fecha");
            System.out.println("Introduce el año:");
            int anio= sc.nextInt();
            System.out.println("Introduce el mes:");
            int mes= sc.nextInt();
            System.out.println("Introduce el dia:");
            int dia= sc.nextInt();
            
            java.sql.Date sqldate= java.sql.Date.valueOf(LocalDate.of(anio,mes,dia));
            
            
            try{
                stmt= con.prepareStatement("INSERT INTO PEDIDO VALUES(?,?,?)");
                stmt.setInt(1,cpedido);
                stmt.setInt(2,ccliente);
                stmt.setDate(3, sqldate);
                stmt.executeUpdate();
            }catch(SQLException e){
                System.err.println("ERROR: Ha fallado la insercion del pedido.");
                opcion= 4;
            }  
            
            Savepoint dsp_datos_pedido= con.setSavepoint("dsp_datos_pedido");
            boolean vacio= true;
            while(opcion != 4 || vacio){
                System.out.println("---MENU DAR DE ALTA PEDIDO---\n" + 
                        "Elegir una de las siguientes opciones: \n" + "1- Añadir detalle de producto \n" +
                        "2- Eliminar todos los detalles de producto\n" + "3- Cancelar pedido\n" + 
                        "4- Finalizar pedido\n" + "Introducir un numero del 1 al 4 para elegir\n");
                
                opcion= sc.nextInt();
                 
                switch(opcion){
                    case 1:
                        System.out.println("Ejecutando primera opcion...");
                        aniadir_detalle_producto(con,cpedido);
                        vacio= false;
                        System.out.println("Listo!\n");
                        break;
                    case 2:
                        System.out.println("Ejecutando segunda opcion...");
                        con.rollback(dsp_datos_pedido);
                        vacio= true;
                        System.out.println("Listo!\n");
                        break;  
                    case 3:
                        System.out.println("Ejecutando tercera opcion...");
                        con.rollback(antes_pedido);
                        vacio= false;
                        opcion= 4;
                        System.out.println("Listo!\n");
                        break;
                    case 4:
                        if(!vacio){
                            System.out.println("Ejecutando cuarta opcion...");
                            con.commit();
                            System.out.println("Pedido finalizado! ");
                        }else
                            System.err.println("ERROR: Introduce un producto o cancela el pedido. ");
                        break;
                    default :
                        System.err.println("ERROR: Introduzca un numero del 1 al 4.\n");
                }
            }        
    }
    
    /*private static int asignar_sueldo(Connection con, String id_personal) throws SQLException{
    	Scanner sc = new Scanner(System.in);
        PreparedStatement stmt = null;
        int anio;
        float sueldo;
        
        System.out.println("Introducir la edicion en la que quieres que trabaje: ");
        anio= sc.nextInt();
        
        System.out.println("Introducir el sueldo por hora: ");
        sueldo= sc.nextFloat();
        
        try{
                stmt= con.prepareStatement("INSERT INTO TRABAJA VALUES(?,?,?)");
                stmt.setInt(1,anio);
                stmt.setString(2,id_personal);
                stmt.setFloat(3, sueldo);
                stmt.executeUpdate();
    
        }catch(SQLException e){
                System.err.println("ERROR: Ha fallado insertarle el horario al trabajador.");
        }
        
        con.commit();
        
        return anio;                                          
        
    }*/
    
    private static void asignar_horario(Connection con) throws SQLException{
    	Scanner sc = new Scanner(System.in);
        PreparedStatement stmt = null;
        String id_personal;
        int anio;
        int id_pista;
        
        System.out.println("Introducir el identificador del trabajador: ");
        id_personal= String.valueOf(sc.nextInt());
        
        //anio = asignar_sueldo(con, id_personal);
        System.out.println("Introducir la edicion en la que quieres que trabaje: ");
        anio= sc.nextInt();
        
        
        System.out.println("Introducir la pista a asignar: ");
        id_pista= sc.nextInt();
        
        System.out.println("INTRODUCIR FECHA DE INICIO");
        System.out.println("Introduce el dia:");
        int dia= sc.nextInt();
        System.out.println("Introduce el mes:");
        int mes= sc.nextInt();
        System.out.println("Introduce el año:");
        int anio_horario= sc.nextInt();

        System.out.println("Introduce la hora:");
        int hora= sc.nextInt();
        System.out.println("Introduce los minutos:");
        int minutos= sc.nextInt();
        
        Timestamp sqldate= new Timestamp(anio_horario, mes-1, dia, hora, minutos, 0, 0);
        
        System.out.println("INTRODUCIR FECHA DE FIN");
        System.out.println("Introduce el dia:");
        dia= sc.nextInt();
        System.out.println("Introduce el mes:");
        mes= sc.nextInt();
        System.out.println("Introduce el año:");
        anio_horario= sc.nextInt();

        System.out.println("Introduce la hora:");
        hora= sc.nextInt();
        System.out.println("Introduce los minutos:");
        minutos= sc.nextInt();
        
        Timestamp sqldate2= new Timestamp(anio_horario, mes-1, dia, hora, minutos, 0, 0);
        
        
        try{
                stmt= con.prepareStatement("INSERT INTO ASIGNADO VALUES(?,?,?,?,?)");
                stmt.setInt(1,id_pista);
                stmt.setInt(2,anio);
                stmt.setString(3, id_personal);
                stmt.setTimestamp(4, sqldate);
                stmt.setTimestamp(5, sqldate2);
                stmt.executeUpdate();
        }catch(SQLException e){
                System.err.println("ERROR: Ha fallado insertarle el horario al trabajador.");
        }
                                        
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{  
            //step1 load the driver class  
            Class.forName("oracle.jdbc.driver.OracleDriver");  

            //step2 create  the connection object  
            Connection con=DriverManager.getConnection(  
            "jdbc:oracle:thin:@//oracle0.ugr.es:1521/practbd.oracle0.ugr.es","x5610385","x5610385");  
            con.setAutoCommit(false);
            if(con != null){
                System.out.println("Conexion exitosa!");
            }else
                System.out.println("No conecta");
            //step3 create the statement object  

            PreparedStatement stmt= null;
            Statement s= con.createStatement();
            
            Scanner sc= new Scanner(System.in);
            int opcion= 0;            
            while(opcion != 4){
                System.out.println("---MENU---\n" + "Elegir una de las siguientes opciones: \n" +
                        "1- Borrado y creacion de las tablas + insercion de 10 tuplas por defecto en Stock\n" +
                        "2- Dar de alta nuevo pedido\n" + "3- Borrar un pedido\n" + 
                        "4- Salir del programa y cerrar conexion BD\n" + "5- Asignar horario a un trabajador BD\n" + "6- Cambiar dinero patrocinador o algo asi\n" + "Introducir un numero del 1 al 4 para elegir\n");
                
                opcion= sc.nextInt();
                 
                switch(opcion){
                    case 1:
                        System.out.println("Ejecutando primera opcion...");
                        defecto_tuplas(con);
                        con.commit();
                        System.out.println("Listo!\n");
                        break;
                    case 2:
                        System.out.println("Ejecutando segunda opcion...");
                        dar_alta_pedido(con);
                        System.out.println("Listo!\n");
                        break;  
                    case 3:
                        System.out.println("Ejecutando tercera opcion...");
                        borrar_pedido(con);
                        con.commit();
                        System.out.println("Listo!\n");
                        break;
                    case 4:
                        System.out.println("Cerrando conexion...");
                        try{
                            con.close();
                            System.out.println("Adios!");
                        }catch(SQLException e){
                            System.err.println("Error al cerrar la conexion con la base de datos");
                            e.printStackTrace(System.out);
                        }                        
                        break;
                    case 5:
                        System.out.println("Ejecutando quinta opcion...");
                        asignar_horario(con);
                        con.commit();
                        System.out.println("Listo!\n");                     
                        break;
                    case 6:
                        System.out.println("Ejecutando sexta opcion...");
                        
                        
                        cambiar_dinero_patr( con);
                        
                        con.commit();
                        System.out.println("Listo!\n");                     
                        break;
                    default :
                        System.err.println("ERROR: Introduzca un numero del 1 al 4.\n");
                }
            }
            System.exit(0); 

        }catch(Exception e){ System.out.println(e);}  
    }
}