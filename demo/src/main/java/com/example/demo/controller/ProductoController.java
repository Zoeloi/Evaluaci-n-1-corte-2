package com.example.demo.controller;

import com.example.demo.DAO.ProductoDAO;
import com.example.demo.model.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductoController {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbFiltroCategoria;
    @FXML private CheckBox chkSoloExistencias;
    @FXML private Label lblContador;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    private ProductoDAO productoDAO = new ProductoDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        cbFiltroCategoria.setItems(FXCollections.observableArrayList("Todas", "Papelería", "Tecnología", "Accesorios"));
        cbFiltroCategoria.setValue("Todas");

        recargarTabla();
    }

    @FXML
    void buscarProductos(ActionEvent event) {
        recargarTabla();
    }

    @FXML
    void limpiarFiltros(ActionEvent event) {
        txtBuscar.clear();
        cbFiltroCategoria.setValue("Todas");
        chkSoloExistencias.setSelected(false);
        recargarTabla();
    }

    @FXML
    void recargarTabla() {
        String texto = txtBuscar.getText();
        String categoria = cbFiltroCategoria.getValue();
        boolean soloExistencias = chkSoloExistencias.isSelected();

        try {
            List<Producto> lista = productoDAO.buscarProductos(texto, categoria, soloExistencias);
            ObservableList<Producto> observableList = FXCollections.observableArrayList(lista);
            tblProductos.setItems(observableList);
            lblContador.setText("Registros: " + lista.size());

            if (lista.isEmpty() && (!texto.isEmpty() || !categoria.equals("Todas") || soloExistencias)) {
                lblContador.setText("Registros: 0 (Sin coincidencias)");
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error al cargar la base de datos. Verifique su conexión.");
            alert.showAndWait();
        }
    }
}