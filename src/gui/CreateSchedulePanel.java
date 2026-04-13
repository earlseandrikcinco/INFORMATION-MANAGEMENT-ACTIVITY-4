package gui;

import app.AppController;
import app.DataAccess;
import app.DataPB;
import ref.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Time;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CreateSchedulePanel extends BasePanel {

    private final DataAccess db;
    private final SystemUser currentUser;

    private JTextField classCodeField;
    private JTextField courseNoField;

    private JSpinner startHourSpinner, startMinSpinner;
    private JSpinner endHourSpinner, endMinSpinner;

    private JCheckBox[] dayBoxes;
    private JPanel daysPanel;

    private JComboBox<Room> roomCombo;
    private JComboBox<Instructor> instructorCombo;

    private static final String[] DAY_LABELS = {"M", "T", "W", "Th", "F", "S"};

    public CreateSchedulePanel(AppController controller, DataAccess db, SystemUser currentUser) {
        super(controller);
        this.db = db;
        this.currentUser = currentUser;
        buildUI();
    }

    private void buildUI() {

        add(UIHelper.topBar("Create Class Schedule", "Department Head"), BorderLayout.NORTH);

        JPanel card = new JPanel();
        card.setBackground(UIHelper.SURFACE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 32, 24, 32));
        card.setMaximumSize(new Dimension(520, Integer.MAX_VALUE));

        classCodeField = new JTextField();
        card.add(formRow("Class Code", classCodeField));
        card.add(Box.createVerticalStrut(10));

        courseNoField = new JTextField();
        card.add(formRow("Course No.", courseNoField));
        card.add(Box.createVerticalStrut(10));

        startHourSpinner = hourSpinner();
        startMinSpinner = minuteSpinner();
        endHourSpinner = hourSpinner();
        endMinSpinner = minuteSpinner();

        card.add(formRow("Start Time", timeRow(startHourSpinner, startMinSpinner)));
        card.add(Box.createVerticalStrut(10));

        card.add(formRow("End Time", timeRow(endHourSpinner, endMinSpinner)));
        card.add(Box.createVerticalStrut(10));

        daysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        daysPanel.setOpaque(false);

        dayBoxes = new JCheckBox[DAY_LABELS.length];
        for (int i = 0; i < DAY_LABELS.length; i++) {
            dayBoxes[i] = new JCheckBox(DAY_LABELS[i]);
            dayBoxes[i].setOpaque(false);
            daysPanel.add(dayBoxes[i]);
        }

        card.add(formRow("Days", daysPanel));
        card.add(Box.createVerticalStrut(10));

        roomCombo = new JComboBox<>();
        roomCombo.addItem(null);
        for (Room r : db.getAllRooms()) roomCombo.addItem(r);
        card.add(formRow("Room", roomCombo));
        card.add(Box.createVerticalStrut(10));

        instructorCombo = new JComboBox<>();
        instructorCombo.addItem(null);

        int deptID = getDeptID();
        List<Instructor> instructors =
                deptID > 0 ? db.getInstructorsByDept(deptID) : db.getAllInstructors();

        for (Instructor i : instructors) instructorCombo.addItem(i);

        card.add(formRow("Instructor", instructorCombo));
        card.add(Box.createVerticalStrut(10));

        JLabel statusLabel = new JLabel(" ");
        card.add(statusLabel);

        JButton submitBtn = UIHelper.button("Save Schedule");
        card.add(Box.createVerticalStrut(10));
        card.add(submitBtn);

        submitBtn.addActionListener(e -> {

            String err = validateForm();
            if (err != null) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText(err);
                return;
            }

            List<ClassSchedule> conflicts = new ArrayList<>();
            boolean ok = submit(conflicts);

            if (!ok && !conflicts.isEmpty()) {

                StringBuilder msg = new StringBuilder("Schedule conflict detected:\n\n");

                for (ClassSchedule c : conflicts) {
                    msg.append("Class ")
                            .append(c.getClassCode())
                            .append(" | ")
                            .append(c.getCourseNo())
                            .append(" | ")
                            .append(c.getDays())
                            .append(" | ")
                            .append(c.getStartTime())
                            .append(" - ")
                            .append(c.getEndTime())
                            .append("\n");
                }

                JOptionPane.showMessageDialog(
                        this,
                        msg.toString(),
                        "Conflict Detected",
                        JOptionPane.WARNING_MESSAGE
                );

                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Schedule conflict exists.");
                return;
            }

            if (ok) {
                statusLabel.setForeground(new Color(0, 130, 0));
                statusLabel.setText("Schedule created successfully!");
                clearForm();
            } else {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Failed to save schedule.");
            }
        });

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(UIHelper.BG);
        center.add(card);

        add(center, BorderLayout.CENTER);
        add(bottomBar(), BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────
    // VALIDATION
    // ─────────────────────────────────────────────
    private String validateForm() {

        if (classCodeField.getText().isBlank())
            return "Class code is required.";

        int classCode;

        try {
            classCode = Integer.parseInt(classCodeField.getText().trim());
        } catch (Exception e) {
            return "Class code must be numeric.";
        }

        if (courseNoField.getText().isBlank())
            return "Course number required.";

        if (classCodeExists(classCode))
            return "Class code already exists.";

        int sh = (int) startHourSpinner.getValue();
        int sm = (int) startMinSpinner.getValue();
        int eh = (int) endHourSpinner.getValue();
        int em = (int) endMinSpinner.getValue();

        if (eh * 60 + em <= sh * 60 + sm)
            return "End time must be after start time.";

        boolean anyDay = false;
        for (JCheckBox cb : dayBoxes)
            if (cb.isSelected()) anyDay = true;

        if (!anyDay)
            return "Select at least one day.";

        return null;
    }

    private boolean classCodeExists(int code) {

        String sql = "SELECT 1 FROM CLASS_SCHEDULE WHERE classCode = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, code);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    // ─────────────────────────────────────────────
    // SUBMIT (UPDATED)
    // ─────────────────────────────────────────────
    private boolean submit(List<ClassSchedule> conflictsOut) {

        int classCode = Integer.parseInt(classCodeField.getText().trim());

        int sh = (int) startHourSpinner.getValue();
        int sm = (int) startMinSpinner.getValue();
        int eh = (int) endHourSpinner.getValue();
        int em = (int) endMinSpinner.getValue();

        Time start = Time.valueOf(String.format("%02d:%02d:00", sh, sm));
        Time end = Time.valueOf(String.format("%02d:%02d:00", eh, em));

        StringBuilder days = new StringBuilder();
        for (JCheckBox cb : dayBoxes)
            if (cb.isSelected()) days.append(cb.getText());

        Room room = (Room) roomCombo.getSelectedItem();
        Instructor inst = (Instructor) instructorCombo.getSelectedItem();

        ClassSchedule cs = new ClassSchedule(
                classCode,
                courseNoField.getText().trim(),
                start,
                end,
                days.toString(),
                room != null ? room.getRoomID() : null,
                inst != null ? inst.getInstructorID() : null
        );

        List<ClassSchedule> conflicts = db.findScheduleConflicts(
                cs.getClassCode(),
                cs.getRoomID(),
                cs.getInstructID(),
                cs.getDays(),
                cs.getStartTime(),
                cs.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            conflictsOut.addAll(conflicts);
            return false;
        }

        return db.insertClassSchedule(cs, conflictsOut);
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────
    private void clearForm() {
        classCodeField.setText("");
        courseNoField.setText("");
    }

    private int getDeptID() {
        if (currentUser instanceof DeptHead)
            return ((DeptHead) currentUser).getDepartmentID();
        return -1;
    }

    private JSpinner hourSpinner() {
        return new JSpinner(new SpinnerNumberModel(7, 0, 23, 1));
    }

    private JSpinner minuteSpinner() {
        return new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));
    }

    private JPanel timeRow(JComponent a, JComponent b) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setOpaque(false);
        p.add(a);
        p.add(new JLabel(":"));
        p.add(b);
        return p;
    }

    private JPanel formRow(String label, JComponent input) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(120, 30));

        row.add(lbl, BorderLayout.WEST);
        row.add(input, BorderLayout.CENTER);

        return row;
    }
}