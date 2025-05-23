package viewmodel;

import com.itextpdf.layout.properties.TextAlignment;
import com.opencsv.exceptions.CsvValidationException;
import dao.DbConnectivityClass;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import model.Person;
import service.MyLogger;
import javafx.beans.property.SimpleStringProperty;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.itextpdf.layout.Document;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
//import com.itextpdf.layout.property.TextAlignment;


public class DB_GUI_Controller implements Initializable {

    @FXML
    private Label statusLabel;

    @FXML
    private Button addButton, editButton, deleteButton, exportCSVButton, importCSVButton;

    @FXML
    private MenuItem editMenuItem, deleteMenuItem, addMenuItem;

    @FXML
    TextField first_name, last_name, department, email, imageURL;

    @FXML
    ComboBox<Major> majorComboBox;

    @FXML
    ImageView img_view;

    @FXML
    MenuBar menuBar;

    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv.setEditable(true); // Allow editing

            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));

            tv_fn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
            tv_fn.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
            tv_fn.setOnEditCommit(event -> {
                Person p = event.getRowValue();
                p.setFirstName(event.getNewValue());
                cnUtil.editUser(p.getId(), p);
            });

            tv_ln.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
            tv_ln.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
            tv_ln.setOnEditCommit(event -> {
                Person p = event.getRowValue();
                p.setLastName(event.getNewValue());
                cnUtil.editUser(p.getId(), p);
            });

            tv_department.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDepartment()));
            tv_department.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
            tv_department.setOnEditCommit(event -> {
                Person p = event.getRowValue();
                p.setDepartment(event.getNewValue());
                cnUtil.editUser(p.getId(), p);
            });

            tv_major.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMajor()));
            tv_major.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
            tv_major.setOnEditCommit(event -> {
                Person p = event.getRowValue();
                p.setMajor(event.getNewValue());
                cnUtil.editUser(p.getId(), p);
            });

            tv_email.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
            tv_email.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
            tv_email.setOnEditCommit(event -> {
                Person p = event.getRowValue();
                p.setEmail(event.getNewValue());
                cnUtil.editUser(p.getId(), p);
            });

            tv.setItems(data);

            editButton.setDisable(true);
            deleteButton.setDisable(true);
            addButton.setDisable(true);
            if (editMenuItem != null) editMenuItem.setDisable(true);
            if (deleteMenuItem != null) deleteMenuItem.setDisable(true);
            if (addMenuItem != null) addMenuItem.setDisable(true);

            ObservableList<Major> majorOptions = FXCollections.observableArrayList(Major.values());
            majorComboBox.setItems(majorOptions);
            majorComboBox.getSelectionModel().selectFirst();

            tv.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                boolean recordSelected = (newSelection != null);
                editButton.setDisable(!recordSelected);
                deleteButton.setDisable(!recordSelected);
                if (editMenuItem != null) editMenuItem.setDisable(!recordSelected);
                if (deleteMenuItem != null) deleteMenuItem.setDisable(!recordSelected);
            });

            // Detect click on empty space -> Add new empty record
            tv.setOnMouseClicked(event -> {
                if (tv.getSelectionModel().isEmpty() && event.getClickCount() == 2) {
                    Person newPerson = new Person("", "", "", Major.Business.toString(), "", "");
                    cnUtil.insertUser(newPerson);
                    newPerson.setId(cnUtil.retrieveId(newPerson));
                    data.add(newPerson);
                    tv.getSelectionModel().select(newPerson);
                }
            });

            // Validate fields
            first_name.textProperty().addListener((obs, oldText, newText) -> validateForm());
            last_name.textProperty().addListener((obs, oldText, newText) -> validateForm());
            department.textProperty().addListener((obs, oldText, newText) -> validateForm());
            email.textProperty().addListener((obs, oldText, newText) -> validateForm());
            imageURL.textProperty().addListener((obs, oldText, newText) -> validateForm());
            majorComboBox.valueProperty().addListener((obs, oldValue, newValue) -> validateForm());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void validateForm() {
        boolean validEmail = isValidEmail(email.getText());
        boolean valid = !first_name.getText().trim().isEmpty()
                && !last_name.getText().trim().isEmpty()
                && !department.getText().trim().isEmpty()
                && majorComboBox.getValue() != null // ensure a major is selected
                && validEmail;

        // Enable or disable the Add button and MenuItem based on form validation
        addButton.setDisable(!valid);
        if (addMenuItem != null) {
            addMenuItem.setDisable(!valid);
        }
    }

    // Email validation using regex
    private boolean isValidEmail(String email) {
        // Advanced regex pattern for email validation
        String regex = "^(?!.*@.*@)(?!.*\\.\\.)[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @FXML
    protected void addNewRecord() {
        Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                majorComboBox.getValue().toString(), email.getText(), imageURL.getText());
        cnUtil.insertUser(p);
        cnUtil.retrieveId(p);
        p.setId(cnUtil.retrieveId(p));
        data.add(p);
        clearForm();

        statusLabel.setText("Record added successfully!");
    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        email.setText("");
        imageURL.setText("");
        majorComboBox.getSelectionModel().selectFirst(); // Reset ComboBox selection

        tv.getSelectionModel().clearSelection();
        validateForm();
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                majorComboBox.getValue().toString(), email.getText(), imageURL.getText());
        cnUtil.editUser(p.getId(), p2);
        data.remove(p);
        data.add(index, p2);
        tv.getSelectionModel().select(index);
        statusLabel.setText("Record updated successfully!");
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
        statusLabel.setText("Record deleted successfully!");
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }
    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2, textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }


    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            first_name.setText(p.getFirstName());
            last_name.setText(p.getLastName());
            department.setText(p.getDepartment());
            email.setText(p.getEmail());
            imageURL.setText(p.getImageURL());
            majorComboBox.setValue(Major.valueOf(p.getMajor()));

            // Enable buttons for editing/deleting
            editButton.setDisable(false);
            deleteButton.setDisable(false);
            if (editMenuItem != null) editMenuItem.setDisable(false);
            if (deleteMenuItem != null) deleteMenuItem.setDisable(false);
        }
    }

    @FXML
    protected void exportToCSV() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                    // Writing header
                    writer.writeNext(new String[]{"ID", "First Name", "Last Name", "Department", "Major", "Email", "Image URL"});

                    // Writing data
                    for (Person person : data) {
                        writer.writeNext(new String[]{String.valueOf(person.getId()), person.getFirstName(), person.getLastName(),
                                person.getDepartment(), person.getMajor(), person.getEmail(), person.getImageURL()});
                    }
                }
                statusLabel.setText("CSV Exported Successfully!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error exporting CSV.");
        }
    }

    @FXML
    protected void importFromCSV() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fileChooser.showOpenDialog(null);

            if (file != null) {
                try (CSVReader reader = new CSVReader(new FileReader(file))) {
                    String[] nextLine;
                    reader.readNext(); // Skip header
                    while ((nextLine = reader.readNext()) != null) {
                        Person person = new Person(Integer.parseInt(nextLine[0]), nextLine[1], nextLine[2], nextLine[3],
                                nextLine[4], nextLine[5], nextLine[6]);
                        data.add(person);
                    }
                }
                statusLabel.setText("CSV Imported Successfully!");
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            statusLabel.setText("Error importing CSV.");
        }
    }

    // Enum for Major
    private static enum Major {Business, CSC, CPIS}

    private static class Results {

        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

    @FXML
    protected void exportToPDF() {
        try {
            // Count number of students by major
            Map<String, Integer> majorCountMap = new HashMap<>();
            for (Person person : data) {
                String major = person.getMajor();
                majorCountMap.put(major, majorCountMap.getOrDefault(major, 0) + 1);
            }

            // Create PdfWriter and PdfDocument
            PdfWriter writer = new PdfWriter(new FileOutputStream("StudentReport.pdf"));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            Paragraph title = new Paragraph("Student Report: Number of Students by Major")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            document.add(new Paragraph("\n")); // Blank line

            // Create table with 2 columns
            float[] columnWidths = {300f, 100f}; // Adjust widths
            Table table = new Table(columnWidths);

            table.addCell("Major");
            table.addCell("Number of Students");

            for (Map.Entry<String, Integer> entry : majorCountMap.entrySet()) {
                table.addCell(entry.getKey());
                table.addCell(entry.getValue().toString());
            }

            document.add(table);

            document.close();

            statusLabel.setText("PDF Report Generated Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error generating PDF.");
        }
    }
}
