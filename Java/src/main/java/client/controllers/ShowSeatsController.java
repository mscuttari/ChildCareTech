package main.java.client.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import main.java.LogUtils;
import main.java.client.connection.ConnectionManager;
import main.java.client.layout.MyButtonTableCell;
import main.java.models.*;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class ShowSeatsController implements Initializable{

    // Debug
    private static final String TAG = "ShowSeatsController";

    @FXML private TableView<Trip> tableTrip;
    @FXML private TableColumn<Trip, String> columnTripTitle;
    @FXML private TableColumn<Trip, Date> columnTripDate;

    @FXML private TableView<Pullman> tablePullman;
    @FXML private TableColumn<Pullman, String> columnPullmanNumberplate;

    @FXML private TableView<Child> tableChildren;
    @FXML private TableColumn<Child, String> columnChildrenFiscalCode;
    @FXML private TableColumn<Child, String> columnChildrenFirstName;
    @FXML private TableColumn<Child, String> columnChildrenLastName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Connection
        ConnectionManager connectionManager = ConnectionManager.getInstance();

        // Table trip
        List<Trip> trips = connectionManager.getClient().getTrips();
        ObservableList<Trip> tripsData = FXCollections.observableArrayList(trips);

        columnTripTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        columnTripDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        tableTrip.setEditable(true);
        tableTrip.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tableTrip.setItems(tripsData);

        columnPullmanNumberplate.setCellValueFactory(new PropertyValueFactory<>("numberplate"));
        tablePullman.setEditable(true);
        tablePullman.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        columnChildrenFiscalCode.setCellValueFactory(new PropertyValueFactory<>("fiscalCode"));
        columnChildrenFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        columnChildrenLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        tableTrip.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                List<Pullman> pullman = (List<Pullman>) tableTrip.getSelectionModel().getSelectedItem().getTransports();
                ObservableList<Pullman> pullmanData = FXCollections.observableArrayList(pullman);
                tablePullman.setItems(pullmanData);
            }
        });

        tablePullman.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                List<Child> children = (List<Child>) tablePullman.getSelectionModel().getSelectedItem().getChildrenAssignments();
                ObservableList<Child> childrenData = FXCollections.observableArrayList(children);
                tableChildren.setItems(childrenData);
            }
        });
    }


}