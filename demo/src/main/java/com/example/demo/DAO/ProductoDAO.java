package com.example.demo.DAO;

import com.example.demo.database.DatabaseConnection;
import com.example.demo.model.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> buscarProductos(String texto, String categoria, boolean soloExistencias) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM producto WHERE 1=1");
        List<Object> parametros = new ArrayList<>();

        if (texto != null && !texto.trim().isEmpty()) {
            sql.append(" AND (codigo ILIKE ? OR nombre ILIKE ?)");
            parametros.add("%" + texto.trim() + "%");
            parametros.add("%" + texto.trim() + "%");
        }

        if (categoria != null && !categoria.equals("Todas") && !categoria.trim().isEmpty()) {
            sql.append(" AND categoria = ?");
            parametros.add(categoria);
        }

        if (soloExistencias) {
            sql.append(" AND stock > 0");
        }

        sql.append(" ORDER BY codigo ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Producto(
                            rs.getInt("id_producto"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("categoria"),
                            rs.getBigDecimal("precio"),
                            rs.getInt("stock")
                    ));
                }
            }
        }
        return lista;
    }
}