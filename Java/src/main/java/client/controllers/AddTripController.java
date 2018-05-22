package main.java.client.controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.TabPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import main.java.client.InvalidFieldException;
import main.java.client.connection.ConnectionManager;
import main.java.client.gui.*;
import main.java.client.utils.TableUtils;
import main.java.models.*;

import java.net.URL;
import java.time.ZoneId;
import java.util.*;

public class AddTripController extends AbstractController implements Initializable {

    private Trip trip = new Trip();
    private int stopCounter = 1;

    @FXML private Pane addTripPane;
    @FXML private ImageView addTripImage;
    @FXML private ImageView goBackImage;

    @FXML private TabPane tabPane;

    @FXML private TextField tfTripName;
    @FXML private DatePicker dpTripDate;

    @FXML private TableView<GuiChild> tableChildren;
    @FXML private TableColumn<GuiChild, Boolean> columnChildrenSelected;
    @FXML private TableColumn<GuiChild, String> columnChildrenFirstName;
    @FXML private TableColumn<GuiChild, String> columnChildrenLastName;
    @FXML private TableColumn<GuiChild, String> columnChildrenFiscalCode;

    @FXML private TableView<GuiStaff> tableStaff;
    @FXML private TableColumn<GuiStaff, Boolean> columnStaffSelected;
    @FXML private TableColumn<GuiStaff, String> columnStaffFirstName;
    @FXML private TableColumn<GuiStaff, String> columnStaffLastName;
    @FXML private TableColumn<GuiStaff, String> columnStaffFiscalCode;

    @FXML private TextField tfStopName;
    @FXML private TextField tfStopProvince;
    @FXML private TextField tfStopNation;
    @FXML private TextField tfStopNumber;
    @FXML private ListView<GuiStop> lvStops;
    @FXML private Button buttonAddStop;
    @FXML private Button buttonRemoveSelectedStops;

    @FXML private TextField tfPullmanNumberplate;
    @FXML private TextField tfPullmanSeats;
    @FXML private ListView<Pullman> lvPullman;
    @FXML private Button buttonAddPullman;
    @FXML private Button buttonRemoveSelectedPullman;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Save button
        addTripImage.setOnMouseEntered(event -> tabPane.getScene().setCursor(Cursor.HAND));
        addTripImage.setOnMouseExited(event -> tabPane.getScene().setCursor(Cursor.DEFAULT));
        addTripImage.setOnMouseClicked(event -> saveTrip());


        // Go back button
        goBackImage.setOnMouseEntered(event -> tabPane.getScene().setCursor(Cursor.HAND));
        goBackImage.setOnMouseExited(event -> tabPane.getScene().setCursor(Cursor.DEFAULT));
        goBackImage.setOnMouseClicked(event -> goBack());


        // Connection
        ConnectionManager connectionManager = ConnectionManager.getInstance();


        // Children tab
        List<Child> children = connectionManager.getClient().getChildren();
        ObservableList<GuiChild> childrenData = TableUtils.getGuiModelsList(children);

        columnChildrenSelected.setCellFactory(CheckBoxTableCell.forTableColumn(columnChildrenSelected));
        columnChildrenSelected.setCellValueFactory(param -> param.getValue().selectedProperty());
        columnChildrenFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        columnChildrenLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        columnChildrenFiscalCode.setCellValueFactory(new PropertyValueFactory<>("fiscalCode"));

        tableChildren.setEditable(true);
        tableChildren.setItems(childrenData);


        // Staff tab
        List<Staff> staff = connectionManager.getClient().getStaff();
        ObservableList<GuiStaff> staffData = TableUtils.getGuiModelsList(staff);

        columnStaffSelected.setCellFactory(CheckBoxTableCell.forTableColumn(columnStaffSelected));
        columnStaffSelected.setCellValueFactory(param -> param.getValue().selectedProperty());
        columnStaffFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        columnStaffLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        columnStaffFiscalCode.setCellValueFactory(new PropertyValueFactory<>("fiscalCode"));

