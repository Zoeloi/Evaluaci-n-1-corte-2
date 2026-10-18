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
                            rs.getInt("id_producto"), rs.getString("codigo"),
                            rs.getString("nombre"), rs.getString("categoria"),
                            rs.getBigDecimal("precio"), rs.getInt("stock")
                    ));
                }
            }
        }
        return lista;
    }

    public void insertarProducto(Producto p) throws SQLException {
        String sql = "INSERT INTO producto (codigo, nombre, categoria, precio, stock) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getCodigo());
            pstmt.setString(2, p.getNombre());
            pstmt.setString(3, p.getCategoria());
            pstmt.setBigDecimal(4, p.getPrecio());
            pstmt.setInt(5, p.getStock());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw decodificarExcepcionSQL(e);
        }
    }

    public void actualizarProducto(Producto p) throws SQLException {
        String sql = "UPDATE producto SET codigo=?, nombre=?, categoria=?, precio=?, stock=? WHERE id_producto=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getCodigo());
            pstmt.setString(2, p.getNombre());
            pstmt.setString(3, p.getCategoria());
            pstmt.setBigDecimal(4, p.getPrecio());
            pstmt.setInt(5, p.getStock());
            pstmt.setInt(6, p.getIdProducto());

            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("El producto ya no existe en la base de datos.");
            }
        } catch (SQLException e) {
            throw decodificarExcepcionSQL(e);
        }
    }

    public void eliminarProducto(int idProducto) throws SQLException {
        String sql = "DELETE FROM producto WHERE id_producto=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProducto);
            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("El producto seleccionado ya fue eliminado previamente.");
            }
        }
    }

    private SQLException decodificarExcepcionSQL(SQLException e) {
        String estado = e.getSQLState();
        if (estado != null) {
            if (estado.startsWith("23505")) return new SQLException("El código ingresado ya pertenece a otro producto.");
            if (estado.startsWith("23514")) return new SQLException("Los datos violan una regla de negocio de la base de datos.");
            if (estado.startsWith("08")) return new SQLException("No se pudo conectar a la base de datos.");
        }
        return new SQLException("Ocurrió un error inesperado al guardar los datos.");
    }
}