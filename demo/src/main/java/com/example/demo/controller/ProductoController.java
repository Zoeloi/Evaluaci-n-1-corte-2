package com.example.demo.controller;

import com.example.demo.DAO.ProductoDAO;
import com.example.demo.model.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProductoController {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbFiltroCategoria;
    @FXML private CheckBox chkSoloExistencias;
    @FXML private Label lblContador;

    // Campos del formulario reintegrados
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    private ProductoDAO productoDAO = new ProductoDAO();
    private int idProductoSeleccionado = -1;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        cbCategoria.setItems(FXCollections.observableArrayList("Papelería", "Tecnología", "Accesorios"));
        cbFiltroCategoria.setItems(FXCollections.observableArrayList("Todas", "Papelería", "Tecnología", "Accesorios"));
        cbFiltroCategoria.setValue("Todas");

        recargarTabla();
    }

    // ==================== NUEVO CÓDIGO CRUD ====================

    @FXML
    void guardarProducto(ActionEvent event) {
        if (!validarFormulario()) return;
        try {
            productoDAO.insertarProducto(extraerProductoDelFormulario(-1));
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto guardado correctamente.");
            limpiarFormulario();
            recargarTabla();
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar", e.getMessage());
        }
    }

    @FXML
    void actualizarProducto(ActionEvent event) {
        if (idProductoSeleccionado == -1) {
            mostrarAlerta(Alert.AlertType.WARNING, "Acción denegada", "Debe seleccionar un producto de la tabla.");
            return;
        }
        if (!validarFormulario()) return;
        try {
            productoDAO.actualizarProducto(extraerProductoDelFormulario(idProductoSeleccionado));
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto actualizado correctamente.");
            limpiarFormulario();
            recargarTabla();
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al actualizar", e.getMessage());
        }
    }

    @FXML
    void eliminarProducto(ActionEvent event) {
        if (idProductoSeleccionado == -1) {
            mostrarAlerta(Alert.AlertType.WARNING, "Acción denegada", "Debe seleccionar un producto de la tabla.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("¿Seguro que desea eliminar este producto?");
        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                productoDAO.eliminarProducto(idProductoSeleccionado);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto eliminado.");
                limpiarFormulario();
                recargarTabla();
            } catch (SQLException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }

    @FXML
    void seleccionarProducto(MouseEvent event) {
        Producto p = tblProductos.getSelectionModel().getSelectedItem();
        if (p != null) {
            idProductoSeleccionado = p.getIdProducto();
            txtCodigo.setText(p.getCodigo());
            txtNombre.setText(p.getNombre());
            cbCategoria.setValue(p.getCategoria());
            txtPrecio.setText(p.getPrecio().toString());
            txtStock.setText(String.valueOf(p.getStock()));
        }
    }

    @FXML
    void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        cbCategoria.setValue(null);
        txtPrecio.clear();
        txtStock.clear();
        idProductoSeleccionado = -1;
        tblProductos.getSelectionModel().clearSelection();
    }

    private boolean validarFormulario() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String categoria = cbCategoria.getValue();

        if (codigo.isEmpty() || codigo.length() > 10) {
            mostrarAlerta(Alert.AlertType.WARNING, "Código inválido", "El código debe tener entre 1 y 10 caracteres.");
            return false;
        }
        if (nombre.length() < 3 || nombre.length() > 80) {
            mostrarAlerta(Alert.AlertType.WARNING, "Nombre inválido", "El nombre debe tener entre 3 y 80 caracteres.");
            return false;
        }
        if (categoria == null || categoria.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Categoría inválida", "Debe seleccionar una categoría.");
            return false;
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            if (precio.compareTo(BigDecimal.ZERO) <= 0 || precio.scale() > 2 || precio.compareTo(new BigDecimal("99999999.99")) > 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Precio inválido", "El precio debe ser mayor a 0 y tener máximo 2 decimales.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Precio inválido", "El precio ingresado no es válido.");
            return false;
        }

        try {
            if (Integer.parseInt(txtStock.getText().trim()) < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Stock inválido", "El stock no puede ser negativo.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Stock inválido", "El stock debe ser un número entero.");
            return false;
        }

        return true;
    }

    private Producto extraerProductoDelFormulario(int id) {
        return new Producto(id, txtCodigo.getText().trim().toUpperCase(), txtNombre.getText().trim(),
                cbCategoria.getValue(), new BigDecimal(txtPrecio.getText().trim()),
                Integer.parseInt(txtStock.getText().trim()));
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // ==================== CÓDIGO DE BÚSQUEDA (El que ya tenías) ====================

    @FXML
    void buscarProductos(ActionEvent event) { recargarTabla(); }

    @FXML
    void limpiarFiltros(ActionEvent event) {
        txtBuscar.clear();
        cbFiltroCategoria.setValue("Todas");
        chkSoloExistencias.setSelected(false);
        recargarTabla();
    }

    @FXML
    void recargarTabla() {
        try {
            List<Producto> lista = productoDAO.buscarProductos(txtBuscar.getText(), cbFiltroCategoria.getValue(), chkSoloExistencias.isSelected());
            tblProductos.setItems(FXCollections.observableArrayList(lista));
            lblContador.setText("Registros: " + lista.size() + (lista.isEmpty() ? " (Sin coincidencias)" : ""));
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al cargar la base de datos.");
        }
    }
}