        tableStaff.setEditable(true);
        tableStaff.setItems(staffData);


        // Stops tab
        lvStops.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        buttonRemoveSelectedStops.setOnAction(event -> removeSelectedStops());
        buttonAddStop.setOnAction(event -> addStop());

        // Force the number field to be numeric only
        tfStopNumber.setTextFormatter(new TextFormatter<>(change -> {
            String text = change.getText();

            if (text.matches("[0-9]*"))
                return change;

            return null;
        }));


        // Add stop on enter key press
        EventHandler<KeyEvent> stopKeyPressEvent = event -> {
            if (event.getCode() == KeyCode.ENTER)
                addStop();
        };

        tfStopName.setOnKeyPressed(stopKeyPressEvent);
        tfStopProvince.setOnKeyPressed(stopKeyPressEvent);
        tfStopNation.setOnKeyPressed(stopKeyPressEvent);
        tfStopNumber.setOnKeyPressed(stopKeyPressEvent);


        // Pullman tab
        lvPullman.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        buttonRemoveSelectedPullman.setOnAction(event -> removeSelectedPullman());
        buttonAddPullman.setOnAction(event -> addPullman());


        // Add pullman on enter key press
        EventHandler<KeyEvent> PullmanKeyPressEvent = event -> {
            if (event.getCode() == KeyCode.ENTER)
                addPullman();
        };

