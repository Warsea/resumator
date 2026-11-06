package app.resumetor.controller;

import app.resumetor.model.Education;
import app.resumetor.model.Experience;
import app.resumetor.model.Profile;
import app.resumetor.model.ProfileLink;
import app.resumetor.model.Project;
import app.resumetor.model.Skill;
import app.resumetor.repository.CareerDao;
import app.resumetor.repository.ProfileDao;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfileDashboardLayout extends ScrollPane {

    private final VBox container;
    private final ProfileDao profileDao;
    private final CareerDao careerDao;

    public ProfileDashboardLayout() {
        this.profileDao = new ProfileDao();
        this.careerDao = new CareerDao();

        container = new VBox(25);
        container.setPadding(new Insets(30));
        container.setMaxWidth(800);

        this.setContent(container);
        this.setFitToWidth(true);
        this.getStyleClass().add("main-content");

        loadData();
    }

    private void loadData() {
        container.getChildren().clear();

        Label title = new Label("My Career Profile");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        container.getChildren().add(title);

        Profile profile = profileDao.getProfile();
        container.getChildren().add(buildBasicInfoSection(profile));
        container.getChildren().add(buildSection("Experience", "Add Experience"));
        container.getChildren().add(buildSection("Projects", "Add Project"));
        container.getChildren().add(buildSection("Education", "Add Education"));
        container.getChildren().add(buildSection("Skills", "Add Skill"));
    }

    // ------------------------------------------------------------------ Personal info

    private VBox buildBasicInfoSection(Profile existing) {
        Profile profile = existing != null ? existing : new Profile();
        VBox box = createCard();

        Label sectionTitle = new Label("Personal Information");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Button editBtn = new Button("Edit Profile");
        editBtn.getStyleClass().add("btn");
        editBtn.setGraphic(new FontIcon("fth-edit-2"));
        editBtn.setOnAction(e -> openProfileDialog(profile));
        box.getChildren().add(header(sectionTitle, editBtn));

        Label name = new Label(orDash(profile.getFullName()));
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        box.getChildren().add(name);
        box.getChildren().add(infoLine("fth-briefcase", profile.getCurrentTitle()));
        box.getChildren().add(infoLine("fth-mail", profile.getEmail()));
        box.getChildren().add(infoLine("fth-phone", profile.getPhone()));
        for (ProfileLink l : profileDao.getLinks()) {
            box.getChildren().add(infoLine("fth-link", l.getLabel() + ": " + l.getUrl()));
        }
        return box;
    }

    private void openProfileDialog(Profile profile) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.setResizable(true);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = formGrid();
        grid.setPadding(new Insets(10, 12, 10, 4));
        TextField nameField = new TextField(nz(profile.getFullName()));
        TextField titleField = new TextField(nz(profile.getCurrentTitle()));
        titleField.setPromptText("e.g. Software Engineer");
        TextField emailField = new TextField(nz(profile.getEmail()));
        TextField phoneField = new TextField(nz(profile.getPhone()));
        grid.addRow(0, formLabel("Full Name:"), nameField);
        grid.addRow(1, formLabel("Title:"), titleField);
        grid.addRow(2, formLabel("Email:"), emailField);
        grid.addRow(3, formLabel("Phone:"), phoneField);

        ScrollPane dialogScroll = new ScrollPane(grid);
        dialogScroll.setFitToWidth(true);
        dialogScroll.setPrefViewportHeight(320);
        dialogScroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // Links editor: rows of [label][url][remove]
        VBox linksBox = new VBox(6);
        List<TextField[]> linkRows = new ArrayList<>();
        java.util.function.BiConsumer<String, String> addLinkRow = (label, url) -> {
            TextField lf = new TextField(label);
            lf.setPromptText("Label (e.g. LinkedIn)");
            lf.setPrefWidth(140);
            TextField uf = new TextField(url);
            uf.setPromptText("https://linkedin.com/in/you");
            HBox.setHgrow(uf, Priority.ALWAYS);
            TextField[] pair = {lf, uf};
            Button rm = new Button();
            rm.setGraphic(new FontIcon("fth-x"));
            HBox row = new HBox(6, lf, uf, rm);
            rm.setOnAction(ev -> { linksBox.getChildren().remove(row); linkRows.remove(pair); });
            linkRows.add(pair);
            linksBox.getChildren().add(row);
            javafx.application.Platform.runLater(() -> dialogScroll.setVvalue(1.0));
        };
        for (ProfileLink l : profileDao.getLinks()) addLinkRow.accept(l.getLabel(), l.getUrl());
        Button addLinkBtn = new Button("Add Link");
        addLinkBtn.setGraphic(new FontIcon("fth-plus"));
        addLinkBtn.setOnAction(ev -> addLinkRow.accept("", ""));

        Label linksLbl = formLabel("Links:");
        grid.add(linksLbl, 0, 4);
        grid.add(new VBox(6, linksBox, addLinkBtn), 1, 4);
        GridPane.setValignment(linksLbl, javafx.geometry.VPos.TOP);

        dialog.getDialogPane().setContent(dialogScroll);
        dialog.getDialogPane().setPrefWidth(640);
        dialog.getDialogPane().setPrefHeight(440);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            profile.setFullName(nameField.getText().trim());
            profile.setCurrentTitle(titleField.getText().trim());
            profile.setEmail(emailField.getText().trim());
            profile.setPhone(phoneField.getText().trim());
            profileDao.saveProfile(profile);

            List<ProfileLink> links = new ArrayList<>();
            for (TextField[] p : linkRows) links.add(new ProfileLink(p[0].getText(), p[1].getText()));
            profileDao.replaceLinks(links);
            loadData();
        }
    }

    // ------------------------------------------------------------------ Career sections

    private VBox buildSection(String title, String addBtnText) {
        VBox box = createCard();

        Label sectionTitle = new Label(title);
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Button addBtn = new Button(addBtnText);
        addBtn.getStyleClass().add("btn");
        addBtn.setGraphic(new FontIcon("fth-plus"));
        addBtn.setOnAction(e -> openEntryDialog(title, null));
        box.getChildren().add(header(sectionTitle, addBtn));

        Label placeholder = new Label("No " + title.toLowerCase() + " added yet.");
        placeholder.setStyle("-fx-text-fill: -text-secondary;");

        switch (title) {
            case "Experience" -> {
                var items = careerDao.getAllExperiences();
                if (items.isEmpty()) box.getChildren().add(placeholder);
                items.forEach(i -> box.getChildren().add(createItemRow(
                        i.getRole() + " at " + i.getCompany(), dates(i.getStartDate(), i.getEndDate()),
                        () -> openEntryDialog(title, i),
                        () -> { careerDao.deleteExperience(i.getId()); loadData(); })));
            }
            case "Projects" -> {
                var items = careerDao.getAllProjects();
                if (items.isEmpty()) box.getChildren().add(placeholder);
                items.forEach(i -> box.getChildren().add(createItemRow(i.getTitle(), nz(i.getUrl()),
                        () -> openEntryDialog(title, i),
                        () -> { careerDao.deleteProject(i.getId()); loadData(); })));
            }
            case "Education" -> {
                var items = careerDao.getAllEducations();
                if (items.isEmpty()) box.getChildren().add(placeholder);
                items.forEach(i -> {
                    String desc = nz(i.getDegree())
                            + (!nz(i.getField()).isEmpty() ? " in " + i.getField() : "")
                            + (!nz(i.getInstitution()).isEmpty() ? " at " + i.getInstitution() : "");
                    box.getChildren().add(createItemRow(desc.trim(), dates(i.getStartDate(), i.getEndDate()),
                            () -> openEntryDialog(title, i),
                            () -> { careerDao.deleteEducation(i.getId()); loadData(); }));
                });
            }
            case "Skills" -> {
                var items = careerDao.getAllSkills();
                if (items.isEmpty()) box.getChildren().add(placeholder);
                items.forEach(i -> box.getChildren().add(createItemRow(i.getName(), nz(i.getProficiency()),
                        () -> openEntryDialog(title, i),
                        () -> { careerDao.deleteSkill(i.getId()); loadData(); })));
            }
            default -> { }
        }
        return box;
    }

    private HBox createItemRow(String text, String subtitle, Runnable onEdit, Runnable onDelete) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: bold;");
        VBox texts = new VBox(2, lbl);
        if (subtitle != null && !subtitle.isBlank()) {
            Label sub = new Label(subtitle);
            sub.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 12px;");
            texts.getChildren().add(sub);
        }

        Button editBtn = new Button();
        editBtn.setGraphic(new FontIcon("fth-edit-2"));
        editBtn.getStyleClass().add("btn");
        editBtn.setStyle("-fx-padding: 4px 8px;");
        editBtn.setTooltip(new Tooltip("Edit"));
        editBtn.setOnAction(e -> onEdit.run());

        Button delBtn = new Button();
        delBtn.setGraphic(new FontIcon("fth-trash-2"));
        delBtn.getStyleClass().add("btn");
        delBtn.setStyle("-fx-text-fill: -danger; -fx-padding: 4px 8px;");
        delBtn.setTooltip(new Tooltip("Delete"));
        delBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete \"" + text + "\"?");
            if (confirm.showAndWait().filter(b -> b == ButtonType.OK).isPresent()) onDelete.run();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(8, texts, spacer, editBtn, delBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));
        return row;
    }

    /** Add (item == null) or edit an existing entry. */
    private void openEntryDialog(String section, Object item) {
        boolean editing = item != null;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle((editing ? "Edit " : "Add ") + section);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(560);

        GridPane grid = formGrid();
        TextField f1 = new TextField();
        TextField f2 = new TextField();
        TextField f3 = new TextField();
        TextField start = new TextField();
        TextField end = new TextField();
        start.setPromptText("e.g. Jan 2022");
        end.setPromptText("e.g. Present");
        TextArea details = new TextArea();
        details.setPrefRowCount(6);
        details.setWrapText(true);

        switch (section) {
            case "Experience" -> {
                if (item instanceof Experience x) {
                    f1.setText(nz(x.getCompany())); f2.setText(nz(x.getRole()));
                    start.setText(nz(x.getStartDate())); end.setText(nz(x.getEndDate()));
                    details.setText(nz(x.getDescription()));
                }
                details.setPromptText("What did you do? Responsibilities, achievements, technologies...");
                grid.addRow(0, new Label("Company:"), f1);
                grid.addRow(1, new Label("Role:"), f2);
                grid.addRow(2, new Label("Start Date:"), start);
                grid.addRow(3, new Label("End Date:"), end);
                grid.addRow(4, new Label("Details:"), details);
            }
            case "Projects" -> {
                if (item instanceof Project x) {
                    f1.setText(nz(x.getTitle())); f3.setText(nz(x.getUrl())); details.setText(nz(x.getDescription()));
                }
                details.setPromptText("What the project does, your contribution, tech used...");
                grid.addRow(0, new Label("Title:"), f1);
                grid.addRow(1, new Label("Details:"), details);
                grid.addRow(2, new Label("URL (optional):"), f3);
            }
            case "Education" -> {
                if (item instanceof Education x) {
                    f1.setText(nz(x.getInstitution())); f2.setText(nz(x.getDegree())); f3.setText(nz(x.getField()));
                    start.setText(nz(x.getStartDate())); end.setText(nz(x.getEndDate()));
                }
                grid.addRow(0, new Label("Institution:"), f1);
                grid.addRow(1, new Label("Degree:"), f2);
                grid.addRow(2, new Label("Field of Study:"), f3);
                grid.addRow(3, new Label("Start Date:"), start);
                grid.addRow(4, new Label("End Date:"), end);
            }
            case "Skills" -> {
                if (item instanceof Skill x) { f1.setText(nz(x.getName())); f2.setText(nz(x.getProficiency())); }
                grid.addRow(0, new Label("Skill Name:"), f1);
                grid.addRow(1, new Label("Proficiency (optional):"), f2);
            }
            default -> { }
        }

        dialog.getDialogPane().setContent(grid);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;
        if (f1.getText().isBlank()) return;

        switch (section) {
            case "Experience" -> {
                Experience e = item instanceof Experience x ? x : new Experience();
                e.setCompany(f1.getText().trim()); e.setRole(f2.getText().trim());
                e.setStartDate(start.getText().trim()); e.setEndDate(end.getText().trim());
                e.setDescription(details.getText());
                careerDao.saveExperience(e);
            }
            case "Projects" -> {
                Project p = item instanceof Project x ? x : new Project();
                p.setTitle(f1.getText().trim()); p.setUrl(f3.getText().trim()); p.setDescription(details.getText());
                careerDao.saveProject(p);
            }
            case "Education" -> {
                Education e = item instanceof Education x ? x : new Education();
                e.setInstitution(f1.getText().trim()); e.setDegree(f2.getText().trim()); e.setField(f3.getText().trim());
                e.setStartDate(start.getText().trim()); e.setEndDate(end.getText().trim());
                careerDao.saveEducation(e);
            }
            case "Skills" -> {
                Skill s = item instanceof Skill x ? x : new Skill();
                s.setName(f1.getText().trim()); s.setProficiency(f2.getText().trim());
                careerDao.saveSkill(s);
            }
            default -> { }
        }
        loadData();
    }

    // ------------------------------------------------------------------ helpers

    private static HBox header(Label title, Button action) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox h = new HBox(title, spacer, action);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private static Label infoLine(String icon, String text) {
        Label l = new Label(orDash(text));
        l.setGraphic(new FontIcon(icon));
        l.setGraphicTextGap(8);
        return l;
    }

    private static Label formLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #171717; -fx-font-weight: bold;");
        return l;
    }

    private static GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        ColumnConstraints c0 = new ColumnConstraints();
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);
        return grid;
    }

    private static VBox createCard() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: -panel-bg; -fx-background-radius: 8px; -fx-border-color: -border-color; -fx-border-radius: 8px;");
        return box;
    }

    private static String dates(String s, String e) {
        if (nz(s).isEmpty() && nz(e).isEmpty()) return "";
        return nz(s) + " – " + nz(e);
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static String orDash(String s) { return s == null || s.isBlank() ? "—" : s; }
}
