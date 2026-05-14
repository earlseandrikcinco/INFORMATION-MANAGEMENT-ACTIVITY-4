package gui;

import app.AppController;
import app.DataAccess;
import ref.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for recording or updating instructor attendance for a class session.
 * Restricts leave statuses unless an approved Leave Request is found for the date.
 */
public class UpdateAttendancePanel extends BasePanel {

    private final DataAccess db;
    private final SystemUser currentUser;

    private DefaultTableModel tableModel;
    private JTable table;
    private List<ClassSchedule> displayedSchedules;
    private JButton scopeBtn;
    private boolean showingAll = false;

    private static final java.util.Set<String> ABSENT_STATUSES = java.util.Set.of("A", "SL", "OB", "PL");

    public UpdateAttendancePanel(AppController controller, DataAccess db, SystemUser currentUser) {
        super(controller);
        this.db = db;
        this.currentUser = currentUser;
        buildUI();
    }

    private void buildUI() {
        String subtitle = isChecker()
                ? "My Assigned Classes  ·  " + currentUser.getName()
                : "All Classes  ·  " + currentUser.getRole();
        add(UIHelper.topBar("Update Attendance", subtitle), BorderLayout.NORTH);

        String[] cols = {"Class Code", "Course No.", "Days", "Start Time", "End Time", "Instructor"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = UIHelper.makeTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) markSelected();
            }
        });

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(UIHelper.sub("Double-click a class to record or update attendance."), BorderLayout.WEST);

        if (isChecker()) {
            scopeBtn = UIHelper.secondaryButton("Show All Classes");
            scopeBtn.addActionListener(e -> toggleScope());
            topRow.add(scopeBtn, BorderLayout.EAST);
        }

        body.add(topRow, BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        JButton markBtn = UIHelper.button("Mark Attendance →");
        markBtn.addActionListener(e -> markSelected());
        add(bottomBar(markBtn), BorderLayout.SOUTH);

        loadSchedules();
    }

    private void loadSchedules() {
        tableModel.setRowCount(0);
        displayedSchedules = (isChecker() && !showingAll)
                ? db.getSchedulesByCheckerID(currentUser.getUserID())
                : db.getAllClassSchedules();

        for (ClassSchedule cs : displayedSchedules) {
            tableModel.addRow(new Object[]{
                    cs.getClassCode(), cs.getCourseNo(), cs.getDays(),
                    cs.getStartTime(), cs.getEndTime(),
                    cs.getInstructorName() != null ? cs.getInstructorName() : "—"
            });
        }
    }

    private void toggleScope() {
        showingAll = !showingAll;
        scopeBtn.setText(showingAll ? "Show My Classes Only" : "Show All Classes");
        loadSchedules();
    }

    private void markSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        showMarkDialog(displayedSchedules.get(row));
    }

    private void showMarkDialog(ClassSchedule cs) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Attendance: " + cs.getClassCode(), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(460, 500);
        dialog.setLocationRelativeTo(this);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIHelper.ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel titleLbl = new JLabel(cs.getCourseNo() + " (" + cs.getClassCode() + ")");
        titleLbl.setFont(UIHelper.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.BG);
        form.setBorder(BorderFactory.createEmptyBorder(16, 18, 8, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

        JComboBox<StatusItem> statusCombo = new JComboBox<>();
        JLabel existingNote = new JLabel(" ");
        existingNote.setFont(UIHelper.FONT_SUB.deriveFont(Font.ITALIC));

        JComboBox<Instructor> substituteCombo = new JComboBox<>();
        JLabel substituteLbl = fieldLabel("Substitute:");
        JTextArea remarksArea = new JTextArea(3, 20);
        remarksArea.setLineWrap(true);

        // Layout rows
        gbc.gridx = 0; gbc.gridy = 0; form.add(fieldLabel("Date:"), gbc);
        gbc.gridx = 1; form.add(dateSpinner, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(fieldLabel("Status:"), gbc);
        gbc.gridx = 1; form.add(statusCombo, gbc);
        gbc.gridx = 1; gbc.gridy = 2; form.add(existingNote, gbc);
        gbc.gridx = 0; gbc.gridy = 3; form.add(substituteLbl, gbc);
        gbc.gridx = 1; form.add(substituteCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 4; form.add(fieldLabel("Remarks:"), gbc);
        gbc.gridx = 1; form.add(new JScrollPane(remarksArea), gbc);

        // Dynamic Status and Substitute Refresh Logic
        Runnable refreshData = () -> {
            java.util.Date d = (java.util.Date) dateSpinner.getValue();
            Date sqlDate = new Date(d.getTime());
            Attendance existing = db.getAttendanceForClass(cs.getClassCode(), sqlDate);
            LeaveRequest leave = (cs.getInstructID() != null)
                    ? db.getApprovedLeaveForInstructorOnDate(cs.getInstructID(), sqlDate) : null;

            // 1. Build restricted status list
            statusCombo.removeAllItems();
            statusCombo.addItem(new StatusItem("P", "Present"));
            statusCombo.addItem(new StatusItem("A", "Absent (Unexcused)"));

            if (leave != null) {
                statusCombo.addItem(new StatusItem(leave.getLeaveType(), translateStatus(leave.getLeaveType())));
                statusCombo.setSelectedIndex(2); // Auto-select the leave status
                existingNote.setText("✓ Approved Leave Request found.");
                existingNote.setForeground(new Color(0, 120, 0));
            } else {
                existingNote.setText("No leave request for this date.");
                existingNote.setForeground(UIHelper.TEXT_MID);
            }

            // 2. Load available substitutes
            substituteCombo.removeAllItems();
            substituteCombo.addItem(null);
            db.getAvailableSubstitutesForClass(cs.getInstructID(), cs.getDays(), cs.getStartTime(), cs.getEndTime())
                    .forEach(substituteCombo::addItem);

            // 3. Pre-fill existing data if it exists
            if (existing != null) {
                existingNote.setText("⚠ Overwriting: " + translateStatus(existing.getInstructorStatus()));
                existingNote.setForeground(new Color(180, 100, 0));
                remarksArea.setText(existing.getRemarks());

                // Find status in combo
                for (int i = 0; i < statusCombo.getItemCount(); i++) {
                    if (statusCombo.getItemAt(i).code.equals(existing.getInstructorStatus())) {
                        statusCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        };

        dateSpinner.addChangeListener(e -> refreshData.run());
        statusCombo.addActionListener(e -> {
            StatusItem item = (StatusItem) statusCombo.getSelectedItem();
            boolean isAbsent = item != null && ABSENT_STATUSES.contains(item.code);
            substituteLbl.setVisible(isAbsent);
            substituteCombo.setVisible(isAbsent);
        });

        refreshData.run();

        JButton saveBtn = UIHelper.button("Save");
        saveBtn.addActionListener(e -> {
            Date sqlDate = new Date(((java.util.Date) dateSpinner.getValue()).getTime());
            StatusItem selected = (StatusItem) statusCombo.getSelectedItem();
            if (selected == null) return;

            String finalStatusCode = selected.code;
            Instructor sub = (Instructor) substituteCombo.getSelectedItem();

            // Get the approved leave if it exists to shield the original instructor
            LeaveRequest lr = db.getApprovedLeaveForInstructorOnDate(cs.getInstructID(), sqlDate);
            Integer leaveRequestID = (lr != null) ? lr.getLeaveRequestID() : null;

            // --- NEW LOGIC: DETERMINING WHO IS RESPONSIBLE ---
            Integer actualID = null;

            if (sub != null) {
                // A substitute was assigned! They are now the 'Actual' instructor for this row.
                actualID = sub.getInstructorID();

                // If the Checker marks 'A' (Absent), it means the SUBSTITUTE didn't show up.
                // We keep the status 'A' so it hits the sub's stats, but the leaveRequestID
                // will protect the original instructor in the reports.
            } else {
                // No substitute.
                if ("P".equals(finalStatusCode)) {
                    // If present and no sub, the original instructor is the actual one.
                    actualID = cs.getInstructID();
                } else {
                    // If absent and no sub, actualID is null (Original instructor takes the hit).
                    actualID = null;
                }
            }

            // --- SAVE TO DATABASE ---
            boolean success = db.upsertAttendance(
                    cs.getClassCode(),
                    sqlDate,
                    finalStatusCode,
                    currentUser.getUserID(),
                    actualID,
                    leaveRequestID,
                    remarksArea.getText().trim()
            );

            if (success) {
                JOptionPane.showMessageDialog(dialog, "Attendance recorded successfully.");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Error: Could not save attendance.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = UIHelper.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        foot.add(cancelBtn);
        foot.add(saveBtn);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(foot, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private boolean isChecker() { return "Checker".equalsIgnoreCase(currentUser.getRole()); }
    private JLabel fieldLabel(String t) { JLabel l = new JLabel(t); l.setFont(UIHelper.FONT_LABEL); return l; }

    private String translateStatus(String code) {
        return switch (code != null ? code : "") {
            case "P" -> "Present"; case "A" -> "Absent (Unexcused)";
            case "SL" -> "Sick Leave"; case "OB" -> "Official Business";
            case "PL" -> "Personal Leave"; case "Substituted" -> "Substituted";
            default -> code;
        };
    }

    // Helper class for the JComboBox
    private static class StatusItem {
        String code, label;
        StatusItem(String c, String l) { this.code = c; this.label = l; }
        @Override public String toString() { return label; }
    }
}