        tfPullmanNumberplate.setOnKeyPressed(PullmanKeyPressEvent);
        tfPullmanSeats.setOnKeyPressed(PullmanKeyPressEvent);
    }


    /**
     * Add stop to the stops list
     */
    private void addStop() {
        // Get data
        String placeName = tfStopName.getText().trim();
        String placeProvince = tfStopProvince.getText().trim();
        String placeNation = tfStopNation.getText().trim();
        Integer stopNumber = tfStopNumber.getText().isEmpty() ? stopCounter : Integer.valueOf(tfStopNumber.getText().trim());

        // Limit the max stop number to the next available slot
        if (stopNumber > stopCounter + 1)
            stopNumber = stopCounter;

        // Create stop
        Place place = new Place(placeName, placeProvince, placeNation);
        Stop stop = new Stop(trip, place, stopNumber);

        // Check data validity
        try {
            stop.checkDataValidity();
        } catch (InvalidFieldException e) {
            showErrorDialog(e.getMessage());
            return;
        }

        // Check if the number already exists
        boolean numberAlreadyExists = false;

        for (GuiStop guiStop : lvStops.getItems()) {
            if (guiStop.getModel().getNumber().equals(stop.getNumber())) {
                numberAlreadyExists = true;
                break;
            }
        }

        if (numberAlreadyExists) {
            for (GuiStop guiStop : lvStops.getItems()) {
                if (guiStop.getModel().getNumber() >= stop.getNumber())
                    guiStop.numberProperty().setValue(guiStop.numberProperty().getValue() + 1);
            }
        }

        // Add stop
        GuiStop guiStop = new GuiStop(stop);
        lvStops.getItems().add(guiStop);
        lvStops.getItems().setAll(lvStops.getItems().sorted(Comparator.comparing(o -> o.getModel().getNumber())));

        // Reset fields
        tfStopName.setText("");
        tfStopProvince.setText("");
        tfStopNation.setText("");
        tfStopNumber.setText("");

        // Increment stop counter
        stopCounter = getHighestStopNumber() + 1;

        // Assign the focus to the place name textfield
        tfStopName.requestFocus();
    }


    /**
     * Remove the selected stop
     */
    private void removeSelectedStops() {
        GuiStop selectedItem = lvStops.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            showErrorDialog("Nessuna fermata selezionata");

        } else {
            lvStops.getItems().remove(selectedItem);
            lvStops.getSelectionModel().clearSelection();

            // Adjust stops numbers
            List<GuiStop> guiStops = new ArrayList<>(lvStops.getItems());

            for (GuiStop followingItem : guiStops) {
                if (selectedItem.numberProperty().getValue() < followingItem.numberProperty().getValue()) {
                    followingItem.numberProperty().setValue(followingItem.numberProperty().getValue() - 1);
                }
            }

            stopCounter = getHighestStopNumber() + 1;
        }
    }


    /**
     * Get the highest stop number in the ListView
     *
     * @return  stop number
     */
    private int getHighestStopNumber() {
        int highestStopNumber = 1;

        for (GuiStop guiStop1 : lvStops.getItems()) {
            if (guiStop1.numberProperty().getValue() > highestStopNumber)
                highestStopNumber = guiStop1.numberProperty().getValue();
        }

        return highestStopNumber;
    }


    /**
     * Add pullman to the pullman list
     */
    private void addPullman() {
        String pullmanNumberplate = tfPullmanNumberplate.getText().trim();
        Integer pullmanSeats = tfPullmanSeats.getText().isEmpty() ? null : Integer.valueOf(tfPullmanSeats.getText().trim());
        Pullman pullman = new Pullman(trip, pullmanNumberplate, pullmanSeats);

        // Check data
        try {
            pullman.checkDataValidity();
        } catch (InvalidFieldException e) {
            showErrorDialog(e.getMessage());
            return;
        }

        lvPullman.getItems().add(pullman);
        tfPullmanNumberplate.setText("");
        tfPullmanSeats.setText("");
    }


    /**
     * Remove selected pullman from the pullman list
     */
    private void removeSelectedPullman() {
        Pullman selectedPullman = lvPullman.getSelectionModel().getSelectedItem();

        if (selectedPullman == null) {
            showErrorDialog("Nessun pullman selezionato");
        } else {
            lvPullman.getItems().remove(selectedPullman);
            lvPullman.getSelectionModel().clearSelection();
        }
    }


    /**
     * Save trip in the database
     */
    private void saveTrip() {
        // Connection
        ConnectionManager connectionManager = ConnectionManager.getInstance();

        // Data
        String title = tfTripName.getText().trim();
        Date date = dpTripDate.getValue() == null ? null : Date.from(dpTripDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

        trip.setTitle(title);
        trip.setDate(date);
        trip.setTransports(lvPullman.getItems());
        trip.setChildren(TableUtils.getSelectedItems(tableChildren));
        trip.setStaff(TableUtils.getSelectedItems(tableStaff));

        Integer totalNumberOfSeats = trip.getAvailableSeats();
        int i=0;
        int occupiedSeats = 0;
        double totalNumberOfChildren = TableUtils.getSelectedItems(tableChildren).size();
        for (Pullman current : lvPullman.getItems()){
            List<Child> children = new ArrayList<>();
            occupiedSeats += current.getSeats();
            double occupiedSeatsPercentage = occupiedSeats/(double)totalNumberOfSeats;
            children.addAll(TableUtils.getSelectedItems(tableChildren).subList
                    (i, (int)(occupiedSeatsPercentage*totalNumberOfChildren)));
            current.addChildren(children);
            i = (int)(occupiedSeatsPercentage*totalNumberOfChildren);
        }

        // Check data
        try {
            trip.checkDataValidity();
        } catch (InvalidFieldException e) {
            showErrorDialog(e.getMessage());
            return;
        }

        List<GuiStop> guiStops = new ArrayList<>(lvStops.getItems());
        List<Stop> stops = new ArrayList<>();

        for (GuiStop guiStop : guiStops)
            stops.add(guiStop.getModel());

        trip.setStops(stops);

        trip.addTransports(lvPullman.getItems());

        // Save trip
        connectionManager.getClient().create(trip);

        // Go back to the menu
        goBack();
    }


    /**
     * Go back to the add / show trips page
     */
    public void goBack() {
        setCenterFXML((BorderPane)addTripPane.getParent(), "/views/tripAdministration.fxml");
    }

}