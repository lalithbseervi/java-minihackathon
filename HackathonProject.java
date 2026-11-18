package hackathonproject;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import java.sql.*;
import javafx.stage.FileChooser;
import java.io.*;
import java.util.*;


public class HackathonProject extends Application {
    TextField srn = new TextField();
    TextField name = new TextField();

    ComboBox<String> dept = new ComboBox<>();
    ComboBox<Integer> sem = new ComboBox<>();

    RadioButton male = new RadioButton("Male");
    RadioButton female = new RadioButton("Female");

    ColorPicker color = new ColorPicker(Color.LIGHTBLUE);

    TableView<Student> table = new TableView<>();
    VBox card = new VBox(10);
    
    HashMap<String, Integer> courses = new HashMap<>();{
        courses.put("BCA", 6);
        courses.put("CSE", 8);
        courses.put("AI/ML", 8);
        courses.put("ISE", 8);
        courses.put("ECE", 8);
        courses.put("EEE", 8);
    }
    
    Connection con;

    // Student Class
    class Student {
        String srn, name, dept, gender, cardColor;
        int sem;

        Student(String s, String n, String d, int sm, String g, String c) {
            srn = s;
            name = n;
            dept = d;
            sem = sm;
            gender = g;
            cardColor = c;
        }
    }

