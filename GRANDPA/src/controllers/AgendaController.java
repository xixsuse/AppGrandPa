package controllers;


import classes.database.DatabaseConnector;
import classes.utils.Model;
import com.sun.org.apache.xpath.internal.operations.Mod;
import controllers.addevent.AddEventController;
import controllers.editevent.EditEventController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgendaController extends Controller implements Initializable{

    DatabaseConnector databaseHandler;


    @FXML
    private Label prevMonth;

    @FXML
    private Label nextMonth;

    @FXML
    private Label monthLabel;

    @FXML
    private HBox weekdayHeader;


    @FXML
    private GridPane calendarGrid;

    @FXML
    private ScrollPane scrollPane;


    //**************************************************************************
    //**************************************************************************
    //**************************************************************************

    // Events
    private void addEvent(VBox day) {

        // Purpose - Add event to a day

        // Do not add events to days without labels
        if(!day.getChildren().isEmpty()) {

            // Get the day number
            Label lbl = (Label) day.getChildren().get(0);
            System.out.println(lbl.getText());

            // Store event day and month in data singleton
            Model.getInstance().event_day = Integer.parseInt(lbl.getText());

            String [] parts = monthLabel.getText().split(" ");

            Model.getInstance().event_month = Model.getInstance().getMonthIndex(parts[0]);
            Model.getInstance().event_year = Integer.parseInt(parts[1]);

            // Open add event view
            try {
                // Load root layout from fxml file.
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("addevent/add_event.fxml"));
                AnchorPane rootLayout = (AnchorPane) loader.load();
                Stage stage = new Stage(StageStyle.UNDECORATED);
                stage.initModality(Modality.APPLICATION_MODAL);

                // Pass main controller reference to view
                AddEventController eventController = loader.getController();
                eventController.setMainController(this);

                // Show the scene containing the root layout.
                Scene scene = new Scene(rootLayout);
                stage.setScene(scene);
                stage.show();
            } catch (IOException ex) {
                Logger.getLogger(AgendaController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void editEvent(VBox day, String title, int eventID , String descript , String hoursEvt , String minutesEvt ) {

        // Store event fields in data singleton
        Label dayLbl = (Label)day.getChildren().get(0);
        Model.getInstance().event_day = Integer.parseInt(dayLbl.getText());
        Model.getInstance().event_month = Model.getInstance().viewing_month;
        Model.getInstance().event_year = Model.getInstance().viewing_year;
        Model.getInstance().event_subject = title;
        Model.getInstance().event_description = descript;
        Model.getInstance().event_id = eventID;

        Model.getInstance().event_hour = hoursEvt;
        Model.getInstance().event_minutes = minutesEvt;


        // When user clicks on any date in the calendar, event editor window opens
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("editevent/edit_event.fxml"));
            AnchorPane rootLayout = (AnchorPane) loader.load();
            Stage stage = new Stage(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);

            // Pass main controller reference to view
            EditEventController eventController = loader.getController();
            eventController.setMainController(this);

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(AgendaController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void calendarGenerate(){


        // Update the VIEWING YEAR
        Model.getInstance().viewing_year = 2019;



        // Get a list of all the months (1-12) in a year
        DateFormatSymbols dateFormat = new DateFormatSymbols();
        String months[] = dateFormat.getMonths();
        String[] spliceMonths = Arrays.copyOfRange(months, 0, 12);


        // Update the VIEWING MONTH
        Model.getInstance().viewing_month = Model.getInstance().getMonthIndex(spliceMonths[0]);

        // Update view
        repaintView();
    }


    public void repaintView(){
        // Purpose - To be usable anywhere to update view
        // 1. Correct calendar labels based on Gregorian Calendar
        // 2. Display events known to database

        loadCalendarLabels();

        populateMonthWithEvents();

    }


    // Remplissage des events dans calendar
    private void populateMonthWithEvents(){

        databaseHandler = new DatabaseConnector();
        // Get viewing calendar
        String currentMonth = monthLabel.getText().split(" ")[0];

        int currentMonthIndex = Model.getInstance().getMonthIndex(currentMonth) + 1;
        int currentYear = Integer.parseInt(monthLabel.getText().split(" ")[1]);
        // int currentYear = 2019;
        // System.out.println(currentYear);

        // Query to get ALL Events from the selected calendar !!
        // String getMonthEventsQuery = "SELECT * from evenement WHERE month(dateEvent) = "+ Integer.toString(Model.getInstance().viewing_month+1);
        String getMonthEventsQuery = "SELECT * from evenement ";

        // Store the results here
        ResultSet result = databaseHandler.executeQuery(getMonthEventsQuery);

        try {
            while(result.next()){

                // Get date for the event
                Date eventDate = result.getDate("dateEvent");
                String eventDescript = result.getString("description");
                String eventTitle = result.getString("title");
                String eventParts = result.getString("heureEvent");
                int eventID = result.getInt("id_event");
                // Check for year we have selected

                System.out.println(eventDate.toString()  );
                System.out.println(eventTitle + " - Mois : " + eventDate.toLocalDate().getMonth().name());
                System.out.println("Model/- EvDate month : " +  Model.getInstance().viewing_month + " -- " + eventDate.toLocalDate().getMonthValue() );
                if (currentYear == eventDate.toLocalDate().getYear()){
                    System.out.println("-- Anee correct");
                    // Check for the month we already have selected (we are viewing)
                    if (eventDate.toLocalDate().getMonthValue() == Model.getInstance().viewing_month +1  /*&& currentMonthIndex == eventDate.toLocalDate().getMonthValue()*/ ){

                        // Get day for the month
                        int day = eventDate.toLocalDate().getDayOfMonth();

                        // Display decription of the event given it's day
                        showDate(day, eventTitle , eventID, eventDescript , eventParts.split("-")[0], eventParts.split("-")[1]);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(AddEventController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void showDate(int dayNumber, String title, int eventID , String descript , String hoursEvt , String minutesEvt){
        System.out.println(getClass().getResource(".").getFile());
        Image img = new Image(getClass().getClassLoader().getResourceAsStream("ressources/icons/icon2.png"));
        // Image img = new Image(getClass().getClassLoader().getResourceAsStream("../ressources/icons/icon2.png"));
        ImageView imgView = new ImageView();
        imgView.setImage(img);

        for (Node node: calendarGrid.getChildren()) {

            // Get the current day
            VBox day = (VBox) node;

            // Don't look at any empty days (they at least must have a day label!)
            if (!day.getChildren().isEmpty()) {

                // Get the day label for that day
                Label lbl = (Label) day.getChildren().get(0);

                // Get the number
                int currentNumber = Integer.parseInt(lbl.getText());

                // Did we find a match?
                if (currentNumber == dayNumber  ) {

                    // Add an event label with the given description
                    Label eventLbl = new Label(title);
                    eventLbl.setGraphic(imgView);
                    eventLbl.getStyleClass().add("event-label");

                    // Save the term ID in accessible text
                    eventLbl.setAccessibleText(Integer.toString(eventID));
                    System.out.println(eventLbl.getAccessibleText());

                    eventLbl.addEventHandler(MouseEvent.MOUSE_PRESSED, (e)->{
                        editEvent((VBox)eventLbl.getParent(), eventLbl.getText(), eventID , descript , hoursEvt , minutesEvt );

                    });


                    eventLbl.setStyle("-fx-background-color: rgb(235,243,248)");

                    // Stretch to fill box
                    eventLbl.setMaxWidth(Double.MAX_VALUE);

                    // Mouse effects
                    eventLbl.addEventHandler(MouseEvent.MOUSE_ENTERED, (e)->{
                        eventLbl.getScene().setCursor(Cursor.HAND);
                    });

                    eventLbl.addEventHandler(MouseEvent.MOUSE_EXITED, (e)->{
                        eventLbl.getScene().setCursor(Cursor.DEFAULT);
                    });

                    // Add label to calendar
                    day.getChildren().add(eventLbl);
                }
            }
        }
    }

    private void loadCalendarLabels(){

        // Get the current VIEW
        int year = Model.getInstance().viewing_year;
        int month = Model.getInstance().viewing_month;

        System.out.println("Year :" + year + ", Month " + month);

        // Note: Java's Gregorian Calendar class gives us the right
        // "first day of the month" for a given calendar & month
        // This accounts for Leap Year
        GregorianCalendar gc = new GregorianCalendar(year, month, 1);
        int firstDay = gc.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = gc.getActualMaximum(Calendar.DAY_OF_MONTH);

        // We are "offsetting" our start depending on what the
        // first day of the month is.
        // For example: Sunday start, Monday start, Wednesday start.. etc
        int offset = firstDay;
        int gridCount = 1;
        int lblCount = 1;

        // Go through calendar grid
        for(Node node : calendarGrid.getChildren()){

            VBox day = (VBox) node;

            day.getChildren().clear();
            day.setStyle("-fx-backgroud-color: white");
            day.setStyle("-fx-font: 14px \"System\" ");

            // Start placing labels on the first day for the month
            if (gridCount < offset) {
                gridCount++;
                // Darken color of the offset days
                day.setStyle("-fx-background-color: #E9F2F5");
            } else {

                // Don't place a label if we've reached maximum label for the month
                if (lblCount > daysInMonth) {
                    // Instead, darken day color
                    day.setStyle("-fx-background-color: #E9F2F5");
                } else {

                    // Make a new day label
                    Label lbl = new Label(Integer.toString(lblCount));
                    lbl.setPadding(new Insets(5));
                    lbl.setStyle("-fx-text-fill:darkslategray");
                    if (lblCount ==  Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                            && Model.getInstance().viewing_month == Calendar.getInstance().get(Calendar.MONTH)
                            && Model.getInstance().viewing_year == Calendar.getInstance().get(Calendar.YEAR)
                            ){
                        day.setStyle("-fx-background-color: aqua");
                        lbl.setStyle("-fx-text-fill: #333333; -fx-font-weight: 600");
                    }
                    day.getChildren().add(lbl);
                }

                lblCount++;
            }
        }
    }

    public void initializeCalendarGrid(){

        // Go through each calendar grid location, or each "day" (7x6)
        int rows = 6;
        int cols = 7;
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){

                // Add VBox and style it
                VBox vPane = new VBox();
                vPane.getStyleClass().add("calendar_pane");
                vPane.setMinWidth(weekdayHeader.getPrefWidth()/7);

                vPane.addEventHandler(MouseEvent.MOUSE_CLICKED, (e)->{
                    addEvent(vPane);
                });

                GridPane.setVgrow(vPane, Priority.ALWAYS);

                // Add it to the grid
                calendarGrid.add(vPane, j, i);
            }
        }

        // Set up Row Constraints
        for (int i = 0; i < 7; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(scrollPane.getHeight()/7);
            calendarGrid.getRowConstraints().add(row);
        }
    }

    public void initializeCalendarWeekdayHeader(){

        // 7 days in a week
        int weekdays = 7;

        // Weekday names
        String[] weekAbbr = {"Dim","Lun","Mar", "Mer", "Jeu", "Ven", "Sam"};

        for (int i = 0; i < weekdays; i++){

            // Make new pane and label of weekday
            StackPane pane = new StackPane();
            pane.getStyleClass().add("weekday-header");

            // Make panes take up equal space
            HBox.setHgrow(pane, Priority.ALWAYS);
            pane.setMaxWidth(Double.MAX_VALUE);

            // Note: After adding a label to this, it tries to resize itself..
            // So I'm setting a minimum width.
            pane.setMinWidth(weekdayHeader.getPrefWidth()/7);

            // Add it to the header
            weekdayHeader.getChildren().add(pane);

            // Add weekday name
            pane.getChildren().add(new Label(weekAbbr[i]));
        }
    }

    public void prevMonth(){

        if (Model.getInstance().viewing_month == 0){
            Model.getInstance().viewing_month = 11;
            Model.getInstance().viewing_year--;
        }
        else {
            Model.getInstance().viewing_month--;
        }

        monthLabel.setText( Model.getInstance().getMonthName(Model.getInstance().viewing_month) + " " + Integer.toString(Model.getInstance().viewing_year));
        loadCalendarLabels();
        populateMonthWithEvents();
    }

    public void nextMonth(){
        if (Model.getInstance().viewing_month == 11){
            Model.getInstance().viewing_month = 0;
            Model.getInstance().viewing_year++;
        }
        else {
            Model.getInstance().viewing_month++;
        }
        monthLabel.setText( Model.getInstance().getMonthName(Model.getInstance().viewing_month) + " " + Integer.toString(Model.getInstance().viewing_year));
        loadCalendarLabels();

        populateMonthWithEvents();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Model.getInstance().viewing_year =  Calendar.getInstance().get(Calendar.YEAR);
        Model.getInstance().viewing_month =  Calendar.getInstance().get(Calendar.MONTH);
        monthLabel.setText( Model.getInstance().getMonthName(Model.getInstance().viewing_month) + " " + Integer.toString(Model.getInstance().viewing_year));
        initializeCalendarGrid();
        loadCalendarLabels();
        // Peupler evenements
        populateMonthWithEvents();
        initializeCalendarWeekdayHeader();
    }
}
