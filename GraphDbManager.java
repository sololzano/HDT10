/**
@author Luis Diego Sierra, Diego López, Carlos Solórzano
*/

import org.graphstream.algorithm.PageRank;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class GraphDbManager {
    /**
     * Datos de autenticacion de la base de datos remota
     * 
     */
    private static GraphDbManager instance = null;
    private static final String USER = "hdt_10";
    private static final String PASSWORD = "CAdWFcoLA9nzzMRQySI5";
    private static final String CONNECTION = "jdbc:neo4j:bolt://" +
            "hobby-fkpcdappojekgbkenopbehol.dbs.graphenedb.com:24786";
    private GraphConnections connections;

    private GraphDbManager(){connections = new GraphConnections(CONNECTION, USER, PASSWORD);}

    /**
     * Metodo del Multiton para solo obtener una instancia
     * @return Instancia del DataBase Manager
     */
    public static GraphDbManager getInstance(){
        if (instance == null){
            instance = new GraphDbManager();
        }
        return instance;
    }

   

    /**
     * Metodo que muestra el grafo actual de la base de datos
     */
    public void dbToGraph(){
        DbToGraphThread thread = new DbToGraphThread();
        new Thread(thread).start();
    }

    /**
     * Metodo que muestra el Page Rank del grafo de la db
     */
    public void showPageRank(){
        PageRankThread thread = new PageRankThread();
        new Thread(thread).start();
    }

    /**
     * Metodo que muestra el grafo con los nodos que tienen seis o
     * mas correos enviados
     */
    public void sixOrMoreMails(){
        SixOrMoreThread thread = new SixOrMoreThread();
        new Thread(thread).start();
    }

    /**
     * Metodo que muestra el grafo simplificado
     */
    public void simplifyGraph(){
        SimplifyThread thread = new SimplifyThread();
        new Thread(thread).start();
    }


   





    /**
     * Clase que devuelve la cantidad minima de correos enviadas a alguna otra persona. Pueden ser directos o indirectos, dependiendo
     * del query.
     * @param rs Es el resultado del query de correos directos o indirectos
     * @param origen es la persona que envia el correo
     * @return Devuelve el minimo de correos enviados entre dicha persona; Devuelve -1 si no encontro la coincidencia;
     * @throws SQLException
     */
    private int getMinimoCorreosEnviados(ResultSet rs, String origen) throws SQLException {
        boolean found = false;
        int resultado = 0;
        while (rs.next() && !found){
            if (origen.equalsIgnoreCase((String)rs.getObject("x.name"))){
                resultado = Math.toIntExact((long) rs.getObject("total"));
                found = true;
            }
        }
        return resultado;
    }


    /**
     * Clase que devuelve el total de correos enviados entre dos personas. Pueden ser directos o indirectos, dependiendo
     * del rs.
     * @param rs Es el resultado del query de correos directos o indirectos
     * @param origen es la persona que envia el correo
     * @param destino es la persona que recive el correo
     * @return Devuelve el total de correos enviados entre ambas personas; Devuelve -1 si no encontro la coincidencia;
     * @throws SQLException
     */
    private int getTotalCorreosEnviados(ResultSet rs, String origen, String destino) throws SQLException {
        boolean found = false;
        String target = origen + destino;
        int resultado = 0;
        while (rs.next() && !found){
            if (target.equalsIgnoreCase((String)rs.getObject("x.name") + (String)rs.getObject("y.name"))){
                resultado = Math.toIntExact((long) rs.getObject("total"));
                found = true;
            }
        }
        return resultado;
    }




    /**
     * Clase privada, que obtiene de la DB el grafo simplifcado
     */
    private class SimplifyThread implements Runnable {

        @Override
        public void run() {
            try {
                String query = "MATCH (x:User)-[rel:SEND]->(y:User)\n" +
                        "WHERE x <> y\n" +
                        "RETURN rel, x, y";
                final Graph graph = connections.getAsGraph(query, "Simplified Graph");

                SwingUtilities.invokeLater( () -> {
                    graph.addAttribute("ui.stylesheet",
                            "node {fill-color: blue; size-mode: dyn-size;} edge {fill-color:grey;}");
                    Viewer viewer = graph.display();
                    viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
                });
            }catch (SQLException ex){
                ex.printStackTrace();
            }
        }
    }

    /**
     * Clase thread que obtiene de la db el grafo con los nodos con mas de 6 correos
     */
    private class SixOrMoreThread implements Runnable {

        @Override
        public void run() {
            try{
                // Query que devuelve los nodos con 6 o mas correos
                String query = "MATCH (x:User)-[rel:SEND]->(y:User) \n" +
                        "WHERE rel.quantity >= 6\n" +
                        "RETURN rel, x, y";
                final Graph graph = connections.getAsGraph(query, "Six or More");

                SwingUtilities.invokeLater( () -> {
                    graph.addAttribute("ui.stylesheet",
                            "node {fill-color: blue; size-mode: dyn-size;} edge {fill-color:grey;}");
                    Viewer viewer = graph.display();
                    viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
                });

            }catch (SQLException ex){
                ex.printStackTrace();
            }

        }
    }

    /**
     * Clase thread que obtiene de la db el grafo con los nodos y muestra el PageRank
     */
    private class PageRankThread implements Runnable {

        @Override
        public void run() {
            try {
                // Query que devuelve toda la DB
                String query = "MATCH (x:User)-[rel:SEND]->(y:User)\n" +
                        "RETURN rel, x, y";
                final Graph graph = connections.getAsGraph(query, "Page Rank Complete Graph");

                SwingUtilities.invokeLater( () -> {
                    graph.addAttribute("ui.stylesheet",
                            "node {fill-color: red; size-mode: dyn-size;} edge {fill-color:grey;}");
                    Viewer viewer = graph.display();
                    PageRank pageRank = new PageRank();
                    pageRank.setVerbose(true);
                    pageRank.init(graph);
                    for (Node node : graph){
                        double rank = pageRank.getRank(node);
                        node.addAttribute("ui.size", 5 + Math.sqrt(graph.getNodeCount() * rank * 20));
                        node.addAttribute("ui.label", node.getId() + "(" + String.format("%.2f%%", rank * 100) + ")");
                    }
                    viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
                });
            }catch (SQLException ex){
                ex.printStackTrace();
            }
        }
    }

    /**
     * Clase thread que  muestra el grafo de la base de datos de neo4j
     */
    private class DbToGraphThread implements Runnable {

        @Override
        public void run() {
            try {
                // Query que devuelve toda la DB
                String query = "MATCH (x:User)-[rel:SEND]->(y:User)\n" +
                        "RETURN rel, x, y";
                final Graph graph = connections.getAsGraph(query, "Complete Graph");

                SwingUtilities.invokeLater( () -> {
                    graph.addAttribute("ui.stylesheet",
                            "node {fill-color: blue; size-mode: dyn-size;} edge {fill-color:grey;}");
                    Viewer viewer = graph.display();
                    viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
                });
            }catch (SQLException ex){
                ex.printStackTrace();
            }
        }
    }

    
    


}