    // Database Connection
    void connect() {
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/college_id_cards", "root", "");
        } catch (SQLException e) {
            alert("Database Error", e.getMessage());
        }
    }

    // Alert
    void alert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    String gender() {
        return male.isSelected() ? "Male" : female.isSelected() ? "Female" : "";
    }

    // Validation
    boolean valid() {
        if (srn.getText().trim().isEmpty() ||
            name.getText().trim().isEmpty() ||
            dept.getValue() == null ||
            sem.getValue() == null ||
            gender().isEmpty()) {

            alert("Error", "Please fill all fields.");
            return false;
        }
        return true;
    }

    // Insert
    void insert() {
        if (!valid()) return;

        try {
            String sql = "INSERT INTO students VALUES(?,?,?,?,?,?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, srn.getText());
            ps.setString(2, name.getText());
            ps.setString(3, dept.getValue());
            ps.setInt(4, sem.getValue());
            ps.setString(5, gender());

            ps.setString(6, color.getValue().toString());

            ps.executeUpdate();

            alert("Success", "Student inserted successfully.");
            view();
        } catch (SQLException e) {
            alert("Error", e.getMessage());
        }
    }

    // Search
    void search() {
        try {
            String sql = "SELECT * FROM students WHERE srn = ?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, srn.getText().trim());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                name.setText(rs.getString("name"));
                dept.setValue(rs.getString("department"));
                sem.setValue(rs.getInt("semester"));

                if (rs.getString("gender").equals("Male"))
                    male.setSelected(true);
                else
                    female.setSelected(true);

                alert("Success", "Student found.");
            } else {
                alert("Error", "Student not found.");
            }

        } catch (SQLException e) {
            alert("Error", e.getMessage());
        }
    }

    // Update
    void update() {
        if (!valid()) return;

        try {
            String sql = "UPDATE students SET name = ?, department = ?, " +
             "semester = ?, gender = ?, card_color = ? WHERE srn = ?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, name.getText());
            ps.setString(2, dept.getValue());
            ps.setInt(3, sem.getValue());
            ps.setString(4, gender());
            ps.setString(5, color.getValue().toString());
            ps.setString(6, srn.getText());

            ps.executeUpdate();


            if (ps.executeUpdate() > 0) {
                alert("Success", "Student updated.");
                view();
            } else {
                alert("Error", "Student not found.");
            }

        } catch (SQLException e) {
            alert("Error", e.getMessage());
        }
    }
    
    void delete() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setContentText("Are you sure you want to delete?");

        if (a.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
            return;

        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM students WHERE srn = ?");

            ps.setString(1, srn.getText().trim());

            if (ps.executeUpdate() > 0) {
                alert("Success", "Student deleted.");
                reset();
                view();
            } else {
                alert("Error", "Student not found.");
            }

        } catch (SQLException e) {
            alert("Error", e.getMessage());
        }
    }

    // View Records
    void view() {
        try {
            table.getItems().clear();

            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM students");

            while (rs.next()) {
                table.getItems().add(new Student(
                    rs.getString("srn"),
                    rs.getString("name"),
                    rs.getString("department"),
                    rs.getInt("semester"),
                    rs.getString("gender"),
                    rs.getString("card_color")
                ));
            }

        } catch (SQLException e) {
            alert("Error", e.getMessage());
        }
    }
    
    void reset() {
        srn.clear();
        name.clear();
        dept.setValue(null);
        sem.setValue(null);
        male.setSelected(false);
        female.setSelected(false);
        card.getChildren().clear();
    }
    
    void generateID() {
        if (!valid()) return;

        card.getChildren().clear();
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.BASELINE_LEFT);

        card.setBackground(new Background(
            new BackgroundFill(
                color.getValue(),
                new CornerRadii(10),
                Insets.EMPTY
            )
        ));

        card.getChildren().addAll(
            new Label("PES University"),
            new Label("Name: " + name.getText()),
            new Label("SRN: " + srn.getText()),
            new Label("Department: " + dept.getValue()),
            new Label("Semester: " + sem.getValue()),
            new Label("Gender: " + gender())
        );
    }
    
    void generateFromCSV() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select CSV File");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = chooser.showOpenDialog(null);
        if (file == null) return;

        List<String[]> students = new ArrayList<>();

        // Read and Validate CSV
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",", -1);

                if (data.length != 6)
                    throw new Exception("Invalid CSV format.");

                String srnValue = data[0].trim();
                String nameValue = data[1].trim();
                String deptValue = data[2].trim();
                int semester = Integer.parseInt(data[3].trim());
                String genderValue = data[4].trim();
                String cardColor = data[5].trim();

                if (srnValue.isEmpty() || nameValue.isEmpty()
                        || deptValue.isEmpty()
                        || !genderValue.equals("Male")
                        && !genderValue.equals("Female")
                        || semester < 1 || semester > 8) {
                    throw new Exception("Invalid student details: " + srnValue);
                }

                // Validate color
                Color.web(cardColor);
                students.add(data);
            }
        } catch (Exception e) {
            alert("CSV Error", e.getMessage());
            return;
        }

        if (students.isEmpty()) {
            alert("Error", "CSV file contains no student records.");
            return;
        }

        boolean oldAutoCommit = true;

        try {
            oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            String sql = "INSERT INTO students " +
                "(srn, name, department, semester, gender, card_color) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            for (String[] d : students) {
                ps.setString(1, d[0].trim());
                ps.setString(2, d[1].trim());
                ps.setString(3, d[2].trim());
                ps.setInt(4, Integer.parseInt(d[3].trim()));
                ps.setString(5, d[4].trim());
                ps.setString(6, d[5].trim());

                ps.addBatch();
            }
            
            ps.executeBatch();
            con.commit();

            // Generate ID Cards
            VBox cards = new VBox(20);
            cards.setPadding(new Insets(20));

            for (String[] d : students) {
                VBox id = new VBox(10);
                id.setPadding(new Insets(20));
                id.setAlignment(Pos.CENTER);
                id.setMaxWidth(300);

                id.setBackground(new Background(
                    new BackgroundFill(
                        Color.web(d[5].trim()),
                        new CornerRadii(10),
                        Insets.EMPTY
                    )
                ));

                id.getChildren().addAll(
                    new Label("STUDENT ID CARD"),
                    new Label("Name: " + d[1].trim()),
                    new Label("SRN: " + d[0].trim()),
                    new Label("Department: " + d[2].trim()),
                    new Label("Semester: " + d[3].trim()),
                    new Label("Gender: " + d[4].trim())
                );

                cards.getChildren().add(id);
            }

            ScrollPane scroll = new ScrollPane(cards);
            scroll.setFitToWidth(true);

            Stage window = new Stage();
            window.setTitle("Generated Student ID Cards");
            window.setScene(new Scene(scroll, 450, 650));
            window.show();

            alert("Success", students.size() + " students registered and ID cards generated!");
            view();
        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                e.addSuppressed(ex);
            }

            alert("Transaction Failed", "No student records were imported.\n" + e.getMessage());
        } finally {
            try {
                con.setAutoCommit(oldAutoCommit);
            } catch (SQLException e) {
                alert("Database Error", e.getMessage());
            }
        }
    }

    @Override
    public void start(Stage stage) {
        connect();

        Label heading = new Label("STUDENT REGISTRATION SYSTEM");
        heading.setStyle("-fx-font-size:22px; -fx-font-weight:bold;");

        ToggleGroup group = new ToggleGroup();
        male.setToggleGroup(group);
        female.setToggleGroup(group);

        dept.getItems().addAll(courses.keySet());
        dept.setOnAction(e -> {
            sem.getItems().setAll(1, 2, 3, 4, 5, 6, 7, 8);
            sem.getItems().removeIf(s -> s > courses.get(dept.getValue()));
            sem.setValue(null);
        });


        HBox genderBox = new HBox(10, male, female);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);

        form.addRow(0, new Label("SRN:"), srn);
        form.addRow(1, new Label("Name:"), name);
        form.addRow(2, new Label("Department:"), dept);
        form.addRow(3, new Label("Semester:"), sem);
        form.addRow(4, new Label("Gender:"), genderBox);

        Button insert = new Button("Insert");
        Button search = new Button("Search");
        Button update = new Button("Update");
        Button delete = new Button("Delete");
        Button resetBtn = new Button("Reset");
        Button generate = new Button("Generate ID");
        Button apply = new Button("Apply Color");
        Button bulkID = new Button("Import CSV & Generate IDs");


        HBox buttons = new HBox(8, insert, search, update, delete, resetBtn, bulkID);

        // Table Columns
        String[] columns = {"SRN", "Name", "Department", "Semester", "Gender"};

        for (int i = 0; i < columns.length; i++) {
            final int col = i;

            TableColumn<Student, String> c = new TableColumn<>(columns[i]);

            c.setCellValueFactory(data -> {
                Student s = data.getValue();

                String value = switch (col) {
                    case 0 -> s.srn;
                    case 1 -> s.name;
                    case 2 -> s.dept;
                    case 3 -> String.valueOf(s.sem);
                    default -> s.gender;
                };

                return new SimpleStringProperty(value);
            });

            table.getColumns().add(c);
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.setOnMouseClicked(e -> {
            Student s = table.getSelectionModel().getSelectedItem();
            color.setValue(Color.web(s.cardColor));

            if (s != null) {
                srn.setText(s.srn);
                name.setText(s.name);
                dept.setValue(s.dept);
                sem.setValue(s.sem);

                if (s.gender.equals("Male"))
                    male.setSelected(true);
                else
                    female.setSelected(true);
            }
        });

        // Button Actions
        insert.setOnAction(e -> insert());
        search.setOnAction(e -> search());
        update.setOnAction(e -> update());
        delete.setOnAction(e -> delete());
        resetBtn.setOnAction(e -> reset());
        generate.setOnAction(e -> generateID());
        bulkID.setOnAction(e -> generateFromCSV());

        apply.setOnAction(e -> card.setBackground(
            new Background(new BackgroundFill(
                Color.web(color.getValue().toString()),
                new CornerRadii(10),
                Insets.EMPTY
            ))
        ));

        VBox left = new VBox(15, heading, form, buttons, table);

        VBox right = new VBox(
            10,
            new Label("ID Card Color"),
            color,
            apply,
            generate,
            card
        );

        HBox layout = new HBox(30, left, right);
        layout.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(layout);
        scroll.setFitToWidth(true);

        stage.setTitle("Student Registration System");
        stage.setScene(new Scene(scroll, 1100, 750));
        stage.show();

        view();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
